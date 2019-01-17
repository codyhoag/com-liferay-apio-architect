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

import io.vavr.Predicates;
import io.vavr.collection.Stream;

import static com.liferay.apio.architect.internal.annotation.util.AnnotationUtil.mergeMissingAnnotations;

import static java.util.Objects.isNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Instances of this class contains the accumulated annotation information of a
 * class, its superclass, interfaces and superinterfaces.
 *
 * @author Alejandro Hernández
 * @author Víctor Galán
 * @review
 */
public class AnnotatedClass {

	/**
	 * Creates a new {@link AnnotatedClass} version of the provided {@link
	 * Class}.
	 *
	 * @review
	 */
	public static AnnotatedClass of(Class<?> clazz) {
		return new AnnotatedClass(clazz);
	}

	/**
	 * Returns the list of methods available in the class.
	 *
	 * <p>This list will be flat, meaning that the information of a method will
	 * be provided by aggregating all the method information, regarding of where
	 * it is present.
	 *
	 * <p>Synthesize and {@link Object} methods will not be added to this list.
	 *
	 * @review
	 */
	public List<AnnotatedMethod> getAnnotatedMethods() {
		return new ArrayList<>(_annotatedMethods);
	}

	/**
	 * Returns an instance of the annotation class provided, if it is present in
	 * the class, its superclass, interfaces or superinterfaces. Returns {@code
	 * null} otherwise.
	 *
	 * @review
	 */
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		for (Annotation annotation : _annotations) {
			if (annotationClass.equals(annotation.annotationType())) {
				return (T)annotation;
			}
		}

