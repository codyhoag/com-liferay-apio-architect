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

package com.liferay.apio.architect.internal.action;

import static com.liferay.apio.architect.operation.HTTPMethod.GET;

import static java.lang.String.join;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;

import com.liferay.apio.architect.internal.action.resource.Resource;
import com.liferay.apio.architect.internal.action.resource.Resource.Item;
import com.liferay.apio.architect.internal.action.resource.Resource.Paged;
import com.liferay.apio.architect.internal.annotation.Action;
import com.liferay.apio.architect.pagination.Page;

import io.vavr.CheckedFunction1;
import io.vavr.control.Try;

import java.util.Arrays;
import java.util.List;

import org.immutables.value.Value.Immutable;

import org.junit.Test;

/**
 * @author Alejandro Hernández
 */
public class ActionSemanticsTest {

	@Test
	public void testBuilderCreatesActionSemantics() throws Throwable {
		ActionSemantics actionSemantics = ActionSemantics.ofResource(
			Paged.of("name")
		).name(
			"action"
		).method(
			GET
		).receivesParams(
			String.class, Long.class
		).returns(
			Long.class
		).annotatedWith(
			_myAnnotation
		).executeFunction(
			_join
		).build();

		assertThat(actionSemantics.annotations(), contains(_myAnnotation));
		assertThat(actionSemantics.name(), is("action"));
		assertThat(actionSemantics.method(), is("GET"));

		List<Class<?>> paramClasses = actionSemantics.paramClasses();

		assertThat(paramClasses, contains(String.class, Long.class));

		assertThat(actionSemantics.resource(), is(Paged.of("name")));
		assertThat(actionSemantics.returnClass(), is(equalTo(Long.class)));

		CheckedFunction1<List<?>, ?> executeFunction =
			actionSemantics.executeFunction();

		String result = (String)executeFunction.apply(Arrays.asList("1", "2"));

		assertThat(result, is("1-2"));
	}

	@Test
	public void testBuilderWithoutAnnotationsCreatesActionSemantics()
		throws Throwable {

		ActionSemantics actionSemantics = ActionSemantics.ofResource(
			Paged.of("name")
		).name(
			"action"
		).method(
			GET
		).receivesNoParams(
		).returnsNothing(
		).notAnnotated(
		).executeFunction(
			_join
		).build();

		assertThat(actionSemantics.resource(), is(Paged.of("name")));
		assertThat(actionSemantics.name(), is("action"));
		assertThat(actionSemantics.method(), is("GET"));
		assertThat(actionSemantics.paramClasses(), is(empty()));
		assertThat(actionSemantics.returnClass(), is(equalTo(Void.class)));
		assertThat(actionSemantics.annotations(), is(empty()));

		CheckedFunction1<List<?>, ?> executeFunction =
			actionSemantics.executeFunction();

		String result = (String)executeFunction.apply(Arrays.asList("1", "2"));

		assertThat(result, is("1-2"));
	}

	@Test
	public void testBuilderWithoutParamsCreatesActionSemantics()
		throws Throwable {

		ActionSemantics actionSemantics = ActionSemantics.ofResource(
			Paged.of("name")
		).name(
			"action"
		).method(
			GET
		).receivesNoParams(
		).returns(
			Long.class
		).annotatedWith(
			_myAnnotation
		).executeFunction(
			_join
		).build();

		assertThat(actionSemantics.resource(), is(Paged.of("name")));
		assertThat(actionSemantics.name(), is("action"));
		assertThat(actionSemantics.method(), is("GET"));
		assertThat(actionSemantics.paramClasses(), is(empty()));
		assertThat(actionSemantics.returnClass(), is(equalTo(Long.class)));
		assertThat(actionSemantics.annotations(), contains(_myAnnotation));

		CheckedFunction1<List<?>, ?> executeFunction =
			actionSemantics.executeFunction();

		String result = (String)executeFunction.apply(Arrays.asList("1", "2"));

		assertThat(result, is("1-2"));
	}

