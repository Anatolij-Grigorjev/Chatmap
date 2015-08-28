package lt.mediapark.chatmap

import grails.transaction.Transactional
import lt.mediapark.chatmap.utils.DistanceCalc

@Transactional
class MapService {

    Collection<User> getChainFor(User me) {

        def allUsers = User.all
        def usersChain = getChainRecur(me, allUsers)

        //chain acquired, time to find chain center


    }

    private Collection<User> getChainRecur(User user, List<User> allUsers) {
        //all users close to one currently explored
        def closeUsers = allUsers.findAllParallel { DistanceCalc.getHavershineDistance((User) it, user) < 200 }
        Set set = [user] as Set
        closeUsers.each { set.addAll getChainRecur((User) it, allUsers) }

        return set
    }
}
