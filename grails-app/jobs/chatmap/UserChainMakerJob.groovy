package chatmap

import lt.mediapark.chatmap.User


class UserChainMakerJob {
    def usersService
    def mapService

    //only make one chain at a time
    def concurrent = false

    def execute() {
        User.withNewSession {
            def coordinatedUsers = usersService.usersWithCoordinates
            if (coordinatedUsers) {
                log.debug("Starting CHAIN_MAKER for ${coordinatedUsers.size()} users!")
                log.debug("clearing ${mapService.lastUserChain.size()} user chain links!")
                def now = System.currentTimeMillis()
                //its important to replace the chains one at a time so as not to impede regular service
                def staleChains = coordinatedUsers.id.collectEntries { [(it): true] }
                int chains = 0
                coordinatedUsers.each { user ->
                    //replace old chains
                    if (staleChains[(user.id)]) {
                        chains++
                        //generate new chain
                        def chain = mapService.generateNewChainFor(user)
                        //notify of new chain
                        mapService.notifyOfChainChanges(user, chain)
                        //mark as no longer stale for everybody involved
                        chain.user.id.each { staleChains[(it)] = false }
                    }
                }
                if (chains) {
                    log.debug("Created ${chains} new chains in ${new Date().time - now}ms!")
                }
            }
        }
    }


}
