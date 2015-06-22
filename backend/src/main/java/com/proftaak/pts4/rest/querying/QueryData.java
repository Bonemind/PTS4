package com.proftaak.pts4.rest.querying;

import com.proftaak.pts4.database.IDatabaseModel;
import com.proftaak.pts4.rest.HTTPException;
import com.proftaak.pts4.rest.RequestData;
import com.proftaak.pts4.utils.JSONUtils;
import flexjson.BeanProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * All query data, basically a collection of QueryParameters with some utility methods
 */
public class QueryData extends HashMap<String, Collection<QueryParameter>> {
    /**
     * Get the value of the query parameter with the given key and operator, if any
     *
     * If none is found, return the default value instead
     *
     * @param  key          The key of the query parameter
     * @param  operator     The operator of the query parameter
     * @param  defaultValue The default to be used if the value is not present
     * @return Either the value from the query parameters, or the default value
     */
    public String getStringValueForOp(String key, Operator operator, String defaultValue) {
        for (QueryParameter parameter : this.getOrDefault(key, new ArrayList<>())) {
            if (parameter.getOperator() == operator) {
                return parameter.getValue();
            }
        }
        return defaultValue;
    }

    /**
     * Get all query parameters
     *
     * @return All query parameters
     */
    public Collection<QueryParameter> getForUserParameters() {
        Collection<QueryParameter> queryParameters = new ArrayList<>();
        for (Collection<QueryParameter> parameters : this.values()) {
            queryParameters.addAll(parameters);
        }
        return queryParameters;
    }

    private QueryData() {
        super();
    }

    /**
     * Parse a query string into a QueryData object
     *
     * @param  queryString The query string
     * @return A new QueryData object with all parameters in the query as QueryParameters
     */
    public static QueryData fromQueryString(String queryString) throws HTTPException {
        QueryData queryData = new QueryData();

        // For each query parameter, build a QueryParameter object
        for (NameValuePair parameter : URLEncodedUtils.parse(queryString, Charset.defaultCharset())) {
            QueryParameter queryParameter = QueryParameter.fromNameValuePair(parameter);
            if (!queryData.containsKey(queryParameter.getKey())) {
                queryData.put(queryParameter.getKey(), new ArrayList<>());
            }
            queryData.get(queryParameter.getKey()).add(queryParameter);
        }

        return queryData;
    }

    /**
     * Parse the query string from the request in RequestData into a QueryData object
     *  ho* @see   fromQueryString
     * @param requestData The requestdata
     */
    public static QueryData fromRequestData(RequestData requestData) throws HTTPException {
        return QueryData.fromQueryString(requestData.getRequest().getQueryString());
    }
}
