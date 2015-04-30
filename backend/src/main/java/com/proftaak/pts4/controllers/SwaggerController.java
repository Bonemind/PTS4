package com.proftaak.pts4.controllers;

import com.proftaak.pts4.rest.HTTPMethod;
import com.proftaak.pts4.rest.RequestData;
import com.proftaak.pts4.rest.annotations.Controller;
import com.proftaak.pts4.rest.annotations.Route;
import flexjson.JSONDeserializer;
import org.glassfish.grizzly.http.util.Header;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * @author Michon
 */
@Controller
public class SwaggerController {
    private static final String SWAGGER_FILE = "swagger/swagger.json";
    private static Map<String, Object> data;

    /**
     * GET /swagger.json
     */
    @Route(method = HTTPMethod.GET, path = "/swagger.json")
    public static Object swaggerSpecHandler(RequestData requestData) {
        if (SwaggerController.data == null) {
            // First request, read the swagger data.
            JSONDeserializer deserializer = new JSONDeserializer();
            InputStream is = SwaggerController.class.getClassLoader().getResourceAsStream(SWAGGER_FILE);
            InputStreamReader isr = new InputStreamReader(is);
            SwaggerController.data = (Map<String, Object>) deserializer.deserialize(isr);

            // Inject the host
            SwaggerController.data.put("host", requestData.getRequest().getHeader(Header.Host));
        }

        return SwaggerController.data;
    }
}
