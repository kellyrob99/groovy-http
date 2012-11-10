package org.kar.http

import groovyx.net.http.HTTPBuilder
import spock.lang.Shared
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

import static groovyx.net.http.ContentType.TEXT
/**
 * Testing different ways Groovy can help to interact with HTTP as a client.
 */
class GroovyHttpClientTest extends Specification {

    private static final String HELLO_WORLD_HTML = '''\
<html>
  <body>
    <p>hello world</p>
  </body>
</html>'''

    private static final String POST_RESPONSE = 'Successfully posted [arg:[foo]] with method POST'

    static int httpPort
    static String appName

    @Shared
    static HTTPBuilder http

    def setupSpec() {
        httpPort = System.properties['httpPort'] as int
        appName = System.properties['appName']
        http = new HTTPBuilder(makeURL(''))
    }

    def "from a String to an HTTP GET"() {
        when:
        String html = makeURL('helloWorld.groovy').toURL().text

        then:
        html == HELLO_WORLD_HTML
    }

    def "from a String to an HTTP GET, HttpServletResponse.SC_NOT_FOUND will result in FileNotFoundException"() {
        when:
        String html = 'http://google.com/notThere'.toURL().text

        then:
        def e = thrown(FileNotFoundException)
    }

    def "from a String to an HTTP GET with a bad url will throw MalformedURLException"() {
        when:
        String html = 'htp://foo.com'.toURL().text

        then:
        def e = thrown(MalformedURLException)
    }


    def "from a String to URLConnection"() {
        when:
        String html
        makeURL('helloWorld.groovy').toURL().openConnection().inputStream.withReader { Reader reader ->
            html = reader.text
        }

        then:
        html == HELLO_WORLD_HTML
    }

    def "from a String to URLConnection with error handling"() {
        when:
        String html
        final HttpURLConnection connection = makeURL('notThere.groovy').toURL().openConnection()
        connection.inputStream.withReader { Reader reader ->
            html = reader.text
        }

        then:
        connection.responseCode == HttpServletResponse.SC_NOT_FOUND
        def e = thrown(FileNotFoundException)
    }

    def "POST from a URLConnection"() {
        when:
        final HttpURLConnection connection = makeURL('post.groovy').toURL().openConnection()
        connection.setDoOutput(true)
        connection.outputStream.withWriter { Writer writer ->
            writer << "arg=foo"
        }

        String response
        connection.inputStream.withReader { Reader reader ->
            response = reader.text
        }

        then:
        connection.responseCode == HttpServletResponse.SC_OK
        response == POST_RESPONSE
    }

    def "GET with HTTPBuilder"() {
        when:
        String html
        int responseStatus
        http.get(path: 'helloWorld.groovy', contentType: TEXT) { resp, reader ->
            html = reader.text
            responseStatus = resp.status
        }

        then:
        html == HELLO_WORLD_HTML
        responseStatus == HttpServletResponse.SC_OK
    }

    def "GET with HTTPBuilder and error handling"() {
        when:
        int responseStatus
        http.handler.failure = { resp ->
            responseStatus = resp.status
        }
        http.get(path: 'notThere.groovy', contentType: TEXT) { resp, reader ->
            throw new IllegalStateException('should not be executed')
        }

        then:
        responseStatus == HttpServletResponse.SC_NOT_FOUND
    }

    def "POST with HTTPBuilder"() {
        when:
        String response
        int responseStatus

        http.post(path: 'post.groovy', body: [arg: 'foo']) { resp, reader ->
             responseStatus = resp.status
             response = reader.text()
        }

        then:
        responseStatus == HttpServletResponse.SC_OK
        response == POST_RESPONSE
    }

    def "POST reverse example"(){
        when:
        String response
        int responseStatus

        final String foo = 'foo bar'
        http.post(path: 'reverse.groovy', body: [string: foo]) { resp, reader ->
            responseStatus = resp.status
            response = reader.text()
        }

        then:
        responseStatus == HttpServletResponse.SC_OK
        response == foo.reverse()
    }

    private static String makeURL(String page) {
        "http://localhost:$httpPort/$appName/$page"
    }
}
