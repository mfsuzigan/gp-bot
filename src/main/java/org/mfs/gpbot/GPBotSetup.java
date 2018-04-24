package org.mfs.gpbot;

import java.io.BufferedReader;
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

public class GPBotSetup {

	private static final Logger LOGGER = Logger.getLogger(GPBotSetup.class);

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

	public static ChromeDriver getDriver(String chromeVersion) {
		Map<String, String> chromeDriverVersionsByChrome = getChromeDriverVersionsByChrome();

		String chromeDrivePath = "lib/chromedriver/{0}/chromedriver";
		ChromeDriver driver = null;

		if (StringUtils.isNotBlank(chromeVersion)) {

			if (!chromeDriverVersionsByChrome.keySet().contains(chromeVersion)) {
				throw new UnsupportedOperationException("Versao do Chrome/Chromium nao suportada");
			}

			chromeDrivePath = MessageFormat.format(chromeDrivePath, chromeDriverVersionsByChrome.get(chromeVersion));
			System.setProperty("webdriver.chrome.driver", chromeDrivePath);
			driver = new ChromeDriver();

		} else {
			LOGGER.info("Versao do Chrome/Chromium nao informada, identificando versao do ChromeDriver aplicavel...");

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

	private static Map<String, String> getChromeDriverVersionsByChrome() {
		Map<String, String> chromeDriverVerionsByChrome = new HashMap<>();
		chromeDriverVerionsByChrome.put("61", "2.34");
		chromeDriverVerionsByChrome.put("62", "2.34");
		chromeDriverVerionsByChrome.put("63", "2.34");
		chromeDriverVerionsByChrome.put("64", "2.37");
		chromeDriverVerionsByChrome.put("65", "2.37");
		chromeDriverVerionsByChrome.put("66", "2.37");
		chromeDriverVerionsByChrome.put("67", "2.38");
		return chromeDriverVerionsByChrome;
	}

	private static List<String> getChromeDriverVersionsByPriority() {
		return Arrays.asList("2.34", "2.37", "2.38");
	}
}
