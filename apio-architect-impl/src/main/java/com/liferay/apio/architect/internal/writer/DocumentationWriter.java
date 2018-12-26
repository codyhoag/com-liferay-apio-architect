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

package com.liferay.apio.architect.internal.writer;

import static com.liferay.apio.architect.internal.wiring.osgi.manager.message.json.DocumentationField.FieldType.BOOLEAN;
import static com.liferay.apio.architect.internal.wiring.osgi.manager.message.json.DocumentationField.FieldType.BOOLEAN_LIST;
import static com.liferay.apio.architect.internal.wiring.osgi.manager.message.json.DocumentationField.FieldType.FILE;
import static com.liferay.apio.architect.internal.wiring.osgi.manager.message.json.DocumentationField.FieldType.LINKED_MODEL;
import static com.liferay.apio.architect.internal.wiring.osgi.manager.message.json.DocumentationField.FieldType.NESTED_MODEL;
import static com.liferay.apio.architect.internal.wiring.osgi.manager.message.json.DocumentationField.FieldType.NESTED_MODEL_LIST;
import static com.liferay.apio.architect.internal.wiring.osgi.manager.message.json.DocumentationField.FieldType.NUMBER;
import static com.liferay.apio.architect.internal.wiring.osgi.manager.message.json.DocumentationField.FieldType.NUMBER_LIST;
import static com.liferay.apio.architect.internal.wiring.osgi.manager.message.json.DocumentationField.FieldType.RELATED_COLLECTION;
import static com.liferay.apio.architect.internal.wiring.osgi.manager.message.json.DocumentationField.FieldType.STRING;
import static com.liferay.apio.architect.internal.wiring.osgi.manager.message.json.DocumentationField.FieldType.STRING_LIST;

import com.liferay.apio.architect.alias.representor.FieldFunction;
import com.liferay.apio.architect.alias.representor.NestedFieldFunction;
import com.liferay.apio.architect.consumer.TriConsumer;
import com.liferay.apio.architect.documentation.contributor.CustomDocumentation;
import com.liferay.apio.architect.form.Form;
import com.liferay.apio.architect.internal.documentation.Documentation;
import com.liferay.apio.architect.internal.message.json.DocumentationMessageMapper;
import com.liferay.apio.architect.internal.message.json.JSONObjectBuilder;
import com.liferay.apio.architect.internal.operation.CreateOperation;
import com.liferay.apio.architect.internal.operation.DeleteOperation;
import com.liferay.apio.architect.internal.operation.RetrieveOperation;
import com.liferay.apio.architect.internal.operation.UpdateOperation;
import com.liferay.apio.architect.internal.request.RequestInfo;
import com.liferay.apio.architect.internal.wiring.osgi.manager.message.json.DocumentationField;
import com.liferay.apio.architect.internal.wiring.osgi.manager.message.json.DocumentationField.FieldType;
import com.liferay.apio.architect.language.AcceptLanguage;
import com.liferay.apio.architect.operation.Operation;
import com.liferay.apio.architect.related.RelatedCollection;
import com.liferay.apio.architect.related.RelatedModel;
import com.liferay.apio.architect.representor.BaseRepresentor;
import com.liferay.apio.architect.representor.Representor;
import com.liferay.apio.architect.routes.CollectionRoutes;
import com.liferay.apio.architect.routes.ItemRoutes;
import com.liferay.apio.architect.routes.NestedCollectionRoutes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Writes the documentation.
 *
 * @author Alejandro Hernández
 */
public class DocumentationWriter {

	/**
	 * Creates a new {@code DocumentationWriter} object, without creating the
	 * builder.
	 *
	 * @param  function the function that transforms a builder into the {@code
	 *         DocumentationWriter}
	 * @return the {@code DocumentationWriter} instance
	 */
	public static DocumentationWriter create(
		Function<Builder, DocumentationWriter> function) {

		return function.apply(new Builder());
	}

