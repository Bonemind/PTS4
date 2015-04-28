package com.proftaak.pts4.swagger;

import com.proftaak.pts4.database.DatabaseModel;
import com.proftaak.pts4.rest.Router;
import com.proftaak.pts4.rest.annotations.Field;
import com.proftaak.pts4.rest.annotations.Fields;
import com.proftaak.pts4.rest.annotations.RequireAuth;
import com.sun.javadoc.*;
import flexjson.JSONSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
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
    }};

    public static void setType(Map<String, Object> base, Class type) {
        if (Collection.class.isAssignableFrom(type)) {
            base.put("type", "array");
            base = subMap(base, "items");
            type = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
        }
        if (DatabaseModel.class.isAssignableFrom(type)) {
            base.put("$ref", "#/definitions/" + type.getSimpleName());
        } else if (TYPE_MAP.containsKey(type)) {
            base.put("type", TYPE_MAP.get(type).getLeft());
        } else if (type != Object.class) {
            throw new RuntimeException(String.format(
                "Unknown return type %s for method %s in class %s",
                type.getName(), method.getName(), method.getDeclaringClass().getName()
            ));
        }
    }

    public static boolean start(RootDoc root) {
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

        // Definitions
        Map<String, Object> definitions = subMap(data, "definitions");

        // Create the error definition
        Map<String, Object> definition = subMap(definitions, "Error");
        Map<String, Object> properties = subMap(definition, "properties");
        Map<String, Object> fieldData = subMap(properties, "error");
        fieldData.put("type", "string");
        fieldData.put("description", "An human-readable message describing what went wrong");

        // Create a schema definition for each of the database model classes
        for (Class<? extends DatabaseModel> modelCls : reflections.getSubTypesOf(DatabaseModel.class)) {
            ClassDoc clsDoc = root.classNamed(modelCls.getName());

            // Root object for this model
            definition = subMap(definitions, clsDoc.simpleTypeName());

            // Create a map of field -> FieldDoc
            Map<String, FieldDoc> fields = new HashMap<>();
            for (FieldDoc fieldDoc : clsDoc.fields(false)) {
                fields.put(fieldDoc.name().toUpperCase(), fieldDoc);
            }

            // Create a map of method -> MethodDoc
            Map<String, MethodDoc> methods = new HashMap<>();
            for (MethodDoc methodDoc : clsDoc.methods(false)) {
                methods.put(methodDoc.name().toUpperCase(), methodDoc);
            }

            // Create a list of properties for the model
            properties = subMap(definition, "properties");
            for (Method method : modelCls.getDeclaredMethods()) {
                // If the method is not a getter, don't bother
                if (!method.getName().startsWith("get")) {
                    continue;
                }

                // If the method returns a collection, don't bother as there will be a separate route for this
                if (method.getReturnType().isAssignableFrom(Collection.class)) {
                    continue;
                }

                // Root object for this field.
                fieldData = subMap(properties, StringUtils.uncapitalize(method.getName().substring(3)));

                // Get the MethodDoc for this method
                MethodDoc methodDoc = methods.get(method.getName().toUpperCase());

                // Get the FieldDoc for this method
                FieldDoc fieldDoc = fields.get(method.getName().toUpperCase().substring(3));

                // Get the type info of the property
                Pair<String, String> typeInfo = TYPE_MAP.getOrDefault(method.getReturnType(), TYPE_MAP.get(String.class));
                fieldData.put("type", typeInfo.getLeft());
                if (typeInfo.getRight() != null) {
                    fieldData.put("format", typeInfo.getRight());
                }

                // Get the description of the property
                if (!StringUtils.isEmpty(methodDoc.commentText())) {
                    fieldData.put("description", methodDoc.commentText());
                } else {
                    fieldData.put("description", fieldDoc.commentText());
                }
            }
        }

        // Paths
        Map<String, Object> paths = subMap(data, "paths");

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
                throw new RuntimeException(String.format(
                    "Unable to find MethodDoc for method %s in class %s",
                    method.getName(), method.getDeclaringClass().getName()
                ));
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
                    Map<String, Object> parameter = new LinkedHashMap<>();
                    parameters.add(parameter);
                    parameter.put("name", field.name());
                    parameter.put("description", field.description());
                    parameter.put("required", field.required());
                    parameter.put("in", "body");
                    Pair<String, String> typeInfo = TYPE_MAP.getOrDefault(field.type(), TYPE_MAP.get(String.class));
                    parameter.put("type", typeInfo.getLeft());
                    if (typeInfo.getRight() != null) {
                        parameter.put("format", typeInfo.getRight());
                    }
                }
            }

            // Map for the responses.
            Map<String, Object> responses = subMap(methodMap, "responses");
            Map<String, Object> response;
            Map<String, Object> schema;

            // Set the default response.
            Class<?> returnType = method.getReturnType();
            if (returnType == Void.TYPE) {
                response = subMap(responses, "204");
                response.put("description", "Action succeeded, nothing to return");
            } else {
                response = subMap(responses, "200");

                // Get the summary (the @return documentation of the method)
                Tag[] returnTags = methodDoc.tags("return");
                if (returnTags.length > 1) {
                    throw new RuntimeException(String.format(
                        "Method %s in class %s must have exactly one return doc tag",
                        method.getName(), method.getDeclaringClass().getName()
                    ));
                }
                if (returnTags.length > 0) {
                    response.put("description", returnTags[0].text());
                }

                // Get the return type
                schema = subMap(response, "schema");
            }

            // Set the error response
            response = subMap(responses, "default");
            response.put("description", "Something has gone wrong. Usually, this will be your fault. See the returned message for more details of your failure");
            schema = subMap(response, "schema");
            schema.put("$ref", "#/definitions/Error");
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

        return true;
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
