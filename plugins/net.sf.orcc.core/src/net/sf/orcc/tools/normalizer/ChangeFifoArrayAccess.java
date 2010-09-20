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
package net.sf.orcc.tools.normalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.sf.orcc.OrccException;
import net.sf.orcc.ir.Actor;
import net.sf.orcc.ir.Expression;
import net.sf.orcc.ir.Instruction;
import net.sf.orcc.ir.IrFactory;
import net.sf.orcc.ir.LocalVariable;
import net.sf.orcc.ir.StateVariable;
import net.sf.orcc.ir.Use;
import net.sf.orcc.ir.Variable;
import net.sf.orcc.ir.expr.BinaryExpr;
import net.sf.orcc.ir.expr.BinaryOp;
import net.sf.orcc.ir.expr.IntExpr;
import net.sf.orcc.ir.expr.VarExpr;
import net.sf.orcc.ir.instructions.Load;
import net.sf.orcc.ir.instructions.Store;
import net.sf.orcc.ir.transforms.AbstractActorTransformation;
import net.sf.orcc.util.OrderedMap;

/**
 * This class defines a transform that changes the load/store on FIFO arrays so
 * they use global arrays.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ChangeFifoArrayAccess extends AbstractActorTransformation {

	private OrderedMap<String, StateVariable> stateVars;

	@Override
	public void transform(Actor actor) throws OrccException {
		stateVars = actor.getStateVars();
		super.transform(actor);
	}

	@SuppressWarnings("unchecked")
	private void updateIndex(Variable var, Instruction instr,
			List<Expression> indexes, Object... args) {
		Expression index = indexes.get(0);

		if (index.equals(new IntExpr(0))) {
			Variable varCount = stateVars.get(var.getName() + "_count");
			Use use = new Use(varCount, instr);
			indexes.set(0, new VarExpr(use));

			indexes = new ArrayList<Expression>(0);
			use = new Use(varCount);
			Store store = new Store(varCount, indexes, new BinaryExpr(
					new VarExpr(use), BinaryOp.PLUS, new IntExpr(1),
					IrFactory.eINSTANCE.createTypeInt(32)));
			use.setNode(store);

			ListIterator<Instruction> it = (ListIterator<Instruction>) args[0];
			it.add(store);
		} else {
			System.err.println("TODO index");
		}
	}

	@Override
	public void visit(Load load, Object... args) {
		Use use = load.getSource();
		Variable var = use.getVariable();
		if (!var.isGlobal() && ((LocalVariable) var).isPort()) {
			load.setSource(new Use(stateVars.get(var.getName()), load));
			updateIndex(var, load, load.getIndexes(), args);
		}
	}

	@Override
	public void visit(Store store, Object... args) {
		Variable var = store.getTarget();
		if (!var.isGlobal() && var.isPort()) {
			store.setTarget(stateVars.get(var.getName()));
			updateIndex(var, store, store.getIndexes(), args);
		}
	}

}