	@Test
	public void testBuilderWithoutReturnCreatesActionSemantics()
		throws Throwable {

		ActionSemantics actionSemantics = ActionSemantics.ofResource(
			Paged.of("name")
		).name(
			"action"
		).method(
			GET
		).receivesNoParams(
		).returnsNothing(
		).annotatedWith(
			_myAnnotation
		).executeFunction(
			_join
		).build();

		assertThat(actionSemantics.resource(), is(Paged.of("name")));
		assertThat(actionSemantics.name(), is("action"));
		assertThat(actionSemantics.method(), is("GET"));
		assertThat(actionSemantics.paramClasses(), is(empty()));
		assertThat(actionSemantics.returnClass(), is(equalTo(Void.class)));
		assertThat(actionSemantics.annotations(), contains(_myAnnotation));

		CheckedFunction1<List<?>, ?> executeFunction =
			actionSemantics.executeFunction();

		String result = (String)executeFunction.apply(Arrays.asList("1", "2"));

		assertThat(result, is("1-2"));
	}

	@Test
	public void testBuilderWithStringMethodCreatesActionSemantics()
		throws Throwable {

		ActionSemantics actionSemantics = ActionSemantics.ofResource(
			Paged.of("name")
		).name(
			"action"
		).method(
			"POST"
		).receivesNoParams(
		).returnsNothing(
		).annotatedWith(
			_myAnnotation
		).executeFunction(
			_join
		).build();

		assertThat(actionSemantics.resource(), is(Paged.of("name")));
		assertThat(actionSemantics.name(), is("action"));
		assertThat(actionSemantics.method(), is("POST"));
		assertThat(actionSemantics.paramClasses(), is(empty()));
		assertThat(actionSemantics.returnClass(), is(equalTo(Void.class)));
		assertThat(actionSemantics.annotations(), contains(_myAnnotation));

		CheckedFunction1<List<?>, ?> executeFunction =
			actionSemantics.executeFunction();

		String result = (String)executeFunction.apply(Arrays.asList("1", "2"));

		assertThat(result, is("1-2"));
	}

	@Test
	public void testToActionTransformsAnActionSemanticsIntoAnAction() {
		ActionSemantics actionSemantics = ActionSemantics.ofResource(
			Resource.Paged.of("name")
		).name(
			"action"
		).method(
			GET
		).receivesParams(
			String.class, Long.class
		).returns(
			String.class
		).annotatedWith(
			_myAnnotation
		).executeFunction(
			_join
		).build();

		Action action = actionSemantics.toAction(
			(semantics, request, clazz) -> clazz.getSimpleName());

		Object object = action.apply(null);

		assertThat(object, is(instanceOf(Try.class)));

		@SuppressWarnings("unchecked")
		Try<String> stringTry = (Try<String>)object;

		assertThat(stringTry.get(), is("String-Long"));
	}

	@Test
	public void testWithResourceReturnsActionSemanticsWithDifferentResource() {
		ActionSemantics actionSemantics = ActionSemantics.ofResource(
			Paged.of("name")
		).name(
			"retrieve"
		).method(
			"GET"
		).receivesNoParams(
		).returns(
			Page.class
		).notAnnotated(
		).executeFunction(
			__ -> null
		).build();

		ActionSemantics newActionSemantics = actionSemantics.withResource(
			Item.of("name"));

		assertThat(actionSemantics.resource(), is(Paged.of("name")));

		assertThat(newActionSemantics.resource(), is(Item.of("name")));
		assertThat(
			newActionSemantics.resource(),
			is(not(equalTo(actionSemantics.resource()))));
	}

	@Immutable(singleton = true)
	public static @interface MyAnnotation {
	}

	@SuppressWarnings("unchecked")
	private static final CheckedFunction1<List<?>, Object> _join = list -> join(
		"-", (List<String>)list);

	private static final MyAnnotation _myAnnotation =
		ImmutableMyAnnotation.of();

}