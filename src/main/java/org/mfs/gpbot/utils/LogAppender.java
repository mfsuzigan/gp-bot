package org.mfs.gpbot.utils;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;

public class LogAppender extends FileAppender {

	@Override
	public void setFile(String file) {
		super.setFile(formatarNomeArquivo(file));
	}

	private static String formatarNomeArquivo(String nomeArquivo) {
		return MessageFormat.format(nomeArquivo, new SimpleDateFormat("yyyyMMddhh").format(new Date()));
	}
}