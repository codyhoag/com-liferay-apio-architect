/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.apio.architect.internal.test.provider;

import static co.unruly.matchers.StreamMatchers.contains;

import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.liferay.apio.architect.language.AcceptLanguage;
import com.liferay.apio.architect.provider.Provider;
import com.liferay.apio.architect.test.base.internal.BaseTest;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite for {@link
 * com.liferay.apio.architect.internal.jaxrs.filter.AcceptLanguageEmptyFilter}.
 *
 * @author Alejandro Hernández
 * @review
 */
public class AcceptLanguageProviderIntegrationTest extends BaseTest {

	@BeforeClass
	public static void setUpClass() {
		BaseTest.setUpClass();

		_provider = getService(
			Provider.class, "provided.class", AcceptLanguage.class.getName());
	}

	@Test
	public void testCreateContextReturnsAcceptLanguage() {
		HttpServletRequest request = mock(HttpServletRequest.class);

		when(
			request.getLocale()
		).thenReturn(
			Locale.US
		);

		when(
			request.getLocales()
		).thenReturn(
			enumeration(asList(Locale.US, Locale.UK))
		);

		AcceptLanguage acceptLanguage = _provider.createContext(request);

		assertThat(acceptLanguage.getPreferredLocale(), is(Locale.US));
		assertThat(acceptLanguage.getLocales(), contains(Locale.US, Locale.UK));
	}

	private static Provider<AcceptLanguage> _provider;

}