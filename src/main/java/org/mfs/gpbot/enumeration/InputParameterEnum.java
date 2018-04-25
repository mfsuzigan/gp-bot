package org.mfs.gpbot.enumeration;

public enum InputParameterEnum {
	USERNAME("Usuario: ", "--usuario=.*"), 
	PASSWORD("Senha: ", "--senha=.*"), 
	APPLICATION("Nome do aplicativo: ", "--aplicativo=.*"),
	ACTIVITY("Atividade (Codificação, Implantação, Estudo de projeto etc.): ", "--atividade=.*"),
	MONTH("Mes (1, 2, ..., 12) [opcional]: ", "--mes=.*"), 
	SKIP_DAYS("Ignorar estes dias (formato: d, d, d[...]) [opcional]: ", "--skipdays=.*"), 
	CUSTOM_DAYS("Lancar horas especificas nestes dias (formato: d[h], d[h], d[h][...])  [opcional]: ", "--customdays=.*"),
	BROWSER_VERSION("Versao do Chrome (61 - 67) [opcional]: ", "--chromeversion=."); 

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