	public DocumentationWriter(Builder builder) {
		_documentation = builder._documentation;
		_documentationMessageMapper = builder._documentationMessageMapper;
		_requestInfo = builder._requestInfo;
		_typeFunction = builder._typeFunction;

		_customDocumentation = _documentation.getCustomDocumentation();
	}

	/**
	 * Writes the {@link Documentation} to a string.
	 *
	 * @return the JSON representation of the {@code Documentation}
	 */
	public String write() {
		JSONObjectBuilder jsonObjectBuilder = new JSONObjectBuilder();

		_writeDocumentationMetadata(jsonObjectBuilder);

		Map<String, Representor> representors =
			_documentation.getRepresentors();

		Map<String, ItemRoutes> itemRoutesMap = _documentation.getItemRoutes();

		itemRoutesMap.forEach(
			(name, itemRoutes) -> _writeRoute(
				jsonObjectBuilder, name, representors.get(name),
				_documentationMessageMapper::mapResource,
				this::_writeItemOperations,
				resourceJsonObjectBuilder -> _writeAllFields(
					representors.get(name), resourceJsonObjectBuilder)));

		Map<String, NestedCollectionRoutes> nestedCollectionRoutesMap =
			_documentation.getNestedCollectionRoutes();

		Map<String, CollectionRoutes> collectionRoutes =
			_documentation.getCollectionRoutes();

		Set<String> collectionResources = new HashSet<>(
			collectionRoutes.keySet());

		Set<String> nestedRoutes = new HashSet<>(itemRoutesMap.keySet());

		nestedRoutes.addAll(collectionRoutes.keySet());

		nestedRoutes.forEach(
			name -> {
				Optional<String> nestedCollectionRoute =
					_getNestedCollectionRouteOptional(
						representors, nestedCollectionRoutesMap, name);

				nestedCollectionRoute.ifPresent(collectionResources::add);
			});

		collectionResources.forEach(
			name -> _writeRoute(
				jsonObjectBuilder, name, representors.get(name),
				_documentationMessageMapper::mapResourceCollection,
				this::_writePageOperations,
				__ -> {
				}));

		_documentationMessageMapper.onFinish(jsonObjectBuilder, _documentation);

		return jsonObjectBuilder.build();
	}

	/**
	 * Creates {@code DocumentationWriter} instances.
	 */
	public static class Builder {

		/**
		 * Add information about the documentation being written to the builder.
		 *
		 * @param  documentation the documentation being written
		 * @return the updated builder
		 */
		public DocumentationMessageMapperStep documentation(
			Documentation documentation) {

			_documentation = documentation;

			return new DocumentationMessageMapperStep();
		}

		public class BuildStep {

			/**
			 * Constructs and returns a {@code DocumentationWriter} instance
			 * with the information provided to the builder.
			 *
			 * @return the {@code DocumentationWriter} instance
			 */
			public DocumentationWriter build() {
				return new DocumentationWriter(Builder.this);
			}

		}

		public class DocumentationMessageMapperStep {

			/**
			 * Adds information to the builder about the {@link
			 * DocumentationMessageMapper}.
			 *
			 * @param  documentationMessageMapper the {@code
			 *         DocumentationMessageMapper}
			 * @return the updated builder
			 */
			public RequestInfoStep documentationMessageMapper(
				DocumentationMessageMapper documentationMessageMapper) {

				_documentationMessageMapper = documentationMessageMapper;

				return new RequestInfoStep();
			}

		}

		public class RequestInfoStep {

			/**
			 * Adds information to the builder about the request.
			 *
			 * @param  requestInfo the information obtained from the request. It
			 *         can be created by using a {@link RequestInfo.Builder}.
			 * @return the updated builder
			 */
			public TypeFunctionStep requestInfo(RequestInfo requestInfo) {
				_requestInfo = requestInfo;

				return new TypeFunctionStep();
			}

		}

		public class TypeFunctionStep {

