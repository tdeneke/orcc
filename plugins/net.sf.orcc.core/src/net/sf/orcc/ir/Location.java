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
package net.sf.orcc.ir;

/**
 * This class represents a location. A location keeps track of where a
 * particular element was in the original file. It contains the line, and
 * starting and ending columns.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class Location {

	private int endColumn;

	private int startColumn;

	private int startLine;

	/**
	 * Constructs a dummy location.
	 */
	public Location() {
	}

	/**
	 * Constructs a location from the specified start line, start column, end
	 * column.
	 * 
	 * @param startLine
	 *            The line where the location starts.
	 * @param startColumn
	 *            The column where the location starts.
	 * @param endColumn
	 *            The column where the location ends.
	 */
	public Location(int startLine, int startColumn, int endColumn) {
		this.startLine = startLine;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	/**
	 * Constructs a location from two given locations.
	 * 
	 * @param start
	 *            starting location
	 * @param end
	 *            ending location
	 */
	public Location(Location start, Location end) {
		this.startLine = start.startLine;
	}

	/**
	 * Returns the ending column of this location.
	 * 
	 * @return the ending column of this location
	 */
	public int getEndColumn() {
		return endColumn;
	}

	/**
	 * Returns the starting column of this location.
	 * 
	 * @return the starting column of this location
	 */
	public int getStartColumn() {
		return startColumn;
	}

	/**
	 * Returns the starting line of this location.
	 * 
	 * @return the starting line of this location
	 */
	public int getStartLine() {
		return startLine;
	}

	@Override
	public String toString() {
		String res = "line " + startLine + ", characters " + startColumn + "-"
				+ endColumn;
		return res;
	}

}
