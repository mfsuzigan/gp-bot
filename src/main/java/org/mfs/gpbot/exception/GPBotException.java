package org.mfs.gpbot.exception;

public class GPBotException extends RuntimeException {

	private static final long serialVersionUID = 7483021443489244077L;

	public GPBotException(String message) {
		super(message);
	}

	public GPBotException(Throwable e) {
		super(e);
	}

	public GPBotException(String message, Throwable e) {
		super(message, e);
	}
}
