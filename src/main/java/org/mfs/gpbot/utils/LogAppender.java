package org.mfs.gpbot.utils;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;
import org.mfs.gpbot.GPBotApplication;

public class LogAppender extends FileAppender {

	@Override
	public void setFile(String file) {
		super.setFile(formatarNomeArquivo(GPBotApplication.getPath() + "/logs/" + file));
	}

	private static String formatarNomeArquivo(String nomeArquivo) {
		return MessageFormat.format(nomeArquivo, new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
	}
}