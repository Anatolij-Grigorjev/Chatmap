package lt.mediapark.chatmap

import grails.converters.JSON

class DebugController {

    private static final List<String> names = ['Elvie Flett'
                                               , 'Beatriz Hardwick'
                                               , 'Ethan Brigman'
                                               , 'Jaunita Bluford'
                                               , 'Fabiola Predmore'
                                               , 'Jeana Glover'
                                               , 'Nona Pea'
                                               , 'Connie Ruffner'
                                               , 'Philip Reetz'
                                               , 'Monet Marlin'
                                               , 'Leslie Rodkey'
                                               , 'Signe Bresnahan'
                                               , 'Vinnie Siers'
                                               , 'Bobbi Fleetwood'
                                               , 'Dovie Lamarca'
                                               , 'Jannette Alegria'
                                               , 'Rutha Post'
                                               , 'Wilson Coghill'
                                               , 'Georgeann Marez'
                                               , 'Bethanie Brownfield'
                                               , 'Kaleigh Smith'
                                               , 'Cyndi Pressnell'
                                               , 'Pamala Pilon'
                                               , 'Flossie Mccardell'
                                               , 'Jalisa Trees'
                                               , 'Dominque Reineke'
                                               , 'Ammie Schroyer'
                                               , 'Soila Goranson'
                                               , 'Roscoe Westrich'
                                               , 'Cathleen Polasek'
                                               , 'Jules Valerio'
                                               , 'Suzanna Sergi'
                                               , 'Dorine Lopiccolo'
                                               , 'Tonita Hillock'
                                               , 'Willia Bowes'
                                               , 'Lelia Beall'
                                               , 'Aurelio Freese'
                                               , 'Brice Kreitzer'
                                               , 'Criselda Kovach'
                                               , 'Eun Parry'
                                               , 'Rudy Paavola'
                                               , 'Madalyn Blas'
                                               , 'Lena Tsosie'
                                               , 'Alla Stratford'
                                               , 'Sheryl Tsui'
                                               , 'Carmen Flicker'
                                               , 'Jolanda Yip'
                                               , 'Debrah Verrill'
                                               , 'Farrah Sherrod'
                                               , 'Sallie Bridwell'
    ]


    def converterService

    def login = {
        int amount = Integer.parseInt(params.id)
        def users = User.all
        def rnd = new Random()
        def result = []

        amount.times {
            //54.689566, 25.272500
            Double latOrigin = params.lat ? Double.parseDouble(params.lat) : 54.689566
            Double lngOrigin = params.lng ? Double.parseDouble(params.lng) : 25.272500
            def user
            if (users.size() > it) {
                //login some existing users
                user = users[it]
            } else {
                //need more users
                user = new User()
                user.gender = Gender.values()[rnd.nextBoolean() ? 1 : 0]
                //take random first name and random last name
                user.name = names[rnd.nextInt(names.size())].split('\\s+')[0] + ' ' + names[rnd.nextInt(names.size())].split('\\s+')[1]
                user.emoji = 1 + rnd.nextInt(33)
                user.uuid = UUID.randomUUID()
                user.save()
            }
            if (user.id != ChatService.GLOBAL_CHAT_USER_ID) {
                user.lat = latOrigin + (rnd.nextDouble() / (1 + rnd.nextInt(870))) * (rnd.nextBoolean() ? 1 : -1)
                user.lng = lngOrigin + (rnd.nextDouble() / (1 + rnd.nextInt(750))) * (rnd.nextBoolean() ? 1 : -1)
            }
            result << user
        }
        def json = result.collect { converterService.userToJSONForMap(it) }
        render json as JSON
    }

}
