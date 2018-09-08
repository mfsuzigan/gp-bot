package org.mfs.gpbot;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.mfs.gpbot.enumeration.InputParameterEnum;

/**
 * Modela os dados necessarios para o lancamento do GP e configuracoes possiveis
 * do bot
 *
 * @author Michel Suzigan
 *
 */
public class Data {

	private String username;
	private String password;
	private String applicationName;
	private String activityName;
	private String month;
	private Boolean todayOnly;
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

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public Boolean isTodayOnly() {
		return todayOnly;
	}

	public void setTodayOnly(String onlyToday) {
		todayOnly = !(StringUtils.isBlank(onlyToday) || "N".equals(onlyToday));
	}

	public boolean hasEssentialInformation() {
		return StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)
				&& StringUtils.isNotBlank(activityName) && StringUtils.isNotBlank(applicationName);
	}

	@SuppressWarnings("rawtypes")
	public boolean isSet(InputParameterEnum inputParameter) {

		Object parameterSetValue = null;

		switch (inputParameter) {
		case USERNAME:
			parameterSetValue = getUsername();
			break;
		case PASSWORD:
			parameterSetValue = getPassword();
			break;
		case APPLICATION:
			parameterSetValue = getApplicationName();
			break;
		case ACTIVITY:
			parameterSetValue = getActivityName();
			break;
		case SKIP_DAYS:
			parameterSetValue = getSkipDays();
			break;
		case CUSTOM_DAYS:
			parameterSetValue = getCustomDays();
			break;
		case MONTH:
			parameterSetValue = getMonth();
			break;
		case ONLY_TODAY:
			parameterSetValue = isTodayOnly();
			break;
		default:
			break;
		}

		boolean stringIsSet = parameterSetValue instanceof String && StringUtils.isNotBlank((String) parameterSetValue);
		boolean collectionIsSet = parameterSetValue instanceof Collection && ((Collection) parameterSetValue) != null
				&& !((Collection) parameterSetValue).isEmpty();
		boolean booleanIsSet = parameterSetValue instanceof Boolean && parameterSetValue != null;

		return stringIsSet || collectionIsSet || booleanIsSet;
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
		case ONLY_TODAY:
			setTodayOnly(parameterValue);
			break;
		default:
			break;
		}
	}

}
