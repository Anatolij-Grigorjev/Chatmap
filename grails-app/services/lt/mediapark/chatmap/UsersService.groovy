package lt.mediapark.chatmap

import grails.transaction.Transactional
import lt.mediapark.chatmap.utils.Converter

import java.util.concurrent.atomic.AtomicLong

@Transactional
class UsersService {

    AtomicLong usersCounter = new AtomicLong(10)

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
        if (updates?.deviceToken) user?.deviceToken = updates.deviceToken
        if (updates?.lng) user?.lng = updates.lng
        if (updates?.lat) user?.lat = updates.lat

        if (updates?.gender && Gender.isValid(updates.gender))
            user?.gender = Gender.valueOf(updates.gender)
        if (updates?.wantsNofitications != null)
            user?.wantsNotifications = Boolean.valueOf(updates.wantsNotifications.toString())

        user.save(flush: true)
    }

    /**
     * Get a user or make a new default one if not there yet
     * @param uuid the UUID of the (new) user
     * @return the (new) user
     */
    def gatherUser(def uuid) {
        def user = User.findByUuid(uuid)
        if (!user) {
            user = new User(uuid: uuid
                    , emoji: 1
                    , gender: Gender.M
                    , name: 'User-' + usersCounter.getAndIncrement()
            )
        }
        user.save(flush: true)
    }
}
