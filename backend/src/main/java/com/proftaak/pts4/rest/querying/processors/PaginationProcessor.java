package com.proftaak.pts4.rest.querying.processors;

import com.avaje.ebean.Query;
import com.proftaak.pts4.database.IDatabaseModel;
import com.proftaak.pts4.rest.HTTPException;
import com.proftaak.pts4.rest.querying.Operator;
import com.proftaak.pts4.rest.querying.QueryData;
import com.proftaak.pts4.rest.response.metadata.Metadata;
import com.proftaak.pts4.rest.response.metadata.PaginationMetadata;

/**
 * Created by Michon on 22-6-2015.
 */
public class PaginationProcessor implements IProcessor {
    /**
     * The default limit
     */
    private static final int LIMIT_DEFAULT = 25;

    /**
     * The maximum limit
     */
    private static final int LIMIT_MAX = 50;

    /**
     * Parse a parameter for an integer value, and bind it between an upper and lower limit
     *
     * @param queryData    The query data
     * @param metadata     The metadata object on which to set warnings
     * @param paramName    The parameter name
     * @param defaultValue The default value
     * @param minValue     The lower limit
     * @param maxValue     The upper limit
     * @return The integer value
     */
    private static int parseAndBound(QueryData queryData, Metadata metadata, String paramName, int defaultValue, int minValue, int maxValue) {
        String paramText = queryData.getStringValueForOp(paramName, Operator.EQ, String.valueOf(defaultValue));
        try {
            int value = Integer.parseInt(paramText);
            if (value < minValue) {
                metadata.addWarning(String.format(
                        "%s cannot be lower than %d",
                        paramName, minValue
                ));
                value = minValue;
            } else if (value > maxValue) {
                metadata.addWarning(String.format(
                        "%s cannot be higher than %d",
                        paramName, maxValue
                ));
                value = maxValue;
            }
            return value;
        } catch (NumberFormatException ignored) {
            metadata.addWarning(String.format(
                    "Invalid value %s for %s",
                    paramText, paramText
            ));
            return defaultValue;
        }
    }

    /**
     * Add pagination taken from the URL query to the given query
     */
    @Override
    public <T extends IDatabaseModel> void apply(QueryData queryData, Metadata metadata, Class<T> modelCls, Query<T> query) throws HTTPException {
        // Get the amount of matches
        int count = query.findRowCount();

        // Get pagination parameters
        int limit = PaginationProcessor.parseAndBound(queryData, metadata, "limit", LIMIT_DEFAULT, 1, LIMIT_MAX);
        int pages = (int) Math.ceil(((double) count) / limit);
        int page = PaginationProcessor.parseAndBound(queryData, metadata, "page", 1, 1, pages);

        // Apply pagination to the query
        query.setMaxRows(limit);
        query.setFirstRow((page - 1) * limit);

        // Set pagination metadata
        PaginationMetadata paginationMetadata = new PaginationMetadata();
        paginationMetadata.setCount(count);
        paginationMetadata.setLimit(limit);
        paginationMetadata.setPage(page);
        paginationMetadata.setPages(pages);
        metadata.setPaginationMetadata(paginationMetadata);
    }
}
