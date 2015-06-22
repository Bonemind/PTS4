package com.proftaak.pts4.rest.querying.processors;

import com.avaje.ebean.OrderBy;
import com.avaje.ebean.Query;
import com.proftaak.pts4.database.IDatabaseModel;
import com.proftaak.pts4.rest.HTTPException;
import com.proftaak.pts4.rest.querying.Operator;
import com.proftaak.pts4.rest.querying.QueryData;
import com.proftaak.pts4.rest.response.metadata.Metadata;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by Michon on 22-6-2015.
 */
public class SortProcessor implements IProcessor {
    /**
     * Add sort taken from the URL query to the given query
     */
    @Override
    public <T extends IDatabaseModel> void apply(QueryData queryData, Metadata metadata, Class<T> modelCls, Query<T> query) throws HTTPException {
        // Get the json name -> field name mapping for all queryable fields
        Map<String, String> queryFields = IProcessor.getFieldMapping(modelCls);

        // Create the OrderBy object
        OrderBy<T> orderBy = new OrderBy<>();

        // Get the sort fields
        String sort = queryData.getStringValueForOp("sort", Operator.EQ, "");
        for (String sortPart : StringUtils.split(sort, ',')) {
            // Get field name/direction
            String sortField = sortPart;
            boolean isAscending = true;
            if (sortField.startsWith("+") || sortField.startsWith("-")) {
                isAscending = sortField.startsWith("+");
                sortField = sortField.substring(1);
            }

            // Get the field name
            if (!queryFields.containsKey(sortField)) {
                metadata.addWarning(String.format("Sort contains unknown field %s", sortField));
                continue;
            }
            sortField = queryFields.get(sortField);

            // Apply the sort.
            if (isAscending) {
                orderBy.asc(sortField);
            } else {
                orderBy.desc(sortField);
            }
        }

        // Apply the OrderBy object
        query.setOrderBy(orderBy);
    }
}
