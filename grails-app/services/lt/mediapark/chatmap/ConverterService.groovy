package lt.mediapark.chatmap

import grails.transaction.Transactional
import lt.mediapark.chatmap.chat.ChatMessage
import lt.mediapark.chatmap.utils.DistanceCalc

@Transactional
class ConverterService {

    def chatService

    Map userToJSONForChat(User user, User relation) {
        def map = msgUserToJSON(user)
        if (user?.hasLocation() && relation?.hasLocation()) {
            map['distance'] = DistanceCalc.getHavershineDistance(user, relation)
        }
        def message = chatService.getLatestMessage(relation, user)
        if (message) {
            map['latestMessage'] = chatMessageToJSON(message)
        }
        map
    }

    Map userToJSONForMap(User user) {
        def map = msgUserToJSON(user)
        if (user?.lat) map['lat'] = user.lat
        if (user?.lng) map['lng'] = user.lng

        map
    }

    Map pictureToJSONForMap(Picture picture) {
        def map = [:]
        if (picture?.id) map['id'] = picture.id
        if (picture?.lat) map['lat'] = picture.lat
        if (picture?.lng) map['lng'] = picture.lng

        map
    }


    Map chatMessageToJSON(ChatMessage chatMessage) {
        def map = [:]
        if (chatMessage?.id) map['id'] = chatMessage.id
        if (chatMessage?.receiver) map['receiver'] = msgUserToJSON(chatMessage.receiver)
        if (chatMessage?.sender) map['sender'] = msgUserToJSON(chatMessage.sender)
        if (chatMessage?.sendDate) map['sendDate'] = chatMessage.sendDate
        if (chatMessage?.receiveDate) map['receiveDate'] = chatMessage.receiveDate
        if (chatMessage?.text) map['text'] = chatMessage.text
        if (chatMessage?.picture) map['picId'] = chatMessage.picture.id

        map
    }

    Map msgUserToJSON(User user) {
        def map = [:]
        if (user?.name) map['name'] = user.name
        if (user?.id) map['id'] = user.id
        if (user?.emoji) map['emoji'] = user.emoji
        if (user?.gender) map['gender'] = user.gender.toString()
        map
    }
}
