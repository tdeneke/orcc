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
				«IF child.label == "AVCSource" || child.label == "AVCDisplay" || child.label == "HEVCSource" || child.label == "HEVCDisplay"»
					//&«child.label»
				«ELSE»
					&«child.label»	
				«ENDIF»
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
		network_t network = {"«network.name»", «network.getSimpleName()»_actors, «network.getSimpleName()»_connections, «network.allActors.size»-2, «network.connections.size»};
		options_t *«network.getSimpleName()»_opt;
		
		
		void* «network.getSimpleName()»_start_actors(void* args) {
			«network.getSimpleName()»_param_t* param = («network.getSimpleName()»_param_t*) args;
			«network.getSimpleName()»_opt = init_orcc(param->argc, param->argv);
			set_scheduling_strategy(«IF !newSchedul»"RR"«ELSE»"DD"«ENDIF», «network.getSimpleName()»_opt);
			launcher(«network.getSimpleName()»_opt, &network);
			return compareErrors;
		}
		
		int «network.getSimpleName()»_decoder_init(lib«network.getSimpleName()»Context *ctx){
			printf("Initiating decoder «network.getSimpleName()» \n");
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
	
	// This method is used to add a modified source actor components which are useful 
	// to write incoming packets from ffmpeg in to the dataflow decoder. this components will 
	// probably be moved to the ffmpeg side to the interface 
	def protected printSourceActions() '''
		////////////////////////////////////////////////////////////////////////////////
		// Instance
		extern actor_t source;
		
		////////////////////////////////////////////////////////////////////////////////
		// Output FIFOs
		extern fifo_u8_t *source_O;
		
		////////////////////////////////////////////////////////////////////////////////
		// Output Fifo control variables
		static unsigned int index_O;
		static unsigned int numFree_O;
		#define NUM_READERS_O 1
		#define SIZE_O «fifoSize»
		#define tokens_O source_O->contents
		////////////////////////////////////////////////////////////////////////////////
		// Successors
		extern actor_t AVCDecoder_Syn_Parser_Algo_Synp;
				
		////////////////////////////////////////////////////////////////////////////////
		// State variables of the actor
		int sent = 0;
		int to_send = 0;
		AVPacket *tmp_avpkt;
		
		////////////////////////////////////////////////////////////////////////////////
		// Token functions
		
		static void write_O() {
			index_O = source_O->write_ind;
			numFree_O = index_O + fifo_u8_get_room(source_O, NUM_READERS_O);
		}
		
		static void write_end_O() {
			source_O->write_ind = index_O;
		}
		
		////////////////////////////////////////////////////////////////////////////////
		// Functions/procedures
		extern void source_init();
		
		////////////////////////////////////////////////////////////////////////////////
		// Actions
		static i32 isSchedulable_s() {
			i32 result;
			result = sent < to_send;
			return result;
		}
		
		static void s() {

			// Compute aligned port indexes
			i32 index_aligned_O = index_O % SIZE_O;
			tokens_O[(index_O + (0)) % SIZE_O] = tmp_avpkt->data[sent];
			sent += 1;
			// Update ports indexes
			//printf("sent token %d of %d\n", index_O, to_send);
			index_O += 1;

		}

		////////////////////////////////////////////////////////////////////////////////
		// Initializes
		
		void source_initialize(schedinfo_t *si) {
		        int i = 0;
		        write_O();
		        source_init();
		finished:
		        write_end_O();
		        return;
		}

		////////////////////////////////////////////////////////////////////////////////
		// Action scheduler
		void source_scheduler(schedinfo_t *si) {
		        int i = 0;
		        si->ports = 0;
		
		        write_O();
		
		        while (1) {
		                if (isSchedulable_s()) {
		                        int stop = 0;
		                        if (1 > SIZE_O - index_O + source_O->read_inds[0]) {
		                                stop = 1;
		                        }
		                        if (stop != 0) {
		                                si->num_firings = i;
		                                si->reason = full;
		                                goto finished;
		                        }
		                        s();
		                        i++;
		                } else {
		                        si->num_firings = i;
		                        si->reason = starved;
		                        //cnt = 0;
		                        goto finished;
		                }
		        }
		
		finished:
		
		        write_end_O();
		}
	'''
	
	// 
	def protected printDisplayActions() '''
	'''
	
	def protected printInitDecoder() '''
		ctx->frames_decoded = 0;
		ctx->param = («network.getSimpleName()»_param_t*) malloc(sizeof(«network.getSimpleName()»_param_t));                                                            

		//default arguments for the dataflow decoder
		char* argv[8];
		argv[0] = "./fforcc";
		argv[1] = "-i";
		argv[2] = " ";
		argv[3] = "-n";
		argv[4] = "-l";
		argv[5] = "1";
		argv[6] = "-c";
		argv[7] = "4"; //ncores
		int argc = 8;
		
		(ctx->param)->argv = argv;
		(ctx->param)->argc = argc;
		«network.getSimpleName()»_opt = set_default_options();
		orcc_thread_create(ctx->launch_thread, «network.getSimpleName()»_start_actors, *(ctx->param), ctx->launch_thread_id);
		
		//either we need to sleep till initilization or check by pooling.
		while(opt->display_flags==NULL);
		while(displayYUV_getFlags()!=0);
		ctx->sink_sched_info = (schedinfo_t*) malloc(sizeof(schedinfo_t));
		(ctx->sink_sched_info)->num_firings = 0;
		(ctx->sink_sched_info)->reason = 0;
		(ctx->sink_sched_info)->ports = 0;
		ctx->frames_decoded = 0;
		display_initialize(ctx->sink_sched_info);
		
		ctx->source_sched_info = (schedinfo_t*) malloc(sizeof(schedinfo_t));
		(ctx->source_sched_info)->num_firings = 0;
		(ctx->source_sched_info)->reason = 0;
		(ctx->source_sched_info)->ports = 0;
		source_initialize(ctx->source_sched_info);
		
		return 0;
	'''
	
	def protected printFrameDecode()'''
		int buf_size	= pkt->size;
		int buf_index	= 0;
		tmp_pkt = pkt;
		to_send = pkt->size;
		sent = 0;
		source_scheduler(ctx->source_sched_info);
		//nbFrameDecoded
		ctx->frames_decoded = nbFrameDecoded;
		display_scheduler(ctx->sink_sched_info);
			
		if(nbFrameDecoded > ctx->frames_decoded){
			// printf("**** inside decode_frame ***** 4 --- %d, %d\n", nbFrameDecoded,  q->frames_decoded);
			// av_log(avctx, AV_LOG_ERROR, "Packet size is %d !\n", avpkt->size);
			// Initialize the AVFrame
					
			pic->width = pictureWidthLuma;
			pic->height = pictureHeightLuma;
			pic->format = AV_PIX_FMT_YUV420P;
			pic->pic_buf_y =  pictureBufferY;
			pic->pic_buf_u =  pictureBufferU;
			pic->pic_buf_v =  pictureBufferV;
					
			// will be moved out to the h264 library 
			// Initialize frame->linesize
			// avpicture_fill((AVPicture*)frame, NULL, frame->format, frame->width, frame->height);
					
			// Set frame->data pointers manually
			// frame->data[0] =  pictureBufferY;
			// frame->data[1] =  pictureBufferU;
			// frame->data[2] =  pictureBufferV;
			*got_frame = 1;
				
		}else{
			*got_frame = 0;
		}
				
		pkt->size -= sent;
		pkt->buf += sent;
			
		return 0;
	'''
	
	def protected printEndDecoder() '''
		//Free everything
		free(ctx->sink_sched_info);
		free(ctx->source_sched_info);
		free(ctx->param);
		untagged_0();
		orcc_thread_join(ctx->launch_thread);
		//fclose(ofile);
		//Return 0 if everything is ok, -1 if not
		return 0;			
	'''
	// This method can be override by other backends to print additional includes
	def protected printAdditionalIncludes() ''''''
	
	// This method can be override by other backends to print additional declarations 
	def protected additionalDeclarations() ''''''
	
	// This method can be override by other backends to print additional statements
	// when the program is terminating
	def protected additionalAtExitActions()''''''
}
