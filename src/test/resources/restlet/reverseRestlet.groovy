import org.restlet.data.*

def myPort = builder.getVariable('port')
def server = builder.server(protocol: protocol.HTTP, port: myPort) {
    restlet(handle: {org.restlet.data.Request req, resp ->
        Form form = req.resourceRef.queryAsForm
        if (form.isEmpty() || !form[0].name == 'string') {
            resp.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Missing 'string' param")
        }
        else {
            resp.setEntity(form[0].value.reverse(), mediaType.TEXT_PLAIN)
        }
    })
}

server.start();

def client = builder.client(protocol: protocol.HTTP)

[client, server] //return a list so we can work with the client and eventually stop the server

