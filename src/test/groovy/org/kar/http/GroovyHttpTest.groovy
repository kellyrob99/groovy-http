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

    static int httpPort
    static String appName

    def setupSpec() {
        httpPort = System.properties['httpPort'] as int
        appName = System.properties['appName']
    }

    def "from a String to an HTTP GET"() {
        when:
        String html = "http://localhost:$httpPort/$appName/helloWorld.groovy".toURL().text

        then:
        html == HELLO_WORLD_HTML
    }

    def "from a String to URLConnection"() {
        when:
        String html
        "http://localhost:$httpPort/$appName/helloWorld.groovy".toURL().openConnection().inputStream.withReader { Reader reader ->
            html = reader.text
        }

        then:
        html == HELLO_WORLD_HTML
    }

    def "POST from a URLConnection"(){

    }
}
