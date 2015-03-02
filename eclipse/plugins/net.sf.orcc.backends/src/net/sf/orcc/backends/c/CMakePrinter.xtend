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
 * 
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

import net.sf.orcc.backends.CommonPrinter
import net.sf.orcc.df.Actor
import net.sf.orcc.df.Network

/**
 * Generate CMakeList.txt content
 * 
 * @author Antoine Lorence
 */
class CMakePrinter extends CommonPrinter {

	protected var Network network

	def setNetwork(Network network) {
		this.network = network
	}

	def rootCMakeContent() '''
		# Generated from «network.simpleName»

		cmake_minimum_required (VERSION 2.6)

		project («network.simpleName»)

		# Configure ouput folder for generated binary
		set(EXECUTABLE_OUTPUT_PATH ${CMAKE_SOURCE_DIR}/bin)

		# Definitions configured and used in subdirectories
		set(extra_definitions)
		set(extra_includes)
		set(extra_libraries)

		# Runtime libraries inclusion
		include_directories(
			${PROJECT_BINARY_DIR}/libs # to find config.h
			libs/orcc-native/include
			libs/orcc-runtime/include
		)

		«addLibrariesSubdirs»
	'''

	/**
	 * Goal of this method is to allow text produced to be extended
	 * for specific usages (other backends)
	 */
	def protected addLibrariesSubdirs() '''
		# Compile required libs
		add_subdirectory(libs)
		
		# Compile application
		add_subdirectory(src)
	'''

	def srcCMakeContent() '''
		# Generated from «network.simpleName»

		set(filenames
			«network.simpleName».c
			«FOR child : network.children.actorInstances.filter[!getActor.native]»
				«child.label».c
			«ENDFOR»
			«FOR child : network.children.filter(typeof(Actor)).filter[!native]»
				«child.label».c
			«ENDFOR»
		)
		
		include_directories(${extra_includes})
		add_definitions(${extra_definitions})
		
		# Compile libraries
		add_library(«network.simpleName» STATIC ${filenames} «network.simpleName».h)
		
		SET(PKG_CONFIG_REQUIRES )
		SET(PKG_CONFIG_LIBDIR
		    "\${prefix}/lib"
		)
		SET(PKG_CONFIG_INCLUDEDIR
		    "\${prefix}/include"
		)
		SET(PKG_CONFIG_LIBS
		    "-L\${libdir} -l«network.simpleName» -lorcc-runtime  -lorcc-native -lroxml -lSDL"
		)
		SET(PKG_CONFIG_CFLAGS
		    "-I\${includedir}"
		)
		
		CONFIGURE_FILE(
		  "${CMAKE_CURRENT_SOURCE_DIR}/pkg-config.pc.cmake"
		  "${CMAKE_CURRENT_BINARY_DIR}/«network.simpleName».pc"
		)
		
		INSTALL(FILES "${CMAKE_CURRENT_BINARY_DIR}/«network.simpleName».pc" DESTINATION lib/pkgconfig)
		install(TARGETS «network.simpleName» DESTINATION lib)
		install(FILES «network.simpleName».h DESTINATION include)
			
		#add_executable(«network.simpleName»app ${filenames})
		#Build library without any external library required
		#target_link_libraries(«network.simpleName»app orcc-native orcc-runtime ${extra_libraries})
	'''
	
	def srcPkgConfigCMakeContent() '''
		Name: ${PROJECT_NAME}
		Description: ${PROJECT_DESCRIPTION}
		Version: ${PROJECT_VERSION}
		Requires: ${PKG_CONFIG_REQUIRES}
		prefix=${CMAKE_INSTALL_PREFIX}
		includedir=${PKG_CONFIG_INCLUDEDIR}
		libdir=${PKG_CONFIG_LIBDIR}
		Libs: ${PKG_CONFIG_LIBS}
		Cflags: ${PKG_CONFIG_CFLAGS}
	'''
	
}
