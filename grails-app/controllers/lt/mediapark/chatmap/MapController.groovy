package lt.mediapark.chatmap

import grails.converters.JSON

class MapController {

    def usersService
    def mapService
    def converterService

    def index = {

        def user = usersService.get(params.id)
        Collection<User> usersChain = mapService.getChainFor(user)

//        def (minLat, minLng) = getExtremePoint(usersChain, 'min')
        def (maxLat, maxLng) = getExtremePoint(usersChain, 'max')

        def center = usersChain.find { it.isCenter }

        //set correct margin from group center to let everybody see stuff
        //from centrail chain link
        double marginsLat = 0.017 + Math.abs(center.lat - maxLat)
        double marginsLng = 0.017 + Math.abs(center.lng - maxLng)

        def target = [:]

        target.minLat = (center.lat - marginsLat)
        target.minLng = (center.lng - marginsLng)
        target.maxLat = (center.lat + marginsLat)
        target.maxLng = (center.lng + marginsLng)

        target.users = usersChain.collect { converterService.userToJSONForMap(it) }

        render target as JSON
    }

    def getExtremePoint(Collection<User> users, String extreme) {
        Double lat = users.lat."${extreme}"()
        Double lng = users.lng."${extreme}"()

        [lat, lng]
    }
}
