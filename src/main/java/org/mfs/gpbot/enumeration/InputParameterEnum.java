package org.mfs.gpbot.enumeration;

import org.apache.commons.lang3.StringUtils;

/**
 * Listagem dos parametros de entrada possiveis do bot
 *
 * @author Michel Suzigan
 *
 */
public enum InputParameterEnum {
	USERNAME("	Usuario: ", "--usuario=.*", null), PASSWORD("	Senha: ", "--senha=.*", null),
	APPLICATION("	Nome do aplicativo: ", "--aplicativo=.*", null),
	ACTIVITY("	Atividade (nome ou codigo acima): ", "--atividade=.*", null),
	TODAY_ONLY("	Lançar somente hoje? (s ou sim, em branco para não) [opcional]: ", "--hoje(=.*)?",
			new String[] { "s", "sim" }),
	MONTH("	Mes (1, 2, ..., 12) [opcional]: ", "--mes=.*", null),
	SKIP_DAYS("	Nao lancar estes dias (formato: d, d, d, ...) [opcional]: ", "--skipdays=.*", null),
	CUSTOM_DAYS("	Lancar horas customizadas nestes dias (formato: d(hh:mm), d(hh:mm), d(hh:mm), ...)  [opcional]: ",
			"--customdays=.*", null),
	VISIBLE(null, "--visivel", new String[] { "s", "sim" });

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