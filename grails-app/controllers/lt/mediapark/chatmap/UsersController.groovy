package lt.mediapark.chatmap

import grails.converters.JSON

class UsersController {

    def usersService
    def converterService

    static allowedMethods = [
            update: 'POST',
            index : 'GET'
    ]

    def index() {}

    def update = {
        def user = usersService.get(Long.parseLong(params.id), false)
        user = usersService.updateUser(user, (Map) request.JSON)

        def map = converterService.userToJSONForMap(user)

        render map as JSON
    }
}
