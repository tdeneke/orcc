/*
 * Copyright (c) 2009-2011, IETR/INSA of Rennes
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

import org.eclipse.emf.common.util.EList;

/**
 * This interface defines an instruction that Loads data from memory to a local
 * variable. The source can be a global (scalar or array), or a local array.
 * 
 * @author Matthieu Wipliez
 * @model extends="net.sf.orcc.ir.Instruction"
 */
public interface InstLoad extends Instruction {

	/**
	 * Returns the (possibly empty) list of indexes of this load.
	 * 
	 * @return the (possibly empty) list of indexes of this load
	 * @model containment="true"
	 */
	EList<Expression> getIndexes();

	/**
	 * Returns the variable loaded by this load instruction.
	 * 
	 * @return the variable loaded by this load instruction
	 * @model containment="true"
	 */
	Use getSource();

	/**
	 * Returns the target of this load instruction.
	 * 
	 * @return the target of this load instruction
	 * @model containment="true"
	 */
	Def getTarget();

	/**
	 * Sets the variable loaded by this load instruction.
	 * 
	 * @param source
	 *            the variable loaded by this load instruction
	 */
	void setSource(Use source);

	/**
	 * Sets the target of this load.
	 * 
	 * @param target
	 *            a local variable
	 */
	void setTarget(Def target);

}
