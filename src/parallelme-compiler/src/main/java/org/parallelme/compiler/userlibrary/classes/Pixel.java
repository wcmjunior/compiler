/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / __/ /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 */

package org.parallelme.compiler.userlibrary.classes;

import java.util.HashMap;

import org.parallelme.compiler.userlibrary.UserLibraryClassImpl;

/**
 * Defines the user library class Pixel.
 * 
 * @author Wilson de Carvalho
 */
public class Pixel extends UserLibraryClassImpl {
	private static Pixel instance = new Pixel();

	private Pixel() {
		this.initValidMethodsSet();
	}

	public static Pixel getInstance() {
		return instance;
	}	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTyped() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initValidMethodsSet() {
		this.validMethods = new HashMap<>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public static String getName() {
		return "Pixel";
	}
}
