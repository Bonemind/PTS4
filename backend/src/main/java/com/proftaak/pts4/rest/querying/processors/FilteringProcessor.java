package com.proftaak.pts4.rest.querying.processors;

import com.avaje.ebean.Query;
import com.proftaak.pts4.database.IDatabaseModel;
import com.proftaak.pts4.rest.HTTPException;
import com.proftaak.pts4.rest.querying.QueryData;
import com.proftaak.pts4.rest.querying.QueryParameter;
import com.proftaak.pts4.rest.response.metadata.Metadata;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Michon on 22-6-2015.
 */
public class FilteringProcessor implements IProcessor {
    /**
     * Reserved query parameters that have special meaning and will not be used for filtering
     */
    private static final Collection<String> reservedKeys = Arrays.asList("limit", "page", "sort");

    /**
     * Add filters taken from the URL query to the given query
     */
    @Override
    public <T extends IDatabaseModel> void apply(QueryData queryData, Metadata metadata, Class<T> modelCls, Query<T> query) throws HTTPException {
        // Get the json name -> field name mapping for all queryable fields
        Map<String, String> queryFields = IProcessor.getFieldMapping(modelCls);

        // Apply the query parameters to the query
        for (QueryParameter parameter : queryData.getForUserParameters()) {
            // Ignore the reserved parameters
            if (reservedKeys.contains(parameter.getKey())) {
                continue;
            }

            // Get the field info
            String fieldName = queryFields.get(parameter.getKey());
            if (fieldName == null) {
                metadata.addWarning(String.format("Query parameter for unknown field %s", parameter.getKey()));
                continue;
            }

            // Add the filter
            switch (parameter.getOperator()) {
                case EQ:
                    query.where().eq(fieldName, parameter.getValue());
                    break;
                case NE:
                    query.where().ne(fieldName, parameter.getValue());
                    break;
                case GT:
                    query.where().gt(fieldName, parameter.getValue());
                    break;
                case GE:
                    query.where().ge(fieldName, parameter.getValue());
                    break;
                case LT:
                    query.where().lt(fieldName, parameter.getValue());
                    break;
                case LE:
                    query.where().le(fieldName, parameter.getValue());
                    break;
                case IN:
                    query.where().in(fieldName, (Object[]) StringUtils.split(parameter.getValue(), ','));
                    break;
                case LIKE:
                    query.where().like(fieldName, parameter.getValue());
                    break;
            }
        }
    }
}
