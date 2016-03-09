/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / _ / /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 *  DCC-UFMG
 */

package br.ufmg.dcc.parallelme.compiler;

import java.io.FileInputStream;
import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import br.ufmg.dcc.parallelme.compiler.antlr.JavaLexer;
import br.ufmg.dcc.parallelme.compiler.antlr.JavaParser;
import br.ufmg.dcc.parallelme.compiler.runtime.RuntimeDefinition;
import br.ufmg.dcc.parallelme.compiler.symboltable.*;

/**
 * Main class for ParallelME compiler. It is responsible for parsing the user
 * code in Java, detect ParallelME usage and translate it into an output code
 * that can execute on the target runtime.
 * 
 * @author Wilson de Carvalho, Pedro Caldeira
 */
public class Compiler {
	// Target runtime for output code.
	private final RuntimeDefinition targetRuntime;

	public Compiler(RuntimeDefinition targetRuntime) {
		this.targetRuntime = targetRuntime;
	}

	/**
	 * Compile a list of files storing them on the folder informed.
	 * 
	 * @param files
	 *            List of files that must be compiled.
	 * @param destinationFolder
	 *            Destination folder.
	 * @throws IOException
	 *             Exception thrown in case of issue while reading or writing
	 *             files.
	 */
	public void compile(String[] files, String destinationFolder)
			throws IOException {
		RootSymbol[] symbolTables = new RootSymbol[files.length];
		ParseTree[] parseTrees = new ParseTree[files.length];
		JavaParser[] javaParser = new JavaParser[files.length];
		TokenStreamRewriter[] tokenStreams = new TokenStreamRewriter[files.length];
		// ####### First pass #######
		TranslatorFirstPass firstPass = new TranslatorFirstPass();
		for (int i = 0; i < files.length; i++) {
			String file = files[i];
			SimpleLogger.info("1st pass file - " + file);
			FileInputStream is = new FileInputStream(file);
			try {
				CommonTokenStream tokenStream = new CommonTokenStream(
						new JavaLexer(new ANTLRInputStream(is)));
				JavaParser parser = new JavaParser(tokenStream);
				ParseTree tree = parser.compilationUnit();
				// Root of this file's symbol table
				RootSymbol symbolTable = new RootSymbol();
				// Object that will be used to rewrite this file, if necessary
				TokenStreamRewriter tokenStreamRewriter = new TokenStreamRewriter(
						tokenStream);
				// Executes the first pass
				firstPass.run(tokenStreamRewriter, symbolTable, tree);
				// Stores objects by file for next compiler pass
				tokenStreams[i] = tokenStreamRewriter;
				javaParser[i] = parser;
				parseTrees[i] = tree;
				symbolTables[i] = symbolTable;
			} finally {
				is.close();
			}
		}
		int iteratorCount = 0;
		// ####### Second pass #######
		TranslatorSecondPass secondPass = new TranslatorSecondPass(
				this.targetRuntime, destinationFolder);
		for (int i = 0; i < files.length; i++) {
			String file = files[i];
			SimpleLogger.info("2nd pass file - " + file);
			secondPass.run(tokenStreams[i], symbolTables[i], parseTrees[i],
					iteratorCount);
			iteratorCount += secondPass.getFunctionsCount();
		}
		// Export internal library files for this target runtime
		this.targetRuntime.exportInternalLibrary("", destinationFolder);
	}
}