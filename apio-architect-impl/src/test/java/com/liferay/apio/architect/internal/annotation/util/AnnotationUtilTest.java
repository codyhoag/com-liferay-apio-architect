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

package com.liferay.apio.architect.internal.annotation.util;

import static com.liferay.apio.architect.internal.annotation.util.AnnotationUtil.findAnnotationInAnyParameter;
import static com.liferay.apio.architect.internal.annotation.util.AnnotationUtil.findAnnotationInMethodOrInItsAnnotations;

import static java.util.Arrays.asList;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

import com.liferay.apio.architect.annotation.Actions.Action;
import com.liferay.apio.architect.annotation.Id;
import com.liferay.apio.architect.annotation.Vocabulary.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Alejandro Hernández
 * @author Javier Gamarra
 */
public class AnnotationUtilTest {

	@BeforeClass
	public static void setUpClass() throws NoSuchMethodException {
		_annotatedWithActionMethod = MyAnnotatedInterface.class.getMethod(
			"annotatedWithAction");
		_annotatedWithCreateMethod = MyAnnotatedInterface.class.getMethod(
			"annotatedWithCreate");
		_annotatedWithRemoveMethod = MyAnnotatedInterface.class.getMethod(
			"annotatedWithRemove");
		_notAnnotatedMethod = MyAnnotatedInterface.class.getMethod(
			"notAnnotated");
		_withParameterAnnotatedMethod = MyAnnotatedInterface.class.getMethod(
			"withParameterAnnotated", long.class);
		_withParameterNotAnnotatedMethod = MyAnnotatedInterface.class.getMethod(
			"withParametersNotAnnotated", long.class);
	}

	@Test
	public void test() {
		Action actionMethodAction = findAnnotationInMethodOrInItsAnnotations(
			_annotatedWithActionMethod, Action.class);
		Action createMethodAction = findAnnotationInMethodOrInItsAnnotations(
			_annotatedWithCreateMethod, Action.class);
		Action nullAction = findAnnotationInMethodOrInItsAnnotations(
			_notAnnotatedMethod, Action.class);
		Action removeMethodAction = findAnnotationInMethodOrInItsAnnotations(
			_annotatedWithRemoveMethod, Action.class);

		assertThat(actionMethodAction, is(notNullValue()));
		assertThat(actionMethodAction.name(), is("name"));
		assertThat(actionMethodAction.httpMethod(), is("GET"));

		assertThat(createMethodAction, is(notNullValue()));
		assertThat(createMethodAction.name(), is("create"));
		assertThat(createMethodAction.httpMethod(), is("POST"));

		assertThat(nullAction, is(nullValue()));

		assertThat(removeMethodAction, is(notNullValue()));
		assertThat(removeMethodAction.name(), is("remove"));
		assertThat(removeMethodAction.httpMethod(), is("DELETE"));
	}

	@Test
	public void testFindAnnotationInAnyParameter() {
		Id id = findAnnotationInAnyParameter(
			_withParameterAnnotatedMethod, Id.class);
		Id nullId1 = findAnnotationInAnyParameter(
			_withParameterNotAnnotatedMethod, Id.class);
		Id nullId2 = findAnnotationInAnyParameter(
			_notAnnotatedMethod, Id.class);

		assertThat(id, is(instanceOf(Id.class)));
		assertThat(nullId1, is(nullValue()));
		assertThat(nullId2, is(nullValue()));
	}

	@Test
	public void testMergeMissingAnnotationsOnlyAddsMissingAnnotations() {
		@Type("Good")
		@Produces(APPLICATION_JSON)
		class OldAnnotationsClass {
		}

		@Type("Bad")
		@Path("/apio")
		class NewAnnotationsClass {
		}

		List<Annotation> list = asList(
			OldAnnotationsClass.class.getAnnotations());

		Annotation[] array = NewAnnotationsClass.class.getAnnotations();

		List<Annotation> annotations = AnnotationUtil.mergeMissingAnnotations(
			list, array);

		assertThat(annotations, hasSize(3));

		Annotation typeAnnotation = annotations.get(0);

		assertThat(typeAnnotation, is(instanceOf(Type.class)));
		Type type = (Type)typeAnnotation;

		assertThat(type.value(), is("Good"));

		Annotation producesAnnotation = annotations.get(1);

		assertThat(producesAnnotation, is(instanceOf(Produces.class)));
		Produces produces = (Produces)producesAnnotation;

		assertThat(produces.value(), is(arrayContaining(APPLICATION_JSON)));

		Annotation pathAnnotation = annotations.get(2);

		assertThat(pathAnnotation, is(instanceOf(Path.class)));
		Path path = (Path)pathAnnotation;

		assertThat(path.value(), is("/apio"));
	}

	private static Method _annotatedWithActionMethod;
	private static Method _annotatedWithCreateMethod;
	private static Method _annotatedWithRemoveMethod;
	private static Method _notAnnotatedMethod;
	private static Method _withParameterAnnotatedMethod;
	private static Method _withParameterNotAnnotatedMethod;

}