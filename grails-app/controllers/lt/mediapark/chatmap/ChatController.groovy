package lt.mediapark.chatmap

import grails.converters.JSON

class ChatController {

    def chatService
    def converterService
    def usersService

    def index() {
        def requestorId = params.requestor

        boolean requestorIs1 = params.id1 == requestorId
        //only a user (and one of the members of the chat at that!) can view chat history
        if (requestorId && (params.id1 == requestorId || params.id2 == requestorId)) {

            def user1 = usersService.get(params.id1)
            def user2 = usersService.get(params.id2)
            Date time = params.time ?: new Date()
            Integer limit = Integer.parseInt(params.msgLmt ?: "50")

            def history = chatService.getChatHistory(
                    requestorIs1 ? user1 : user2,
                    requestorIs1 ? user2 : user1,
                    time,
                    limit
            )

            Collection<Map> messages = history.collect { converterService.chatMessageToJSON(it) }
            render messages as JSON

        } else {
            render 403
        }
    }
}
