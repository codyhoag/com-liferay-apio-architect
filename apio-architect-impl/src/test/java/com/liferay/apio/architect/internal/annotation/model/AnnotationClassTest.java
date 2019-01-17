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

package com.liferay.apio.architect.internal.annotation.model;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.liferay.apio.architect.annotation.Actions.Create;
import com.liferay.apio.architect.annotation.Actions.Remove;
import com.liferay.apio.architect.annotation.Actions.Update;
import com.liferay.apio.architect.annotation.Body;
import com.liferay.apio.architect.annotation.GenericParentId;
import com.liferay.apio.architect.annotation.Id;
import com.liferay.apio.architect.annotation.ParentId;
import com.liferay.apio.architect.annotation.Vocabulary.Type;
import com.liferay.apio.architect.language.AcceptLanguage;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import java.util.List;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Alejandro Hernández
 */
public class AnnotationClassTest {

	@BeforeClass
	public static void setUpClass() {
		_annotatedClass = AnnotatedClass.of(TestClass.class);
	}

	@Test
	public void testFirstAnnotatedMethodIsCorrect()
		throws NoSuchMethodException {

		Method createMethod = TestClass.class.getMethod(
			"create", String.class, AcceptLanguage.class);

		List<AnnotatedClass.AnnotatedMethod> annotatedMethods =
			_annotatedClass.getAnnotatedMethods();

		AnnotatedClass.AnnotatedMethod annotatedMethod = annotatedMethods.get(
			0);

		assertThat(annotatedMethod.name, is("create"));
		assertThat(annotatedMethod.returnType, is(equalTo(void.class)));
		assertThat(annotatedMethod.method, is(createMethod));
		assertThat(
			annotatedMethod.getAnnotations(),
			contains(instanceOf(Create.class)));

		assertTrue(annotatedMethod.hasAnnotation(Create.class));
		assertFalse(annotatedMethod.hasAnnotation(Remove.class));

		assertNotNull(annotatedMethod.getAnnotation(Create.class));
		assertNull(annotatedMethod.getAnnotation(Remove.class));

		List<AnnotatedClass.AnnotatedMethod.AnnotatedParam> annotatedParams =
			annotatedMethod.getAnnotatedParams();

		assertThat(annotatedParams, hasSize(2));

		AnnotatedClass.AnnotatedMethod.AnnotatedParam annotatedParam0 =
			annotatedParams.get(0);

		assertThat(annotatedParam0.parameterClass, is(equalTo(String.class)));
		assertThat(annotatedParam0.genericType, is(equalTo(String.class)));

		assertThat(
			annotatedParam0.getAnnotations(),
			contains(instanceOf(GenericParentId.class)));

		assertTrue(annotatedParam0.hasAnnotation(GenericParentId.class));
		assertFalse(annotatedParam0.hasAnnotation(ParentId.class));

		assertNotNull(annotatedParam0.getAnnotation(GenericParentId.class));
		assertNull(annotatedParam0.getAnnotation(ParentId.class));

		AnnotatedClass.AnnotatedMethod.AnnotatedParam annotatedParam1 =
			annotatedParams.get(1);

		assertThat(
			annotatedParam1.parameterClass, is(equalTo(AcceptLanguage.class)));
		assertThat(
			annotatedParam1.genericType, is(equalTo(AcceptLanguage.class)));

		assertThat(annotatedParam1.getAnnotations(), is(empty()));
	}

	@Test
	public void testGetAnnotatedMethodsReturnsFlatMethodList()
		throws NoSuchMethodException {

		List<AnnotatedClass.AnnotatedMethod> annotatedMethods =
			_annotatedClass.getAnnotatedMethods();

		assertThat(annotatedMethods, hasSize(3));

		AnnotatedClass.AnnotatedMethod annotatedMethod2 = annotatedMethods.get(
			1);

		assertThat(annotatedMethod2.name, is("remove"));
		assertThat(annotatedMethod2.returnType, is(equalTo(void.class)));

		Method removeMethod = TestClass.class.getMethod("remove", long.class);

		assertThat(annotatedMethod2.method, is(removeMethod));

		assertThat(
			annotatedMethod2.getAnnotations(),
			contains(instanceOf(Remove.class)));

		AnnotatedClass.AnnotatedMethod annotatedMethod3 = annotatedMethods.get(
			2);

		assertThat(annotatedMethod3.name, is("update"));
		assertThat(annotatedMethod3.returnType, is(equalTo(String.class)));

		Method updateMethod = TestClass.class.getMethod(
			"update", String.class, long.class);

		assertThat(annotatedMethod3.method, is(updateMethod));

		assertThat(
			annotatedMethod3.getAnnotations(),
			contains(instanceOf(Update.class)));
	}

