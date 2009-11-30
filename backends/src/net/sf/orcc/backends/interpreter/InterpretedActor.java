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
package net.sf.orcc.backends.interpreter;

import java.util.Map;
import java.util.Map.Entry;

import net.sf.orcc.ir.Action;
import net.sf.orcc.ir.ActionScheduler;
import net.sf.orcc.ir.Actor;
import net.sf.orcc.ir.CFGNode;
import net.sf.orcc.ir.Constant;
import net.sf.orcc.ir.Port;
import net.sf.orcc.ir.Procedure;
import net.sf.orcc.ir.StateVariable;
import net.sf.orcc.ir.Type;
import net.sf.orcc.ir.Variable;
import net.sf.orcc.ir.FSM.NextStateInfo;
import net.sf.orcc.ir.consts.ConstantEvaluator;

/**
 * Interpreted actors made of an IR Actor, an FSM state and a scheduling state.
 * 
 * @author Pierre-Laurent Lagalaye
 * 
 */
public class InterpretedActor extends AbstractInterpretedActor {

	private ConstantEvaluator constEval;

	private String fsmState;

	private NodeInterpreter interpret;

	private ListAllocator listAllocator;

	private ActionScheduler sched;

	// private List<Actor> schedPred;

	public InterpretedActor(String id, Actor actor) {
		super(id, actor);
		sched = actor.getActionScheduler();
		if (sched.hasFsm()) {
			this.fsmState = sched.getFsm().getInitialState().getName();
		} else {
			fsmState = "IDLE";
		}
		// TODO : get scheduling predecessors

		// Create the List allocator for state and procedure local vars
		listAllocator = new ListAllocator();

		// Build a node interpreter for visiting CFG and instructions
		interpret = new NodeInterpreter(name);

		// Creates an expression evaluator for state and local variables init
		this.constEval = new ConstantEvaluator();
	}

	private boolean checkOutputPattern(Map<Port, Integer> outputPattern) {
		if (outputPattern != null) {
			boolean freeOutput = true;
			for (Entry<Port, Integer> entry : outputPattern.entrySet()) {
				Port outputPort = entry.getKey();
				Integer nbOfTokens = entry.getValue();
				freeOutput &= outputPort.fifo().hasRoom(nbOfTokens);
			}

			return freeOutput;
		} else {
			return true;
		}
	}

	public String getFsmState() {
		return fsmState;
	}

	/**
	 * Launch initializing actions for each network actor.
	 * 
	 */
	public void initialize() {
		// Check for List state variables which need to be allocated or
		// initialized
		for (Variable stateVar : actor.getStateVars()) {
			Type type = stateVar.getType();
			// Initialize variables with constant values
			if (stateVar.hasExpression()) {
				Constant initConst = ((StateVariable) stateVar)
						.getConstantValue();
				Object initVal = initConst.accept(constEval);
				stateVar.setValue(initVal);
			} else if (type.getType() == Type.LIST) {
				// Allocate empty array variable
				stateVar.setValue(listAllocator.allocate(type));
			}
		}
		// Get initializing procedure if any
		for (Action action : actor.getInitializes()) {
			System.out.println("Initialize actor " + name);
			Object isSchedulable = interpretProc(action.getScheduler());
			if (isSchedulable instanceof Boolean) {
				if ((Boolean) isSchedulable) {
					interpretProc(action.getBody());
				}
			}
		}
	}

	/**
	 * Interprets the given procedure.
	 * 
	 * @param procedure
	 *            a procedure
	 * 
	 * @return an object which contains procedure returned value
	 */
	private Object interpretProc(Procedure procedure) {
		// Don't mind about procedure parameters (already set)

		// Allocate local List variables
		for (Variable local : procedure.getLocals()) {
			Type type = local.getType();
			if (type.getType() == Type.LIST) {
				local.setValue(listAllocator.allocate(type));
			}
		}

		// Interpret procedure body
		for (CFGNode node : procedure.getNodes()) {
			node.accept(interpret);
		}

		// Procedure return value
		Object result = interpret.getReturnValue();

		// TODO : check return type
		// Type type = procedure.getReturnType();

		// Return the result object
		return result;
	}

	private boolean isSchedulable(Action action) {
		Object isSchedulable = interpretProc(action.getScheduler());
		if ((isSchedulable instanceof Boolean) && ((Boolean) isSchedulable)) {
			if (checkOutputPattern(action.getOutputPattern())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check next action to be scheduled and interpret it if I/O FIFO are free.
	 * 
	 */
	public Integer schedule() {
		if (sched.hasFsm()) {
			System.out.println("Current FSM state is : " + fsmState);

			// Check for untagged actions first
			for (Action action : sched.getActions()) {
				if (isSchedulable(action)) {
					interpretProc(action.getBody());
					System.out.println("Executing action : "
							+ action.getBody().getName());
				}
			}

			// Then check for next FSM transition
			for (NextStateInfo info : sched.getFsm().getTransitions(fsmState)) {
				Action action = info.getAction();
				String name = action.getBody().getName();
				System.out.println("Check schedulable : " + name);
				if (isSchedulable(action)) {
					interpretProc(action.getBody());
					System.out.println("Executing action : " + name);

					// Update FSM state
					fsmState = info.getTargetState().getName();

					// Execute 1 action only per actor scheduler cycle
					return 1;
				}
			}
		} else {
			for (Action action : sched.getActions()) {
				String name = action.getBody().getName();
				System.out.println("Checking schedulable action : " + name);
				if (isSchedulable(action)) {
					interpretProc(action.getBody());
					System.out.println("Executing action : " + name);

					// Execute 1 action only per actor scheduler cycle
					return 1;
				}
			}
		}
		return 0;
	}

}
