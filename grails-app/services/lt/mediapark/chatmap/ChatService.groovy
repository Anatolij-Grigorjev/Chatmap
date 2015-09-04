package lt.mediapark.chatmap

import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import grails.transaction.Transactional
import lt.mediapark.chatmap.chat.ChatMessage
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartRequest

import javax.servlet.http.HttpServletRequest

@Transactional
class ChatService {

    def usersService
    public static final Integer MAX_MESSAGE_CHARS = 50

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
        def historyMessages = ChatMessage.createCriteria().list {
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
            order('sendDate', 'desc')
            maxResults(limit)
        } as List<ChatMessage>
        historyMessages.reverse(true)
        def receivedMessages = historyMessages.findAll { ChatMessage message -> requestor == message.receiver }
        receivedMessages.each { ChatMessage msg -> if (!msg.receiveDate) msg.receiveDate = new Date() }
        ChatMessage.saveAll(receivedMessages)

        historyMessages
    }

    ChatMessage sendMessage(def senderId, def receiverId, HttpServletRequest request) {
        User sender = usersService.get(senderId)
        User receiver = usersService.get(receiverId)
        String text = request['text']
        Picture picture = null
        if (request instanceof MultipartRequest) {
            MultipartFile file = request.getFile("picture")
            picture = new Picture(name: file.name, data: file.bytes)
            picture = picture.save()
        }
        ChatMessage message = new ChatMessage(sender: sender, receiver: receiver, text: text, picture: picture)
        message.sendDate = new Date()
        message.save(flush: true)
        if (receiver.deviceToken && receiver.wantsNotifications && this.apnsManager) {
            sendNotification(receiver.deviceToken) { ApnsPayloadBuilder builder ->
                String photoText = message.picture ? '[PHOTO]' : ''
                boolean smallText = (photoText + text).size() < MAX_MESSAGE_CHARS
                //total message cannot exceed 250 bytes
                builder.with {
                    alertBody = "${sender.name}: "
                    +photoText
                    +"${text.subSequence(0, smallText ? text.size() : MAX_MESSAGE_CHARS - photoText.length())}"
                    +"${smallText ? '' : '...'}" //50-53 bytes max
                    addCustomProperty('senderId', sender.id) //8 + 8 bytes
                    addCustomProperty('senderGender', sender.gender.literal) //12 + 1 bytes
                    addCustomProperty('senderEmoji', sender.emoji) //11 + 4 bytes
                }
            }
        }
    }
}
