package org.mfs.gpbot.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mfs.gpbot.Application;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeDriverLoader {

	private static final String SHIPPED_CHROMEDRIVERS_PATH = "/lib/chromedriver/{0}/chromedriver";
	private static final String CUSTOM_CHROMEDRIVER_PATH = "/chromedriver/chromedriver";
	private static final Logger LOGGER = Logger.getLogger(ChromeDriverLoader.class);

	private ChromeDriverLoader() {

	}

	private static final String CHROME_VERSION_COMMAND_OUTPUT_PATTERN = "Google Chrome \\d{2}.*";

	public static ChromeDriver getDriver(boolean shouldRunGUI) {

		Map<String, String> chromeDriverVersionsByChrome = getChromeDriverVersionsByChrome();
		String chromeDrivePath = Application.getPath() + CUSTOM_CHROMEDRIVER_PATH;
		ChromeDriver driver = null;

		if (new File(chromeDrivePath).isFile()) {
			driver = loadDriver(shouldRunGUI);
		}

		String chromeDrivePath = Application.getPath() + SHIPPED_CHROMEDRIVERS_PATH;

		String chromeVersion = getChromeVersion();

		if (driver == null && StringUtils.isNotBlank(chromeVersion)) {

			if (!chromeDriverVersionsByChrome.keySet().contains(chromeVersion)) {
				throw new UnsupportedOperationException("Versao do Chrome nao suportada");
			} else {
				LOGGER.info("Identificada versao " + chromeVersion + " do Chrome");
			}

			chromeDrivePath = MessageFormat.format(chromeDrivePath, chromeDriverVersionsByChrome.get(chromeVersion));
			System.setProperty("webdriver.chrome.driver", chromeDrivePath);

			driver = loadDriver(shouldRunGUI);

		} else {
			LOGGER.info("Versao do Chrome nao identificada, identificando versao do ChromeDriver aplicavel...");

			for (String chromeDriverVersion : getChromeDriverVersionsByPriority()) {
				chromeDrivePath = MessageFormat.format(chromeDrivePath, chromeDriverVersion);

				try {
					System.setProperty("webdriver.chrome.driver", chromeDrivePath);
					driver = new ChromeDriver();
					break;

				} catch (Exception e) {
					// driver nao encontrado ainda
				}
			}
		}

		if (driver == null) {
			throw new UnsupportedOperationException("Nenhuma versao do ChromeDriver disponivel pode ser aplicada");
		}

		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		return driver;
	}

	private static ChromeDriver loadDriver(boolean shouldRunGUI) {
		ChromeDriver driver;
		if (shouldRunGUI) {
			driver = new ChromeDriver();
		} else {
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.addArguments("headless");
			chromeOptions.setCapability("acceptInsecureCerts", true);
			driver = new ChromeDriver(chromeOptions);
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

	private static Map<String, String> getChromeDriverVersionsByChrome() {
		Map<String, String> chromeDriverVersionsByChrome = new HashMap<>();
		chromeDriverVersionsByChrome.put("61", "2.34");
		chromeDriverVersionsByChrome.put("62", "2.34");
		chromeDriverVersionsByChrome.put("63", "2.34");
		chromeDriverVersionsByChrome.put("64", "2.37");
		chromeDriverVersionsByChrome.put("65", "2.37");
		chromeDriverVersionsByChrome.put("66", "2.37");
		chromeDriverVersionsByChrome.put("67", "2.38");
		return chromeDriverVersionsByChrome;
	}

	private static List<String> getChromeDriverVersionsByPriority() {
		return Arrays.asList("2.34", "2.37", "2.38");
	}
}
