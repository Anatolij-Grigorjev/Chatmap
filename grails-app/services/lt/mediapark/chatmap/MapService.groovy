package lt.mediapark.chatmap

import grails.transaction.Transactional
import groovyx.gpars.GParsPool
import lt.mediapark.chatmap.utils.DistanceCalc
import lt.mediapark.chatmap.utils.UserChainLink

@Transactional
class MapService {

    Collection<UserChainLink> getChainFor(User me) {

        //prefetching all users not to do this many times in recursive method
        def allUsers = User.all
        Set<UserChainLink> usersChain = getChainRecur(me, allUsers)

        //finalize bidirectional relations
        postRecurMapMerge(usersChain)

        //chain ready, time to find chain center
        UserChainLink center = null

        GParsPool.withPool {
            //filter to users with most connections
            int max = usersChain.collectParallel { UserChainLink link -> link.connections.size() }.maxParallel()
            Set<UserChainLink> mostConnected =
                    usersChain.findAllParallel { UserChainLink link -> link.connections.size() >= max }
            log.debug("Largest connection size was ${max}, spotted in ${mostConnected.size()} users")
            //find the smallest average distance
            double minAvgDist = mostConnected.collectParallel { UserChainLink link -> link.avgDist }.minParallel()
            center = mostConnected.findParallel { UserChainLink link -> link.avgDist == minAvgDist }
            log.debug("Min average distance was ${minAvgDist}, first spotted in ${center}")
        }
        center?.isCenter = true

        usersChain
    }

    private void postRecurMapMerge(Set<UserChainLink> usersChain) {
        Map<Long, UserChainLink> idToLink = usersChain.collectEntries { [(it.user.id): it] }
        // add additional users to everybody because the distance is a bidirectional
        //relationship
        def millis = System.currentTimeMillis()
        idToLink.each { Long id, UserChainLink link ->
            GParsPool.withPool {
                link.connections.keySet().eachParallel { Long key ->
                    idToLink[(key)]?.connections << [(id): link.connections[(key)]]
                }
            }
        }
        log.debug("Extra map-merge took ${(System.currentTimeMillis() - millis)} ms")
    }

    Set<UserChainLink> getChainRecur(User user, List<User> allUsers) {
        //all users close to one currently explored
        Set<User> closeUsers = [] as Set
        GParsPool.withPool {
            List closeUsersList = (List) allUsers.findAllParallel {
                it != user && DistanceCalc.getHaversineDistance((User) it, user) < 200
            }
            closeUsers.addAll(closeUsersList)
        }
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
