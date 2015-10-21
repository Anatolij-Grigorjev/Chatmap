package lt.mediapark.chatmap

import com.google.common.collect.Sets
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder
import grails.transaction.Transactional
import groovyx.gpars.GParsPool
import lt.mediapark.chatmap.utils.DistanceCalc
import lt.mediapark.chatmap.utils.UserChainLink

@Transactional
class MapService {

    def usersService

    static Map<Long, Set<Long>> lastUserChainIds = [:]

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


    def notifyOfChainChanges(User user, Collection<UserChainLink> newChain) {
        def oldChain = lastUserChainIds[(user.id)]
        if (oldChain) {
            log.debug("Old chain size: ${oldChain.size()} - New chain size: ${newChain.size()}")
            int sizeDiff = newChain.size() - oldChain.size()
            //people were actually added to the chain, something noteworthy
            if (sizeDiff > 0 && user.deviceToken) {
                //this set is guaranteed to not be empty
                def diffIds = Sets.difference(newChain.user.id as Set, oldChain) as List

                //first can be named, others grouped in notification
                //searching for the name of the person through the chain is slow,
                //but better than making another Hibernate session (this runs in a separate thread)
                String firstName = newChain.find { it.user.id == diffIds[0] }?.user?.name
                boolean manyIds = diffIds.size() > 1
                String msgBody = firstName
                +(manyIds ? " and ${diffIds.size() - 1} other(-s)" : "")
                +" ha${manyIds ? 've' : 's'} joined your map group!"

                sendNotification(user.deviceToken) { ApnsPayloadBuilder builder ->
                    builder.with {
                        alertBody = msgBody
                        alertTitle = 'Map Group Update!'
                        addCustomProperty('userId', user.id)
                    }
                }
            }
        }
        //finish it all by moving what the old chain was
        lastUserChainIds[(user.id)] = newChain.user.id as Set
    }
}
