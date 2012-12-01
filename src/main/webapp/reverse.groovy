// START SNIPPET Listing4.groovy
import javax.servlet.http.HttpServletResponse

final string = request.parameterMap.string
if (!string || string.size() != 1){
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST)
    return
}
print URLDecoder.decode(string[0], 'UTF-8').reverse()
// END SNIPPET Listing4.groovy