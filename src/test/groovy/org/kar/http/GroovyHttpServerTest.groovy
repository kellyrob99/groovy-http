/**
 * Copyright (C) 20012 by Kelly Robinson
 * http://www.kellyrob99.com/
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.kar.http

import groovy.servlet.GroovyServlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.lpny.groovyrestlet.GroovyRestlet
import org.vertx.groovy.core.Vertx

import java.util.concurrent.Executors
import javax.servlet.http.HttpServletResponse

import spock.lang.*
import org.vertx.groovy.core.http.HttpClientRequest

/**
 * Created with IntelliJ IDEA.
 * User: krobinson
 */
class GroovyHttpServerTest extends Specification {

    static final int HTTP_SERVER_PORT = 8090
    static final String HTTP_SERVER_HOST = "http://localhost:$HTTP_SERVER_PORT/"
    static final int JETTY_SERVER_PORT = 8091
    static final String JETTY_SERVER_HOST = "http://localhost:$JETTY_SERVER_PORT"
    static final int RESTLET_SERVER_PORT = 8092
    static final String TEST_STRING = 'foobar'
    static final String MISSING_STRING_PARAM = "Missing 'string' param"
    static final int VERTX_PORT = 8083

    @Shared com.sun.net.httpserver.HttpServer httpServer
    @Shared org.eclipse.jetty.server.Server jettyServer
    @Shared org.restlet.Server restletServer
    @Shared org.restlet.Client restletClient

    def setupSpec() {
        // http://www.java2s.com/Tutorial/Java/0320__Network/LightweightHTTPServer.htm
        // START SNIPPET Listing1.groovy
        //configuring a Java 6 HttpServer
        InetSocketAddress addr = new InetSocketAddress(HTTP_SERVER_PORT)
        httpServer = com.sun.net.httpserver.HttpServer.create(addr, 0)
        httpServer.with {
            createContext('/', new ReverseHandler())
            createContext('/groovy/', new GroovyReverseHandler())
            setExecutor(Executors.newCachedThreadPool())
            start()
        }
        // END SNIPPET Listing1.groovy

        // START SNIPPET Listing3.groovy
        //configuring Jetty 8 with GroovyServlet support
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS)
        context.with {
            contextPath = '/'
            resourceBase = 'src/main/webapp'
            addServlet(GroovyServlet, '*.groovy')
        }
        jettyServer = new Server(JETTY_SERVER_PORT)
        jettyServer.with {
            setHandler(context)
            start()
        }
        // END SNIPPET Listing3.groovy

        // START SNIPPET Listing6.groovy
        //configuring a Restlet Server and Client using an external dsl file
        GroovyRestlet gr = new GroovyRestlet()
        gr.builder.setVariable('port', RESTLET_SERVER_PORT)
        (restletClient, restletServer) = gr.build(new File('src/test/resources/restlet/reverseRestlet.groovy').toURI()) as List
        // END SNIPPET Listing6.groovy
    }

    def "HttpServer reverse test"() {
        when: 'We execute a GET request against HttpServer'
        def response = "$HTTP_SERVER_HOST?string=$TEST_STRING".toURL().text

        then: 'We get the same text back in reverse'
        response == TEST_STRING.reverse()
    }

    def "HttpServer missing params test"() {
        when: 'We forget to include the required parameter to HttpServer'
        String html
        final HttpURLConnection connection = HTTP_SERVER_HOST.toURL().openConnection()
        connection.inputStream.withReader { Reader reader ->
            html = reader.text
        }

        then: 'An exception is thrown and we get an HTTP 400 response'
        connection.responseCode == HttpServletResponse.SC_BAD_REQUEST
        def e = thrown(IOException)
    }

    def "HttpServer Groovy handler reverse test"() {
        when: 'We execute a GET request against HttpServer'
        def response = "$HTTP_SERVER_HOST/groovy/?string=$TEST_STRING".toURL().text

        then: 'We get the same text back in reverse'
        response == TEST_STRING.reverse()
    }

    def "HttpServer Groovy handler missing params test"() {
        when: 'We forget to include the required parameter to HttpServer'
        String html
        final HttpURLConnection connection = "$HTTP_SERVER_HOST/groovy/".toURL().openConnection()
        connection.inputStream.withReader { Reader reader ->
            html = reader.text
        }

        then: 'An exception is thrown and we get an HTTP 400 response'
        connection.responseCode == HttpServletResponse.SC_BAD_REQUEST
        def e = thrown(IOException)
    }

    def "JettyServer reverse test"() {
        when: 'We execute a GET request against a JettyServer hosted Groovlet'
        def response = "$JETTY_SERVER_HOST/reverse.groovy?string=$TEST_STRING".toURL().text

        then: 'We get the same text back in reverse'
        response == TEST_STRING.reverse()
    }

    def "JettyServer missing params test"() {
        when: 'We forget to include the required parameter to JettyServer'
        String html
        final HttpURLConnection connection = "$JETTY_SERVER_HOST/reverse.groovy".toURL().openConnection()
        connection.inputStream.withReader { Reader reader ->
            html = reader.text
        }

        then: 'An exception is thrown and we get an HTTP 400 response'
        connection.responseCode == HttpServletResponse.SC_BAD_REQUEST
        def e = thrown(IOException)
    }

    // START SNIPPET Listing8.groovy
    def "restlet"() {
        when: 'We use the Restlet Client to execute a GET request against the Restlet Server'
        String response = restletClient.get("http://localhost:$RESTLET_SERVER_PORT/?string=$TEST_STRING").entity.text

        then: 'We get the same text back in reverse'
        TEST_STRING.reverse() == response
    }
    // END SNIPPET Listing8.groovy

    // START SNIPPET Listing9.groovy
    def "restlet failure"() {
        when: 'We forget to include the required parameter to Restlet'
        org.restlet.data.Response response = restletClient.get("http://localhost:$RESTLET_SERVER_PORT")

        then: 'An exception is thrown and we get an HTTP 400 response indicated as a client error'
        response.status.isClientError()
        !response.status.isServerError()
        response.status.code == 400
        response.status.description == MISSING_STRING_PARAM
        null == response.entity.text
    }
    // END SNIPPET Listing9.groovy

    def "embedded vert.x"() {
        when: 'We run a vert.x server and create a matching vert.x client'
        // START SNIPPET Listing10.groovy
        Vertx vertx = Vertx.newVertx()
        final org.vertx.groovy.core.http.HttpServer server = vertx.createHttpServer()
        server.requestHandler { HttpClientRequest req ->
            if (req.params['string'] == null) {
                req.response.with {
                    statusCode = 400
                    statusMessage = MISSING_STRING_PARAM
                    end()
                }
            }
            else {
                req.response.end(req.params['string'].reverse())
            }

        }.listen(VERTX_PORT, 'localhost')
        // END SNIPPET Listing10.groovy

        // START SNIPPET Listing11.groovy
        def client = vertx.createHttpClient(port: VERTX_PORT, host: 'localhost')
        // END SNIPPET Listing11.groovy

        then: 'We get our standard error and success'
        // START SNIPPET Listing12.groovy

        client.getNow("/") { resp ->
            400 == resp.statusCode
            MISSING_STRING_PARAM == resp.statusMessage
        }

        client.getNow("/?string=$TEST_STRING") { resp ->
            200 == resp.statusCode
            resp.dataHandler { buffer ->
                TEST_STRING.reverse() == buffer.toString()
            }
        }
        // END SNIPPET Listing12.groovy

        cleanup:
        server.close()
    }

    def cleanupSpec() {
        httpServer.stop(0)
        jettyServer.stop()
        restletServer.stop()
    }
}
