package lt.mediapark.chatmap

class BasicFilters {

    def filters = {
        printRequest(controller: '*', action: '*') {
            before = {
                log.info("DESTINATION: ${request.requestURL.append(request.queryString ?: '')}")
                log.info("Headers: ${request.getHeaderNames().collect { "${it}=${request.getHeader(it)}" }}")
                if (request.JSON) {
                    log.info("JSON: ${request.JSON}")
                }
            }
        }
    }
}
