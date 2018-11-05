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

package com.liferay.apio.architect.internal.message.json.ld;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.liferay.apio.architect.internal.operation.BatchCreateOperation;
import com.liferay.apio.architect.internal.operation.CreateOperation;
import com.liferay.apio.architect.internal.operation.DeleteOperation;
import com.liferay.apio.architect.internal.operation.UpdateOperation;
import com.liferay.apio.architect.operation.Operation;

import java.util.List;

/**
 * Provides utility functions for JSON-LD message mappers.
 *
 * <p>
 * This class shouldn't be instantiated.
 * </p>
 *
 * @author Alejandro Hernández
 */
public class JSONLDMessageMapperUtil {

	/**
	 * Return the list of {@link Operation} types.
	 *
	 * @param  operation the operation
	 * @return the list
	 */
	public static List<String> getOperationTypes(Operation operation) {
		if (operation instanceof BatchCreateOperation) {
			return asList("BatchCreateAction", "Operation");
		}

		if (operation instanceof CreateOperation) {
			return asList("CreateAction", "Operation");
		}

		if (operation instanceof DeleteOperation) {
			return asList("DeleteAction", "Operation");
		}

		if (operation instanceof UpdateOperation) {
			return asList("ReplaceAction", "Operation");
		}

		return singletonList("Operation");
	}

	public static List<String> getOperationTypes(String actionName) {
		return Match(
			actionName
		).of(
			Case($("create"), asList("CreateAction", "Operation")),
			Case($("batch-create"), asList("CreateAction", "Operation")),
			Case($("remove"), asList("DeleteAction", "Operation")),
			Case($("replace"), asList("ReplaceAction", "Operation")),
			Case($(), singletonList("Operation"))
		);
	}

	private JSONLDMessageMapperUtil() {
		throw new UnsupportedOperationException();
	}

}