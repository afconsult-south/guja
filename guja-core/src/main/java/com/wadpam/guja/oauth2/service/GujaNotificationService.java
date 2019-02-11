package com.wadpam.guja.oauth2.service;

import java.util.Locale;

public interface GujaNotificationService {

	boolean sendAccountVerificationEmail(String email, Locale locale, AccountVerificationNotificationRequest request);

	boolean sendEmailChangeEmail(String email, Locale locale, ChangeEmailNotificationRequest request);

	boolean sendPasswordRequestEmail(String email, Locale locale, PasswordResetNotificationRequest request);

}
