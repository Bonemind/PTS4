package com.proftaak.pts4.rest.querying.processors;

import com.avaje.ebean.Query;
import com.proftaak.pts4.database.IDatabaseModel;
import com.proftaak.pts4.rest.HTTPException;
import com.proftaak.pts4.rest.response.metadata.Metadata;
import com.proftaak.pts4.rest.querying.QueryData;
import com.proftaak.pts4.utils.JSONUtils;
import flexjson.BeanProperty;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michon on 22-6-2015.
 */
public interface IProcessor {
    /**
     * Apply query parameters taken from the URL query to the given query
     *
     * @param requestData    The current request scope
     * @param metadata       The metadata object
     * @param modelCls       The class that is being queried/filtered
     * @param query          The prepared query
     * @param <T>            The class that is being queried/filtered
     * @throws HTTPException If any invalid query parameters are found
     */
    <T extends IDatabaseModel> void apply(QueryData queryData, Metadata metadata, Class<T> modelCls, Query<T> query) throws HTTPException;

    /**
     * Get a mapping of all json field names to the corresponding column names
     *
     * @param modelCls The model class for which to get the mapping
     */
    static <T extends IDatabaseModel> Map<String, String> getFieldMapping(Class<T> modelCls) {
        // Get the json name -> field name mapping for all queryable fields
        Map<String, String> queryFields = new HashMap<>();
        for (BeanProperty property : JSONUtils.getProperties(modelCls)) {
            // Get the column name
            String fieldName = property.getProperty().getName();
            Column columnAnnotation = property.getProperty().getAnnotation(Column.class);
            JoinColumn joinColumnAnnotation = property.getProperty().getAnnotation(JoinColumn.class);
            if (columnAnnotation != null && StringUtils.isNotEmpty(columnAnnotation.name())) {
                fieldName = columnAnnotation.name();
            } else if (joinColumnAnnotation != null && StringUtils.isNotEmpty(joinColumnAnnotation.name())) {
                fieldName = joinColumnAnnotation.name();
            }

            // Store the json name -> field name mapping
            queryFields.put(property.getJsonName(), fieldName);
        }

        return queryFields;
    }
}
