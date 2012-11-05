package org.kar.http

import spock.lang.Specification

/**
 * Testing different ways Groovy can interact with HTTP.
 */
class GroovyHttpTest extends Specification {

    private static final String HELLO_WORLD_HTML = '''\
<html>
  <body>
    <p>hello world</p>
  </body>
</html>'''

    private static final String POST_RESPONSE = 'Successfully posted [arg:[foo]] with method POST\n'

    static int httpPort
    static String appName

    def setupSpec() {
        httpPort = System.properties['httpPort'] as int
        appName = System.properties['appName']
    }

    def "from a String to an HTTP GET"() {
        when:
        String html = makeURL('helloWorld.groovy').toURL().text

        then:
        html == HELLO_WORLD_HTML
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

    def "POST from a URLConnection"(){
        when:
        final HttpURLConnection connection = makeURL('post.groovy').toURL().openConnection()
        connection.setDoOutput(true)
        connection.outputStream.withWriter {Writer writer ->
            writer << "arg=foo"
        }

        String response
        connection.inputStream.withReader {Reader reader ->
            response =  reader.text
        }

        then:
        connection.responseCode == 200
        response == POST_RESPONSE
    }



    private static String makeURL(String page) {
        "http://localhost:$httpPort/$appName/$page"
    }
}
