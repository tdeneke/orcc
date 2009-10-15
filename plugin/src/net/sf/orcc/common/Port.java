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
package net.sf.orcc.common;

import net.sf.orcc.ir.type.IType;

/**
 * This class represents a port.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class Port {

	/**
	 * port location
	 */
	private Location location;

	/**
	 * port name
	 */
	private String name;

	/**
	 * port type
	 */
	private IType type;

	/**
	 * Creates a new port with the given location, type, and name.
	 * 
	 * @param location
	 *            the port location
	 * @param type
	 *            the port type
	 * @param name
	 *            the port name
	 */
	public Port(Location location, IType type, String name) {
		this.location = location;
		this.type = type;
		this.name = name;
	}

	/**
	 * Creates a new port from the given port
	 */
	public Port(Port port) {
		this.location = port.location;
		this.type = port.type;
		this.name = port.name;
	}

	/**
	 * Returns the location of this port;
	 * 
	 * @return the location of this port
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Returns the name of this port.
	 * 
	 * @return the name of this port
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the type of this port.
	 * 
	 * @return the type of this port
	 */
	public IType getType() {
		return type;
	}

	/**
	 * Sets the name of this port.
	 * 
	 * @param name
	 *            the new name of this port
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
