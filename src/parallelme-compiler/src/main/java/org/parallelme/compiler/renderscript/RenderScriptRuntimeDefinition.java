/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / __/ /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 */

package org.parallelme.compiler.renderscript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.stringtemplate.v4.ST;
import org.parallelme.compiler.RuntimeDefinitionImpl;
import org.parallelme.compiler.SimpleLogger;
import org.parallelme.compiler.intermediate.*;
import org.parallelme.compiler.translation.CTranslator;
import org.parallelme.compiler.userlibrary.classes.*;
import org.parallelme.compiler.util.FileWriter;

/**
 * General definitions for RenderScript runtime.
 * 
 * @author Wilson de Carvalho, Pedro Caldeira
 */
public class RenderScriptRuntimeDefinition extends RuntimeDefinitionImpl {
	private static final String templateRSFile = "<introductoryMsg>\n<header>\n<functions:{functionName|\n\n<functionName>}>";
	private static final String templateKernels = "private ScriptC_<className> <kernelName>;\n\n";
	private static final String templateConstructor = "public <className>(RenderScript $mRS) {\n\tthis.$mRS = $mRS;\n\tthis.<kernelName> = new ScriptC_<className>($mRS);\n\\}\n";

	public RenderScriptRuntimeDefinition(CTranslator cCodeTranslator,
			String outputDestinationFolder) {
		super(cCodeTranslator, outputDestinationFolder);
		this.initTranslators();
	}

	private void initTranslators() {
		if (super.translators == null) {
			super.translators = new HashMap<>();
			super.translators.put(Array.getName(), new RSArrayTranslator(
					cCodeTranslator));
			super.translators.put(BitmapImage.getName(),
					new RSBitmapImageTranslator(cCodeTranslator));
			super.translators.put(HDRImage.getName(), new RSHDRImageTranslator(
					cCodeTranslator));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TargetRuntime getTargetRuntime() {
		return TargetRuntime.RenderScript;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getIsValidBody() {
		ArrayList<String> ret = new ArrayList<>();
		ret.add("true;");
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getInitializationString(String packageName,
			String className) {
		StringBuilder init = new StringBuilder();
		init.append("private RenderScript $mRS;\n");
		ST st1 = new ST(templateKernels);
		ST st2 = new ST(templateConstructor);
		st1.add("className", className);
		st1.add("kernelName", this.commonDefinitions.getKernelName(className));
		st2.add("className", className);
		st2.add("kernelName", this.commonDefinitions.getKernelName(className));
		init.append(st1.render());
		init.append(st2.render());
		ArrayList<String> ret = new ArrayList<String>();
		String[] tmp = init.toString().split("\n");
		for (int i = 0; i < tmp.length; i++)
			ret.add(tmp[i]);
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getImports(List<UserLibraryData> iteratorsAndBinds) {
		StringBuffer ret = new StringBuffer();
		ret.append("import android.support.v8.renderscript.*;\n");
		ret.append(this.getUserLibraryImports(iteratorsAndBinds));
		return ret.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean translateIteratorsAndBinds(String packageName,
			String className, List<Iterator> iterators,
			List<InputBind> inputBinds, List<OutputBind> outputBinds) {
		// 1. Add file header
		ST st = new ST(templateRSFile);
		st.add("introductoryMsg", this.commonDefinitions.getHeaderComment());
		st.add("header", "#pragma version(1)\n#pragma rs java_package_name("
				+ packageName + ")");
		// 2. Translate input binds
		Set<String> inputBindTypes = new HashSet<String>();
		for (InputBind inputBind : inputBinds) {
			if (!inputBindTypes.contains(inputBind.variable.typeName)) {
				inputBindTypes.add(inputBind.variable.typeName);
				st.add("functions",
						this.translators.get(inputBind.variable.typeName)
								.translateInputBind(className, inputBind));
			}
		}
		// 3. Translate iterators
		for (Iterator iterator : iterators)
			st.add("functions",
					this.translators.get(iterator.variable.typeName)
							.translateIterator(className, iterator));
		// 4. Translate outputbinds
		Set<String> outputBindTypes = new HashSet<String>();
		for (OutputBind outputBind : outputBinds) {
			if (!outputBindTypes.contains(outputBind.variable.typeName)) {
				outputBindTypes.add(outputBind.variable.typeName);
				st.add("functions",
						this.translators.get(outputBind.variable.typeName)
								.translateOutputBind(className, outputBind));
			}
		}
		// 5. Write translated file
		FileWriter.writeFile(className + ".rs", this.outputDestinationFolder,
				st.render());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exportInternalLibrary(String packageName,
			String destinationFolder) throws IOException {
		this.exportResource("Common", destinationFolder);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String translateMethodCall(MethodCall methodCall) {
		String ret = "";
		if (methodCall.variable.typeName.equals(BitmapImage.getName())
				|| methodCall.variable.typeName.equals(HDRImage.getName())) {
			if (methodCall.methodName.equals(BitmapImage.getInstance()
					.getWidthMethodName())) {
				ret = this.commonDefinitions
						.getVariableInName(methodCall.variable)
						+ ".getType().getX()";
			} else if (methodCall.methodName.equals(BitmapImage.getInstance()
					.getHeightMethodName())) {
				ret = this.commonDefinitions
						.getVariableInName(methodCall.variable)
						+ ".getType().getY()";
			}
		} else {
			SimpleLogger.error("");
		}
		return ret;
	}
}
