package chatmap

import lt.mediapark.chatmap.User


class UserChainMakerJob {
    static triggers = {
        simple(
                repeatInterval: 20000l, // execute job once every 20 seconds
                startDelay: 20000l //execute job after 20 seconds of work
        )
    }

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
                mapService.lastUserChain.clear()
                int chains = 0
                coordinatedUsers.each { user ->
                    def lastChain = mapService.lastUserChain[(user.id)]
                    if (!lastChain) {
                        chains++
                        def chain = mapService.generateNewChainFor(user)
                        mapService.notifyOfChainChanges(user, chain)
                    }
                }
                if (chains) {
                    log.debug("Created ${chains} new chains!")
                }
            }
        }
    }
}
