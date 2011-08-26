/*
 * Copyright (c) 2010, EPFL
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
 *   * Neither the name of the EPFL nor the names of its contributors may be used 
 *     to endorse or promote products derived from this software without specific 
 *     prior written permission.
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

package net.sf.orcc.tools.merger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.orcc.OrccRuntimeException;
import net.sf.orcc.moc.CSDFMoC;
import net.sf.orcc.network.Connection;
import net.sf.orcc.network.Instance;
import net.sf.orcc.network.Vertex;
import net.sf.orcc.util.Rational;

import org.jgrapht.DirectedGraph;

/**
 * This class computes the repetition vector of the graph. All instances of the
 * network are assumed to be SDF. The network classifier is assumed to be
 * computed first.
 * 
 * @author Ghislain Roquier
 * 
 */
public class RepetitionsAnalyzer {

	private DirectedGraph<Vertex, Connection> graph;

	private Map<Vertex, Rational> rationals = new HashMap<Vertex, Rational>();

	private Map<Vertex, Integer> repetitions = new HashMap<Vertex, Integer>();

	public RepetitionsAnalyzer(DirectedGraph<Vertex, Connection> graph) {
		this.graph = graph;

		analyze();
	}

	/**
	 * Computes the repetition vector of the given SDF graph
	 * 
	 */
	private void analyze() {
		// must be an instance's vertex
		Vertex initialVertex = graph.vertexSet().iterator().next();

		calculateRate(initialVertex, new Rational(1, 1));

		Iterator<Rational> it = rationals.values().iterator();
		int lcm = it.next().getDenominator();
		while (it.hasNext()) {
			lcm = Rational.lcm(lcm, it.next().getDenominator());
		}

		for (Map.Entry<Vertex, Rational> entry : rationals.entrySet()) {
			Vertex vertex = entry.getKey();
			Rational rat = entry.getValue();
			int rep = rat.getNumerator() * lcm / rat.getDenominator();
			repetitions.put(vertex, rep);
		}

		checkConsistency();
	}

	/**
	 * Calculates the rate of each instance of the graph
	 * 
	 * @param vertex
	 * @param rate
	 * 
	 */
	private void calculateRate(Vertex vertex, Rational rate) {
		Instance instance = vertex.getInstance();
		if (!instance.getMoC().isCSDF()) {
			throw new OrccRuntimeException("actor" + instance.getClasz()
					+ "is not SDF or CSDF!");
		}

		CSDFMoC moc = (CSDFMoC) instance.getMoC();

		rationals.put(vertex, rate);

		for (Connection conn : graph.outgoingEdgesOf(vertex)) {
			Vertex tgt = graph.getEdgeTarget(conn);
			if (tgt.isInstance()) {
				CSDFMoC tgtMoC = (CSDFMoC) tgt.getInstance().getMoC();
				if (!rationals.containsKey(tgt)) {
					int prd = moc.getNumTokensProduced(conn.getSource());
					int cns = tgtMoC.getNumTokensConsumed(conn.getTarget());
					calculateRate(tgt, rate.mul(new Rational(prd, cns)));
				}
			}
		}

		for (Connection conn : graph.incomingEdgesOf(vertex)) {
			Vertex src = graph.getEdgeSource(conn);
			if (src.isInstance()) {
				CSDFMoC srcMoC = (CSDFMoC) src.getInstance().getMoC();
				if (!rationals.containsKey(src)) {
					int prd = srcMoC.getNumTokensProduced(conn.getSource());
					int cns = moc.getNumTokensConsumed(conn.getTarget());
					calculateRate(src, rate.mul(new Rational(cns, prd)));
				}
			}
		}
	}

	/**
	 * Checks the consistency of the given SDF graph
	 * 
	 */
	private void checkConsistency() {
		for (Connection connection : graph.edgeSet()) {
			int srcRate = repetitions.get(graph.getEdgeSource(connection));
			int tgtRate = repetitions.get(graph.getEdgeTarget(connection));

			CSDFMoC srcMoc = (CSDFMoC) graph.getEdgeSource(connection)
					.getInstance().getMoC();
			CSDFMoC tgtMoc = (CSDFMoC) graph.getEdgeTarget(connection)
					.getInstance().getMoC();

			int prd = srcMoc.getNumTokensProduced(connection.getSource());
			int cns = tgtMoc.getNumTokensConsumed(connection.getTarget());

			if (srcRate * prd != tgtRate * cns) {
				throw new OrccRuntimeException(
						"the given network is inconsistent!");
			}
		}
	}

	public Map<Vertex, Integer> getRepetitions() {
		return repetitions;
	}

}