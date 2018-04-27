package org.mfs.gpbot;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mfs.gpbot.enumeration.InputParameterEnum;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * Captura dados das entradas, identifica a versao do Chrome e do ChromeVersion
 * aplicavel
 * 
 * @author Michel Suzigan
 *
 */
public class GPBotSetup {

	private static final Logger LOGGER = Logger.getLogger(GPBotSetup.class);
	private static final String CHROME_VERSION_COMMAND_OUTPUT_PATTERN = "Google Chrome \\d{2}.*";

	private GPBotSetup() {

	}

	public static GPBotData getData(String[] args) {
		GPBotData data;

		try {
			data = getDataFromArgs(args);

			if (!data.hasEssentialInformation()) {
				data = getDataFromDialog();
			}

		} catch (Exception e) {
			throw new IllegalArgumentException("Erro ao ler informacoes de entrada", e);
		}

		if (!data.hasEssentialInformation()) {
			throw new IllegalArgumentException(
					"Informacoes insuficientes: usuario, senha, atividade e aplicativo sao obrigatorios");
		}

		return data;
	}

	private static GPBotData getDataFromArgs(String[] args) {
		GPBotData data = new GPBotData();

		Stream.of(InputParameterEnum.values()).forEach(inputParameter -> {
			String argParameter = Stream.of(args).filter(arg -> arg.matches(inputParameter.getCommandLinePattern()))
					.findAny().orElse(null);

			if (StringUtils.isNotBlank(argParameter)) {
				String parameterValue = argParameter.split("=")[1];
				data.set(inputParameter, parameterValue.trim());
			}
		});

		return data;
	}

	private static GPBotData getDataFromDialog() {
		GPBotData data = new GPBotData();

		Stream.of(InputParameterEnum.values()).forEach(inputParameter -> {
			String parameterValue = requestInput(inputParameter);
			data.set(inputParameter, parameterValue.trim());
		});

		return data;
	}

	private static String requestInput(InputParameterEnum inputParameter) {
		System.out.print(inputParameter.getInputMessage());
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

		try {
			String parameterInput = null;

			if (InputParameterEnum.PASSWORD.equals(inputParameter)) {
				parameterInput = new String(System.console().readPassword());

			} else {
				parameterInput = bufferedReader.readLine();
			}

			return parameterInput;

		} catch (Exception e) {
			throw new IllegalArgumentException("Erro ao ler parametro de entrada", e);
		}
	}

	public static ChromeDriver getDriver() {
		Map<String, String> chromeDriverVersionsByChrome = getChromeDriverVersionsByChrome();

		String chromeDrivePath = "lib/chromedriver/{0}/chromedriver";
		String chromeVersion = getChromeVersion();
		ChromeDriver driver = null;

		if (StringUtils.isNotBlank(chromeVersion)) {

			if (!chromeDriverVersionsByChrome.keySet().contains(chromeVersion)) {
				throw new UnsupportedOperationException("Versao do Chrome nao suportada");
			} else {
				LOGGER.info("Identificada versao " + chromeVersion + " do Chrome");
			}

			chromeDrivePath = MessageFormat.format(chromeDrivePath, chromeDriverVersionsByChrome.get(chromeVersion));
			System.setProperty("webdriver.chrome.driver", chromeDrivePath);
			driver = new ChromeDriver();

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
