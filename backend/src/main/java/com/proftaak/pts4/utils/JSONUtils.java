package com.proftaak.pts4.utils;

import com.proftaak.pts4.json.JSONSerializerFactory;
import flexjson.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @author Michon
 */
public class JSONUtils {
    private static final JSONContext jsonContext;

    static {
        // Initialize a JSONContext, using the settings from a JSONSerializer out of our factory
        jsonContext = new JSONContext();
        JSONSerializer jsonSerializer = JSONSerializerFactory.createSerializer();
        try {
            jsonContext.setPathExpressions((List<PathExpression>) ReflectionUtils.getFieldValue(JSONSerializer.class, "pathExpressions", jsonSerializer));
        } catch (NoSuchFieldException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Get the properties of the given class that would be visible in serialization.
     */
    public static Collection<BeanProperty> getProperties(Class cls) {
        // Create a BeanAnalyzer for this class
        // This can be used to get a list of all fields that FlexJSON sees for this class
        BeanAnalyzer beanAnalyzer = BeanAnalyzer.analyze(cls);

        // Filter out the properties that would not be included when serializing the JSON
        Collection<BeanProperty> properties = new ArrayList<>();
        synchronized (jsonContext) {
            for (BeanProperty property : beanAnalyzer.getProperties()) {
                try {
                    ReflectionUtils.setFieldValue(JSONContext.class, "path", jsonContext, Path.parse(property.getJsonName()));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    continue;
                }

                if (jsonContext.isIncluded(property)) {
                    properties.add(property);
                }
            }
        }

        return properties;
    }

    /**
     * Convert an object to a hashmap by serializing and deserializing it
     *
     * @param obj The object to convert
     * @return The hashmap
     */
    public static HashMap<String, Object> toHashMap(Object obj) {
        JSONSerializer jsonSerializer = JSONSerializerFactory.createSerializer();
        JSONDeserializer<HashMap<String, Object>> jsonDeserializer = new JSONDeserializer<>();
        return jsonDeserializer.deserialize(jsonSerializer.serialize(obj), HashMap.class);
    }
}
