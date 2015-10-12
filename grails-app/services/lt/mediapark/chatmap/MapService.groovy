package lt.mediapark.chatmap

import grails.transaction.Transactional
import groovyx.gpars.GParsPool
import lt.mediapark.chatmap.utils.DistanceCalc
import lt.mediapark.chatmap.utils.UserChainLink

@Transactional
class MapService {

    Collection<UserChainLink> getChainFor(User me) {

        //if user lacks coordinates, they don't have a chain
        if (!me.hasLocation()) {
            return Collections.EMPTY_LIST
        }
        //prefetching all users not to do this many times in recursive method
        // (though must filter out those w/o coordinates)
        def allUsers = User.findAllByLatIsNotNullAndLngIsNotNull()
        def millis = System.currentTimeMillis()
        Set<UserChainLink> usersChain = getChainRecur(me, allUsers)
        log.debug "Recursive chain generating took ${System.currentTimeMillis() - millis} ms"

        //finalize bidirectional relations
        // called separately from main recur code
        // not to end up being called many times over
        millis = System.currentTimeMillis()
        postRecurMapMerge(usersChain)
        log.debug "Extra map-merge took ${System.currentTimeMillis() - millis} ms"

        //chain ready, time to find chain center
        UserChainLink center = null

        //filter to users with most connections
        if (usersChain) {
            int max = usersChain.collect { UserChainLink link -> link.connections.size() }.max()
            Set<UserChainLink> mostConnected =
                    usersChain.findAll { UserChainLink link -> link.connections.size() >= max }
            //find the smallest average distance
            double minAvgDist = mostConnected.collect { UserChainLink link -> link.avgDist }.min()
            center = mostConnected.find { UserChainLink link -> link.avgDist == minAvgDist }

            log.debug("Largest connection size was ${max}, spotted in ${mostConnected.size()}/${usersChain.size()} users")
            log.debug("Min average distance was ${minAvgDist}, first spotted in ${center.user}")

            center?.isCenter = true

            usersChain
        } else {
            Collections.EMPTY_SET
        }
    }

    private void postRecurMapMerge(Set<UserChainLink> usersChain) {
        Map<Long, UserChainLink> idToLink = usersChain.collectEntries { [(it.user.id): it] }
        // add additional users to everybody because the distance is a bidirectional
        //relationship
        idToLink.each { Long id, UserChainLink link ->
            link.connections.keySet().each { Long key ->
                idToLink[(key)]?.connections << [(id): link.connections[(key)]]
            }
        }
    }

    Set<UserChainLink> getChainRecur(User user, List<User> allUsers) {
        //all users close to one currently explored
        Set<User> closeUsers = [] as Set
        List closeUsersList = (List) allUsers.findAll {
            (it != user) && (DistanceCalc.getHaversineDistance((User) it, user) < 200)
        }
        closeUsers.addAll(closeUsersList)
        def userChainLink = new UserChainLink(user)
        userChainLink.connections = closeUsers.collectEntries {
            [(it.id): DistanceCalc.getHaversineDistance(user, (User) it)]
        }
        Set<UserChainLink> set = [userChainLink]
        //removing user from all users list since we already know everybody they are close to
        //and this prevent infinite recursion
        allUsers.remove(user)
        closeUsers.each { set.addAll getChainRecur((User) it, allUsers) }

        return set
    }

    def getExtremePoint(Collection<User> users, String extreme) {
        Double lat = 0.0, lng = 0.0
        GParsPool.withPool {
            lat = users.lat."${extreme}Parallel"()
            lng = users.lng."${extreme}Parallel"()

        }
        [lat, lng]
    }
}
