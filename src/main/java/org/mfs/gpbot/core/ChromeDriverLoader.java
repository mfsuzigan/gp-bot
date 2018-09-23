package org.mfs.gpbot.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mfs.gpbot.Application;
import org.mfs.gpbot.exception.GPBotException;
import org.mfs.gpbot.utils.FilesUtils;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeDriverLoader {

	private static final String SHIPPED_CHROMEDRIVERS_PATH = "/lib/chromedriver/{0}/chromedriver";
	private static final String CUSTOM_CHROMEDRIVER_PATH = "/chromedriver/chromedriver";
	private static final Logger LOGGER = Logger.getLogger(ChromeDriverLoader.class);

	private ChromeDriverLoader() {

	}

	private static final String CHROME_VERSION_COMMAND_OUTPUT_PATTERN = "Google Chrome \\d{2}.*";

	private static ChromeDriver loadCustomChromeDriver(boolean shouldRunGUI) {

		ChromeDriver driver = null;
		String chromeDriverPath = Application.getPath() + CUSTOM_CHROMEDRIVER_PATH;

		if (new File(chromeDriverPath).isFile()) {
			LOGGER.info("Informada versao customizada do ChromeDriver. Carregando...");
			driver = loadDriver(chromeDriverPath, shouldRunGUI);
			LOGGER.info("Versao customizada do ChromeDriver carregada com sucesso");
		}

		return driver;
	}

	private static ChromeDriver loadChromeDriverFromShippedVersions(boolean shouldRunGUI) {

		ChromeDriver driver = null;
		String chromeVersion = getChromeVersion();

		if (StringUtils.isNotBlank(chromeVersion)) {
			LOGGER.info("Identificada versao " + chromeVersion
					+ " do Chrome. Carregando ChromeDriver dentre as versoes distribuidas com o GP Bot...");

			Properties chromeDriverVersionsByChrome = FilesUtils
					.loadProperties(Application.getPath() + "/config/chrome_driver_version_mappings.dat");

			if (!chromeDriverVersionsByChrome.keySet().contains(chromeVersion)) {
				LOGGER.warn("Nao foi possivel identificar uma versao do ChromeDriver compativel");

			} else {
				String chromeDriverPath = Application.getPath() + MessageFormat.format(SHIPPED_CHROMEDRIVERS_PATH,
						chromeDriverVersionsByChrome.get(chromeVersion));
				driver = loadDriver(chromeDriverPath, shouldRunGUI);
			}
		}

		return driver;
	}

	private static ChromeDriver loadChromeDriverFromVersionsPrioritized(boolean shouldRunGUI) {

		ChromeDriver driver = null;
		LOGGER.info("Tentando carregamento do ChromeDriver pela priorizacao de versoes disponiveis...");
		String applicationPath = Application.getPath();

		for (String chromeDriverVersion : getChromeDriverVersionsByPriority()) {
			String chromeDriverPath = applicationPath
					+ MessageFormat.format(SHIPPED_CHROMEDRIVERS_PATH, chromeDriverVersion);

			try {
				LOGGER.info("	versao " + chromeDriverVersion + " do ChromeDriver...");
				driver = loadDriver(chromeDriverPath, shouldRunGUI);
				break;

			} catch (Exception e) {
				LOGGER.warn("	falha");
			}
		}

		return driver;
	}

	public static ChromeDriver getDriver(boolean shouldRunGUI) {

		ChromeDriver driver = null;
		driver = loadCustomChromeDriver(shouldRunGUI);

		if (driver == null) {
			driver = loadChromeDriverFromShippedVersions(shouldRunGUI);
		}

		if (driver == null) {
			driver = loadChromeDriverFromVersionsPrioritized(shouldRunGUI);
		}

		if (driver == null) {
			throw new UnsupportedOperationException("Nenhuma versao do ChromeDriver disponivel pode ser aplicada");
		}

		LOGGER.info("ChromeDriver carregado com sucesso");
		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

		return driver;

	}

	private static ChromeDriver loadDriver(String chromeDriverPath, boolean shouldRunGUI) {

		System.setProperty("webdriver.chrome.driver", chromeDriverPath);
		ChromeDriver driver;

		try {

			if (shouldRunGUI) {
				driver = new ChromeDriver();

			} else {
				ChromeOptions chromeOptions = new ChromeOptions();
				chromeOptions.addArguments("headless");
				chromeOptions.setCapability("acceptInsecureCerts", true);
				driver = new ChromeDriver(chromeOptions);
			}
		} catch (Exception e) {
			String errorMessage = "Erro ao carregar ChromeDriver (" + chromeDriverPath + ")";
			LOGGER.error(errorMessage);
			throw new GPBotException(errorMessage, e);
		}

		return driver;
	}

	private static String getChromeVersion() {
		String chromeVersion = null;

		try {
			InputStream inputStream = Runtime.getRuntime().exec("google-chrome --version").getInputStream();
			String versionCommandOutput = new BufferedReader(new InputStreamReader(inputStream)).readLine();

			if (versionCommandOutput.matches(CHROME_VERSION_COMMAND_OUTPUT_PATTERN)) {
				chromeVersion = versionCommandOutput.split("Google Chrome ")[1].substring(0, 2);
			}

		} catch (Exception e) {
			// erro ao detectar versao do Chrome, tentara os drivers disponiveis
			// sequencialmente
		}

		return chromeVersion;
	}

	private static List<String> getChromeDriverVersionsByPriority() {
		return FilesUtils.readAllLinesFrom(Application.getPath() + "/config/chrome_driver_version_priorities.dat");
	}
}
