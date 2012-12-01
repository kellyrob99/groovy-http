// START SNIPPET Listing5.groovy
@Grab('org.eclipse.jetty.aggregate:jetty-all-server:8.1.0.v20120127')
import org.eclipse.jetty.servlet.ServletContextHandler
import groovy.servlet.GroovyServlet
import org.eclipse.jetty.server.Server

int JETTY_SERVER_PORT = 8094
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
// END SNIPPET Listing5.groovy