package org.mfs.gpbot.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.mfs.gpbot.exception.GPBotException;

/**
 * Classe de operacoes utilitarias para arquivos
 *
 * @author michelsuzigan
 *
 */
public class FilesUtils {

	private FilesUtils() {

	}

	public static List<String> readAllLinesFromFile(String filePath) {
		try {
			return Files.readAllLines(Paths.get(filePath));

		} catch (IOException e) {
			throw new GPBotException("Erro ao ler arquivo", e);
		}
	}
}
