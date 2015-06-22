package com.proftaak.pts4.rest.response;

import com.avaje.ebean.Query;
import com.proftaak.pts4.database.IDatabaseModel;
import com.proftaak.pts4.rest.HTTPException;
import com.proftaak.pts4.rest.RequestData;
import com.proftaak.pts4.rest.querying.QueryData;
import com.proftaak.pts4.rest.querying.processors.FilteringProcessor;
import com.proftaak.pts4.rest.querying.processors.IProcessor;
import com.proftaak.pts4.rest.querying.processors.PaginationProcessor;
import com.proftaak.pts4.rest.querying.processors.SortProcessor;
import org.apache.commons.collections4.IteratorUtils;

import java.util.*;
import java.util.function.Function;

/**
 * Created by Michon on 17-6-2015.
 */
public class ResponseFactory {
    /**
     * The processors, in order of use
     */
    private static IProcessor[] processors = new IProcessor[] {
            new FilteringProcessor(),
            new SortProcessor(),
            new PaginationProcessor(),
    };

    /**
     * Convert a query into a list, using filters and pagination from the query parameters of the URL
     *
     * @param  requestData The current request context
     * @param  modelCls    The model class that is being queried/filtered
     * @param  query       The prepared query
     * @param  <T>         The model class that is being queried/filtered
     * @return The JSON response with the results, and all relevant metadata
     * @throws HTTPException If any of the URL's query parameters are invalid
     */
    public static <T extends IDatabaseModel> JSONResponse<Collection<T>> queryToList(RequestData requestData, Class<T> modelCls, Query<T> query) throws HTTPException {
        return ResponseFactory.queryToList(requestData, modelCls, query, (u) -> u);
    }

    /**
     * Like queryToList(RequestData, Class<T>, Query<T>), but with the collection mapped through a function
     *
     * What this essentially means is that each of the items in the resulting list will be fed to a mapping function,
     * and the returned value is used instead
     *
     * @param mapper The mapper function
     * @param <U>    The resulting type returned by the mapper function
     */
    public static <T extends IDatabaseModel, U> JSONResponse<Collection<U>> queryToList(
            RequestData requestData, Class<T> modelCls, Query<T> query, Function<T, U> mapper
    ) throws HTTPException {
        // Prepare the response
        JSONResponse<Collection<U>> response = new JSONResponse<>();

        // Get the query data
        QueryData queryData = QueryData.fromRequestData(requestData);

        // Apply the processors
        for (IProcessor processor : processors) {
            processor.apply(queryData, response.getMetadata(), modelCls, query);
        }

        // Set the response body
        response.setResponse(IteratorUtils.toList(query.findList().stream().map(mapper).iterator()));

        return response;
    }
}
