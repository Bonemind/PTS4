package com.proftaak.pts4.rest.querying;

import com.proftaak.pts4.rest.HTTPException;
import org.apache.http.NameValuePair;
import org.glassfish.grizzly.http.util.HttpStatus;

/**
 * A single query parameter
 */
public class QueryParameter {
    /**
     * The key of the query parameter
     *
     * This is everything before the operator
     */
    private String key;

    /**
     * The operator of the query parameter
     */
    private Operator operator;

    /**
     * The value of the query parameter
     *
     * This is everything after the operator
     */
    private String value;

    private QueryParameter(String key, Operator operator, String value) {
        this.key = key;
        this.operator = operator;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public Operator getOperator() {
        return this.operator;
    }

    public String getValue() {
        return this.value;
    }

    /**
     * Parse a NameValuePair into a QueryParameter
     *
     * The already made split on = is ignored, and instead we use our own operators from the Operator class
     * This is done by looking for the symbols defined in Operator in the string, and splitting it on that
     * Everything before the operator becomes the key, everything after it becomes the value
     */
    public static QueryParameter fromNameValuePair(NameValuePair pair) throws HTTPException {
        // Re-join the name and value
        String data;
        if (pair.getValue() == null) {
            data = pair.getName();
        } else {
            data = pair.getName() + "=" + pair.getValue();
        }

        // Find the operator and value
        for (Operator operator : Operator.values()) {
            int index = data.indexOf(operator.symbol);
            if (index != -1) {
                // Get key/value
                String key = data.substring(0, index);
                String value = data.substring(index + operator.symbol.length());

                // Tranform the value
                if (value.isEmpty() || value.equals("null")) {
                    value = null;
                } else if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 2);
                }

                return new QueryParameter(key, operator, value);
            }
        }

        // No operator matched, error out
        throw new HTTPException(String.format("Invalid query filter %s", data), HttpStatus.BAD_REQUEST_400);
    }
}