			/**
			 * Adds information to the builder about how to get the type from an
			 * identifier class.
			 *
			 * @param  typeFunction the information obtained from the request.
			 * @return the updated builder
			 */
			public BuildStep typeFunction(
				Function<Class<?>, Optional<String>> typeFunction) {

				_typeFunction = typeFunction;

				return new BuildStep();
			}

		}

		private Documentation _documentation;
		private DocumentationMessageMapper _documentationMessageMapper;
		private RequestInfo _requestInfo;
		private Function<Class<?>, Optional<String>> _typeFunction;

	}

	private Stream<DocumentationField> _calculateNestableFieldNames(
		BaseRepresentor representor) {

		Stream<DocumentationField> applicationRelativeURLStream =
			_getDocumentationFieldStream(
				representor.getApplicationRelativeURLFunctions(), STRING);
		Stream<DocumentationField> binaryStream = _getDocumentationFieldStream(
			representor.getBinaryFunctions(), FILE);
		Stream<DocumentationField> booleanStream = _getDocumentationFieldStream(
			representor.getBooleanFunctions(), BOOLEAN);
		Stream<DocumentationField> booleanListStream =
			_getDocumentationFieldStream(
				representor.getBooleanListFunctions(), BOOLEAN_LIST);
		Stream<DocumentationField> linkStream = _getDocumentationFieldStream(
			representor.getLinkFunctions(), STRING);
		Stream<DocumentationField> localizedStringStream =
			_getDocumentationFieldStream(
				representor.getLocalizedStringFunctions(), STRING);
		Stream<DocumentationField> nestedStream = _getDocumentationFieldStream(
			representor.getNestedFieldFunctions(), NESTED_MODEL);
		Stream<DocumentationField> nestedListStream =
			_getDocumentationFieldStream(
				representor.getNestedFieldFunctions(), NESTED_MODEL_LIST);
		Stream<DocumentationField> numberStream = _getDocumentationFieldStream(
			representor.getNumberFunctions(), NUMBER);
		Stream<DocumentationField> numberListStream =
			_getDocumentationFieldStream(
				representor.getNumberListFunctions(), NUMBER_LIST);
		Stream<DocumentationField> relativeURLStream =
			_getDocumentationFieldStream(
				representor.getRelativeURLFunctions(), STRING);
		Stream<DocumentationField> stringStream = _getDocumentationFieldStream(
			representor.getStringFunctions(), STRING);
		Stream<DocumentationField> stringListStream =
			_getDocumentationFieldStream(
				representor.getStringListFunctions(), STRING_LIST);

		List<RelatedModel> relatedModels = representor.getRelatedModels();

		Stream<RelatedModel> relatedModelStream = relatedModels.stream();

		Stream<DocumentationField> relatedModelDocumentationFieldStream =
			relatedModelStream.map(_createDocumentationFieldFromModel());

		Stream<DocumentationField> fieldsStream = Stream.of(
			applicationRelativeURLStream, binaryStream, booleanStream,
			booleanListStream, linkStream, localizedStringStream, nestedStream,
			nestedListStream, numberStream, numberListStream, relativeURLStream,
			stringStream, stringListStream, relatedModelDocumentationFieldStream
		).flatMap(
			Function.identity()
		);

		List<NestedFieldFunction> nestedFieldFunctions =
			representor.getNestedFieldFunctions();

		Stream<NestedFieldFunction> nestedFieldFunctionStream =
			nestedFieldFunctions.stream();

		Stream<DocumentationField> nestedFieldNamesStream =
			nestedFieldFunctionStream.map(
				nestedFieldFunction -> _calculateNestableFieldNames(
					nestedFieldFunction.getNestedRepresentor())
			).reduce(
				Stream::concat
			).orElseGet(
				Stream::empty
			);

		return Stream.concat(fieldsStream, nestedFieldNamesStream);
	}

	private Function<RelatedCollection, DocumentationField>
		_createDocumentationFieldFromCollection() {

		return relatedCollection -> {
			String extraType = _typeFunction.apply(
				relatedCollection.getIdentifierClass()
			).orElse(
				null
			);

			return DocumentationField.of(
				relatedCollection.getKey(), RELATED_COLLECTION, extraType);
		};
	}

