package org.kar.http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: krobinson
 */
class MyEchoHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        String requestMethod = httpExchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("GET")) {
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/plain");
            OutputStream responseBody = httpExchange.getResponseBody();

            final String query = httpExchange.getRequestURI().getRawQuery();
            if (query == null || !query.contains("string")) {
                httpExchange.sendResponseHeaders(400, 0);
                return;
            }

            final String[] param = query.split("=");
            assert param.length == 2 && param[0].equals("string");

            httpExchange.sendResponseHeaders(200, 0);
            responseBody.write(new StringBuffer(param[1]).reverse().toString().getBytes());
            responseBody.close();
        }
    }
}
