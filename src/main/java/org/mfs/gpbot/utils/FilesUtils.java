package org.mfs.gpbot.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

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

	public static List<String> readAllLinesFrom(String filePath) {
		try {
			return Files.readAllLines(Paths.get(filePath));

		} catch (IOException e) {
			throw new GPBotException("Erro ao ler arquivo", e);
		}
	}

	public static Properties loadProperties(String propertiesFilePath) {

		Properties activitiesByCode = new Properties();

		try {
			activitiesByCode
					.load(com.google.common.io.Files.newReader(new File(propertiesFilePath), Charset.defaultCharset()));

		} catch (IOException e) {
			throw new GPBotException("Erro ao ler arquivo de propriedades", e);
		}

		return activitiesByCode;
	}
}
