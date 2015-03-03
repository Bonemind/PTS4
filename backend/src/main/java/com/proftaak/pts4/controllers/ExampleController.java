package com.proftaak.pts4.controllers;

import com.proftaak.pts4.core.BaseController;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;

/**
 * @author Michon
 */
public class ExampleController extends BaseController {
    /**
     * GET /example
     */
    public Map<String, Object> getHandler() throws Exception {
        throw new NotImplementedException();
    }

    /**
     * GET /example/1
     */
    public Map<String, Object> getHandler(String urlParam) throws Exception {
        throw new NotImplementedException();
    }

    /**
     * POST /example
     */
    public Map<String, Object> postHandler(Map<String, Object> data) throws Exception {
        throw new NotImplementedException();
    }

    /**
     * PUT /example/1
     */
    public Map<String, Object> putHandler(Map<String, Object> data, String urlParam) throws Exception {
        throw new NotImplementedException();
    }

    /**
     * DELETE /example/1
     */
    public Map<String, Object> deleteHandler(String urlParam) throws Exception {
        throw new NotImplementedException();
    }
}
