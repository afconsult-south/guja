package com.wadpam.guja.oauth2.service;

public final class PasswordResetNotificationRequest {

	private final String displayName;
	private final String confirmationUrl;

	public PasswordResetNotificationRequest(String displayName, String confirmationUrl) {
		this.displayName = displayName;
		this.confirmationUrl = confirmationUrl;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getConfirmationUrl() {
		return confirmationUrl;
	}

}
