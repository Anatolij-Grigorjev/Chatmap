package lt.mediapark.chatmap

import grails.converters.JSON

class MapController {

    def usersService
    def mapService
    def converterService

    def index = {

        def user = usersService.get(params.id)
        Collection<User> usersChain = mapService.getChainFor(user)

        def (minLat, minLng) = getExtremePoint(usersChain, 'min')
        def (maxLat, maxLng) = getExtremePoint(usersChain, 'max')

        //small margins to offset hte map and make it look more framed
        double margins = 0.017

        def target = [:]

        target.minLat = (minLat - margins)
        target.minLng = (minLng - margins)
        target.maxLat = (maxLat + margins)
        target.maxLng = (maxLng + margins)

        target.users = usersChain.collect { converterService.userToJSONForMap(it) }

        render target as JSON
    }

    def getExtremePoint(Collection<User> users, String extreme) {
        Double lat = users.lat."${extreme}"()
        Double lng = users.lng."${extreme}"()

        [lat, lng]
    }
}
