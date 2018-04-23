package org.mfs.gpbot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class GPBotApplication {

	private static WebDriver driver;
	private static String applicationName;
	private static String activityName;
	private static final String PASSWORD = "m68456845";
	private static final String USERNAME = "michel.suzigan";
	private static final String DEFAULT_WORK_HOURS_AMOUNT = "8";
	private static final SimpleDateFormat GP_DATE_FORMAT = new SimpleDateFormat("ddMMyyyy");
	private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private static final Logger LOGGER = Logger.getLogger(GPBotApplication.class);

	enum ElementFilterType {
		ID, NAME
	}

	public static void main(String[] args) {
		LOGGER.info("Iniciando lancamento automatizado de horas no TQI-GP");

		try {
			GPBotEngine.execute(GPBotSetup.getData(args), GPBotSetup.getDriver());

		} catch (Exception e) {
			LOGGER.error("Erro ao lancar horas no TQI-GP", e);
		}

		LOGGER.info("Finalizado lancamento automatizado de horas no TQI-GP");
	}

	private static List<String> getWorkingDays() {
		Calendar today = Calendar.getInstance();
		Calendar firstDayOfMonth = Calendar.getInstance();
		firstDayOfMonth.set(Calendar.DAY_OF_MONTH, today.getActualMinimum(Calendar.DAY_OF_MONTH));

		List<String> workingDays = new ArrayList<>();

		for (Calendar dayInMonth = firstDayOfMonth; dayInMonth.before(today) || dayInMonth.equals(today); dayInMonth
				.add(Calendar.DAY_OF_MONTH, 1)) {

			if (dayInMonth.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
					&& dayInMonth.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				workingDays.add(GP_DATE_FORMAT.format(dayInMonth.getTime()));
			}
		}

		return workingDays;
	}

	private static void submitDay(String day) {
		driver.navigate().to("https://helpdesk.tqi.com.br/tqiextranet/helpdesk/atividades.asp?TelaOrigem=menu");
		findElementAndSendKeys(ElementFilterType.NAME, "CmbAtividade", activityName);
		findElementAndSendKeys(ElementFilterType.NAME, "DesAplicativo", applicationName);
		findElementAndSendKeys(ElementFilterType.NAME, "horas_trab", DEFAULT_WORK_HOURS_AMOUNT);

		WebElement date = driver.findElement(By.name("DtaAtividade"));
		date.click();
		date.clear();
		date.sendKeys("01012018");

		WebElement recordButton = driver.findElement(By.name("BtGravar"));
		recordButton.click();

		LOGGER.info("Registradas " + DEFAULT_WORK_HOURS_AMOUNT + " horas no dia " + formatDateForLogging(day));

	}

	private static String formatDateForLogging(String day) {

		try {
			return DEFAULT_DATE_FORMAT.format(GP_DATE_FORMAT.parse(day));

		} catch (ParseException e) {
			LOGGER.error("Error formatting date", e);
			throw new IllegalStateException(e);
		}
	}

	private static void logonTQI() {
		driver.get("https://helpdesk.tqi.com.br/sso/login.action");

		findElementAndSendKeys(ElementFilterType.ID, "userName", USERNAME);
		findElementAndSendKeys(ElementFilterType.ID, "userPass", PASSWORD);

		WebElement okButton = driver.findElement(By.id("submitLogin"));
		okButton.click();

		driver.findElement(By.name("conteúdo"));
	}

	private static void findElementAndSendKeys(ElementFilterType elementFilterType, String elementFilter, String keys) {

		if (elementFilterType == ElementFilterType.ID) {
			driver.findElement(By.id(elementFilter)).sendKeys(keys);

		} else if (elementFilterType == ElementFilterType.NAME) {
			driver.findElement(By.name(elementFilter)).sendKeys(keys);
		}
	}
}
