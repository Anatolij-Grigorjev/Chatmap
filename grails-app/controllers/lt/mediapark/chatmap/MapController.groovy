package lt.mediapark.chatmap

import grails.converters.JSON
import lt.mediapark.chatmap.utils.DistanceCalc
import lt.mediapark.chatmap.utils.UserChainLink

class MapController {

    def usersService
    def mapService
    def converterService

    def index = {

        def user = usersService.get(params.id)
        if (!user) {
            return render(status: 403)
        }
        if (!user.hasLocation()) {
            return render([]) as JSON
        }
        Collection<UserChainLink> usersChain = mapService.getChainFor(user)

//        def (minLat, minLng) = getExtremePoint(usersChain, 'min')
        def (maxLat, maxLng) = mapService.getExtremePoint(usersChain.user, 'max')

        //deciding center and excluding those too far away
        UserChainLink center = null
        center = usersChain.find { it.isCenter } as UserChainLink
        usersChain = usersChain.findAll { DistanceCalc.getHaversineDistance(center.user, it.user) < 1500 }

        //set correct margin from group center to let everybody see stuff
        //from central chain link
        double marginsLat = 0.017 + Math.abs(center?.user?.lat - maxLat)
        double marginsLng = 0.017 + Math.abs(center?.user?.lng - maxLng)

        def target = [:]

        target.minLat = (center?.user?.lat - marginsLat)
        target.minLng = (center?.user?.lng - marginsLng)
        target.maxLat = (center?.user?.lat + marginsLat)
        target.maxLng = (center?.user?.lng + marginsLng)

        target.users = usersChain.collect { converterService.userToJSONForMap(it) }

        render target as JSON
    }

}
