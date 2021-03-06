/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / __/ /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 */

package org.parallelme.compiler.translation.userlibrary;

import java.util.ArrayList;
import java.util.List;

import org.parallelme.compiler.RuntimeCommonDefinitions;
import org.parallelme.compiler.intermediate.Operation;
import org.parallelme.compiler.intermediate.Operation.OperationType;
import org.parallelme.compiler.intermediate.Variable;
import org.parallelme.compiler.intermediate.Operation.ExecutionType;
import org.parallelme.compiler.translation.BoxedTypes;
import org.parallelme.compiler.translation.PrimitiveTypes;
import org.parallelme.compiler.translation.userlibrary.UserLibraryTranslatorDefinition;
import org.parallelme.compiler.userlibrary.classes.Float32;
import org.parallelme.compiler.userlibrary.classes.Int16;
import org.parallelme.compiler.userlibrary.classes.Int32;
import org.parallelme.compiler.userlibrary.classes.Pixel;

/**
 * Base class for translators containing code that is shared between different
 * target runtimes.
 * 
 * @author Wilson de Carvalho
 */
public abstract class BaseUserLibraryTranslator implements
		UserLibraryTranslatorDefinition {
	protected RuntimeCommonDefinitions commonDefinitions = RuntimeCommonDefinitions
			.getInstance();

	/**
	 * Used to determine a given function type as follows:
	 * 
	 * <pre>
	 * BaseOperation: the operation that will be called by the user code and,
	 * if necessary, call other functions to perform data processing;
	 * 
	 * Tile: function that will process a fraction of the user data;
	 * 
	 * UserCode: a function that corresponds exactly to the user code.  
	 * It will be used to encapsulate user code to be called by BaseOperation
	 * and Tile functions.
	 * 
	 * SetAllocation: function used to configure allocation parameters.
	 * </pre>
	 */
	protected enum FunctionType {
		BaseOperation, Tile, UserCode, SetAllocation;
	}

	/**
	 * Translates variables on the give code to a correspondent runtime-specific
	 * type. Example: replaces all RGB objects by float3 on RenderScript.
	 * 
	 * @param variable
	 *            Variable that must be translated.
	 * @param code
	 *            Original code that must have the reference replaced.
	 * @return A string with the new code with the variable replaced.
	 */
	public String translateVariable(Variable variable, String code) {
		String translatedCode = "";
		if (variable.typeName.equals(Pixel.getInstance().getClassName())) {
			translatedCode = this.translatePixelVariable(variable, code);
		} else if (variable.typeName.equals(Int16.getInstance().getClassName())
				|| variable.typeName.equals(Int32.getInstance().getClassName())
				|| variable.typeName.equals(Float32.getInstance()
						.getClassName())) {
			translatedCode = this.translateNumericVariable(variable, code);
		} else if (PrimitiveTypes.isPrimitive(variable.typeName)) {
			translatedCode = code.replaceAll(variable.typeName,
					PrimitiveTypes.getCType(variable.typeName));
		} else if (BoxedTypes.isBoxed(variable.typeName)) {
			translatedCode = code.replaceAll(variable.typeName,
					BoxedTypes.getCType(variable.typeName));
		}
		return translatedCode;
	}

	public String translatePixelVariable(Variable variable, String code) {
		String ret = code.replaceAll(variable.typeName,
				this.commonDefinitions.translateToCType(variable.typeName));
		ret = ret.replaceAll(variable.name + ".x", "x");
		ret = ret.replaceAll(variable.name + ".y", "y");
		ret = ret
				.replaceAll(variable.name + ".rgba.red", variable.name + ".s0");
		ret = ret.replaceAll(variable.name + ".rgba.green", variable.name
				+ ".s1");
		ret = ret.replaceAll(variable.name + ".rgba.blue", variable.name
				+ ".s2");
		ret = ret.replaceAll(variable.name + ".rgba.alpha", variable.name
				+ ".s3");
		return ret;
	}

	public String translateNumericVariable(Variable variable, String code) {
		String ret = code.replaceAll(variable.typeName,
				this.commonDefinitions.translateToCType(variable.typeName));
		ret = ret.replaceAll(variable.name + ".value", variable.name);
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> translateOperation(Operation operation) {
		List<String> ret = new ArrayList<>();
		// If a given operation contains non-final variables, it will be
		// translated to a sequential version.
		for (Variable variable : operation.getExternalVariables()) {
			if (!variable.isFinal()) {
				operation.setExecutionType(ExecutionType.Sequential);
				break;
			}
		}
		// C functions must be declared before used, so user function
		// must be the first
		ret.add(translateUserFunction(operation));
		if (operation.operationType == OperationType.Foreach) {
			ret.add(translateForeach(operation));
		} else {
			if (operation.operationType == OperationType.Reduce) {
				if (operation.getExecutionType() == ExecutionType.Parallel)
					ret.add(translateParallelReduceTile(operation));
				ret.add(translateReduce(operation));
			} else if (operation.operationType == OperationType.Map) {
				ret.add(translateMap(operation));
			} else if (operation.operationType == OperationType.Filter) {
				ret.add(translateFilterTile(operation));
				ret.add(translateFilter(operation));
			} else {
				throw new RuntimeException("Invalid operation: "
						+ operation.operationType);
			}
		}
		return ret;
	}

	/**
	 * Translates a foreach operation returning a C code compatible with this
	 * runtime.
	 */
	abstract protected String translateForeach(Operation operation);

	/**
	 * Translates a reduce operation returning a C code compatible with this
	 * runtime.
	 */
	abstract protected String translateReduce(Operation operation);

	/**
	 * Translates a reduce tile operation returning a C code compatible with
	 * this runtime.
	 */
	abstract protected String translateParallelReduceTile(Operation operation);

	/**
	 * Translates a map operation returning a C code compatible with this
	 * runtime.
	 */
	abstract protected String translateMap(Operation operation);

	/**
	 * Translates a filter operation returning a C code compatible with this
	 * runtime.
	 */
	abstract protected String translateFilter(Operation operation);

	/**
	 * Translates a filter tile operation returning a C code compatible with
	 * this runtime.
	 */
	abstract protected String translateFilterTile(Operation operation);

	/**
	 * Translates the user code that will be used for composing operations.
	 * 
	 * @param operation
	 *            Operation that must be translated.
	 * @return C code with operation's user code compatible with this runtime.
	 */
	abstract protected String translateUserFunction(Operation operation);

	/**
	 * Given an operation and its body, creates a String with the equivalent
	 * kernelF function.
	 */
	protected String createKernelFunction(Operation operation, String body,
			FunctionType functionType) {
		StringBuilder ret = new StringBuilder();
		ret.append(this.getOperationFunctionSignature(operation, functionType));
		ret.append(" {\n");
		ret.append(body);
		ret.append("}");
		return ret.toString();
	}

	/**
	 * Create a function signature for a given operation.
	 */
	protected String getOperationFunctionSignature(Operation operation,
			FunctionType functionType) {
		String ret;
		if (functionType == FunctionType.UserCode) {
			ret = initializeUserFunctionSignature(operation);
		} else if (operation.operationType == OperationType.Foreach) {
			ret = initializeForeachSignature(operation, functionType);
		} else if (operation.operationType == OperationType.Reduce) {
			ret = initializeReduceSignature(operation, functionType);
		} else if (operation.operationType == OperationType.Map) {
			ret = initializeMapSignature(operation, functionType);
		} else if (operation.operationType == OperationType.Filter) {
			ret = initializeFilterSignature(operation, functionType);
		} else {
			throw new RuntimeException("Operation not supported: "
					+ operation.operationType);
		}
		return ret;
	}

	/**
	 * Initialize the user function signature.
	 */
	abstract protected String initializeUserFunctionSignature(
			Operation operation);

	/**
	 * Initialize foreach function signature.
	 */
	abstract protected String initializeForeachSignature(Operation operation,
			FunctionType functionType);

	/**
	 * Initialize reduce function signature.
	 */
	abstract protected String initializeReduceSignature(Operation operation,
			FunctionType functionType);

	/**
	 * Initialize map function signature.
	 */
	abstract protected String initializeMapSignature(Operation operation,
			FunctionType functionType);

	/**
	 * Initialize filter function signature.
	 */
	abstract protected String initializeFilterSignature(Operation operation,
			FunctionType functionType);

	/**
	 * Name for base variable that is used to index allocation data in C kernel
	 * code.
	 */
	protected String getBaseVariableName() {
		return this.commonDefinitions.getPrefix() + "base";
	}
}
