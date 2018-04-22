package org.mfs;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class GPBotTest {

	private WebDriver browser;
	private String applicationName;
	private String activityName;
	private static final String DEFAULT_WORK_HOURS_AMOUNT = "8";

	enum ElementFilterType {
		ID, NAME
	}

	@Before
	public void setUp() {
		applicationName = "PagSeguro";
		activityName = "Codificação";

		System.setProperty("webdriver.chrome.driver", "libs/chromedriver");
		browser = new ChromeDriver();
		browser.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
	}

	@Test
	public void execute() {
		logonOnTQI();
		List<String> workingDays = Arrays.asList("05042018", "06042018");
		workingDays.forEach(this::submitDay);
	}

	private void submitDay(String day) {
		browser.navigate().to("https://helpdesk.tqi.com.br/tqiextranet/helpdesk/atividades.asp?TelaOrigem=menu");
		findElementAndSendKeys(ElementFilterType.NAME, "CmbAtividade", activityName);
		findElementAndSendKeys(ElementFilterType.NAME, "DesAplicativo", applicationName);
		findElementAndSendKeys(ElementFilterType.NAME, "horas_trab", DEFAULT_WORK_HOURS_AMOUNT);

		WebElement date = browser.findElement(By.name("DtaAtividade"));
		date.click();
		date.clear();
		date.sendKeys(day);

		WebElement recordButton = browser.findElement(By.name("BtGravar"));
		recordButton.click();
	}

	private void logonOnTQI() {
		browser.get("https://helpdesk.tqi.com.br/sso/login.action");

		findElementAndSendKeys(ElementFilterType.ID, "userName", "michel.suzigan");
		findElementAndSendKeys(ElementFilterType.ID, "userPass", "m68456845");

		WebElement okButton = browser.findElement(By.id("submitLogin"));
		okButton.click();

		browser.findElement(By.name("conteúdo"));
	}

	private void findElementAndSendKeys(ElementFilterType elementFilterType, String elementFilter, String keys) {

		switch (elementFilterType) {
		case ID:
			browser.findElement(By.id(elementFilter)).sendKeys(keys);
			break;

		case NAME:
			browser.findElement(By.name(elementFilter)).sendKeys(keys);
			break;
		}

	}
}