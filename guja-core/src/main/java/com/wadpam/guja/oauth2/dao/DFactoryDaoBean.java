package com.wadpam.guja.oauth2.dao;

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


import com.google.inject.Inject;
import net.sf.mardao.dao.Supplier;

import javax.cache.annotation.CacheDefaults;

/**
 * Implementation of Business Methods related to entity DFactory.
 * This (empty) class is generated by mardao, but edited by developers.
 * It is not overwritten by the generator once it exists.
 * <p/>
 * Generated on 2014-12-02T22:54:58.210+0100.
 *
 * @author mardao DAO generator (net.sf.mardao.plugin.ProcessDomainMojo)
 */
@CacheDefaults(cacheName = "DFactory")
public class DFactoryDaoBean
    extends GeneratedDFactoryDaoImpl{

  @Inject
  public DFactoryDaoBean(Supplier supplier) {
    super(supplier);
  }

}
