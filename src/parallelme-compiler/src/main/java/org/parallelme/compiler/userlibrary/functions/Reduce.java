/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / __/ /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 */

package org.parallelme.compiler.userlibrary.functions;

import java.util.HashMap;

import org.parallelme.compiler.userlibrary.UserLibraryClass;

/**
 * Defines the user library function class Reduce.
 * 
 * @author Wilson de Carvalho
 */
public class Reduce extends UserLibraryClass {
	private static Reduce instance = new Reduce();
	private static final String className = "Reduce";
	private static final String packageName = "org.parallelme.userlibrary.function";

	private Reduce() {
		this.initValidMethodsSet();
	}

	public static Reduce getInstance() {
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initValidMethodsSet() {
		this.validMethods = new HashMap<>();
		this.validMethods.put("function", "UserData");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTyped() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return className;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPackageName() {
		return packageName;
	}
}
