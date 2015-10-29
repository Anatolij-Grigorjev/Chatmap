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

    public static Map<Long, Set<UserChainLink>> lastUserChain = Collections.synchronizedMap([:])

    Collection<UserChainLink> getChainFor(User me) {
        //if user lacks coordinates, they don't have a chain
        if (!me.hasLocation()) {
            return Collections.EMPTY_LIST
        }
        while (!lastUserChain[(me.id)]) {
            synchronized (this) {
                wait(2500l)
            }
//            if (!lastUserChain[(me.id)]) {
//                UserChainMakerJob.executeForUsers([me])
//            }
        }
        return lastUserChain[(me.id)]
    }

    def Collection<UserChainLink> generateNewChainFor(User me) {
        //prefetching all users not to do this many times in recursive method
        // (though must filter out those w/o coordinates)
        def allUsers = usersService.usersWithCoordinates
        def millis = System.currentTimeMillis()
        Set<UserChainLink> usersChain = [] as Set<UserChainLink>
        allUsers.remove(me)
        //recursively get the chain and chains of all buddies
        //might be a problem if a buddy has over a 100 levels depths of buddies of their own
        getChainRecur(me, allUsers, usersChain)

        log.debug "Recursive chain generating took ${System.currentTimeMillis() - millis} ms"
        log.debug("Total users in chain: ${usersChain.size()}")

        //finalize bidirectional relations
        // called separately from main recur code
        // not to end up being called many times over
        millis = System.currentTimeMillis()
        postRecurMapMerge(usersChain)
        log.debug "Extra map-merge took ${System.currentTimeMillis() - millis} ms"

        //concurrent search for the center
        GParsPool.withPool {

            //chain ready, time to find chain center
            UserChainLink center = null

            //filter to users with most connections
            if (usersChain) {
                int max = usersChain.collectParallel { UserChainLink link -> link.connections.size() }.max()
                Set<UserChainLink> mostConnected =
                        usersChain.findAllParallel { UserChainLink link -> link.connections.size() >= max }
                //find the smallest average distance
                double minAvgDist = mostConnected.collectParallel { UserChainLink link -> link.avgDist }.min()
                center = mostConnected.findParallel { UserChainLink link -> link.avgDist == minAvgDist }

                log.debug("Largest connection size was ${max}, spotted in ${mostConnected.size()}/${usersChain.size()} users")
                log.debug("Min average distance was ${minAvgDist}, first spotted in ${center.user}")

                center?.isCenter = true
                //now that center is known, let's exclude those too far away
                return usersChain.findAllParallel {
                    DistanceCalc.getHaversineDistance(center.user, it.user) < 1500
                } as Set<UserChainLink>
            } else {
                return Collections.EMPTY_SET
            }
        }
    }

    /**
     * Takes a chain and fills in any missing connections between users if even one side has
     * a distance between the two
     * @param usersChain the unfilled chain
     */
    private static void postRecurMapMerge(Set<UserChainLink> usersChain) {
        //bi-directionality matters only when there is more than one
        if (!usersChain || usersChain.size() == 1) {
            return
        }
        //set up mapping before the loop to speed things along
        Map<Long, UserChainLink> idToLinkMap = usersChain.collectEntries { [(it.user.id): it] }
        // add additional users to everybody because the distance is a bidirectional
        //relationship
        idToLinkMap.each { Long id, UserChainLink link ->
            link.connections.keySet().each { Long userId ->
                def targetLink = idToLinkMap[(userId)]
                def linkDistance = link.connections[(userId)]
                targetLink.connections.put(link.user.id, linkDistance)
            }
        }
    }

    def getChainRecur(User user, List<User> remainingUsers, Set<UserChainLink> finalSet) {
        //all users close to one currently explored form a chain link
        def userChainLink = new UserChainLink(user)
        def closeUsers = new LinkedList<User>()
        def newConn = [:]
        remainingUsers.each {
            Double distance = DistanceCalc.getHaversineDistance(it, user)
            if (distance < 200) {
                closeUsers << it
                newConn << [(it.id): distance]
            }
        }
        userChainLink.connections = newConn
        finalSet << userChainLink
        //removing user from all users list since we already know everybody they are close to
        remainingUsers.remove(user)
        //any more unexplored users found to be close to this one
        //deserve their own chain segments
        while (closeUsers) {
            def nextUser = closeUsers.get(0)
            closeUsers.remove(0)
            getChainRecur(nextUser, remainingUsers, finalSet)
        }

        return finalSet
    }

    def getExtremePoint(Collection<User> users, String extreme) {
        GParsPool.withPool {
            Double lat, lng
            lat = users.lat."${extreme}Parallel"()
            lng = users.lng."${extreme}Parallel"()
            return [lat, lng]
        }
    }


    def notifyOfChainChanges(User user, Collection<UserChainLink> newChain) {
        def oldChain = lastUserChain[(user.id)]
        if (oldChain) {
            log.debug("Old chain size: ${oldChain.size()} - New chain size: ${newChain.size()}")
            int sizeDiff = newChain.size() - oldChain.size()
            //people were actually added to the chain, something noteworthy
            if (sizeDiff > 0 && user.deviceToken) {
                //this set is guaranteed to not be empty
                def diffs = Sets.difference(newChain as Set<UserChainLink>, oldChain) as List

                //first can be named, others grouped in notification
                //lets use one of the new names
                String firstName = diffs[0].user.name
                boolean manyIds = diffs.size() > 1
                String msgBody = firstName
                +(manyIds ? " and ${diffs.size() - 1} other(-s)" : "")
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
        //register the chain as latest for schmuck
        //and for all those within the chain as well
        lastUserChain[(user.id)] = (newChain as Set<UserChainLink>)
        newChain.each { lastUserChain[(it.user.id)] = (newChain as Set<UserChainLink>) }
    }

    Set<User> getInBounds(double minLat, double minLng, double maxLat, double maxLng) {
        User.createCriteria().list {
            and {
                gte('lat', minLat)
                gte('lng', minLng)
                le('lat', maxLat)
                le('lng', maxLng)
            }
        } as Set<User>
    }
}
