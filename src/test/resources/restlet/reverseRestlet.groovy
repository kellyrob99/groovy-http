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
// START SNIPPET Listing7.groovy
import org.restlet.data.*

def myPort = builder.getVariable('port')
def server = builder.server(protocol: protocol.HTTP, port: myPort) {
    restlet(handle: {Request req, Response resp ->
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
// END SNIPPET Listing7.groovy

