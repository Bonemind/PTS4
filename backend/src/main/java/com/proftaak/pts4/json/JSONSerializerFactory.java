package com.proftaak.pts4.json;

import flexjson.JSONSerializer;

/**
 * Created by Michon on 25-5-2015.
 */
public class JSONSerializerFactory {
    private JSONSerializerFactory() {
    }

    /**
     * Create a new JSONSerializer, preconfigured to ignore irrelevant fields
     *
     * @return The new JSONSerializer
     */
    public static JSONSerializer createSerializer() {
        JSONSerializer jsonSerializer = new JSONSerializer();

        // Exclude the class properties, as these are completely irrelevant for the frontend.
        jsonSerializer.exclude("*.class");

        // Exclude the PK property, ad this is already included under it's primary name.
        jsonSerializer.exclude("*.PK");

        return jsonSerializer;
    }
}
