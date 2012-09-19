/*
 * Copyright (c) 2012, IRISA
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
 *   * Neither the name of IRISA nor the names of its
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
 
 package net.sf.orcc.backends.llvm.tta

import net.sf.orcc.backends.llvm.tta.architecture.Design
import net.sf.orcc.backends.llvm.tta.architecture.Processor
import net.sf.orcc.backends.llvm.tta.architecture.util.ArchitectureSwitch

/*
 * The template to print the Multiprocessor Architecture Description File.
 *  
 * @author Herve Yviquel
 * 
 */
class TCE_Design_PNDF extends ArchitectureSwitch<CharSequence> {
	
	String path;
	
	new(String path){
		this.path = path;
	}
	
	override caseDesign(Design design)
		'''
		<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
		<processor-network version="0.1">
			«FOR processor:design.processors»
				«processor.doSwitch»
			«ENDFOR»
		</processor-network>
		'''
	
	override caseProcessor(Processor processor)
		'''
		<processor name="«processor.name»" >
			<adf>«path»/«processor.name»/«processor.name».adf</adf>
			<tpef>«path»/«processor.name»/«processor.name».tpef</tpef>
			«FOR instance: processor.mappedActors»
				«FOR input: instance.actor.inputs.filter(port | !port.native)»
					«var incoming = instance.incomingPortMap.get(input)»
					<input name="fifo_«incoming.getValue("id").toString»">
						<address-space>«processor.getMemory(incoming).name»</address-space>
						<signed>«input.type.int»</signed>
						<width>«input.type.sizeInBits/8»</width>
						<size>«incoming.size»</size>
						<trace>«path»/trace/«instance.name»_«input.name».txt</trace>
					</input>
				«ENDFOR»
				«FOR output : instance.actor.outputs.filter(port | !port.native)»
					«var outgoing = instance.outgoingPortMap.get(output).get(0)»
					<output name="fifo_«outgoing.getValue("id").toString»">
						<address-space>«processor.getMemory(outgoing).name»</address-space>
						<signed>«output.type.int»</signed>
						<width>«output.type.sizeInBits/8»</width>
						<size>«outgoing.size»</size>
						<trace>«path»/trace/«instance.name»_«output.name».txt</trace>
					</output>
				«ENDFOR»
			«ENDFOR»
		</processor>
		'''
	
}