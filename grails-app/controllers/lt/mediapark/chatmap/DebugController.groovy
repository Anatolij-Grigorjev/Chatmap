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


    private static final List<String> emojis = [new String([0xF0, 0x9F, 0x98, 0x81] as byte[])
                                                , new String([0xF0, 0x9F, 0x98, 0x82] as byte[])
                                                , new String([0xF0, 0x9F, 0x98, 0x83] as byte[])
                                                , new String([0xF0, 0x9F, 0x98, 0x84] as byte[])
                                                , new String([0xF0, 0x9F, 0x98, 0x85] as byte[])
                                                , new String([0xF0, 0x9F, 0x98, 0x86] as byte[])
                                                , new String([0xF0, 0x9F, 0x98, 0x89] as byte[])
                                                , new String([0xF0, 0x9F, 0x98, 0x8A] as byte[])
                                                , new String([0xF0, 0x9F, 0x98, 0x8B] as byte[])
                                                , new String([0xF0, 0x9F, 0x98, 0x8C] as byte[])
                                                , new String([0xF0, 0x9F, 0x98, 0x8D] as byte[])
                                                , new String([0xF0, 0x9F, 0x98, 0x8F] as byte[])
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
                user.name = names[rnd.nextInt(names.size())].split('\\s+')[0]
                +' '
                +names[rnd.nextInt(names.size())].split('\\s+')[1]
                user.emoji = emojis[rnd.nextInt(emojis.size())]
                user.save()
            }
            user.lat = latOrigin - (rnd.nextDouble() / rnd.nextInt(10000))
            user.lng = lngOrigin + (rnd.nextDouble() / rnd.nextInt(10000))
            result << user
        }
        def json = result.collect { converterService.userToJSONForMap(it) }
        render json as JSON
    }

}
