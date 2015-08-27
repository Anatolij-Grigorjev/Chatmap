package lt.mediapark.chatmap

import grails.transaction.Transactional
import lt.mediapark.chatmap.utils.Converter

@Transactional
class UsersService {

    def User get(def anyUserId) {
        Long userId = Converter.coerceToLong(anyUserId)
        def user = User.get(userId)
//        if (loggedInUsers.containsKey(userId)) {
//            log.info("Fetching ${userId} from cache!")
//            user = User.get(userId)
//        }
//        if (!userId && canBeOffline) {
//            log.info("Fetching ${userId} from storage!")
//            user = User.findById(userId)
//        }
        user
    }

    User updateUser(User user, Map updates) {
        if (updates?.name) user?.name = updates.name
        if (updates?.emoji) user?.emoji = updates.emoji
        if (updates?.gender
                && Gender.values().any { it.toString() == updates.gender })
            user?.gender = Gender.valueOf(updates.gender.toString())
        if (updates?.deviceToken) user?.deviceToken = updates.deviceToken
        if (updates?.lng) user?.lng = updates.lng
        if (updates?.lat) user?.lat = updates.lat
        if (updates?.wantsNofitications != null)
            user?.wantsNotifications = Boolean.valueOf(updates.wantsNotifications.toString())

        user.save(flush: true)
    }
}
