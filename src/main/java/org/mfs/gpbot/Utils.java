package org.mfs.gpbot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Classe de operacoes utilitarias
 *
 * @author michelsuzigan
 *
 */
public class Utils {

	private Utils() {

	}

	public static List<String> readAllLinesFromFile(String filePath) {
		try {
			return Files.readAllLines(Paths.get(filePath));

		} catch (IOException e) {
			throw new GPBotException("Erro ao ler arquivo", e);
		}
	}
}
