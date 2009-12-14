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

import java.util.Map;

/**
 * This class defines an action. An action has a location, a tag, an input and
 * an output pattern. Contrarily to the original CAL model, the scheduling
 * information of an action is decoupled from its body. This is why an action
 * has a "scheduler" and a body.
 * 
 * @author Matthieu Wipliez, Samuel Keller
 * 
 */
public class Action extends AbstractLocalizable {

	private Procedure body;

	private Map<Port, Integer> inputPattern;

	private Map<Port, Integer> outputPattern;

	private Procedure scheduler;

	private Tag tag;

	/**
	 * Creates a new action.
	 * 
	 * @param location
	 *            location of the action
	 * @param tag
	 *            action tag
	 * @param inputPattern
	 *            input pattern
	 * @param outputPattern
	 *            output pattern
	 * @param scheduler
	 *            procedure that computes scheduling information
	 * @param body
	 *            procedure that holds the body of the action
	 */
	public Action(Location location, Tag tag, Map<Port, Integer> inputPattern,
			Map<Port, Integer> outputPattern, Procedure scheduler,
			Procedure body) {
		super(location);
		this.body = body;
		this.inputPattern = inputPattern;
		this.outputPattern = outputPattern;
		this.scheduler = scheduler;
		this.tag = tag;
	}

	/**
	 * Returns the procedure that holds the body of this action.
	 * 
	 * @return the procedure that holds the body of this action
	 */
	public Procedure getBody() {
		return body;
	}

	/**
	 * Returns the input pattern of this action.
	 * 
	 * @return the input pattern of this action
	 */
	public Map<Port, Integer> getInputPattern() {
		return inputPattern;
	}

	/**
	 * Returns the output pattern of this action.
	 * 
	 * @return the output pattern of this action
	 */
	public Map<Port, Integer> getOutputPattern() {
		return outputPattern;
	}

	/**
	 * Returns the procedure that holds the scheduling information of this
	 * action.
	 * 
	 * @return the procedure that holds the scheduling information of this
	 *         action
	 */
	public Procedure getScheduler() {
		return scheduler;
	}

	/**
	 * Returns the tag of this action.
	 * 
	 * @return the tag of this action
	 */
	public Tag getTag() {
		return tag;
	}

	/**
	 * Returns action name (tag or body name)
	 * @return action name
	 */
	public String getName() {
		if (tag.isEmpty()) {
			return body.getName();
		} else {
			String str = "";
			for (int i = 0; i < tag.size() - 1; i++) {
				str += tag.get(i) + "_";
			}

			str += tag.get(tag.size() - 1);
			return str;
		}
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
