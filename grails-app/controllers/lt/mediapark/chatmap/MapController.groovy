package lt.mediapark.chatmap

import grails.converters.JSON
import lt.mediapark.chatmap.utils.Converter
import lt.mediapark.chatmap.utils.UserChainLink

class MapController {

    static allowedMethods = [
            index: 'GET',
            all  : 'POST'
    ]


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


    def all = {

        Double minLat = request.JSON.minLat
        Double minLng = request.JSON.minLng
        Double maxLat = request.JSON.maxLat
        Double maxLng = request.JSON.maxLng

        //swap if values not ordered good
        if (minLat > maxLat) {
            (minLat, maxLat) = [maxLat, minLat]
        }
        if (minLng > maxLng) {
            (minLng, maxLng) = [maxLng, minLng]
        }

        def bounds = [minLat, minLng, maxLat, maxLng]

        if (bounds.any { it == null }) {
            return render(status: 400, text: "At least one of the 4 required coordinates was null!")
        }

        log.debug("Received bounds: ${bounds}")

        Set<User> peopleInBounds = mapService.getInBounds(*bounds)

        def result = peopleInBounds.collect { converterService.userToJSONForMap(it) }

        if (params.requestor) {
            Long requestorId = Converter.coerceToLong(params.requestor)
            def theChain = mapService.lastUserChain[(requestorId)]
            //user has a chain, so we can mark those retrieved who are in it
            def chainIds = theChain.user.id
            if (theChain) {
                result.each { it['inGroup'] = false }
                //now lets find those in the chain
                result.findAll { map -> chainIds.contains(map.id) }.each {
                    it['inGroup'] = true
                }
            }
        }

        render result as JSON
    }

}
