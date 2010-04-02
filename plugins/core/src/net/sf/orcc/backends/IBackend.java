/*
 * Copyright (c) 2009, IETR/INSA of Rennes
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
package net.sf.orcc.backends;

import net.sf.orcc.OrccException;

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * This interface defines a back-end.
 * 
 * @author Matthieu Wipliez
 * 
 */
public interface IBackend {

	/**
	 * Loads a hierarchical XDF network and IR files, and generates code. Some
	 * back-ends may flatten the network and close actors.
	 * 
	 * @param inputFile
	 *            absolute path of top-level input network
	 * @param outputFolder
	 *            folder absolute path of output folder
	 * @param fifoSize
	 *            default FIFO size
	 * @throws OrccException
	 *             if something goes wrong
	 */
	void generateCode(String inputFile, String outputFolder, int fifoSize)
			throws OrccException;

	/**
	 * Set the launch configuration of this back-end.
	 * 
	 * @param configuration
	 *            the launch configuration of this back-end
	 */
	void setLaunchConfiguration(ILaunchConfiguration configuration);

}
