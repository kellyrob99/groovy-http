package org.kar.http

import spock.lang.Specification
import spock.lang.Shared
import com.sun.net.httpserver.HttpServer
import org.eclipse.jetty.server.*
import java.util.concurrent.Executors
import javax.servlet.http.HttpServletResponse
import org.eclipse.jetty.servlet.ServletContextHandler
import groovy.servlet.GroovyServlet
import org.lpny.groovyrestlet.GroovyRestlet
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.http.HttpServer

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
    private static final String MISSING_STRING_PARAM = "Missing 'string' param"

    @Shared
    com.sun.net.httpserver.HttpServer httpServer

    @Shared
    Server jettyServer

    def setupSpec() {
        // http://www.java2s.com/Tutorial/Java/0320__Network/LightweightHTTPServer.htm
        //configuring a Java 6 HttpServer
        InetSocketAddress addr = new InetSocketAddress(HTTP_SERVER_PORT);
        httpServer = com.sun.net.httpserver.HttpServer.create(addr, 0);
        httpServer.createContext("/", new MyEchoHandler());
        httpServer.setExecutor(Executors.newCachedThreadPool());
        httpServer.start();

        //configuring Jetty 8 with GroovyServlet support
        jettyServer = new Server(JETTY_SERVER_PORT)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        jettyServer.setHandler(context);
        context.resourceBase = 'src/main/webapp'
        context.addServlet(GroovyServlet, '*.groovy')
        jettyServer.start()
    }

    def "HttpServer reverse test"() {
        when:
        def response = "$HTTP_SERVER_HOST?string=$TEST_STRING".toURL().text

        then:
        response == TEST_STRING.reverse()
    }

    def "HttpServer missing params test"() {
        when:
        String html
        final HttpURLConnection connection = HTTP_SERVER_HOST.toURL().openConnection()
        connection.inputStream.withReader { Reader reader ->
            html = reader.text
        }

        then:
        connection.responseCode == HttpServletResponse.SC_BAD_REQUEST
        def e = thrown(IOException)
    }

    def "JettyServer reverse test"() {
        when:
        def response = "$JETTY_SERVER_HOST/reverse.groovy?string=$TEST_STRING".toURL().text

        then:
        response == TEST_STRING.reverse()
    }

    def "JettyServer missing params test"() {
        when:
        String html
        final HttpURLConnection connection = "$JETTY_SERVER_HOST/reverse.groovy".toURL().openConnection()
        connection.inputStream.withReader { Reader reader ->
            html = reader.text
        }

        then:
        connection.responseCode == HttpServletResponse.SC_BAD_REQUEST
        def e = thrown(IOException)
    }

    def "restlet"() {
        when:
        GroovyRestlet gr = new GroovyRestlet()
        gr.builder.setVariable('port', RESTLET_SERVER_PORT)
        def (client, server) = gr.build(new File('src/test/resources/reverseRestlet.groovy').toURI())

        then:
        TEST_STRING.reverse() == client.get("http://localhost:$RESTLET_SERVER_PORT/?string=$TEST_STRING").entity.text
        server.stop()
    }

    def "restlet failure"() {
        when:
        GroovyRestlet gr = new GroovyRestlet()
        gr.builder.setVariable('port', RESTLET_SERVER_PORT)
        def (client, server) = gr.build(new File('src/test/resources/reverseRestlet.groovy').toURI())
        org.restlet.data.Response response = client.get("http://localhost:$RESTLET_SERVER_PORT")

        then:
        response.status.code == 400
        null == response.entity.text
        server.stop()
    }

    def "embedded vert.x"() {
        when:
        Vertx vertx = Vertx.newVertx()
        final HttpServer server = vertx.createHttpServer()
        server.requestHandler { req ->
            if (req.params.get('string') == null) {
                req.response.with {
                    statusCode = 400
                    statusMessage = MISSING_STRING_PARAM
                    end()
                }
            }
            else {
                req.response.end(req.params['string'].reverse())
            }

        }.listen(8083, 'localhost')

        def client = vertx.createHttpClient(port: 8083, host: 'localhost')

        then:
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
    }

    def cleanupSpec() {
        httpServer.stop(0)
        jettyServer.stop()
    }
}
