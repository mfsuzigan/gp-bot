package org.mfs.gpbot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class GPBotEngine {
	private static final String DEFAULT_WORK_HOURS_AMOUNT = "8";
	private static final String CUSTOM_DAY_PATTERN = "(1[0-9]|2[0-9]|3[0-1]|[1-9])\\((\\d+|\\d+\\.\\d{1,2})\\)";
	private static final String SKIP_DAY_PATTERN = "(1[0-9]|2[0-9]|3[0-1]|[1-9])";
	private static final SimpleDateFormat GP_DATE_FORMAT = new SimpleDateFormat("ddMMyyyy");
	private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private static final Logger LOGGER = Logger.getLogger(GPBotSetup.class);

	enum ElementFilterType {
		ID, NAME
	}

	public static void execute(GPBotData data, RemoteWebDriver driver) {
		Map<String, String> daysWithHours = getWorkingDaysWithHours(data.getMonth());
		processDays(daysWithHours, data.getCustomDays(), CUSTOM_DAY_PATTERN);
		processDays(daysWithHours, data.getSkipDays(), SKIP_DAY_PATTERN);
		logonTQI(driver, data);

		int successfullySubmittedDaysCount = 0;
		double successfullySubmittedHoursCount = 0.0d;

		for (Entry<String, String> dayWithHours : daysWithHours.entrySet()) {
			if (submitDay(driver, data, dayWithHours)) {
				successfullySubmittedDaysCount++;
				successfullySubmittedHoursCount += Double.valueOf(dayWithHours.getValue());
			}
		}

		LOGGER.info("RESULTADO: lancados com sucesso " + successfullySubmittedDaysCount + " de " + daysWithHours.size()
				+ " dias, totalizando " + successfullySubmittedHoursCount + " horas");
	}

	private static Map<String, String> getWorkingDaysWithHours(String month) {

		Calendar today = Calendar.getInstance();

		if (StringUtils.isNotBlank(month) && !Integer.toString(today.get(Calendar.MONTH)).equals(month)) {
			today.set(Calendar.MONTH, Integer.valueOf(month) - 1);
			today.set(Calendar.DAY_OF_MONTH, today.getActualMaximum(Calendar.DAY_OF_MONTH));
		}

		Calendar firstDayOfMonth = Calendar.getInstance();
		firstDayOfMonth.set(Calendar.MONTH, today.get(Calendar.MONTH));
		firstDayOfMonth.set(Calendar.DAY_OF_MONTH, today.getActualMinimum(Calendar.DAY_OF_MONTH));

		Map<String, String> workingDays = new TreeMap<>();
		List<String> fixedHolidays = getFixedHolidays(today);

		for (Calendar dayInMonth = firstDayOfMonth; dayInMonth.before(today) || dayInMonth.equals(today); dayInMonth
				.add(Calendar.DAY_OF_MONTH, 1)) {

			String formattedDay = GP_DATE_FORMAT.format(dayInMonth.getTime());

			if (dayInMonth.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
					&& dayInMonth.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
					&& !fixedHolidays.contains(formattedDay)) {
				workingDays.put(formattedDay, DEFAULT_WORK_HOURS_AMOUNT);
			}
		}

		return workingDays;
	}

	private static List<String> getFixedHolidays(Calendar today) {
		try {
			List<String> fixedHolidaysWithYearAsPlaceHolder = Files.readAllLines(Paths.get("ext/fixed_holidays.dat"));

			String currentDayAndMonth = String.format("%02d", today.get(Calendar.MONTH) + 1)
					+ String.format("%02d", today.get(Calendar.DAY_OF_MONTH));

			String currentYear = Integer.toString(today.get(Calendar.YEAR));
			String previousYear = Integer.toString(today.get(Calendar.YEAR) - 1);

			List<String> fixedHolidays = new ArrayList<>();

			for (String fixedHoliday : fixedHolidaysWithYearAsPlaceHolder) {
				String normalizedFixedHoliday = fixedHoliday.substring(2, 4) + fixedHoliday.substring(0, 2);

				if (currentDayAndMonth.compareTo(normalizedFixedHoliday) >= 0) {
					fixedHolidays.add(fixedHoliday + currentYear);

				} else {
					fixedHolidays.add(fixedHoliday + previousYear);
				}
			}

			return fixedHolidays;

		} catch (IOException e) {
			throw new GPBotException("Erro ao ler arquivos de feriados", e);
		}
	}

	private static Map<String, String> processDays(Map<String, String> workingDaysWithHours, List<String> daysList,
			String dayPattern) {

		if (daysList != null) {
			for (String day : daysList) {
				processDay(workingDaysWithHours, day.trim(), dayPattern);
			}
		}

		return workingDaysWithHours;
	}

	private static Map<String, String> processDay(Map<String, String> workingDaysWithHours, String dayToProcess,
			String pattern) {

		if (SKIP_DAY_PATTERN.equals(pattern) && dayToProcess.matches(SKIP_DAY_PATTERN)) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dayToProcess));
			String formattedDay = GP_DATE_FORMAT.format(calendar.getTime());

			workingDaysWithHours.remove(formattedDay);

		} else if (CUSTOM_DAY_PATTERN.equals(pattern) && dayToProcess.matches(CUSTOM_DAY_PATTERN)) {
			String day = dayToProcess.split("\\(")[0];
			String hours = (dayToProcess.split("\\(")[1]).split("\\)")[0];

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));
			String formattedDay = GP_DATE_FORMAT.format(calendar.getTime());

			workingDaysWithHours.put(formattedDay, hours);

		} else {
			LOGGER.warn("Argumento invalido: " + dayToProcess);
		}

		return workingDaysWithHours;
	}

	private static boolean submitDay(RemoteWebDriver driver, GPBotData data, Map.Entry<String, String> dayWithHours) {
		driver.navigate().to("https://helpdesk.tqi.com.br/tqiextranet/helpdesk/atividades.asp?TelaOrigem=menu");
		String defaultLogoTableHeight = driver.findElement(By.xpath("//table")).getCssValue("height");
		
		findElementAndSendKeys(driver, ElementFilterType.NAME, "CmbAtividade", data.getActivityName());
		findElementAndSendKeys(driver, ElementFilterType.NAME, "DesAplicativo", data.getApplicationName());
		findElementAndSendKeys(driver, ElementFilterType.NAME, "horas_trab", dayWithHours.getValue());

		WebElement date = driver.findElement(By.name("DtaAtividade"));
		date.click();
		date.clear();
		date.sendKeys(dayWithHours.getKey());

		WebElement recordButton = driver.findElement(By.name("BtGravar"));
		recordButton.click();

		String resultLogoTableHeight = driver.findElement(By.xpath("//table")).getCssValue("height");
		
		LOGGER.info(
				"Lançando " + dayWithHours.getValue() + " horas no dia " + formatDateForLogging(dayWithHours.getKey()) + ":");
		boolean daySuccessfullySubmitted = false;

		if (!defaultLogoTableHeight.equals(resultLogoTableHeight)) {
			LOGGER.info("		erro! Verifique os dados informados e tente novamente");
		} else {
			daySuccessfullySubmitted = true;
			LOGGER.info("		sucesso!");
		}

		return daySuccessfullySubmitted;
	}

	private static String formatDateForLogging(String day) {
		try {
			return DEFAULT_DATE_FORMAT.format(GP_DATE_FORMAT.parse(day));

		} catch (ParseException e) {
			LOGGER.error("Error formatting date", e);
			throw new IllegalStateException(e);
		}
	}

	private static void logonTQI(RemoteWebDriver driver, GPBotData data) {
		driver.get("https://helpdesk.tqi.com.br/sso/login.action");

		findElementAndSendKeys(driver, ElementFilterType.ID, "userName", data.getUsername());
		findElementAndSendKeys(driver, ElementFilterType.ID, "userPass", data.getPassword());

		WebElement okButton = driver.findElement(By.id("submitLogin"));
		okButton.click();

		driver.findElement(By.name("conteúdo"));
	}

	private static void findElementAndSendKeys(RemoteWebDriver driver, ElementFilterType elementFilterType,
			String elementFilter, String keys) {

		if (elementFilterType == ElementFilterType.ID) {
			driver.findElement(By.id(elementFilter)).sendKeys(keys);

		} else if (elementFilterType == ElementFilterType.NAME) {
			driver.findElement(By.name(elementFilter)).sendKeys(keys);
		}
	}
}
