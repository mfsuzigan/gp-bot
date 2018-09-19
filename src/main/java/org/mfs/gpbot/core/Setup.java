package org.mfs.gpbot.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.mfs.gpbot.Application;
import org.mfs.gpbot.enumeration.InputParameterEnum;
import org.mfs.gpbot.utils.FilesUtils;

/**
 * Captura dados das entradas, identifica a versao do Chrome e do ChromeVersion
 * aplicavel
 *
 * @author Michel Suzigan
 *
 */
public class Setup {

	private Setup() {

	}

	public static Data getData(String[] args) {
		Data data;

		try {
			data = getDataFromArgs(args);

			if (!data.hasEssentialInformation()) {
				data = getDataFromDialog(data);
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

	private static Data getDataFromArgs(String[] args) {
		Data data = new Data();

		Stream.of(InputParameterEnum.values()).forEach(inputParameter -> {

			String argParameter = Stream.of(args).filter(arg -> arg.matches(inputParameter.getCommandLinePattern()))
					.findAny().orElse(null);

			if (StringUtils.isNotBlank(argParameter)) {
				String[] parameterParts = argParameter.split("=");
				String parameterValue = StringUtils.EMPTY;

				if (parameterParts.length > 1) {
					parameterValue = parameterParts[1].trim();
				}

				if (InputParameterEnum.TODAY_ONLY.equals(inputParameter)) {
					parameterValue = (StringUtils.isBlank(parameterValue)
							|| InputParameterEnum.TODAY_ONLY.matchesAnyExpectedValues(parameterValue)) ? "S" : "N";
				}

				if (InputParameterEnum.VISIBLE.equals(inputParameter)) {
					parameterValue = (StringUtils.isBlank(parameterValue)
							|| InputParameterEnum.VISIBLE.matchesAnyExpectedValues(parameterValue)) ? "S" : "N";
				}

				if (InputParameterEnum.ACTIVITY.equals(inputParameter)) {
					parameterValue = normalizeActivityInput(parameterValue);
				}

				data.set(inputParameter, parameterValue);
			}
		});

		return data;
	}

	private static Data getDataFromDialog(Data data) {

		if (data == null) {
			data = new Data();
		}

		for (InputParameterEnum inputParameter : InputParameterEnum.values()) {

			if (!data.isSet(inputParameter)) {
				String parameterValue = requestInput(inputParameter, data);
				data.set(inputParameter, parameterValue.trim());
			}
		}

		return data;

	}

	private static String readInput(InputParameterEnum inputParameter) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		String parameterInput = StringUtils.EMPTY;

		if (InputParameterEnum.PASSWORD.equals(inputParameter)) {
			parameterInput = new String(System.console().readPassword());

		} else {
			parameterInput = bufferedReader.readLine();
		}

		if (InputParameterEnum.ACTIVITY.equals(inputParameter)) {
			parameterInput = normalizeActivityInput(parameterInput);
		}

		if (InputParameterEnum.TODAY_ONLY.equals(inputParameter)) {
			parameterInput = InputParameterEnum.TODAY_ONLY.matchesAnyExpectedValues(parameterInput) ? "S" : "N";
		}

		return parameterInput;
	}

	private static String normalizeActivityInput(String activityInput) {

		if (StringUtils.isNotBlank(activityInput)) {
			Properties activitiesByCode = FilesUtils.loadProperties(Application.getPath() + "/ext/activities.dat");

			if (!activitiesByCode.values().contains(activityInput)) {
				activityInput = activitiesByCode.getProperty(activityInput);
			}
		}

		return activityInput;
	}

	private static void showActivitiesList(InputParameterEnum inputParameter) {

		if (InputParameterEnum.ACTIVITY.equals(inputParameter)) {
			List<String> activities = FilesUtils.readAllLinesFrom(Application.getPath() + "/ext/activities.dat");
			System.out.println("	Lista de atividades:");
			activities.forEach(activity -> System.out.println("		" + activity));
		}
	}

	private static String requestInput(InputParameterEnum inputParameter, Data data) {
		String parameterInput = StringUtils.EMPTY;

		if (shouldRequestInput(inputParameter, data)) {
			showActivitiesList(inputParameter);
			System.out.print(inputParameter.getInputMessage());

			try {
				parameterInput = readInput(inputParameter);

			} catch (Exception e) {
				throw new IllegalArgumentException("Erro ao ler parametro de entrada", e);
			}
		}

		return parameterInput;
	}

	private static boolean shouldRequestInput(InputParameterEnum inputParameter, Data data) {
		boolean parameterIsNowUseless = Boolean.TRUE.equals(data.isTodayOnly())
				&& (InputParameterEnum.MONTH.equals(inputParameter)
						|| InputParameterEnum.CUSTOM_DAYS.equals(inputParameter)
						|| InputParameterEnum.SKIP_DAYS.equals(inputParameter));

		boolean parameterMustBeHidden = inputParameter.getInputMessage() == null;

		return !parameterIsNowUseless && !parameterMustBeHidden;
	}

}