	private Function<RelatedModel, DocumentationField>
		_createDocumentationFieldFromModel() {

		return relatedCollection -> {
			String extraType = _typeFunction.apply(
				relatedCollection.getIdentifierClass()
			).orElse(
				null
			);

			return DocumentationField.of(
				relatedCollection.getKey(), LINKED_MODEL, extraType);
		};
	}

	private String _getCustomDocumentation(String name) {
		return Optional.ofNullable(
			_customDocumentation.getDescriptionFunction(name)
		).map(
			localeFunction -> {
				AcceptLanguage acceptLanguage =
					_requestInfo.getAcceptLanguage();

				return localeFunction.apply(
					acceptLanguage.getPreferredLocale());
			}
		).orElse(
			null
		);
	}

	private Stream<DocumentationField> _getDocumentationFieldStream(
		List<FieldFunction> fieldFunctionList, FieldType fieldType) {

		Stream<FieldFunction> stream = fieldFunctionList.stream();

		return stream.map(
			fieldFunction -> DocumentationField.of(
				fieldFunction.getKey(), fieldType));
	}

	private Optional<String> _getNestedCollectionRouteOptional(
		Map<String, Representor> representorMap, Map<String, ?> nestedRoutesMap,
		String name) {

		Set<String> nestedRoutes = nestedRoutesMap.keySet();

		Stream<String> nestedRoutesStream = nestedRoutes.stream();

		return nestedRoutesStream.map(
			nestedRoute -> nestedRoute.split(name + "-")
		).filter(
			routes ->
				routes.length == 2 && !routes[0].equals("") &&
				!routes[0].equals(routes[1]) &&
				representorMap.containsKey(routes[1])
		).map(
			routes -> routes[1]
		).findFirst();
	}

	private void _writeAllFields(
		Representor representor, JSONObjectBuilder resourceJsonObjectBuilder) {

		Stream<DocumentationField> fieldNamesStream =
			_calculateNestableFieldNames(representor);

		Stream<RelatedCollection> relatedCollections =
			representor.getRelatedCollections();

		Stream<DocumentationField> relatedCollectionsNamesStream =
			relatedCollections.map(_createDocumentationFieldFromCollection());

		fieldNamesStream = Stream.concat(
			fieldNamesStream, relatedCollectionsNamesStream);

		_writeFields(fieldNamesStream, resourceJsonObjectBuilder);
	}

	private void _writeDocumentationMetadata(
		JSONObjectBuilder jsonObjectBuilder) {

		Optional<String> apiTitleOptional =
			_documentation.getAPITitleOptional();

		apiTitleOptional.ifPresent(
			title -> _documentationMessageMapper.mapTitle(
				jsonObjectBuilder, title));

		Optional<String> apiDescriptionOptional =
			_documentation.getAPIDescriptionOptional();

		apiDescriptionOptional.ifPresent(
			description -> _documentationMessageMapper.mapDescription(
				jsonObjectBuilder, description));

		Optional<String> entryPointOptional =
			_documentation.getEntryPointOptional();

		entryPointOptional.ifPresent(
			entryPoint -> _documentationMessageMapper.mapEntryPoint(
				jsonObjectBuilder, entryPoint)
		);
	}

	private void _writeFields(
		Stream<DocumentationField> fields,
		JSONObjectBuilder resourceJsonObjectBuilder) {

		Stream<DocumentationField> stream = fields.distinct();

		stream.forEach(
			field -> _writeFormField(resourceJsonObjectBuilder, field));
	}

	private void _writeFormField(
		JSONObjectBuilder resourceJsonObjectBuilder,
		DocumentationField documentationField) {

		JSONObjectBuilder jsonObjectBuilder = new JSONObjectBuilder();

		String customDocumentation = _getCustomDocumentation(
			documentationField.getName());

		_documentationMessageMapper.mapProperty(
			jsonObjectBuilder, documentationField, customDocumentation);

		_documentationMessageMapper.onFinishProperty(
			resourceJsonObjectBuilder, jsonObjectBuilder,
			documentationField.getName());
	}