	@Test
	public void testGetAnnotationReturnsAnnotationIfPresent() {
		Path path = _annotatedClass.getAnnotation(Path.class);

		assertThat(path, is(notNullValue()));
		assertThat(path.value(), is("/blog-posting"));

		Produces produces = _annotatedClass.getAnnotation(Produces.class);

		assertThat(produces, is(notNullValue()));
		assertThat(produces.value(), is(arrayContaining(APPLICATION_XML)));

		Type type = _annotatedClass.getAnnotation(Type.class);

		assertThat(type, is(notNullValue()));
		assertThat(type.value(), is("BlogPosting"));
	}

	@Test
	public void testGetAnnotationReturnsNullIfAnnotationNotPresent() {
		Consumes consumes = _annotatedClass.getAnnotation(Consumes.class);

		assertThat(consumes, is(nullValue()));

		ApplicationPath applicationPath = _annotatedClass.getAnnotation(
			ApplicationPath.class);

		assertThat(applicationPath, is(nullValue()));
	}

	@Test
	public void testGetAnnotationsReturnsFlatAnnotations() {
		List<Annotation> annotations = _annotatedClass.getAnnotations();

		assertThat(annotations, hasSize(3));

		Annotation producesAnnotation = annotations.get(0);

		assertThat(producesAnnotation, is(instanceOf(Produces.class)));
		Produces produces = (Produces)producesAnnotation;

		assertThat(produces.value(), is(arrayContaining(APPLICATION_XML)));

		Annotation pathAnnotation = annotations.get(1);

		assertThat(pathAnnotation, is(instanceOf(Path.class)));
		Path path = (Path)pathAnnotation;

		assertThat(path.value(), is("/blog-posting"));

		Annotation typeAnnotation = annotations.get(2);

		assertThat(typeAnnotation, is(instanceOf(Type.class)));
		Type type = (Type)typeAnnotation;

		assertThat(type.value(), is("BlogPosting"));
	}

	@Test
	public void testHasAnnotationReturnsFalseIfAnnotationIsNotPresent() {
		assertFalse(_annotatedClass.hasAnnotation(ApplicationPath.class));
		assertFalse(_annotatedClass.hasAnnotation(Consumes.class));
	}

	@Test
	public void testHasAnnotationReturnsTrueIfAnnotationIsPresent() {
		assertTrue(_annotatedClass.hasAnnotation(Path.class));
		assertTrue(_annotatedClass.hasAnnotation(Produces.class));
		assertTrue(_annotatedClass.hasAnnotation(Type.class));
	}

	@Test
	public void testSecondAnnotatedMethodIsCorrect()
		throws NoSuchMethodException {

		Method removeMethod = TestClass.class.getMethod("remove", long.class);

		List<AnnotatedClass.AnnotatedMethod> annotatedMethods =
			_annotatedClass.getAnnotatedMethods();

		AnnotatedClass.AnnotatedMethod annotatedMethod = annotatedMethods.get(
			1);

		assertThat(annotatedMethod.name, is("remove"));
		assertThat(annotatedMethod.returnType, is(equalTo(void.class)));
		assertThat(annotatedMethod.method, is(removeMethod));
		assertThat(
			annotatedMethod.getAnnotations(),
			contains(instanceOf(Remove.class)));

		assertTrue(annotatedMethod.hasAnnotation(Remove.class));
		assertFalse(annotatedMethod.hasAnnotation(Create.class));

		assertNotNull(annotatedMethod.getAnnotation(Remove.class));
		assertNull(annotatedMethod.getAnnotation(Create.class));

		List<AnnotatedClass.AnnotatedMethod.AnnotatedParam> annotatedParams =
			annotatedMethod.getAnnotatedParams();

		assertThat(annotatedParams, hasSize(1));

		AnnotatedClass.AnnotatedMethod.AnnotatedParam annotatedParam =
			annotatedParams.get(0);

		assertThat(annotatedParam.parameterClass, is(equalTo(long.class)));
		assertThat(annotatedParam.genericType, is(equalTo(long.class)));

		assertThat(
			annotatedParam.getAnnotations(), contains(instanceOf(Id.class)));

		assertTrue(annotatedParam.hasAnnotation(Id.class));
		assertFalse(annotatedParam.hasAnnotation(ParentId.class));

		assertNotNull(annotatedParam.getAnnotation(Id.class));
		assertNull(annotatedParam.getAnnotation(ParentId.class));
	}

