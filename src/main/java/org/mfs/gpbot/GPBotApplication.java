package org.mfs.gpbot;

import org.apache.log4j.Logger;

public class GPBotApplication {

	private static final Logger LOGGER = Logger.getLogger(GPBotApplication.class);
	
	public static void main(String[] args) {
		LOGGER.info("Iniciando lancamento automatizado de horas no TQI-GP");

		try {
			GPBotData data = GPBotSetup.getData(args);
			GPBotEngine.execute(data, GPBotSetup.getDriver(data.getChromeVersion()));

		} catch (Exception e) {
			LOGGER.error("Erro ao lancar horas no TQI-GP", e);
		}

		LOGGER.info("Finalizado lancamento automatizado de horas no TQI-GP");
	}	
}