	private void _writeItemOperations(
		String name, String type, JSONObjectBuilder resourceJsonObjectBuilder) {

		Map<String, ItemRoutes> itemRoutesMap = _documentation.getItemRoutes();

		Optional.ofNullable(
			itemRoutesMap.getOrDefault(name, null)
		).ifPresent(
			itemRoutes -> {
				RetrieveOperation retrieveOperation = new RetrieveOperation(
					name, false);

				_writeOperation(
					retrieveOperation, resourceJsonObjectBuilder, name, type);

				Optional<Form> optional = itemRoutes.getFormOptional();

				UpdateOperation updateOperation = optional.map(
					form -> new UpdateOperation(form, name)
				).orElse(
					new UpdateOperation(null, name)
				);

				_writeOperation(
					updateOperation, resourceJsonObjectBuilder, name, type);

				DeleteOperation deleteOperation = new DeleteOperation(name);

				_writeOperation(
					deleteOperation, resourceJsonObjectBuilder, name, type);
			}
		);
	}

	private void _writeOperation(
		Operation operation, JSONObjectBuilder jsonObjectBuilder,
		String resourceName, String type) {

		JSONObjectBuilder operationJsonObjectBuilder = new JSONObjectBuilder();

		String customDocumentation = _getCustomDocumentation(
			operation.getName());

		_documentationMessageMapper.mapOperation(
			operationJsonObjectBuilder, resourceName, type, operation,
			customDocumentation);

		_documentationMessageMapper.onFinishOperation(
			jsonObjectBuilder, operationJsonObjectBuilder, operation);
	}

	private void _writePageOperations(
		String resource, String type,
		JSONObjectBuilder resourceJsonObjectBuilder) {

		Map<String, CollectionRoutes> collectionRoutesMap =
			_documentation.getCollectionRoutes();

		Optional.ofNullable(
			collectionRoutesMap.getOrDefault(resource, null)
		).ifPresent(
			collectionRoutes -> {
				_writeOperation(
					new RetrieveOperation(resource, true),
					resourceJsonObjectBuilder, resource, type);

				Optional<Form> optional = collectionRoutes.getFormOptional();

				CreateOperation createOperation = optional.map(
					form -> new CreateOperation(form, resource)
				).orElse(
					new CreateOperation(null, resource)
				);

				_writeOperation(
					createOperation, resourceJsonObjectBuilder, resource, type);
			}
		);
	}

	private void _writeRoute(
		JSONObjectBuilder jsonObjectBuilder, String name,
		Representor representor,
		TriConsumer<JSONObjectBuilder, String, String> writeResourceBiConsumer,
		TriConsumer<String, String, JSONObjectBuilder>
			writeOperationsTriConsumer,
		Consumer<JSONObjectBuilder> writeFieldsRepresentor) {

		List<String> types = representor.getTypes();

		types.forEach(
			type -> {
				JSONObjectBuilder resourceJsonObjectBuilder =
					new JSONObjectBuilder();

				String customDocumentation = _getCustomDocumentation(type);

				writeResourceBiConsumer.accept(
					resourceJsonObjectBuilder, type, customDocumentation);

				writeOperationsTriConsumer.accept(
					name, type, resourceJsonObjectBuilder);

				writeFieldsRepresentor.accept(resourceJsonObjectBuilder);

				_documentationMessageMapper.onFinishResource(
					jsonObjectBuilder, resourceJsonObjectBuilder, type);
			});
	}

	private final CustomDocumentation _customDocumentation;
	private final Documentation _documentation;
	private final DocumentationMessageMapper _documentationMessageMapper;
	private final RequestInfo _requestInfo;
	private final Function<Class<?>, Optional<String>> _typeFunction;

}