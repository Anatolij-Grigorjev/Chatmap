package lt.mediapark.chatmap

import grails.transaction.Transactional
import lt.mediapark.chatmap.chat.ChatMessage

@Transactional
class ChatService {

    ChatMessage getLatestMessage(User requestor, User other) {
        def c = Calendar.getInstance()
        c.setTime(new Date())
        c.add(Calendar.HOUR, -24)
        def last24h = c.time
        def message = ChatMessage.createCriteria().list {
            or {
                and {
                    eq('sender.id', requestor.id)
                    eq('receiver.id', other.id)
                }
                and {
                    eq('sender.id', other.id)
                    eq('receiver.id', requestor.id)
                }
            }
            ge('created', last24h)
            order('sendDate', 'desc')
            maxResults(1)
        }
        if (message) {
            return ((List<ChatMessage>) message).first()
        }
        null
    }

    List<ChatMessage> getChatHistory(User requestor, User other, Date before, int limit) {
        def historyMessages = (List) ChatMessage.createCriteria().list {
            or {
                and {
                    eq('sender.id', requestor.id)
                    eq('receiver.id', other.id)
                }
                and {
                    eq('sender.id', other.id)
                    eq('receiver.id', requestor.id)
                }
            }
            le('created', before)
            order('sendDate', 'asc')
            maxResults(limit)
        }
        def receivedMessages = historyMessages.findAll { ChatMessage message -> requestor == message.receiver }
        receivedMessages.each { ChatMessage msg -> if (!msg.receiveDate) msg.receiveDate = new Date() }
        ChatMessage.saveAll(receivedMessages)

        historyMessages
    }
}
