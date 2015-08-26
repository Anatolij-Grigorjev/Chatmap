package lt.mediapark.chatmap.chat

import lt.mediapark.chatmap.Picture
import lt.mediapark.chatmap.User

class ChatMessage {

    static constraints = {
        sender nullable: false
        receiver nullable: false
        picture nullable: true
    }

    User sender
    User receiver

    Date sendDate
    Date receiveDate
    Date created = new Date()

    def isReceived() { !!receiveDate }

    String text
    Picture picture

}
