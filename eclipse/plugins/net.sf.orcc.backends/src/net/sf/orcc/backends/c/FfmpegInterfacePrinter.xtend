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
 * Generate and print ffmpeg codec interface file for C backend.
 *  
 * @author Tewodros Deneke
 * 
 */
class FfmpegInterfacePrinter extends CTemplate {
	
	protected var Network network;	
	def setNetwork(Network network) {
		this.network = network
	}

	override setOptions(Map<String, Object> options) {
		super.setOptions(options)
	}

	def protected getNetworkFileContent() '''
		// Generated from "«network.name»"
		
		#include <locale.h>
		#include <stdio.h>
		#include <stdlib.h>
		#include "config.h"
		
		#include "types.h"
		#include "fifo.h"
		#include "util.h"
		#include "dataflow.h"
		#include "serialize.h"
		#include "options.h"
		#include "scheduler.h"
		#include "cycle.h"
		#include "thread.h"
		
		typedef struct «network.getSimpleName()»_param_t{
			int argc;
			char **argv;
		} «network.getSimpleName()»_param_t;
		

		typedef struct lib«network.getSimpleName()»Context {
		    schedinfo_t* source_sched_info;
		    schedinfo_t* sink_sched_info;
		    int frames_decoded;
		    orcc_thread_t launch_thread;
		    orcc_thread_id_t launch_thread_id;
		    «network.getSimpleName()»_param_t* param;
		} lib«network.getSimpleName()»Context;
		
		typedef struct lib«network.getSimpleName()»Packet {
		    int size;
		    unsigned char* buf;
		} lib«network.getSimpleName()»Packet;
		
		typedef struct lib«network.getSimpleName()»Picture {
		    short int width;
		    short int height;
		    int format;
		    unsigned char* pic_buf_y;
		    unsigned char* pic_buf_u;
		    unsigned char* pic_buf_v;
		} lib«network.getSimpleName()»Picture;
		
		int «network.getSimpleName()»_decoder_init(lib«network.getSimpleName()»Context *ctx);
		int «network.getSimpleName()»_decoder_decode(lib«network.getSimpleName()»Context *ctx, lib«network.getSimpleName()»Picture *pic, int *got_frame, lib«network.getSimpleName()»Packet *pkt);
		int «network.getSimpleName()»_decoder_end(lib«network.getSimpleName()»Context *ctx);
		void* «network.getSimpleName()»_start_actors(void* args);
		#define SIZE «fifoSize»
	'''
}
