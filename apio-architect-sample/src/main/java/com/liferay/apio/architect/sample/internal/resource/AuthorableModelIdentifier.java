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

package com.liferay.apio.architect.sample.internal.resource;

import aQute.bnd.annotation.ProviderType;

/**
 * @author Javier Gamarra
 */
@ProviderType
public interface AuthorableModelIdentifier {

	public static AuthorableModelIdentifier create(
		String modelName, long modelId) {

		return new AuthorableModelIdentifier() {

			@Override
			public long getModelId() {
				return modelId;
			}

			@Override
			public String getModelName() {
				return modelName;
			}

		};
	}

	public long getModelId();

	public String getModelName();

}