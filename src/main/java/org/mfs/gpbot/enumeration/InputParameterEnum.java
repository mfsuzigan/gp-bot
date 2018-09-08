package org.mfs.gpbot.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * Listagem dos parametros de entrada possiveis do bot
 *
 * @author Michel Suzigan
 *
 */
public enum InputParameterEnum {
	USERNAME("Usuario: ", "--usuario=.*", null), PASSWORD("Senha: ", "--senha=.*", null),
	APPLICATION("Nome do aplicativo: ", "--aplicativo=.*", null),
	ACTIVITY("Atividade (Codificação, Implantação, Estudo de projeto etc.): ", "--atividade=.*", null),
	ONLY_TODAY("Lançar somente hoje? (S/N) [opcional]: ", "--hoje", new String[] { "s", "sim" }),
	MONTH("Mes (1, 2, ..., 12) [opcional]: ", "--mes=.*", null),
	SKIP_DAYS("Ignorar estes dias (formato: d, d, d[...]) [opcional]: ", "--skipdays=.*", null),
	CUSTOM_DAYS("Lancar horas especificas nestes dias (formato: d(h), d(h), d(h)[...])  [opcional]: ",
			"--customdays=.*", null);

	private String inputMessage;
	private String commandLinePattern;
	private String[] expectedValues;

	private InputParameterEnum(String inputMessage, String commandLineAlias, String[] expectedValues) {
		this.inputMessage = inputMessage;
		commandLinePattern = commandLineAlias;
		this.expectedValues = expectedValues;
	}

	public String getInputMessage() {
		return inputMessage;
	}

	public String getCommandLinePattern() {
		return commandLinePattern;
	}

	public boolean matchesAnyExpectedValues(String value) {
		return StringUtils.equalsAnyIgnoreCase(value, expectedValues);
	}
}