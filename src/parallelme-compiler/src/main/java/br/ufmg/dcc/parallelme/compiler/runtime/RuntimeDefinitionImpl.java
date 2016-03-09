/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / _ / /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 *  DCC-UFMG
 */

package br.ufmg.dcc.parallelme.compiler.runtime;

import br.ufmg.dcc.parallelme.compiler.runtime.translation.CTranslator;
import br.ufmg.dcc.parallelme.compiler.runtime.translation.data.Iterator;
import br.ufmg.dcc.parallelme.compiler.runtime.translation.data.Variable;

/**
 * Code useful for specfic runtime definition implementation.
 * 
 * @author Wilson de Carvalho
 */
public abstract class RuntimeDefinitionImpl implements RuntimeDefinition {
	private final String inSuffix = "In";
	private final String outSuffix = "Out";
	private final String functionName = "function";
	private final String prefix = "$";
	private final CTranslator cCodeTranslator;

	public RuntimeDefinitionImpl(CTranslator cCodeTranslator) {
		this.cCodeTranslator = cCodeTranslator;
	}

	protected String getVariableInName(Variable variable) {
		return prefix + variable.name + inSuffix;
	}

	protected String getVariableOutName(Variable variable) {
		return prefix + variable.name + outSuffix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFunctionName(int functionNumber) {
		return functionName + functionNumber;
	}

	protected String getPrefix() {
		return prefix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String translateIteratorCode(Iterator iterator) {
		String returnString = "return "
				+ iterator.getUserFunctionData().variableArgument.name + ";";
		String code2Translate = iterator.getUserFunctionData().Code;
		// Remove the last curly brace to add the return statement
		code2Translate = code2Translate.substring(0,
				code2Translate.lastIndexOf("}"));
		code2Translate = code2Translate + "\n" + returnString + "\n}";
		return this.getIteratorFunctionSignature(iterator)
				+ this.translateVariable(
						iterator.getUserFunctionData().variableArgument,
						this.cCodeTranslator.translate(code2Translate));
	}

	/**
	 * Create the function signature for a given iterator.
	 * 
	 * @param iterator
	 *            Iterator that must be analyzed in order to create a function
	 *            signature.
	 * 
	 * @return Function signature.
	 */
	abstract protected String getIteratorFunctionSignature(Iterator iterator);
}