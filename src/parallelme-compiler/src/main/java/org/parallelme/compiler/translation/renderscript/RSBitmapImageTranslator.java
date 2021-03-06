/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / __/ /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 */

package org.parallelme.compiler.translation.renderscript;

import java.util.ArrayList;
import java.util.List;

import org.parallelme.compiler.intermediate.InputBind;
import org.parallelme.compiler.intermediate.MethodCall;
import org.parallelme.compiler.intermediate.OutputBind;
import org.parallelme.compiler.translation.CTranslator;
import org.parallelme.compiler.translation.userlibrary.BitmapImageTranslator;
import org.parallelme.compiler.userlibrary.classes.BitmapImage;
import org.stringtemplate.v4.ST;

/**
 * Definitions for BitmapImage translation to RenderScript runtime.
 * 
 * @author Wilson de Carvalho
 */
public class RSBitmapImageTranslator extends RSImageTranslator implements
		BitmapImageTranslator {
	private static final String templateInputBindObjCreation = "Type <dataTypeInputObject>;\n"
			+ "<inputObject> = Allocation.createFromBitmap(PM_mRS, <param>, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT | Allocation.USAGE_SHARED);\n"
			+ "<dataTypeInputObject> = new Type.Builder(PM_mRS, Element.F32_3(PM_mRS))\n"
			+ "\t.setX(<inputObject>.getType().getX())\n"
			+ "\t.setY(<inputObject>.getType().getY())\n"
			+ "\t.create();\n"
			+ "<outputObject> = Allocation.createTyped(PM_mRS, <dataTypeInputObject>);\n"
			+ "<kernelName>.forEach_toFloat<classType>(<inputObject>, <outputObject>);";
	private static final String templateInputBind = "\nfloat3 __attribute__((kernel)) toFloat<classType>(uchar4 PM_in, uint32_t x, uint32_t y) {"
			+ "\n\tfloat3 PM_out;"
			+ "\n\tPM_out.s0 = (float) PM_in.r;"
			+ "\n\tPM_out.s1 = (float) PM_in.g;"
			+ "\n\tPM_out.s2 = (float) PM_in.b;" + "\n\treturn PM_out;" + "\n}";
	private static final String templateOutputBind = "\nuchar4 __attribute__((kernel)) toBitmapBitmapImage(float3 PM_in, uint32_t x, uint32_t y) {"
			+ "\n\tuchar4 PM_out;"
			+ "\n\tPM_out.r = (uchar) (PM_in.s0);"
			+ "\n\tPM_out.g = (uchar) (PM_in.s1);"
			+ "\n\tPM_out.b = (uchar) (PM_in.s2);"
			+ "\n\tPM_out.a = 255;"
			+ "\n\treturn PM_out;\n}";

	public RSBitmapImageTranslator(CTranslator cCodeTranslator) {
		super(cCodeTranslator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String translateInputBind(String className, InputBind inputBind) {
		ST st = new ST(templateInputBind);
		st.add("classType", inputBind.variable.typeName);
		return st.render();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String translateInputBindObjCreation(String className,
			InputBind inputBind) {
		String inputObject = commonDefinitions
				.getVariableInName(inputBind.variable);
		String outputObject = commonDefinitions
				.getVariableOutName(inputBind.variable);
		String dataTypeInputObject = commonDefinitions
				.getVariableInName(inputBind.variable) + "DataType";
		ST st = new ST(templateInputBindObjCreation);
		st.add("dataTypeInputObject", dataTypeInputObject);
		st.add("inputObject", inputObject);
		st.add("outputObject", outputObject);
		st.add("param", inputBind.parameters.get(0));
		st.add("kernelName", this.commonDefinitions.getKernelName(className));
		st.add("classType", inputBind.variable.typeName);
		return st.render();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String translateOutputBind(String className, OutputBind outputBind) {
		return templateOutputBind;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getJavaInterfaceImports() {
		ArrayList<String> ret = new ArrayList<>();
		ret.add("android.graphics.Bitmap");
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getJavaClassImports() {
		return this.getJavaInterfaceImports();
	}

	/**
	 * {@inheritDoc}
	 */
	public String translateMethodCall(String className, MethodCall methodCall) {
		// TODO Throw an exception whenever a non supported method is provided.
		String ret = "return ";
		if (methodCall.methodName.equals(BitmapImage.getInstance()
				.getHeightMethodName())) {
			ret += this.commonDefinitions
					.getVariableInName(methodCall.variable)
					+ ".getType().getY();";
		} else if (methodCall.methodName.equals(BitmapImage.getInstance()
				.getWidthMethodName())) {
			ret += this.commonDefinitions
					.getVariableInName(methodCall.variable)
					+ ".getType().getX();";
		}
		return ret;
	}
}
