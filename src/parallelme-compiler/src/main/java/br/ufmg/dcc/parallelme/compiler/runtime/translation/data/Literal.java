/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / _ / /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 *  DCC-UFMG
 */

package br.ufmg.dcc.parallelme.compiler.runtime.translation.data;


/**
 * Basic information for runtime literal parameters.
 * 
 * @author Wilson de Carvalho
 * @see Parameter
 */
public class Literal implements Parameter {
	public final String value;
	public final String typeName;

	public Literal(String value, String typeName) {
		this.value = value;
		this.typeName = typeName;
	}

	@Override
	public boolean equals(Object other) {
		if (other.getClass() == this.getClass()) {
			return ((Literal) other).value == this.value
					&& ((Literal) other).typeName == this.typeName;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return this.value;
	}
}