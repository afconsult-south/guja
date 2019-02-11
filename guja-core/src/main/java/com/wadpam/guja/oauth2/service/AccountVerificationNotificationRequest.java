package com.wadpam.guja.oauth2.service;

public final class AccountVerificationNotificationRequest {

	private final String displayName;
	private final String verificationUrl;

	public AccountVerificationNotificationRequest(String displayName, String verificationUrl) {
		this.displayName = displayName;
		this.verificationUrl = verificationUrl;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getVerificationUrl() {
		return verificationUrl;
	}

}
