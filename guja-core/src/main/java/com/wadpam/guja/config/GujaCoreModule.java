package com.wadpam.guja.config;

/*
 * #%L
 * guja-core
 * %%
 * Copyright (C) 2014 Wadpam
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.persist.Transactional;
import com.wadpam.guja.admintask.AdminTask;
import com.wadpam.guja.admintask.AdminTaskResource;
import com.wadpam.guja.exceptions.RestExceptionMapper;
import com.wadpam.guja.i18n.DaoLocalizationBuilder;
import com.wadpam.guja.i18n.PropertyFileLocalizationBuilder;
import com.wadpam.guja.oauth2.api.FactoryResource;
import com.wadpam.guja.oauth2.api.OAuth2AuthorizationResource;
import com.wadpam.guja.oauth2.api.OAuth2FederatedResource;
import com.wadpam.guja.oauth2.api.OAuth2UserResource;
import com.wadpam.guja.oauth2.api.UserResource;
import com.wadpam.guja.oauth2.dao.DConnectionDaoBean;
import com.wadpam.guja.oauth2.dao.DFactoryDaoBean;
import com.wadpam.guja.oauth2.dao.DOAuth2UserDaoBean;
import com.wadpam.guja.oauth2.dao.DUserDaoBean;
import com.wadpam.guja.oauth2.provider.DefaultPasswordEncoder;
import com.wadpam.guja.oauth2.provider.DefaultTokenGenerator;
import com.wadpam.guja.oauth2.provider.Oauth2UserProvider;
import com.wadpam.guja.oauth2.provider.PasswordEncoder;
import com.wadpam.guja.oauth2.provider.RequestScopedLocale;
import com.wadpam.guja.oauth2.provider.TemporaryTokenCache;
import com.wadpam.guja.oauth2.provider.TokenGenerator;
import com.wadpam.guja.oauth2.provider.UserAuthenticationProvider;
import com.wadpam.guja.oauth2.service.UserAdminTask;
import com.wadpam.guja.oauth2.service.UserService;
import com.wadpam.guja.oauth2.service.UserServiceImpl;
import com.wadpam.guja.oauth2.web.Oauth2ClientAuthenticationFilter;
import com.wadpam.guja.template.RequestScopedVelocityTemplateStringWriterBuilder;
import com.wadpam.guja.template.VelocityTemplateStringWriterBuilder;

import net.sf.mardao.dao.Supplier;

/**
 * Binds {@link com.google.inject.persist.UnitOfWork}, {@link com.google.inject.persist.PersistService} and {@link MardaoTransactionManager}.
 *
 * @author osandstrom
 *         Date: 1/19/14 Time: 8:59 PM
 */
public class GujaCoreModule extends AbstractModule {

  private final boolean bindAuthorization;
  private final boolean bindFederated;

  public GujaCoreModule(boolean bindAuthorization, boolean bindFederated) {
    this.bindAuthorization = bindAuthorization;
    this.bindFederated = bindFederated;
  }

  @Override
  protected void configure() {

    bind(RestExceptionMapper.class);

    bind(RequestScopedLocale.class);

    bind(PropertyFileLocalizationBuilder.class);
    bind(DaoLocalizationBuilder.class);

    // Binding Jackson mapper has been moved to the backend to allow each project setting their own implementation

    bind(PasswordEncoder.class).to(DefaultPasswordEncoder.class);
    bind(TokenGenerator.class).to(DefaultTokenGenerator.class);
    bind(TemporaryTokenCache.class);

    if (bindAuthorization) {
      bind(OAuth2AuthorizationResource.class);
    }
    if (bindFederated) {
      bind(OAuth2FederatedResource.class);
    }

    bind(UserAuthenticationProvider.class).to(UserServiceImpl.class);
    bind(Oauth2UserProvider.class).to(UserServiceImpl.class);

    bind(DConnectionDaoBean.class);

    bind(FactoryResource.class);
    bind(DFactoryDaoBean.class);

    bind(RequestScopedVelocityTemplateStringWriterBuilder.class);
    bind(VelocityTemplateStringWriterBuilder.class);

    // Binding email service has been moved to the backend to allow each project setting their own implementation
    // bind(EmailService.class).to(JavaMailService.class);

    bind(UserResource.class);
    bind(UserService.class).to(UserServiceImpl.class);
    bind(DUserDaoBean.class);

    bind(OAuth2UserResource.class);
    bind(DOAuth2UserDaoBean.class);

    bind(AdminTaskResource.class);

    Multibinder<AdminTask> adminTaskBinder = Multibinder.newSetBinder(binder(), AdminTask.class);
    adminTaskBinder.addBinding().to(UserAdminTask.class);
    adminTaskBinder.addBinding().to(Oauth2ClientAuthenticationFilter.class);

    MardaoTransactionManager transactionManager = new MardaoTransactionManager(getProvider(Supplier.class));
    requestInjection(transactionManager);
    bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), transactionManager);

  }
}
