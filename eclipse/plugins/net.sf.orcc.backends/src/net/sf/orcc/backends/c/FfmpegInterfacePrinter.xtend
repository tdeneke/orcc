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
class NetworkPrinter extends CTemplate {
	
	protected var Network network;	
	def setNetwork(Network network) {
		this.network = network
	}

	override setOptions(Map<String, Object> options) {
		super.setOptions(options)
	}

	def protected getNetworkFileContent() '''
		typedef struct orcc264_param_t{
			int argc;
			char **argv;
		} orcc264_param_t;
		
		typedef struct liborcc264Context {
		    //schedinfo_t* source_sched_info;
		    //schedinfo_t* sink_sched_info;
		    int frames_decoded;
		    //orcc_thread_t launch_thread;
		    //orcc_thread_id_t launch_thread_id;
		    //orcc264_param_t* param;
		} liborcc264Context;
		
		typedef struct liborcc264Packet {
		    int size;
		    unsigned char* buf;
		} liborcc264Packet;
		
		typedef struct liborcc264Picture {
		    short int width;
		    short int height;
		    int format;
		    unsigned char* pic_buf_y;
		    unsigned char* pic_buf_u;
		    unsigned char* pic_buf_v;
		} liborcc264Picture;
		
		int orcc264_decoder_init(liborcc264Context *ctx);
		int orcc264_decoder_decode(liborcc264Context *ctx, liborcc264Picture *pic, int *got_frame, liborcc264Packet *pkt);
		int orcc_decoder_end(liborcc264Context *ctx);
	'''		
}
