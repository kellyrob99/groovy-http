import org.restlet.data.*

def myPort = builder.getVariable('port')
def server = builder.server(protocol:protocol.HTTP,
        port:myPort){
    restlet(handle:{org.restlet.data.Request req, resp->
        Form form = req.getResourceRef().getQueryAsForm();
        if(!form.size() || !form[0].name == 'string')
        {
            resp.setStatus(Status.CLIENT_ERROR_BAD_REQUEST)
        }
        else{
            resp.setEntity(form[0].value.reverse(), mediaType.TEXT_PLAIN)
        }
    })
}

server.start();

def client = builder.client(protocol:protocol.HTTP)

[client, server] //return a list so we can work with the client and eventually stop the server

