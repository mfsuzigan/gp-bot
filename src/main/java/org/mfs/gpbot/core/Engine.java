package org.mfs.gpbot.core;

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
import org.mfs.gpbot.Application;
import org.mfs.gpbot.utils.FilesUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Interage com o driver para controlar o navegador e efetuar o lancamento do GP
 * com base nos dados de entrada e configuracao escolhida
 *
 * @author Michel Suzigan
 *
 */
public class Engine {
	private static final String DEFAULT_WORK_HOURS_AMOUNT = "8";
	private static final String CUSTOM_DAY_PATTERN = "(1[0-9]|2[0-9]|3[0-1]|[1-9])\\((\\d+|\\d+\\.\\d{1,2})\\)";
	private static final String SKIP_DAY_PATTERN = "(1[0-9]|2[0-9]|3[0-1]|[1-9])";
	private static final String WEEKEND_DAYS_MESSAGE = "Os seguintes dias correspondem a fins de semana e nao serao lancados:";
	private static final String HOLIDAYS_MESSAGE = "Os seguintes dias correspondem a feriados configurados e nao serao lancados:";
	private static final SimpleDateFormat GP_DATE_FORMAT = new SimpleDateFormat("ddMMyyyy");
	private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private static final Logger LOGGER = Logger.getLogger(Engine.class);

	enum ElementFilterType {
		ID, NAME
	}

	public static void execute(Data data) {
		Map<String, String> daysWithHours = getWorkingDaysWithHours(data);
		processDays(daysWithHours, data.getCustomDays(), CUSTOM_DAY_PATTERN);
		processDays(daysWithHours, data.getSkipDays(), SKIP_DAY_PATTERN);

		int successfullySubmittedDaysCount = 0;
		double successfullySubmittedHoursCount = 0.0d;

		if (!daysWithHours.isEmpty()) {
			RemoteWebDriver driver = ChromeDriverLoader.getDriver();
			logonTQI(driver, data);

			for (Entry<String, String> dayWithHours : daysWithHours.entrySet()) {
				if (submitDay(driver, data, dayWithHours)) {
					successfullySubmittedDaysCount++;
					successfullySubmittedHoursCount += Double.valueOf(dayWithHours.getValue());
				}
			}

			driver.close();
		}

		LOGGER.info("RESULTADO: lancados com sucesso " + successfullySubmittedDaysCount + " de " + daysWithHours.size()
				+ " dias, totalizando " + successfullySubmittedHoursCount + " horas");
	}

	private static Map<String, String> getWorkingDaysWithHours(Data data) {
		Calendar finalDay = getFinalDay(data);
		Calendar firstDay = Boolean.TRUE.equals(data.isTodayOnly()) ? (Calendar) finalDay.clone()
				: getFirstDay(finalDay);

		Map<String, String> workingDays = new TreeMap<>();
		List<String> fixedHolidays = getFixedHolidays(finalDay);

		List<String> formattedWeekendDays = new ArrayList<>();
		List<String> formattedHolidays = new ArrayList<>();

		for (Calendar dayInMonth = firstDay; dayInMonth.before(finalDay) || dayInMonth.equals(finalDay); dayInMonth
				.add(Calendar.DAY_OF_MONTH, 1)) {

			if (matchesWeekendDay(dayInMonth)) {
				formattedWeekendDays.add(DEFAULT_DATE_FORMAT.format(dayInMonth.getTime()));

			} else if (matchesHoliday(dayInMonth, fixedHolidays)) {
				formattedHolidays.add(DEFAULT_DATE_FORMAT.format(dayInMonth.getTime()));

			} else {
				workingDays.put(GP_DATE_FORMAT.format(dayInMonth.getTime()), DEFAULT_WORK_HOURS_AMOUNT);
			}
		}

		showSpecialDaysMessage(formattedWeekendDays, WEEKEND_DAYS_MESSAGE);
		showSpecialDaysMessage(formattedHolidays, HOLIDAYS_MESSAGE);

		return workingDays;
	}

	private static void showSpecialDaysMessage(List<String> formattedSpecialDays, String message) {
		if (!formattedSpecialDays.isEmpty()) {
			LOGGER.info(message);
			formattedSpecialDays.forEach(day -> LOGGER.info("   > " + day));
		}
	}

	private static boolean matchesWeekendDay(Calendar dayInMonth) {
		return dayInMonth.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| dayInMonth.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
	}

	private static boolean matchesHoliday(Calendar dayInMonth, List<String> fixedHolidays) {
		return fixedHolidays.contains(GP_DATE_FORMAT.format(dayInMonth.getTime()));
	}

	private static Calendar getFirstDay(Calendar initialDay) {
		Calendar firstDayOfMonth = Calendar.getInstance();
		firstDayOfMonth.set(Calendar.MONTH, initialDay.get(Calendar.MONTH));
		firstDayOfMonth.set(Calendar.DAY_OF_MONTH, initialDay.getActualMinimum(Calendar.DAY_OF_MONTH));

		return firstDayOfMonth;
	}

	private static Calendar getFinalDay(Data data) {
		Calendar finalDay = Calendar.getInstance();

		if (!Boolean.TRUE.equals(data.isTodayOnly()) && StringUtils.isNotBlank(data.getMonth())
				&& !Integer.toString(finalDay.get(Calendar.MONTH)).equals(data.getMonth())) {
			finalDay.set(Calendar.MONTH, Integer.valueOf(data.getMonth()) - 1);
			finalDay.set(Calendar.DAY_OF_MONTH, finalDay.getActualMaximum(Calendar.DAY_OF_MONTH));
		}

		return finalDay;
	}

	private static List<String> getFixedHolidays(Calendar today) {
		String holidaysFilePath = Application.getPath() + "/ext/fixed_holidays.dat";
		LOGGER.info("Lendo arquivo de feriados: " + holidaysFilePath);
		List<String> fixedHolidaysWithYearAsPlaceHolder = FilesUtils.readAllLinesFromFile(holidaysFilePath);

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

	private static boolean submitDay(RemoteWebDriver driver, Data data, Map.Entry<String, String> dayWithHours) {
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

		LOGGER.info("Lançando " + dayWithHours.getValue() + " horas no dia "
				+ formatDateForLogging(dayWithHours.getKey()) + ":");
		boolean daySuccessfullySubmitted = false;

		if (!defaultLogoTableHeight.equals(resultLogoTableHeight)) {
			String message = driver.findElementByXPath("//table/tbody/tr/td[2]/p/font").getText();
			LOGGER.info("	erro! Mensagem: \"" + message + "\"");
		} else {
			daySuccessfullySubmitted = true;
			LOGGER.info("	sucesso!");
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

	private static void logonTQI(RemoteWebDriver driver, Data data) {
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
