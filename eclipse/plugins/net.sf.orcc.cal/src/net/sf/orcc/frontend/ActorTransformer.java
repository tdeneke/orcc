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
package net.sf.orcc.frontend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.orcc.cache.CacheManager;
import net.sf.orcc.cal.cal.AstAction;
import net.sf.orcc.cal.cal.AstActor;
import net.sf.orcc.cal.cal.AstEntity;
import net.sf.orcc.cal.cal.AstExpression;
import net.sf.orcc.cal.cal.AstPort;
import net.sf.orcc.cal.cal.AstProcedure;
import net.sf.orcc.cal.cal.AstTag;
import net.sf.orcc.cal.cal.Function;
import net.sf.orcc.cal.cal.InputPattern;
import net.sf.orcc.cal.cal.OutputPattern;
import net.sf.orcc.cal.cal.RegExp;
import net.sf.orcc.cal.cal.Schedule;
import net.sf.orcc.cal.cal.Variable;
import net.sf.orcc.cal.cal.VariableReference;
import net.sf.orcc.cal.cal.util.CalSwitch;
import net.sf.orcc.cal.services.Evaluator;
import net.sf.orcc.cal.services.Typer;
import net.sf.orcc.cal.util.Util;
import net.sf.orcc.cal.util.VoidSwitch;
import net.sf.orcc.frontend.schedule.ActionSorter;
import net.sf.orcc.frontend.schedule.FSMBuilder;
import net.sf.orcc.frontend.schedule.RegExpConverter;
import net.sf.orcc.ir.Action;
import net.sf.orcc.ir.Actor;
import net.sf.orcc.ir.ExprVar;
import net.sf.orcc.ir.Expression;
import net.sf.orcc.ir.FSM;
import net.sf.orcc.ir.InstAssign;
import net.sf.orcc.ir.InstLoad;
import net.sf.orcc.ir.InstStore;
import net.sf.orcc.ir.IrFactory;
import net.sf.orcc.ir.Node;
import net.sf.orcc.ir.NodeBlock;
import net.sf.orcc.ir.NodeWhile;
import net.sf.orcc.ir.OpBinary;
import net.sf.orcc.ir.Pattern;
import net.sf.orcc.ir.Port;
import net.sf.orcc.ir.Procedure;
import net.sf.orcc.ir.Tag;
import net.sf.orcc.ir.Type;
import net.sf.orcc.ir.TypeList;
import net.sf.orcc.ir.Use;
import net.sf.orcc.ir.Var;
import net.sf.orcc.ir.impl.IrFactoryImpl;
import net.sf.orcc.util.ActionList;
import net.sf.orcc.util.OrccUtil;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * This class transforms an AST actor to its IR equivalent.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class ActorTransformer extends CalSwitch<Actor> {

	/**
	 * count of un-tagged actions
	 */
	private int untaggedCount;

	/**
	 * Creates a new AST to IR transformation.
	 */
	public ActorTransformer() {
	}

	/**
	 * Loads tokens from the data that was read and put in portVariable.
	 * 
	 * @param portVariable
	 *            a local array that contains data.
	 * @param tokens
	 *            a list of token variables
	 * @param repeat
	 *            an integer number of repeat (equals to one if there is no
	 *            repeat)
	 */
	private void actionLoadTokens(Procedure procedure, Var portVariable,
			List<Variable> tokens, int repeat) {
		if (repeat == 1) {
			int i = 0;

			for (Variable token : tokens) {
				List<Expression> indexes = new ArrayList<Expression>(1);
				indexes.add(IrFactory.eINSTANCE.createExprInt(i));
				int lineNumber = portVariable.getLineNumber();

				Var irToken = (Var) Frontend.getMapping(token);
				InstLoad load = IrFactory.eINSTANCE.createInstLoad(lineNumber,
						irToken, portVariable, indexes);
				procedure.getLast().add(load);

				i++;
			}
		} else {
			// creates loop variable and initializes it
			Var loopVar = procedure.newTempLocalVariable(
					IrFactory.eINSTANCE.createTypeInt(32), "num_repeats");
			InstAssign assign = IrFactory.eINSTANCE.createInstAssign(loopVar,
					IrFactory.eINSTANCE.createExprInt(0));
			procedure.getLast().add(assign);

			NodeBlock block = IrFactoryImpl.eINSTANCE.createNodeBlock();

			int i = 0;
			int numTokens = tokens.size();
			Type type = ((TypeList) portVariable.getType()).getType();
			for (Variable token : tokens) {
				int lineNumber = portVariable.getLineNumber();
				List<Expression> indexes = new ArrayList<Expression>(1);
				indexes.add(IrFactory.eINSTANCE.createExprBinary(
						IrFactory.eINSTANCE.createExprBinary(
								IrFactory.eINSTANCE.createExprInt(numTokens),
								OpBinary.TIMES,
								IrFactory.eINSTANCE.createExprVar(loopVar),
								IrFactory.eINSTANCE.createTypeInt(32)),
						OpBinary.PLUS, IrFactory.eINSTANCE.createExprInt(i),
						IrFactory.eINSTANCE.createTypeInt(32)));

				Var tmpVar = procedure.newTempLocalVariable(type, "token");
				InstLoad load = IrFactory.eINSTANCE.createInstLoad(lineNumber,
						tmpVar, portVariable, indexes);
				block.add(load);

				Var irToken = (Var) Frontend.getMapping(token);

				indexes = new ArrayList<Expression>(1);
				indexes.add(IrFactory.eINSTANCE.createExprVar(loopVar));
				InstStore store = IrFactory.eINSTANCE.createInstStore(
						lineNumber, irToken, indexes,
						IrFactory.eINSTANCE.createExprVar(tmpVar));
				block.add(store);

				i++;
			}

			// add increment
			assign = IrFactory.eINSTANCE.createInstAssign(loopVar,
					IrFactory.eINSTANCE.createExprBinary(
							IrFactory.eINSTANCE.createExprVar(loopVar),
							OpBinary.PLUS,
							IrFactory.eINSTANCE.createExprInt(1),
							loopVar.getType()));
			block.add(assign);

			// create while node
			Expression condition = IrFactory.eINSTANCE.createExprBinary(
					IrFactory.eINSTANCE.createExprVar(loopVar), OpBinary.LT,
					IrFactory.eINSTANCE.createExprInt(repeat),
					IrFactory.eINSTANCE.createTypeBool());
			List<Node> nodes = new ArrayList<Node>(1);
			nodes.add(block);

			NodeWhile nodeWhile = IrFactoryImpl.eINSTANCE.createNodeWhile();
			nodeWhile.setJoinNode(IrFactoryImpl.eINSTANCE.createNodeBlock());
			nodeWhile.setCondition(condition);
			nodeWhile.getNodes().addAll(nodes);

			procedure.getNodes().add(nodeWhile);
		}
	}

	/**
	 * Assigns tokens to the data that will be written.
	 * 
	 * @param portVariable
	 *            a local array that will contain data.
	 * @param values
	 *            a list of AST expressions
	 * @param repeat
	 *            an integer number of repeat (equals to one if there is no
	 *            repeat)
	 */
	private void actionStoreTokens(AstTransformer transformer,
			Procedure procedure, Var portVariable, List<AstExpression> values,
			int repeat) {
		if (repeat == 1) {
			int i = 0;

			for (AstExpression expression : values) {
				int lineNumber = portVariable.getLineNumber();
				List<Expression> indexes = new ArrayList<Expression>(1);
				indexes.add(IrFactory.eINSTANCE.createExprInt(i));

				Expression value = transformer.transformExpression(expression);
				InstStore store = IrFactory.eINSTANCE.createInstStore(
						lineNumber, portVariable, indexes, value);
				procedure.getLast().add(store);

				i++;
			}
		} else {
			// creates loop variable and initializes it
			Var loopVar = procedure.newTempLocalVariable(
					IrFactory.eINSTANCE.createTypeInt(32), "num_repeats");
			InstAssign assign = IrFactory.eINSTANCE.createInstAssign(loopVar,
					IrFactory.eINSTANCE.createExprInt(0));
			procedure.getLast().add(assign);

			NodeBlock block = IrFactoryImpl.eINSTANCE.createNodeBlock();

			int i = 0;
			int numTokens = values.size();
			Type type = ((TypeList) portVariable.getType()).getType();
			for (AstExpression value : values) {
				int lineNumber = portVariable.getLineNumber();
				List<Expression> indexes = new ArrayList<Expression>(1);
				indexes.add(IrFactory.eINSTANCE.createExprVar(loopVar));

				// each expression of an output pattern must be of type list
				// so they are necessarily variables
				Var tmpVar = procedure.newTempLocalVariable(type, "token");
				Expression expression = transformer.transformExpression(value);
				Use use = ((ExprVar) expression).getUse();

				InstLoad load = IrFactory.eINSTANCE.createInstLoad(tmpVar,
						use.getVariable(), indexes);
				block.add(load);

				indexes = new ArrayList<Expression>(1);
				indexes.add(IrFactory.eINSTANCE.createExprBinary(
						IrFactory.eINSTANCE.createExprBinary(
								IrFactory.eINSTANCE.createExprInt(numTokens),
								OpBinary.TIMES,
								IrFactory.eINSTANCE.createExprVar(loopVar),
								IrFactory.eINSTANCE.createTypeInt(32)),
						OpBinary.PLUS, IrFactory.eINSTANCE.createExprInt(i),
						IrFactory.eINSTANCE.createTypeInt(32)));
				InstStore store = IrFactory.eINSTANCE.createInstStore(
						lineNumber, portVariable, indexes,
						IrFactory.eINSTANCE.createExprVar(tmpVar));
				block.add(store);

				i++;
			}

			// add increment
			assign = IrFactory.eINSTANCE.createInstAssign(loopVar,
					IrFactory.eINSTANCE.createExprBinary(
							IrFactory.eINSTANCE.createExprVar(loopVar),
							OpBinary.PLUS,
							IrFactory.eINSTANCE.createExprInt(1),
							loopVar.getType()));
			block.add(assign);

			// create while node
			Expression condition = IrFactory.eINSTANCE.createExprBinary(
					IrFactory.eINSTANCE.createExprVar(loopVar), OpBinary.LT,
					IrFactory.eINSTANCE.createExprInt(repeat),
					IrFactory.eINSTANCE.createTypeBool());
			List<Node> nodes = new ArrayList<Node>(1);
			nodes.add(block);

			NodeWhile nodeWhile = IrFactoryImpl.eINSTANCE.createNodeWhile();
			nodeWhile.setJoinNode(IrFactoryImpl.eINSTANCE.createNodeBlock());
			nodeWhile.setCondition(condition);
			nodeWhile.getNodes().addAll(nodes);

			procedure.getNodes().add(nodeWhile);
		}
	}

	/**
	 * Transforms the given AST Actor to an IR actor.
	 * 
	 * @param astActor
	 *            the AST of the actor
	 * @return the actor in IR form
	 */
	@Override
	public Actor caseAstActor(AstActor astActor) {
		Actor actor = IrFactory.eINSTANCE.createActor();
		Frontend.putMapping(astActor, actor);

		actor.setFileName(astActor.eResource().getURI().toPlatformString(true));

		int lineNumber = Util.getLocation(astActor);
		actor.setLineNumber(lineNumber);

		AstTransformer astTransformer = new AstTransformer();

		// parameters
		for (Variable Variable : astActor.getParameters()) {
			Var var = astTransformer.transformGlobalVariable(Variable);
			actor.getParameters().add(var);
		}

		// state variables
		for (Variable Variable : astActor.getStateVariables()) {
			Var var = astTransformer.transformGlobalVariable(Variable);
			actor.getStateVars().add(var);
		}

		// functions
		for (Function function : astActor.getFunctions()) {
			// no need to require this actor
			actor.getProcs().add(Frontend.getProcedure(function, false));
		}

		// procedures
		for (AstProcedure procedure : astActor.getProcedures()) {
			// no need to require this actor
			actor.getProcs().add(Frontend.getProcedure(procedure, false));
		}

		// transform ports
		transformPorts(actor.getInputs(), astActor.getInputs());
		transformPorts(actor.getOutputs(), astActor.getOutputs());

		// transform actions
		ActionList actions = transformActions(astActor.getActions());

		// transform initializes
		ActionList initializes = transformActions(astActor.getInitializes());

		// sort actions by priority
		ActionSorter sorter = new ActionSorter(actions);
		ActionList sortedActions = sorter.applyPriority(astActor
				.getPriorities());

		// transform FSM
		Schedule schedule = astActor.getSchedule();
		RegExp scheduleRegExp = astActor.getScheduleRegExp();
		if (schedule == null && scheduleRegExp == null) {
			actor.getActionsOutsideFsm().addAll(sortedActions.getAllActions());
		} else {
			FSM fsm = null;
			if (schedule != null) {
				FSMBuilder builder = new FSMBuilder(astActor.getSchedule());
				fsm = builder.buildFSM(sortedActions);
			} else {
				RegExpConverter converter = new RegExpConverter(scheduleRegExp);
				fsm = converter.convert(sortedActions);
			}

			actor.getActionsOutsideFsm().addAll(
					sortedActions.getUntaggedActions());
			actor.setFsm(fsm);
		}

		// create IR actor
		AstEntity entity = (AstEntity) astActor.eContainer();
		actor.setName(net.sf.orcc.cal.util.Util.getQualifiedName(entity));
		actor.setNative(entity.isNative());

		actor.getActions().addAll(actions.getAllActions());
		actor.getInitializes().addAll(initializes.getAllActions());

		// TODO clean up
		Frontend.instance.removeDanglingUses(actor);

		// serialize actor and cache
		Frontend.instance.serialize(actor);
		CacheManager.instance.saveCache(astActor.eResource().getURI());

		return actor;
	}

	/**
	 * Creates the test for schedulability of the given action.
	 * 
	 * @param astAction
	 *            an AST action
	 * @param inputPattern
	 *            input pattern of action
	 * @param result
	 *            target local variable
	 */
	private void createActionTest(AstTransformer transformer,
			Procedure procedure, AstAction astAction, Pattern peekPattern,
			Var result) {
		List<AstExpression> guards = astAction.getGuards();
		Expression value;
		if (guards.isEmpty()) {
			value = IrFactory.eINSTANCE.createExprBool(true);
		} else {
			transformInputPatternPeek(transformer, procedure, astAction,
					peekPattern);
			value = transformGuards(transformer, astAction.getGuards());
		}

		InstAssign assign = IrFactory.eINSTANCE.createInstAssign(result, value);
		procedure.getLast().add(assign);
	}

	/**
	 * Creates a variable to hold the number of tokens on the given port.
	 * 
	 * @param port
	 *            a port
	 * @param numTokens
	 *            number of tokens
	 * @return the local array created
	 */
	private Var createPortVariable(int lineNumber, Port port, int numTokens) {
		// create the variable to hold the tokens
		return IrFactory.eINSTANCE.createVar(lineNumber,
				IrFactory.eINSTANCE.createTypeList(numTokens, port.getType()),
				port.getName(), true, 0);
	}

	/**
	 * Transforms the given AST action and adds it to the given action list.
	 * 
	 * @param actionList
	 *            an action list
	 * @param astAction
	 *            an AST action
	 */
	private void transformAction(AstAction astAction, ActionList actionList) {
		int lineNumber = Util.getLocation(astAction);

		// transform tag
		AstTag astTag = astAction.getTag();
		Tag tag;
		String name;
		if (astTag == null) {
			tag = IrFactory.eINSTANCE.createTag();
			name = "untagged_" + untaggedCount++;
		} else {
			tag = IrFactory.eINSTANCE.createTag();
			tag.getIdentifiers().addAll(astAction.getTag().getIdentifiers());
			name = OrccUtil.toString(tag.getIdentifiers(), "_");
		}

		Pattern inputPattern = IrFactory.eINSTANCE.createPattern();
		Pattern outputPattern = IrFactory.eINSTANCE.createPattern();
		Pattern peekPattern = IrFactory.eINSTANCE.createPattern();

		// creates scheduler and body
		Procedure scheduler = IrFactory.eINSTANCE.createProcedure(
				"isSchedulable_" + name, lineNumber,
				IrFactory.eINSTANCE.createTypeBool());
		Procedure body = IrFactory.eINSTANCE.createProcedure(name, lineNumber,
				IrFactory.eINSTANCE.createTypeVoid());

		// transforms action body and scheduler
		transformActionBody(astAction, body, inputPattern, outputPattern);
		transformActionScheduler(astAction, scheduler, peekPattern);

		// creates IR action and add it to action list
		Action action = IrFactory.eINSTANCE.createAction(tag, inputPattern,
				outputPattern, peekPattern, scheduler, body);
		actionList.add(action);
	}

	/**
	 * Transforms the body of the given AST action into the given body
	 * procedure.
	 * 
	 * @param astAction
	 *            an AST action
	 * @param body
	 *            the procedure that will contain the body
	 */
	private void transformActionBody(AstAction astAction, Procedure body,
			Pattern inputPattern, Pattern outputPattern) {
		AstTransformer transformer = new AstTransformer(body);

		for (InputPattern pattern : astAction.getInputs()) {
			transformPattern(transformer, body, pattern, inputPattern);
		}

		transformer.transformLocalVariables(astAction.getVariables());
		transformer.transformStatements(astAction.getStatements());

		List<OutputPattern> astOutputPattern = astAction.getOutputs();
		for (OutputPattern pattern : astOutputPattern) {
			transformPattern(transformer, body, pattern, outputPattern);
		}

		transformer.addReturn(body, null);
	}

	/**
	 * Transforms the given list of AST actions to an ActionList of IR actions.
	 * 
	 * @param actions
	 *            a list of AST actions
	 * @return an ActionList of IR actions
	 */
	private ActionList transformActions(List<AstAction> actions) {
		ActionList actionList = new ActionList();
		for (AstAction astAction : actions) {
			transformAction(astAction, actionList);
		}

		return actionList;
	}

	/**
	 * Transforms the scheduling information of the given AST action into the
	 * given scheduler procedure.
	 * 
	 * @param astAction
	 *            an AST action
	 * @param scheduler
	 *            the procedure that will contain the scheduler
	 * @param inputPattern
	 *            the input pattern filled by
	 *            {@link #fillsInputPattern(AstAction, Pattern)}
	 */
	private void transformActionScheduler(AstAction astAction,
			Procedure scheduler, Pattern peekPattern) {
		AstTransformer transformer = new AstTransformer(scheduler);

		Var result = scheduler.newTempLocalVariable(
				IrFactory.eINSTANCE.createTypeBool(), "result");

		List<AstExpression> guards = astAction.getGuards();
		if (peekPattern.isEmpty() && guards.isEmpty()) {
			// the action is always fireable
			InstAssign assign = IrFactory.eINSTANCE.createInstAssign(result,
					IrFactory.eINSTANCE.createExprBool(true));
			scheduler.getLast().add(assign);
		} else {
			createActionTest(transformer, scheduler, astAction, peekPattern,
					result);
		}

		transformer.addReturn(scheduler,
				IrFactory.eINSTANCE.createExprVar(result));
	}

	/**
	 * Transforms the given guards and assign result the expression g1 && g2 &&
	 * .. && gn.
	 * 
	 * @param guards
	 *            list of guard expressions
	 */
	private Expression transformGuards(AstTransformer transformer,
			List<AstExpression> guards) {
		List<Expression> expressions = transformer.transformExpressions(guards);
		Iterator<Expression> it = expressions.iterator();
		Expression value = it.next();
		while (it.hasNext()) {
			value = IrFactory.eINSTANCE.createExprBinary(value,
					OpBinary.LOGIC_AND, it.next(),
					IrFactory.eINSTANCE.createTypeBool());
		}

		return value;
	}

	/**
	 * Transforms the input patterns of the given AST action when necessary by
	 * generating Peek instructions. An input pattern needs to be transformed to
	 * Peeks when guards reference tokens from the pattern.
	 * 
	 * @param astAction
	 *            an AST action
	 */
	private void transformInputPatternPeek(AstTransformer transformer,
			Procedure scheduler, final AstAction astAction, Pattern peekPattern) {
		final Set<InputPattern> patterns = new HashSet<InputPattern>();
		VoidSwitch peekVariables = new VoidSwitch() {

			@Override
			public Void caseVariableReference(VariableReference reference) {
				EObject obj = reference.getVariable().eContainer();
				if (obj instanceof InputPattern) {
					patterns.add((InputPattern) obj);
				}

				return null;
			}

		};

		// fills the patterns set by visiting guards
		for (AstExpression guard : astAction.getGuards()) {
			peekVariables.doSwitch(guard);
		}

		// add peeks for each pattern of the patterns set
		for (InputPattern pattern : patterns) {
			transformPattern(transformer, scheduler, pattern, peekPattern);
		}
	}

	/**
	 * Transforms the given AST input/output pattern of the given action.
	 * 
	 * @param transformer
	 *            AST to IR transformer
	 * @param procedure
	 *            procedure to which instructions should be added (body or
	 *            scheduler)
	 * @param astPattern
	 *            AST input or output pattern
	 * @param irPattern
	 *            IR input or output pattern
	 */
	private void transformPattern(AstTransformer transformer,
			Procedure procedure, EObject astPattern, Pattern irPattern) {
		Port port;
		int totalConsumption;
		AstExpression astRepeat;
		if (astPattern instanceof InputPattern) {
			InputPattern pattern = ((InputPattern) astPattern);
			astRepeat = pattern.getRepeat();
			port = (Port) Frontend.getMapping(pattern.getPort());
			totalConsumption = pattern.getTokens().size();
		} else {
			OutputPattern pattern = ((OutputPattern) astPattern);
			astRepeat = pattern.getRepeat();
			port = (Port) Frontend.getMapping(pattern.getPort());
			totalConsumption = pattern.getValues().size();
		}

		// evaluates token consumption
		int repeat = 1;
		if (astRepeat != null) {
			repeat = Evaluator.getIntValue(astRepeat);
			totalConsumption *= repeat;
		}
		irPattern.setNumTokens(port, totalConsumption);

		// create port variable
		Var variable = createPortVariable(procedure.getLineNumber(), port,
				totalConsumption);
		irPattern.setVariable(port, variable);

		// load/store tokens (depending on the type of pattern)
		if (astPattern instanceof InputPattern) {
			InputPattern pattern = (InputPattern) astPattern;

			// declare tokens
			List<Variable> tokens = pattern.getTokens();
			for (Variable token : tokens) {
				Var local = transformer.transformLocalVariable(token);
				procedure.getLocals().add(local);
			}

			actionLoadTokens(procedure, variable, tokens, repeat);
		} else {
			OutputPattern pattern = (OutputPattern) astPattern;
			List<AstExpression> values = pattern.getValues();
			actionStoreTokens(transformer, procedure, variable, values, repeat);
		}
	}

	/**
	 * Transforms the given AST ports in an ordered map of IR ports.
	 * 
	 * @param feature
	 * @param portList
	 *            a list of AST ports
	 * @return an ordered map of IR ports
	 */
	private void transformPorts(List<Port> ports, List<AstPort> portList) {
		for (AstPort astPort : portList) {
			Type type = EcoreUtil.copy(Typer.getType(astPort));
			Port port = IrFactory.eINSTANCE.createPort(type, astPort.getName(),
					astPort.isNative());
			Frontend.putMapping(astPort, port);
			ports.add(port);
		}
	}

}