	@Test
	public void testThirdAnnotatedMethodIsCorrect()
		throws NoSuchMethodException {

		Method updateMethod = TestClass.class.getMethod(
			"update", String.class, long.class);

		List<AnnotatedClass.AnnotatedMethod> annotatedMethods =
			_annotatedClass.getAnnotatedMethods();

		AnnotatedClass.AnnotatedMethod annotatedMethod = annotatedMethods.get(
			2);

		assertThat(annotatedMethod.name, is("update"));
		assertThat(annotatedMethod.returnType, is(equalTo(String.class)));
		assertThat(annotatedMethod.method, is(updateMethod));
		assertThat(
			annotatedMethod.getAnnotations(),
			contains(instanceOf(Update.class)));

		assertTrue(annotatedMethod.hasAnnotation(Update.class));
		assertFalse(annotatedMethod.hasAnnotation(Remove.class));

		assertNotNull(annotatedMethod.getAnnotation(Update.class));
		assertNull(annotatedMethod.getAnnotation(Remove.class));

		List<AnnotatedClass.AnnotatedMethod.AnnotatedParam> annotatedParams =
			annotatedMethod.getAnnotatedParams();

		assertThat(annotatedParams, hasSize(2));

		AnnotatedClass.AnnotatedMethod.AnnotatedParam annotatedParam0 =
			annotatedParams.get(0);

		assertThat(annotatedParam0.parameterClass, is(equalTo(String.class)));
		assertThat(annotatedParam0.genericType, is(equalTo(String.class)));

		assertThat(
			annotatedParam0.getAnnotations(), contains(instanceOf(Body.class)));

		assertTrue(annotatedParam0.hasAnnotation(Body.class));
		assertFalse(annotatedParam0.hasAnnotation(ParentId.class));

		assertNotNull(annotatedParam0.getAnnotation(Body.class));
		assertNull(annotatedParam0.getAnnotation(ParentId.class));

		AnnotatedClass.AnnotatedMethod.AnnotatedParam annotatedParam1 =
			annotatedParams.get(1);

		assertThat(annotatedParam1.parameterClass, is(equalTo(long.class)));
		assertThat(annotatedParam1.genericType, is(equalTo(long.class)));

		assertThat(
			annotatedParam1.getAnnotations(), contains(instanceOf(Id.class)));

		assertTrue(annotatedParam1.hasAnnotation(Id.class));
		assertFalse(annotatedParam1.hasAnnotation(ParentId.class));

		assertNotNull(annotatedParam1.getAnnotation(Id.class));
		assertNull(annotatedParam1.getAnnotation(ParentId.class));
	}

	public static class SuperClass implements FirstInterface {

		@Override
		public void remove(long id) {
		}

		@Update
		public String update(@Body String body, @Id long id) {
			return "Updated!";
		}

	}

	@Produces(APPLICATION_XML)
	public static class TestClass
		extends SuperClass implements SecondInterface {

		@Override
		public void create(String id, AcceptLanguage acceptLanguage) {
		}

	}

	@Path("/blog-posting")
	@Type("BlogPosting")
	public interface FirstInterface {

		@Remove
		public void remove(@Id long id);

	}

	@Produces(APPLICATION_JSON)
	public interface SecondInterface {

		@Create
		public void create(
			@GenericParentId String id, AcceptLanguage acceptLanguage);

	}

	private static AnnotatedClass _annotatedClass;

}