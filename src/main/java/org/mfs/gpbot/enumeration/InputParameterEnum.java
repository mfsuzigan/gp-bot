package org.mfs.gpbot.enumeration;

public enum InputParameterEnum {
	USERNAME("Usuario: ", "--usuario=.*"), PASSWORD("Senha: ", "--senha=.*"), ACTIVITY("Atividade: ", "--atividade=.*"),
	APPLICATION("Aplicativo: ", "--aplicativo=.*"), SKIP_DAYS("Dias a serem ignorados: ", "--skipdays=.*"),
	CUSTOM_DAYS("Dias com horas customizadas: ", "--customdays=.*");

	private String inputMessage;
	private String commandLinePattern;

	private InputParameterEnum(String inputMessage, String commandLineAlias) {
		this.inputMessage = inputMessage;
		commandLinePattern = commandLineAlias;
	}

	public String getInputMessage() {
		return inputMessage;
	}

	public String getCommandLinePattern() {
		return commandLinePattern;
	}
}