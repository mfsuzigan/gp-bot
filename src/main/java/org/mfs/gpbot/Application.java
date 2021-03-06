package org.mfs.gpbot;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.mfs.gpbot.core.Data;
import org.mfs.gpbot.core.Engine;
import org.mfs.gpbot.core.Setup;
import org.mfs.gpbot.exception.GPBotException;

/**
 * Classe de inicializacao do bot
 *
 * @author Michel Suzigan
 *
 */
public class Application {

	private static final Logger LOGGER = Logger.getLogger(Application.class);

	public static void main(String[] args) {
		LOGGER.info("Iniciando lancamento automatizado de horas no TQI-GP");

		try {
			Data data = Setup.getData(args);
			Engine.execute(data);

		} catch (Exception e) {
			LOGGER.error("Erro ao lancar horas no TQI-GP", e);
		}

		LOGGER.info("Finalizado lancamento automatizado de horas no TQI-GP");
	}

	public static String getPath() {
		try {
			Path path = Paths
					.get(new URI(Application.class.getProtectionDomain().getCodeSource().getLocation().toString()));

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
