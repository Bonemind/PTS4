package com.proftaak.pts4.swagger;

import com.proftaak.pts4.database.IDatabaseModel;
import com.proftaak.pts4.json.JSONSerializerFactory;
import com.proftaak.pts4.rest.Router;
import com.proftaak.pts4.rest.annotations.Field;
import com.proftaak.pts4.rest.annotations.Fields;
import com.proftaak.pts4.rest.annotations.RequireAuth;
import com.proftaak.pts4.rest.response.JSONResponse;
import com.proftaak.pts4.rest.response.RawResponse;
import com.proftaak.pts4.rest.response.metadata.PaginationMetadata;
import com.proftaak.pts4.utils.JSONUtils;
import com.proftaak.pts4.utils.ReflectionUtils;
import com.sun.javadoc.*;
import flexjson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Michon
 */
@SuppressWarnings("Contract")
public class BuildSwaggerDoclet extends Doclet {
    /**
     * The type presentations for Swagger
     */
    private static Map<Class, Pair<String, String>> TYPE_MAP = new HashMap<Class, Pair<String, String>>() {{
        put(Integer.class, new ImmutablePair<>("integer", "int32"));
        put(int.class, new ImmutablePair<>("integer", "int32"));
        put(Long.class, new ImmutablePair<>("integer", "int64"));
        put(long.class, new ImmutablePair<>("integer", "int64"));
        put(Float.class, new ImmutablePair<>("number", "float"));
        put(float.class, new ImmutablePair<>("number", "float"));
        put(Double.class, new ImmutablePair<>("number", "double"));
        put(double.class, new ImmutablePair<>("number", "double"));
        put(String.class, new ImmutablePair<>("string", null));
        put(Byte.class, new ImmutablePair<>("string", "byte"));
        put(byte.class, new ImmutablePair<>("string", "byte"));
        put(Boolean.class, new ImmutablePair<>("boolean", null));
        put(boolean.class, new ImmutablePair<>("boolean", null));
        put(LocalDate.class, new ImmutablePair<>("string", "date"));
        put(LocalDateTime.class, new ImmutablePair<>("string", "date-time"));
        put(Date.class, new ImmutablePair<>("string", "date-time"));
        put(RawResponse.class, new ImmutablePair<>("string", "see method documentation"));
    }};

    /**
     * Process a class of any type into a type definition
     *
     * @param type      The type
     * @param overrides The overrides
     * @return The type info, or null if the type should be ignored
     */
    public static Map<String, Object> getTypeInfo(Type type, Map<Class, Class> overrides) throws Exception {
        Map<String, Object> data = new HashMap<>();
        if (type instanceof ParameterizedType) {
            if (JSONResponse.class.isAssignableFrom((Class<?>) ((ParameterizedType) type).getRawType())) {
                // JSONReponse object
                return getTypeInfo(((ParameterizedType) type).getActualTypeArguments()[0], overrides);
            } else if (Collection.class.isAssignableFrom((Class<?>) ((ParameterizedType) type).getRawType())) {
                // Collection
                if (overrides.containsKey(Collection.class)) {
                    return getTypeInfo(Collection.class, overrides);
                } else {
                    Map<String, Object> subTypeData = getTypeInfo(((ParameterizedType) type).getActualTypeArguments()[0], overrides);
                    if (((String) subTypeData.getOrDefault("$ref", "")).endsWith("Single")) {
                        String ref = (String) subTypeData.get("$ref");
                        subTypeData.put("$ref", ref.substring(0, ref.length() - 6) + "List");
                        return subTypeData;
                    } else {
                        data.put("type", "array");
                        data.put("items", subTypeData);
                    }
                }
            }
        } else {
            Class cls = (Class) type;
            if (overrides.containsKey(cls)) {
                // Overridden
                return getTypeInfo(overrides.get(cls), overrides);
            } else if (TYPE_MAP.containsKey(cls)) {
                // Known type from TYPE_MAP
                data.put("type", TYPE_MAP.get(cls).getLeft());
                if (StringUtils.isNotEmpty(TYPE_MAP.get(cls).getRight())) {
                    data.put("format", TYPE_MAP.get(cls).getRight());
                }
            } else if (cls.isEnum()) {
                // Enum
                data.put("type", "string");
                data.put("enum", cls.getEnumConstants());
            } else if (IDatabaseModel.class.isAssignableFrom(cls)) {
                // Datadata model
                if (overrides.containsKey(IDatabaseModel.class)) {
                    return getTypeInfo(IDatabaseModel.class, overrides);
                } else {
                    data.put("$ref", "#/definitions/" + cls.getSimpleName() + "Single");
                }
            } else if (cls.getAnnotation(Embeddable.class) != null) {
                // Embeddable
                data.put("$ref", "#/definitions/" + cls.getSimpleName());
            } else if (cls == Void.class || cls == void.class) {
                // Void, returns nothing
                return null;
            } else {
                // Unknown type, error
                throw new Exception(String.format("Unknown type %s", cls.getName()));
            }
        }
        return data;
    }

