/*
 * Copyright (c) 2012, IETR/INSA of Rennes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * about
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package net.sf.orcc.backends.c

import java.util.Map
import net.sf.orcc.df.Actor
import net.sf.orcc.df.Connection
import net.sf.orcc.df.Entity
import net.sf.orcc.df.Network
import net.sf.orcc.df.Port
import net.sf.orcc.graph.Vertex

import static net.sf.orcc.backends.BackendsConstants.*

/**
 * Generate and print network source file for C backend.
 *  
 * @author Antoine Lorence 
 * @author Tewodros Deneke
 */
class FfmpegInterfaceNetworkPrinter extends CTemplate {
	
	protected var Network network;
	
	protected var boolean profile = false	
	protected var boolean newSchedul = false
	
	def setNetwork(Network network) {
		this.network = network
	}

	override setOptions(Map<String, Object> options) {
		super.setOptions(options)
		if(options.containsKey(PROFILE)){
			profile = options.get(PROFILE) as Boolean
		}
		if (options.containsKey(NEW_SCHEDULER)) {
			newSchedul = options.get(NEW_SCHEDULER) as Boolean
		}
	}

	def protected getNetworkFileContent() '''
		// Generated from "«network.name»"

		#include <locale.h>
		#include <stdio.h>
		#include <stdlib.h>
		«printAdditionalIncludes»

		#include "«network.getSimpleName()».h"
		#include "types.h"
		#include "fifo.h"
		#include "util.h"
		#include "dataflow.h"
		#include "serialize.h"
		#include "options.h"
		#include "scheduler.h"

		#define SIZE «fifoSize»

		/////////////////////////////////////////////////
		// FIFO allocation
		«FOR child : network.children»
			«child.allocateFifos»
		«ENDFOR»

		/////////////////////////////////////////////////
		// FIFO pointer assignments
		«FOR child : network.children»
			«child.assignFifo»
		«ENDFOR»

		«IF profile»
			/////////////////////////////////////////////////
			// Declaration of the actions
			
			«FOR child : network.children»
				«FOR action : child.getAdapter(typeof(Actor)).actions»
					action_t action_«child.label»_«action.name» = {"«action.name»", 0, 0, -1, -1, -1, 0, 0};			
				«ENDFOR»
			«ENDFOR»
			«FOR child : network.children»
				action_t *«child.label»_actions[] = {
					«FOR action : child.getAdapter(typeof(Actor)).actions SEPARATOR ","»
						&action_«child.label»_«action.name»
					«ENDFOR»
				};
				
			«ENDFOR»
		«ENDIF»

		«additionalDeclarations»

		/////////////////////////////////////////////////
		// Actor functions
		«FOR child : network.children»
			extern void «child.label»_initialize(schedinfo_t *si);
			extern void «child.label»_scheduler(schedinfo_t *si);
		«ENDFOR»

		/////////////////////////////////////////////////
		// Declaration of the actors array
		«FOR child : network.children»
			«IF profile»
				actor_t «child.label» = {"«child.label»", «child.label»_initialize, «child.label»_scheduler, 0, 0, 0, 0, NULL, -1, «network.children.indexOf(child)», 0, 1, 0, 0, 0, «child.label»_actions, «child.getAdapter(typeof(Actor)).actions.size», 0, "«child.getAdapter(typeof(Actor)).getFile().getProjectRelativePath().removeFirstSegments(1).removeFileExtension().toString().replace("/", ".")»", 0, 0, 0};
			«ELSE»
				actor_t «child.label» = {"«child.label»", «child.label»_initialize, «child.label»_scheduler, 0, 0, 0, 0, NULL, -1, «network.children.indexOf(child)», 0, 1, 0, 0, 0, NULL, 0, 0, "", 0, 0, 0};
			«ENDIF»						
		«ENDFOR»

		actor_t *«network.getSimpleName()»_actors[] = {
			«FOR child : network.children SEPARATOR ","»
				&«child.label»
			«ENDFOR»	
		};

		/////////////////////////////////////////////////
		// Declaration of the connections array
		«FOR connection : network.connections»
			connection_t connection_«connection.target.label»_«connection.targetPort.name» = {&«connection.source.label», &«connection.target.label», 0, 0};
		«ENDFOR»

		connection_t *«network.getSimpleName()»_connections[] = {
			«FOR connection : network.connections SEPARATOR ","»
			    &connection_«connection.target.label»_«connection.targetPort.name»
			«ENDFOR»
		};

		/////////////////////////////////////////////////
		// Declaration of the network
		network_t «network.getSimpleName()»_network = {"«network.name»", «network.getSimpleName()»_actors, «network.getSimpleName()»_connections, «network.allActors.size»-2, «network.connections.size»};
		//network_t network = «network.getSimpleName()»_network
		
		options_t *«network.getSimpleName()»_opt;
		
		int «network.getSimpleName()»_start_actors(void* args) {
			«network.getSimpleName()»_param_t* param = («network.getSimpleName()»_param_t*) args;
			opt = init_orcc(param->argc, param->argv);
			set_scheduling_strategy(«IF !newSchedul»"RR"«ELSE»"DD"«ENDIF», «network.getSimpleName()»_opt);
			launcher(«network.getSimpleName()»_opt, &«network.getSimpleName()»_network);
			
			return compareErrors;
		}
		
		int «network.getSimpleName()»_decoder_init(lib«network.getSimpleName()»Context *ctx){
		    printf("inited «network.getSimpleName()» \n");
		    return 0;
		}
		
		int «network.getSimpleName()»_decoder_decode(lib«network.getSimpleName()»Context *ctx, lib«network.getSimpleName()»Picture *pic, int *got_frame, lib«network.getSimpleName()»Packet *pkt){
		    printf("decoded frame using «network.getSimpleName()» \n");
		    return 0;
		}
		
		int «network.getSimpleName()»_decoder_end(lib«network.getSimpleName()»Context *ctx){
		    printf("ended «network.getSimpleName()» \n");
		    return 0;
		}
	'''
		
	def protected assignFifo(Vertex vertex) '''
		«FOR connList : vertex.getAdapter(typeof(Entity)).outgoingPortMap.values»
			«printFifoAssign(connList.head.source.label, connList.head.sourcePort, connList.head.<Integer>getValueAsObject("idNoBcast"))»
			«FOR conn : connList»
				«printFifoAssign(conn.target.label, conn.targetPort, conn.<Integer>getValueAsObject("idNoBcast"))»
			«ENDFOR»
			
		«ENDFOR»
	'''
	
	def protected printFifoAssign(String name, Port port, int fifoIndex) '''
		fifo_«port.type.doSwitch»_t *«name»_«port.name» = &fifo_«fifoIndex»;
	'''

	def protected allocateFifos(Vertex vertex) '''
		«FOR connectionList : vertex.getAdapter(typeof(Entity)).outgoingPortMap.values»
			«allocateFifo(connectionList.get(0), connectionList.size)»
		«ENDFOR»
	'''
	
	def protected allocateFifo(Connection conn, int nbReaders) '''
		DECLARE_FIFO(«conn.sourcePort.type.doSwitch», «if (conn.size != null) conn.size else "SIZE"», «conn.<Object>getValueAsObject("idNoBcast")», «nbReaders»)
	'''
	
	// This method can be override by other backends to print additional includes
	def protected printAdditionalIncludes() ''''''
	
	// This method can be override by other backends to print additional declarations 
	def protected additionalDeclarations() ''''''
	
	// This method can be override by other backends to print additional statements
	// when the program is terminating
	def protected additionalAtExitActions()''''''
}
