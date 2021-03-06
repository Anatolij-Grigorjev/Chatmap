package lt.mediapark.chatmap

import grails.converters.JSON

class UsersController {

    def usersService
    def converterService

    static allowedMethods = [
            update: 'POST',
            index : 'GET',
            create: 'POST'
    ]

    def auth = {
        def uuid = params.id
        if (!uuid) {
            return render(status: 400, text: "Lackluster identifier!")
        }
        def user = usersService.gatherUser(uuid)
        def map = converterService.userToJSONForMap(user)
        render map as JSON
    }

    def index = {
        def user = usersService.get(Long.parseLong(params.id))
        if (user) {
            def target = converterService.userToJSONForMap(user)
            render target as JSON
        } else {
            render(status: 404)
        }
    }

    def update = {
        def user = usersService.get(Long.parseLong(params.id))
        user = usersService.updateUser(user, (Map) request.JSON)

        def map = converterService.userToJSONForMap(user)

        render map as JSON
    }
}