    private static boolean hasErrors = false;

    private static void logError(String message) {
        if (!BuildSwaggerDoclet.hasErrors) {
            System.err.println("One or more errors have occurred while building the Swagger docs");
            BuildSwaggerDoclet.hasErrors = true;
        }
        System.err.println(message);
    }

    public static boolean start(RootDoc root) {
        Reflections.log = null;
        Reflections reflections = new Reflections();

        // Basic, static info
        Map<String, Object> data = new HashMap<>();
        data.put("swagger", "2.0");
        data.put("schemes", Collections.singletonList("http"));
        data.put("consumes", Collections.singletonList("application/json"));
        data.put("produces", Collections.singletonList("application/json"));

        // API info
        Map<String, Object> info = subMap(data, "info");
        info.put("title", "PTS4");
        info.put("description", "A GitHub-like approach to Agile tooling.");
        info.put("version", "dev");

        // Security definition
        Map<String, Object> security = subMap(data, "securityDefinitions", "api_key");
        security.put("type", "apiKey");
        security.put("description", "Token-based authentication");
        security.put("in", "header");
        security.put("name", "X-TOKEN");

        // Security statement
        security = new HashMap<>();
        security.put("api_key", new Object[0]);
        Collection<Object> securityStatement = Collections.singletonList(security);
        data.put("security", securityStatement);

        // Model definitions
        Map<String, Object> definitions = subMap(data, "definitions");

        // Create the error model definition
        Map<String, Object> definition = subMap(definitions, "Error");
        Map<String, Object> properties = subMap(definition, "properties");
        Map<String, Object> fieldData = subMap(properties, "error");
        fieldData.put("type", "string");
        fieldData.put("description", "An human-readable message describing what went wrong");

        // Create a definition of the envelope for all non-BaseResponse non-collections
        Map<String, Object> envelopeSingleDefinition = new HashMap<>();
        properties = subMap(envelopeSingleDefinition, "properties");
        fieldData = subMap(properties, "warnings");
        fieldData.put("type", "array");
        fieldData.put("description", "Human-readable warning messages about errors in the usage of the API that are not severe enough to warrant stopping the request");
        fieldData = subMap(fieldData, "items");
        fieldData.put("type", "string");

        // Create a definition of the envelope for all non-BaseResponse collections
        Map<String, Object> envelopeCollectionDefinition = JSONUtils.toHashMap(envelopeSingleDefinition);
        properties = subMap(envelopeCollectionDefinition, "properties");
        fieldData = subMap(properties, "pagination");
        fieldData.put("$ref", "#/definitions/PaginationMetadata");

        // Initialize a JSONContext, using the settings from a JSONSerializer out of our factory.
        JSONContext jsonContext = new JSONContext();
        JSONSerializer jsonSerializer = JSONSerializerFactory.createSerializer();
        try {
            jsonContext.setPathExpressions((List<PathExpression>) ReflectionUtils.getFieldValue(JSONSerializer.class, "pathExpressions", jsonSerializer));
        } catch (NoSuchFieldException | IllegalAccessException e1) {
            e1.printStackTrace();
        }

        // Create a schema definition for each of the serializable classes
        Collection<Class> modelClasses = new ArrayList<>();
        modelClasses.addAll(reflections.getTypesAnnotatedWith(Entity.class));
        modelClasses.addAll(reflections.getTypesAnnotatedWith(Embeddable.class));
        modelClasses.add(PaginationMetadata.class);
        for (Class modelCls : modelClasses) {
            ClassDoc clsDoc = root.classNamed(modelCls.getName());

            // Root object for this model
            definition = subMap(definitions, clsDoc.simpleTypeName());

            // Create a map of field -> FieldDoc
            Map<String, FieldDoc> fields = new HashMap<>();
            for (FieldDoc fieldDoc : clsDoc.fields(false)) {
                fields.put(fieldDoc.name(), fieldDoc);
            }

            // Create a map of method -> MethodDoc
            Map<String, MethodDoc> methods = new HashMap<>();
            for (MethodDoc methodDoc : clsDoc.methods(false)) {
                methods.put(methodDoc.name(), methodDoc);
            }

            // Create a BeanAnalyzer for this class
            // This can be used to get a list of all fields that FlexJSON sees for this class
            BeanAnalyzer analyzer = BeanAnalyzer.analyze(modelCls);

            // Create a list of properties for the model
            properties = subMap(definition, "properties");
            for (BeanProperty property : analyzer.getProperties()) {
                // If the property should not be included according to the JSONContext, skip it
                try {
                    ReflectionUtils.setFieldValue(JSONContext.class, "path", jsonContext, Path.parse(property.getJsonName()));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                    return false;
                }
                if (!jsonContext.isIncluded(property)) {
                    continue;
                }

                // Get the type info of the property
                try {
                    fieldData = getTypeInfo(property.getPropertyType(), new HashMap<Class, Class>() {{
                        put(Collection.class, Void.class);
                        put(IDatabaseModel.class, Integer.class);
                    }});
                    if (fieldData == null) {
                        continue;
                    }
                } catch (Exception e) {
                    logError(String.format(
                            "%s for type of property %s in class %s",
                            e.getMessage(), property.getName(), property.getReadMethod().getDeclaringClass().getName()
                    ));
                    continue;
                }

                // Get the description of the property
                if (fields.containsKey(property.getName())) {
                    fieldData.put("description", fields.get(property.getName()).commentText());
                }
                if (property.getReadMethod() != null) {
                    MethodDoc methodDoc = methods.get(property.getReadMethod());
                    if (methodDoc != null && !StringUtils.isEmpty(methodDoc.commentText())) {
                        fieldData.put("description", methodDoc.commentText());
                    }
                }

                // Store the field data into the properties
                properties.put(property.getJsonName(), fieldData);
            }

            // If this is an entity class, add the enveloped versions
            if (modelCls.getAnnotation(Entity.class) != null) {
                envelopeSingleDefinition = JSONUtils.toHashMap(envelopeSingleDefinition);
                properties = subMap(envelopeSingleDefinition, "properties");
                fieldData = subMap(properties, "data");
                fieldData.put("$ref", "#/definitions/" + clsDoc.simpleTypeName());
                definitions.put(clsDoc.simpleTypeName() + "Single", envelopeSingleDefinition);

                envelopeCollectionDefinition = JSONUtils.toHashMap(envelopeCollectionDefinition);
                properties = subMap(envelopeCollectionDefinition, "properties");
                fieldData = subMap(properties, "data");
                fieldData.put("type", "array");
                fieldData = subMap(fieldData, "items");
                fieldData.put("$ref", "#/definitions/" + clsDoc.simpleTypeName());
                definitions.put(clsDoc.simpleTypeName() + "List", envelopeCollectionDefinition);
            }
        }

        // Paths
        Map<String, Object> paths = subMap(data, "paths");

        // Create the error response definition
        Map<String, Object> errorResponse = subMap(data, "responses", "GeneralError");
        errorResponse.put("description", "Something has gone wrong. Usually, this will be your fault. See the returned message for more details of your failure");
        subMap(errorResponse, "schema").put("$ref", "#/definitions/Error");

        // Create a path definition for each route
        Router router = new Router();
        for (Router.Route route : router.getRoutes()) {
            Method method = route.handler;

            // Map for this route
            Map<String, Object> routeMap = subMap(paths, route.pattern);

            // Map for this method
            Map<String, Object> methodMap = subMap(routeMap, route.annotation.method().method.toLowerCase());

            // Get the method documentation
            MethodDoc methodDoc = null;
            for (MethodDoc methodDoc1 : root.classNamed(route.handler.getDeclaringClass().getName()).methods(false)) {
                if (methodDoc1.name().equals(route.handler.getName())) {
                    methodDoc = methodDoc1;
                    break;
                }
            }
            if (methodDoc == null) {
                logError(String.format(
                        "Unable to find MethodDoc for method %s in class %s",
                        method.getName(), method.getDeclaringClass().getName()
                ));
                continue;
            }

            // If secured, mark as such
            if (method.getAnnotation(RequireAuth.class) != null) {
                methodMap.put("security", securityStatement);
            }

            // Set the path description
            String[] parts = methodDoc.commentText().split("\n\n", 2);
            methodMap.put("summary", parts[0].replaceAll("\n", " ").replaceAll("  ", " ").trim());
            if (parts.length > 1) {
                methodMap.put("description", parts[1].replaceAll("\n\n", "\n").replaceAll("\n ", "\n").trim());
            }

            // Add the parameters
            Fields fields = method.getAnnotation(Fields.class);
            if (fields != null && fields.value().length > 0) {
                Collection<Map<String, Object>> parameters = new ArrayList<>();
                methodMap.put("parameters", parameters);
                for (Field field : fields.value()) {
                    Map<String, Object> parameter;
                    try {
                        parameter = getTypeInfo(field.type(), new HashMap<Class, Class>() {{
                            put(IDatabaseModel.class, Integer.class);
                        }});
                        if (parameter == null) {
                            continue;
                        }
                    } catch (Exception e) {
                        logError(String.format(
                                "%s for type of field %s on route %s in class %s",
                                e.getMessage(), field.name(), method.getName(), method.getDeclaringClass().getName()
                        ));
                        continue;
                    }
                    parameters.add(parameter);
                    parameter.put("name", field.name());
                    parameter.put("description", field.description());
                    parameter.put("required", field.required());
                    parameter.put("in", "body");
                }
            }

            // Get the return type info
            Map<String, Object> returnTypeInfo = null;
            try {
                returnTypeInfo = getTypeInfo(method.getGenericReturnType(), new HashMap<>());
            } catch (Exception e) {
                logError(String.format(
                        "%s for return type of route %s in class %s",
                        e.getMessage(), method.getName(), method.getDeclaringClass().getName()
                ));
            }

            // Add the query parameters
            if (returnTypeInfo != null && ((String) returnTypeInfo.getOrDefault("$ref", "")).endsWith("List")) {
                Collection<Map<String, Object>> parameters = (Collection<Map<String, Object>>) methodMap.getOrDefault("parameters", new ArrayList<Map<String, Object>>());
                methodMap.put("parameters", parameters);

                // Get the return class, to determine the query parameters
            }

            // Map for the responses.
            Map<String, Object> responses = subMap(methodMap, "responses");
            Map<String, Object> response;

            // Set the default response.
            Class<?> returnType = method.getReturnType();
            if (returnType == Void.TYPE) {
                response = subMap(responses, "204");
                response.put("description", "Action succeeded, nothing to return");
            } else {
                response = subMap(responses, "200");

                // Get the summary (the @return documentation of the method)
                Tag[] returnTags = methodDoc.tags("return");
                if (returnTags.length != 1) {
                    /*logError(String.format(
                        "Method %s in class %s must have exactly one return doc tag",
                        method.getName(), method.getDeclaringClass().getName()
                    ));*/
                    response.put("description", methodDoc.commentText());
                } else {
                    response.put("description", returnTags[0].text());
                }

                // Set the return type
                if (returnTypeInfo != null) {
                    response.put("schema", returnTypeInfo);
                }
            }

            // Set the error response.
            subMap(responses, "default").put("$ref", "#/responses/GeneralError");
        }

        // Write to file
        try {
            // Open the file
            FileOutputStream fos = new FileOutputStream("swagger.json");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            OutputStreamWriter osw = new OutputStreamWriter(bos);

            // Serialize
            JSONSerializer serializer = new JSONSerializer();
            serializer.include("*");
            serializer.serialize(data, osw);

            // Close the file
            osw.close();
        } catch (IOException ignored) {
        }

        return !BuildSwaggerDoclet.hasErrors;
    }

    /**
     * Create a sub map
     *
     * @param parentMap The parent map
     * @param key       The key of the sub map
     * @return The new map
     */
    private static Map<String, Object> subMap(Map<String, Object> parentMap, String... keys) {
        for (String key : keys) {
            if (!parentMap.containsKey(key)) {
                parentMap.put(key, new LinkedHashMap<String, Object>());
            }
            parentMap = (Map<String, Object>) parentMap.get(key);
        }
        return parentMap;
    }
}
