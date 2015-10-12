package lt.mediapark.chatmap

import grails.converters.JSON
import lt.mediapark.chatmap.chat.ChatMessage
import lt.mediapark.chatmap.utils.Converter

class ChatController {

    def chatService
    def converterService
    def usersService

    static allowedMethods = [
            index: 'GET',
            send : 'POST'
    ]

    def index = {
        def requestorId = params.requestor

        boolean requestorIs1 = params.id1 == requestorId
        //only a user (and one of the members of the chat at that!) can view chat history
        if (requestorId && (params.id1 == requestorId || params.id2 == requestorId)) {

            def user1 = usersService.get(params.id1)
            def user2 = usersService.get(params.id2)
            Long timeLong = Converter.coerceToLong params.time
            Date time = timeLong ? new Date(timeLong) : new Date()
            Integer limit = Integer.parseInt(params.limit ?: "50")

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


    def send = {
        def senderId = params.requestor
        def receiverId = params.id
        if (params.text) {
            def text = URLDecoder.decode(params.text, "UTF-8")
            if (text.startsWith("\"")) text = text.substring(1)
            if (text.endsWith("\"")) text = text.substring(0, text.size() - 1)
            request['text'] = text
        } else if (request.JSON) {
            request['text'] = request.JSON.text
        }
        ChatMessage message = chatService.sendMessage(senderId, receiverId, request)
        def map = converterService.chatMessageToJSON(message)
        render map as JSON
    }
}
