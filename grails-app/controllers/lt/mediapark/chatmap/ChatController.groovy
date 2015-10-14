package lt.mediapark.chatmap

import grails.converters.JSON
import lt.mediapark.chatmap.chat.ChatMessage
import lt.mediapark.chatmap.utils.Converter
import org.springframework.web.multipart.MultipartRequest

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
        if (ChatService.GLOBAL_CHAT_USER_ID.equals(Converter.coerceToLong(senderId))) {
            return render(status: 400, text: "The Global Chat User is a concept, not a person! It cannot send messages.")
        }
        def receiverId = params.id
        if (params.text) {
            def text = URLDecoder.decode(params.text, "UTF-8")
            if (text.startsWith("\"")) text = text.substring(1)
            if (text.endsWith("\"")) text = text.substring(0, text.size() - 1)
            request['text'] = text
        } else if (request.JSON) {
            request['text'] = request.JSON.text
        }
        if (params.lat && params.lng) {
            request['lat'] = Double.parseDouble(params.lat.toString())
            request['lng'] = Double.parseDouble(params.lng.toString())
        }
        if (!request['text'] && !(request instanceof MultipartRequest && request.getFile("picture"))) {
            return render(status: 400, text: "The request did not contain either text or an image. Empty messages " +
                    "not permitted.")
        }
        ChatMessage message = chatService.sendMessage(senderId, receiverId, request)
        def map = converterService.chatMessageToJSON(message)
        render map as JSON
    }
}
