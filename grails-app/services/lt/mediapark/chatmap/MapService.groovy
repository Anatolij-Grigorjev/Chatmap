package lt.mediapark.chatmap

import grails.transaction.Transactional
import lt.mediapark.chatmap.utils.DistanceCalc
import lt.mediapark.chatmap.utils.UserChainLink

@Transactional
class MapService {

    Collection<UserChainLink> getChainFor(User me) {

        //prefetching all users not to do this many times in recursive method
        def allUsers = User.all

        Set<UserChainLink> usersChain = getChainRecur(me, allUsers)

        //chain acquired, time to find chain center

        //filter to users with most connections
        int max = usersChain.collect { it.connections.size() }.max()
        Set<UserChainLink> mostConnected = usersChain.findAll { it.connections.size() >= max }

        //find the smallest average distance
        double minAvgDist = mostConnected.collect { it.avgDist }.min()

        def center = mostConnected.find { it.avgDist == minAvgDist }
        center.isCenter = true

        usersChain
    }

    private Set<UserChainLink> getChainRecur(User user, List<User> allUsers) {
        //all users close to one currently explored
        List closeUsers = (List) allUsers.findAllParallel {
            it != user && DistanceCalc.getHavershineDistance((User) it, user) < 200
        }
        def userChainLink = new UserChainLink(user)
        userChainLink.connections = closeUsers.collectEntries {
            [(it): DistanceCalc.getHavershineDistance(user, (User) it)]
        }
        Set<UserChainLink> set = [userChainLink]
        closeUsers.each { set.addAll getChainRecur((User) it, allUsers) }

        return set
    }
}
