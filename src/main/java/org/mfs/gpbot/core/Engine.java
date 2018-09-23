package org.mfs.gpbot.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mfs.gpbot.Application;
import org.mfs.gpbot.exception.GPBotException;
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
	private static final LocalTime DEFAULT_WORK_DAY_DURATION = LocalTime.of(8, 0);
	private static final String CUSTOM_DAY_PATTERN = "(1[0-9]|2[0-9]|3[0-1]|[1-9])\\((\\d+|\\d+(\\:|\\.)\\d{1,2})\\)";
	private static final String CUSTOM_DAY_PATTERN_COLON = "(1[0-9]|2[0-9]|3[0-1]|[1-9])\\((\\d+|\\d+\\:\\d{1,2})\\)";
	private static final String CUSTOM_DAY_PATTERN_FRACTION = "(1[0-9]|2[0-9]|3[0-1]|[1-9])\\((\\d+|\\d+\\.\\d{1,2})\\)";
	private static final String SKIP_DAY_PATTERN = "(1[0-9]|2[0-9]|3[0-1]|[1-9])";
	private static final String WEEKEND_DAYS_MESSAGE = "Os seguintes dias correspondem a fins de semana e nao serao lancados:";
	private static final String HOLIDAYS_MESSAGE = "Os seguintes dias correspondem a feriados configurados e nao serao lancados:";
	private static final SimpleDateFormat GP_DATE_FORMAT = new SimpleDateFormat("ddMMyyyy");
	private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
	private static final Logger LOGGER = Logger.getLogger(Engine.class);
	private static RemoteWebDriver driver;

	public static void execute(Data data) {
		Map<String, LocalTime> daysWithHours = getWorkingDaysWithHours(data);
		processDays(daysWithHours, data.getCustomDays(), CUSTOM_DAY_PATTERN);
		processDays(daysWithHours, data.getSkipDays(), SKIP_DAY_PATTERN);

		int successfullySubmittedDaysCount = 0;
		int submittedHoursCount = 0;
		int submittedMinutesCount = 0;
		boolean thereHaveBeenErrors = false;

		if (!daysWithHours.isEmpty()) {
			driver = ChromeDriverLoader.getDriver(data.shouldRunGUI());
			loginTQI(data);

			for (Entry<String, LocalTime> dayWithHours : daysWithHours.entrySet()) {
				if (submitDay(data, dayWithHours)) {
					successfullySubmittedDaysCount++;
					submittedHoursCount += dayWithHours.getValue().getHour();
					submittedMinutesCount += dayWithHours.getValue().getMinute();

				} else {
					thereHaveBeenErrors = true;
				}
			}

			submittedHoursCount += submittedMinutesCount / 60;
			submittedMinutesCount = submittedMinutesCount % 60;

			driver.close();
		}

		if (thereHaveBeenErrors) {
			LOGGER.info("	ATENCAO: Houve erros no lancamento. Verifique as mensagens acima!");
		}

		LOGGER.info("	RESULTADO: lancados com sucesso " + successfullySubmittedDaysCount + " de "
				+ daysWithHours.size() + " dias, totalizando " + String.format("%02d", submittedHoursCount) + ":"
				+ String.format("%02d", submittedMinutesCount) + " horas");
	}

	private static Map<String, LocalTime> getWorkingDaysWithHours(Data data) {
		Calendar finalDay = getFinalDay(data);
		Calendar firstDay = Boolean.TRUE.equals(data.isTodayOnly()) ? (Calendar) finalDay.clone()
				: getFirstDay(finalDay);

		Map<String, LocalTime> workingDays = new TreeMap<>();
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
				workingDays.put(GP_DATE_FORMAT.format(dayInMonth.getTime()), DEFAULT_WORK_DAY_DURATION);
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
		String holidaysFilePath = Application.getPath() + "/config/fixed_holidays.dat";
		List<String> fixedHolidaysWithYearAsPlaceHolder = FilesUtils.readAllLinesFrom(holidaysFilePath);

		String currentDayAndMonth = String.format("%02d", today.get(Calendar.MONTH) + 1)
				+ String.format("%02d", today.get(Calendar.DAY_OF_MONTH));

		String currentYear = Integer.toString(today.get(Calendar.YEAR));
		String previousYear = Integer.toString(today.get(Calendar.YEAR) - 1);

		List<String> fixedHolidays = new ArrayList<>();

		for (String fixedHoliday : fixedHolidaysWithYearAsPlaceHolder) {
			String normalizedFixedHoliday = fixedHoliday.substring(2, 4) + fixedHoliday.substring(0, 2);
			String year = currentDayAndMonth.compareTo(normalizedFixedHoliday) >= 0 ? currentYear : previousYear;
			fixedHolidays.add(fixedHoliday + year);
		}

		LOGGER.info("Arquivo de feriados carregado com sucesso");

		return fixedHolidays;
	}

	private static Map<String, LocalTime> processDays(Map<String, LocalTime> daysWithWorkingHours,
			List<String> daysList, String dayPattern) {

		if (daysList != null) {
			for (String day : daysList) {
				processDay(daysWithWorkingHours, day.trim(), dayPattern);
			}
		}

		return daysWithWorkingHours;
	}

	private static Map<String, LocalTime> processDay(Map<String, LocalTime> daysWithHours, String dayToProcess,
			String pattern) {

		if (SKIP_DAY_PATTERN.equals(pattern) && dayToProcess.matches(SKIP_DAY_PATTERN)) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dayToProcess));
			String formattedDay = GP_DATE_FORMAT.format(calendar.getTime());

			daysWithHours.remove(formattedDay);

		} else if (CUSTOM_DAY_PATTERN.equals(pattern) && dayToProcess.matches(CUSTOM_DAY_PATTERN)) {
			processCustomDay(daysWithHours, dayToProcess);

		} else {
			LOGGER.warn("Argumento invalido: " + dayToProcess);
		}

		return daysWithHours;
	}

	private static void processCustomDay(Map<String, LocalTime> daysWithHours, String dayToProcess) {
		String day = dayToProcess.split("\\(")[0];
		String rawDayHours = (dayToProcess.split("\\(")[1]).split("\\)")[0];
		int workingHours = 0;
		int workingMinutes = 0;

		if (dayToProcess.matches(CUSTOM_DAY_PATTERN_COLON)) {
			String[] rawDayHoursParts = rawDayHours.split(":");
			workingHours = Integer.parseInt(rawDayHoursParts[0]);
			workingMinutes = Integer.parseInt(rawDayHoursParts[1]);

		} else if (dayToProcess.matches(CUSTOM_DAY_PATTERN_FRACTION)) {
			Double dayHours = Double.valueOf(rawDayHours);
			workingHours = dayHours.intValue();
			workingMinutes = (int) ((dayHours - Double.valueOf(workingHours)) * 60d);
		}

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day));
		String formattedDay = GP_DATE_FORMAT.format(calendar.getTime());

		daysWithHours.put(formattedDay, LocalTime.of(workingHours, workingMinutes));
	}

	private static void clearAndFillEnabledElement(String elementName, String value) {
		WebElement element = driver.findElement(By.name(elementName));

		if (!"true".equals(element.getAttribute("disabled"))) {
			element.clear();
			element.sendKeys(value);
		}
	}

	private static boolean submitDay(Data data, Entry<String, LocalTime> dayWithHours) {
		driver.navigate().to("https://helpdesk.tqi.com.br/tqiextranet/helpdesk/atividades.asp?TelaOrigem=menu");
		String defaultLogoTableHeight = driver.findElement(By.xpath("//table")).getCssValue("height");

		driver.findElement(By.name("CmbAtividade")).sendKeys(data.getActivityName());
		fillTextElements(data, dayWithHours);

		WebElement recordButton = driver.findElement(By.name("BtGravar"));
		recordButton.click();

		String resultLogoTableHeight = driver.findElement(By.xpath("//table")).getCssValue("height");

		LOGGER.info("Lançando " + dayWithHours.getValue() + " horas no dia "
				+ formatDateForLogging(dayWithHours.getKey()) + ":");
		boolean dayWasSuccessfullySubmitted = false;

		if (!defaultLogoTableHeight.equals(resultLogoTableHeight)) {
			String message = driver.findElementByXPath("//table/tbody/tr/td[2]/p/font").getText();
			LOGGER.info("	erro! Mensagem: \"" + message + "\"");
		} else {
			dayWasSuccessfullySubmitted = true;
			LOGGER.info("	sucesso!");
		}

		return dayWasSuccessfullySubmitted;
	}

	private static void fillTextElements(Data data, Entry<String, LocalTime> dayWithHours) {
		Map<String, String> textElementsWithValues = new HashMap<>();
		textElementsWithValues.put("DesAplicativo", data.getApplicationName());
		textElementsWithValues.put("DtaAtividade", dayWithHours.getKey());
		textElementsWithValues.put("horas_trab", Integer.toString(dayWithHours.getValue().getHour()));
		textElementsWithValues.put("mim_trab", Integer.toString(dayWithHours.getValue().getMinute()));
		textElementsWithValues.forEach((elementName, value) -> clearAndFillEnabledElement(elementName, value));
	}

	private static String formatDateForLogging(String day) {
		try {
			return DEFAULT_DATE_FORMAT.format(GP_DATE_FORMAT.parse(day));

		} catch (ParseException e) {
			LOGGER.error("Erro ao formatar data: " + day, e);
			throw new IllegalStateException(e);
		}
	}

	private static void loginTQI(Data data) {
		LOGGER.info("Efetuando login na intranet TQI...");

		driver.get("https://helpdesk.tqi.com.br/sso/login.action");
		clearAndFillEnabledElement("userName", data.getUsername());
		clearAndFillEnabledElement("userPass", data.getPassword());

		WebElement okButton = driver.findElement(By.id("submitLogin"));
		okButton.click();

		try {
			driver.findElement(By.name("conteúdo"));
			LOGGER.info("Login na intranet TQI efetuado com sucesso");

		} catch (Exception e) {
			throw new GPBotException("Erro ao efetuar login. Verifique usuario e senha informados e tente novamente",
					e);
		}
	}
}
