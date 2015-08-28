package lt.mediapark.chatmap

import grails.converters.JSON
import lt.mediapark.chatmap.utils.UserChainLink

class MapController {

    def usersService
    def mapService
    def converterService

    def index = {

        def user = usersService.get(params.id)
        Collection<UserChainLink> usersChain = mapService.getChainFor(user)

//        def (minLat, minLng) = getExtremePoint(usersChain, 'min')
        def (maxLat, maxLng) = getExtremePoint(usersChain.user, 'max')

        def center = usersChain.find { it.isCenter }

        //set correct margin from group center to let everybody see stuff
        //from central chain link
        double marginsLat = 0.017 + Math.abs(center.user.lat - maxLat)
        double marginsLng = 0.017 + Math.abs(center.user.lng - maxLng)

        def target = [:]

        target.minLat = (center.user.lat - marginsLat)
        target.minLng = (center.user.lng - marginsLng)
        target.maxLat = (center.user.lat + marginsLat)
        target.maxLng = (center.user.lng + marginsLng)

        target.users = usersChain.collect { converterService.userToJSONForMap(it) }

        render target as JSON
    }

    def getExtremePoint(Collection<User> users, String extreme) {
        Double lat = users.lat."${extreme}"()
        Double lng = users.lng."${extreme}"()

        [lat, lng]
    }
}
