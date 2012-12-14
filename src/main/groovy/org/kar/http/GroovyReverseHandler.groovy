/**
 * Copyright (C) 2012 by Kelly Robinson
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

import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpExchange

class GroovyReverseHandler implements HttpHandler {
    @Override
    void handle(HttpExchange httpExchange) {
        if (httpExchange.requestMethod == 'GET') {
            httpExchange.responseHeaders.set('Content-Type', 'text/plain')
            final String query = httpExchange.requestURI.rawQuery

            if(!query || !query.contains('string')){
                httpExchange.sendResponseHeaders(400,0)
                return
            }

            final String[] param = query.split('=')
            assert param.length == 2 && param[0] == 'string'

            httpExchange.sendResponseHeaders(200, 0)
            httpExchange.responseBody.write(param[1].reverse().bytes)
            httpExchange.responseBody.close()
        }
    }
}
