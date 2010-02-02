/*
 * Copyright (c) 2010, IETR/INSA of Rennes
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
package net.sf.orcc.tools.merger.sequitur;

/**
 * This class defines a rule.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class Rule {

	private GuardSymbol first;

	private String name;

	private int referenceCount;

	public Rule(String name) {
		this.name = name;

		first = new GuardSymbol(this);
		first.append(first);
	}

	public Rule(String name, Digram digram) {
		this(name);
		appendSymbol(digram.getS1().copy());
		appendSymbol(digram.getS2().copy());
	}

	public void appendSymbol(Symbol symbol) {
		first.getPrevious().append(symbol);
		symbol.append(first);
	}

	public void decrementReferenceCount() {
		this.referenceCount--;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Rule) {
			Rule rule = (Rule) obj;
			return name.equals(rule.name);
		}

		return false;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public void incrementReferenceCount() {
		this.referenceCount++;
	}

	/**
	 * Removes last symbol.
	 */
	public void pop() {
		Symbol last = first.getPrevious();
		Symbol penultimate = last.getPrevious();
		penultimate.append(first);
	}

	@Override
	public String toString() {
		String res = name + ": ";
		Symbol symbol = first.getNext();
		while (symbol != first) {
			res += symbol.toString();
			symbol = symbol.getNext();
		}

		return res;
	}

	public GuardSymbol getGuard() {
		return first;
	}

}
