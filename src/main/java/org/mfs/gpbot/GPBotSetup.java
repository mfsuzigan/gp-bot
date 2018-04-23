package org.mfs.gpbot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.mfs.gpbot.enumeration.InputParameterEnum;
import org.openqa.selenium.chrome.ChromeDriver;

public class GPBotSetup {

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
				data.set(inputParameter, parameterValue);
			}
		});

		System.out.println(data);

		return data;
	}

	private static GPBotData getDataFromDialog() {

		GPBotData data = new GPBotData();

		Stream.of(InputParameterEnum.values()).forEach(inputParameter -> {
			String parameterValue = requestInput(inputParameter);
			data.set(inputParameter, parameterValue);
		});

		return data;
	}

	private static String requestInput(InputParameterEnum inputParameter) {
		System.out.print(inputParameter.getInputMessage());
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

		try {

			String parameterInput = null;

			if (InputParameterEnum.PASSWORD.equals(inputParameter)) {
				parameterInput = Arrays.toString(System.console().readPassword());

			} else {
				parameterInput = bufferedReader.readLine();
			}

			return parameterInput;

		} catch (Exception e) {
			throw new IllegalArgumentException("Erro ao ler parametro de entrada", e);
		}
	}

	public static ChromeDriver getDriver() {
		System.setProperty("webdriver.chrome.driver", "lib/chromedriver");
		ChromeDriver driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		return driver;
	}
}
