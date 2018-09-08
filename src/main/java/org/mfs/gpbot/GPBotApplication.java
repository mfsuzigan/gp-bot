package org.mfs.gpbot;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

/**
 * Classe de inicializacao do bot
 *
 * @author Michel Suzigan
 *
 */
public class GPBotApplication {

	private static final Logger LOGGER = Logger.getLogger(GPBotApplication.class);

	public static void main(String[] args) {
		LOGGER.info("Iniciando lancamento automatizado de horas no TQI-GP");

		try {
			GPBotData data = GPBotSetup.getData(args);
			GPBotEngine.execute(data);

		} catch (Exception e) {
			LOGGER.error("Erro ao lancar horas no TQI-GP", e);
		}

		LOGGER.info("Finalizado lancamento automatizado de horas no TQI-GP");
	}

	public static String getPath() {
		try {
			Path path = Paths.get(
					new URI(GPBotApplication.class.getProtectionDomain().getCodeSource().getLocation().toString()));

			if (path.toFile().isFile() && path.getParent().toFile().isDirectory()) {
				return path.getParent().toString();

			} else {
				return path.toString();
			}

		} catch (URISyntaxException e) {
			throw new GPBotException("Erro ao obter diretorio da aplicacao", e);
		}
	}
}