		return null;
	}

	/**
	 * Returns the list of annotations present in the class, superclass,
	 * interfaces and superinterfaces.
	 *
	 * @review
	 */
	public List<Annotation> getAnnotations() {
		return new ArrayList<>(_annotations);
	}

	/**
	 * Returns {@code true} if the class is annotated with an annotation of the
	 * provided class. Returns {@code false} otherwise.
	 *
	 * @review
	 */
	@SuppressWarnings("unchecked")
	public <T extends Annotation> boolean hasAnnotation(
		Class<T> annotationClass) {

		for (Annotation annotation : _annotations) {
			if (annotationClass.equals(annotation.annotationType())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Instances of this class contains the annotation information of a method
	 * and its parameters. Regardless of whether those annotations are present
	 * on the method original declaration or one of its overridden versions.
	 *
	 * @review
	 */
	public static class AnnotatedMethod {

		/**
		 * Returns the list of params available in the method.
		 *
		 * <p>This list will be flat, meaning that the information of a param
		 * will be provided by aggregating all the param information, regarding
		 * of where it is present.
		 *
		 * @review
		 */
		public List<AnnotatedParam> getAnnotatedParams() {
			return new ArrayList<>(_annotatedParams);
		}

		/**
		 * Returns an instance of the annotation class provided, if it is
		 * present in the method original declaration, or any of its overridden
		 * versions. Returns {@code null} otherwise.
		 *
		 * @review
		 */
		@SuppressWarnings("unchecked")
		public <T extends Annotation> T getAnnotation(
			Class<T> annotationClass) {

			for (Annotation annotation : _annotations) {
				if (annotationClass.equals(annotation.annotationType())) {
					return (T)annotation;
				}
			}

			return null;
		}

		/**
		 * Returns the list of annotations present in the method, regardless of
		 * whether those annotations are present on the method original
		 * declaration or one of its overridden versions.
		 *
		 * @review
		 */
		public List<Annotation> getAnnotations() {
			return new ArrayList<>(_annotations);
		}

		/**
		 * Returns {@code true} if the method is annotated with an annotation of
		 * the provided class. Returns {@code false} otherwise.
		 *
		 * @review
		 */
		@SuppressWarnings("unchecked")
		public <T extends Annotation> boolean hasAnnotation(
			Class<T> annotationClass) {

			for (Annotation annotation : _annotations) {
				if (annotationClass.equals(annotation.annotationType())) {
					return true;
				}
			}

			return false;
		}

		/**
		 * The original method information.
		 *
		 * @review
		 */
		public final Method method;

		/**
		 * The method's name, extracted from {@link Method#getName()}.
		 *
		 * @review
		 */
		public final String name;

		/**
		 * The method's return type, extracted from {@link Method#getReturnType()}.
		 *
		 * @review
		 */
		public final Class<?> returnType;

		/**
		 * Instances of this class contains the annotation information of a
		 * parameter. Regardless of whether those annotations are present on the
		 * parameter original declaration or one of its overridden versions.
		 *
		 * @review
		 */
		public static class AnnotatedParam {

			/**
			 * Returns an instance of the annotation class provided, if it is
			 * present in the parameter original declaration, or any of its
			 * overridden versions. Returns {@code null} otherwise.
			 *
			 * @review
			 */
			@SuppressWarnings("unchecked")
			public <T extends Annotation> T getAnnotation(
				Class<T> annotationClass) {

				for (Annotation annotation : _annotations) {
					if (annotationClass.equals(annotation.annotationType())) {
						return (T)annotation;
					}
				}

				return null;
			}

			/**
			 * Returns the list of annotations present in the parameter,
			 * regardless of whether those annotations are present on the
			 * parameter original declaration or one of its overridden versions.
			 *
			 * @review
			 */
			public List<Annotation> getAnnotations() {
				return new ArrayList<>(_annotations);
			}

			/**
			 * Returns {@code true} if the parameter is annotated with an
			 * annotation of the provided class. Returns {@code false}
			 * otherwise.
			 *
			 * @review
			 */
			@SuppressWarnings("unchecked")
			public <T extends Annotation> boolean hasAnnotation(
				Class<T> annotationClass) {

				for (Annotation annotation : _annotations) {
					if (annotationClass.equals(annotation.annotationType())) {
						return true;
					}
				}

				return false;
			}

			/**
			 * The parameter's generic information, extracted from {@link Method#getGenericParameterTypes()}.
			 *
			 * @review
			 */
			public final Type genericType;

			/**
			 * The parameter's class, extracted from {@link Method#getParameterTypes()}.
			 *
			 * @review
			 */
			public final Class<?> parameterClass;

			private AnnotatedParam(
				Annotation[] annotations, Class<?> parameterClass,
				Type genericType) {

				this.parameterClass = parameterClass;
				this.genericType = genericType;

				_merge(annotations);
			}

			private void _merge(Annotation[] annotations) {
				_annotations = mergeMissingAnnotations(
					_annotations, annotations);
			}

			private List<Annotation> _annotations = new ArrayList<>();

		}

		private AnnotatedMethod(Method method) {
			this.method = method;
			name = method.getName();
			returnType = method.getReturnType();

			_includeInformationFrom(method);
		}

		private void _includeInformationFrom(Method method) {
			_annotations = mergeMissingAnnotations(
				_annotations, method.getDeclaredAnnotations());

			Annotation[][] parameterAnnotations =
				method.getParameterAnnotations();

			Class<?>[] parameterTypeClasses = method.getParameterTypes();
			Type[] genericParameterTypes = method.getGenericParameterTypes();

			for (int i = 0; i < parameterAnnotations.length; i++) {
				if (i > (_annotatedParams.size() - 1)) {
					AnnotatedParam annotatedParam = new AnnotatedParam(
						parameterAnnotations[i], parameterTypeClasses[i],
						genericParameterTypes[i]);

					_annotatedParams.add(annotatedParam);
				}
				else {
					AnnotatedParam annotatedParam = _annotatedParams.get(i);

					annotatedParam._merge(parameterAnnotations[i]);
				}
			}
		}

		private final List<AnnotatedParam> _annotatedParams = new ArrayList<>();
		private List<Annotation> _annotations = new ArrayList<>();

	}

	private AnnotatedClass(Class<?> clazz) {
		_merge(clazz);
	}

	private Predicate<AnnotatedMethod> _equalTo(Method method2) {
		return annotatedMethod -> {
			if (!Objects.equals(method1.getName(), method2.getName())) {
				return false;
			}

			if (!Objects.equals(method1.getReturnType(), method2.getReturnType())) {
				return false;
			}

			return Objects.deepEquals(
				method1.getParameterTypes(), method2.getParameterTypes());
		};
	}

	private void _merge(Class<?> clazz) {
		if (isNull(clazz) || Object.class.equals(clazz)) {
			return;
		}

		_annotations = mergeMissingAnnotations(
			_annotations, clazz.getDeclaredAnnotations());

		_merge(clazz.getDeclaredMethods());

		for (Class<?> interfaceClass : clazz.getInterfaces()) {
			_merge(interfaceClass);
		}

		_merge(clazz.getSuperclass());
	}

	private void _merge(Method[] methods) {
		for (Method method : methods) {
			if (method.isSynthetic()) {
				continue;
			}

			AnnotatedMethod annotatedMethod = Stream.ofAll(
				_annotatedMethods
			).find(
				_equalTo(method)
			).getOrNull();

			if (annotatedMethod != null) {
				annotatedMethod._includeInformationFrom(method);
			} else {
				_annotatedMethods.add(new AnnotatedMethod(method));
			}
		}
	}

	private final List<AnnotatedMethod> _annotatedMethods = new ArrayList<>();
	private List<Annotation> _annotations = new ArrayList<>();

}