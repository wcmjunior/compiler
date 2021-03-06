/**                                               _    __ ____
 *   _ __  ___ _____   ___   __  __   ___ __     / |  / /  __/
 *  |  _ \/ _ |  _  | / _ | / / / /  / __/ /    /  | / / /__
 *  |  __/ __ |  ___|/ __ |/ /_/ /__/ __/ /__  / / v  / /__
 *  |_| /_/ |_|_|\_\/_/ |_/____/___/___/____/ /_/  /_/____/
 *
 */

package org.parallelme.compiler.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;

import org.parallelme.compiler.SimpleLogger;

/**
 * Offers support for writing translated code.
 * 
 * @author Wilson de Carvalho
 */
public class FileWriter {
	/**
	 * Write a given file to disk.
	 * 
	 * @param fileName
	 *            File name (including the desired extension).
	 * @param destinationFolder
	 *            Destination folder.
	 * @param fileContents
	 *            String with the file contents.
	 */
	public static void writeFile(String fileName, String destinationFolder,
			String fileContents) {
		PrintWriter writer = null;
		try {
			try {
				File destinationFolderStructure = new File(destinationFolder);
				if (!destinationFolderStructure.exists())
					FileUtils.forceMkdir(destinationFolderStructure);
				writer = new PrintWriter(destinationFolder + File.separator
						+ fileName, "UTF-8");
				writer.print(fileContents);
			} catch (IOException e) {
				SimpleLogger
						.error("Error while saving file: " + e.getMessage());
			}
		} finally {
			if (writer != null)
				writer.close();
		}
	}
}
