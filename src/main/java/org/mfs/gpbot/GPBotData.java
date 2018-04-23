package org.mfs.gpbot;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.mfs.gpbot.enumeration.InputParameterEnum;

public class GPBotData {

	private String username;
	private String password;
	private String applicationName;
	private String activityName;
	private String chromeVersion;
	private String month;
	private List<String> skipDays;
	private List<String> customDays;
	private Map<String, String> daysWithWorkingHours;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Map<String, String> getDaysWithWorkingHours() {
		return daysWithWorkingHours;
	}

	public void setDaysWithWorkingHours(Map<String, String> daysWithWorkingHours) {
		this.daysWithWorkingHours = daysWithWorkingHours;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public List<String> getSkipDays() {
		return skipDays;
	}

	public void setSkipDays(List<String> skipDays) {
		this.skipDays = skipDays;
	}

	public List<String> getCustomDays() {
		return customDays;
	}

	public void setCustomDays(List<String> customDays) {
		this.customDays = customDays;
	}

	public String getChromeVersion() {
		return chromeVersion;
	}

	public void setChromeVersion(String chromeVersion) {
		this.chromeVersion = chromeVersion;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public boolean hasEssentialInformation() {
		return StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)
				&& StringUtils.isNotBlank(activityName) && StringUtils.isNotBlank(applicationName);
	}

	public void set(InputParameterEnum inputParameter, String parameterValue) {

		switch (inputParameter) {
		case USERNAME:
			setUsername(parameterValue);
			break;
		case PASSWORD:
			setPassword(parameterValue);
			break;
		case APPLICATION:
			setApplicationName(parameterValue);
			break;
		case ACTIVITY:
			setActivityName(parameterValue);
			break;
		case SKIP_DAYS:
			setSkipDays(Arrays.asList(StringUtils.split(parameterValue, ",")));
			break;
		case CUSTOM_DAYS:
			setCustomDays(Arrays.asList(StringUtils.split(parameterValue, ",")));
			break;
		case MONTH:
			setMonth(parameterValue);
			break;
		case BROWSER_VERSION:
			setChromeVersion(parameterValue);
		}
	}

}
