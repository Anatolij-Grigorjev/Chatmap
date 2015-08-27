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


    private static final List<String> emojis = [
            new String([0xF0, 0x9F, 0x98, 0x81] as byte[]) //GRINNING FACE WITH SMILING EYES
            , new String([0xF0, 0x9F, 0x98, 0x82] as byte[]) //FACE WITH TEARS OF JOY
            , new String([0xF0, 0x9F, 0x98, 0x83] as byte[]) //SMILING FACE WITH OPEN MOUTH
            , new String([0xF0, 0x9F, 0x98, 0x84] as byte[]) //SMILING FACE WITH OPEN MOUTH AND SMILING EYES
            , new String([0xF0, 0x9F, 0x98, 0x85] as byte[]) //SMILING FACE WITH OPEN MOUTH AND COLD SWEAT
            , new String([0xF0, 0x9F, 0x98, 0x86] as byte[]) //SMILING FACE WITH OPEN MOUTH AND TIGHTLY-CLOSED EYES
            , new String([0xF0, 0x9F, 0x98, 0x89] as byte[]) //WINKING FACE
            , new String([0xF0, 0x9F, 0x98, 0x8A] as byte[]) //SMILING FACE WITH SMILING EYES
            , new String([0xF0, 0x9F, 0x98, 0x8B] as byte[]) //FACE SAVOURING DELICIOUS FOOD
            , new String([0xF0, 0x9F, 0x98, 0x8C] as byte[]) //RELIEVED FACE
            , new String([0xF0, 0x9F, 0x98, 0x8D] as byte[]) //SMILING FACE WITH HEART-SHAPED EYES
            , new String([0xF0, 0x9F, 0x98, 0x8F] as byte[]) //SMIRKING FACE
            , new String([0xF0, 0x9F, 0x98, 0x92] as byte[]) //UNAMUSED FACE
            , new String([0xF0, 0x9F, 0x98, 0x93] as byte[]) //FACE WITH COLD SWEAT
            , new String([0xF0, 0x9F, 0x98, 0x94] as byte[]) //PENSIVE FACE
            , new String([0xF0, 0x9F, 0x98, 0x96] as byte[]) //CONFOUNDED FACE
            , new String([0xF0, 0x9F, 0x98, 0x98] as byte[]) //FACE THROWING A KISS
            , new String([0xF0, 0x9F, 0x98, 0x9A] as byte[]) //KISSING FACE WITH CLOSED EYES
            , new String([0xF0, 0x9F, 0x98, 0x9C] as byte[]) //FACE WITH STUCK-OUT TONGUE AND WINKING EYE
            , new String([0xF0, 0x9F, 0x98, 0x9D] as byte[]) //FACE WITH STUCK-OUT TONGUE AND TIGHTLY-CLOSED EYES
            , new String([0xF0, 0x9F, 0x98, 0x9E] as byte[]) //DISAPPOINTED FACE
            , new String([0xF0, 0x9F, 0x98, 0xA0] as byte[]) //ANGRY FACE
            , new String([0xF0, 0x9F, 0x98, 0xA1] as byte[]) //POUTING FACE
            , new String([0xF0, 0x9F, 0x98, 0xA2] as byte[]) //CRYING FACE
            , new String([0xF0, 0x9F, 0x98, 0xA3] as byte[]) //PERSEVERING FACE
            , new String([0xF0, 0x9F, 0x98, 0xA4] as byte[]) //FACE WITH LOOK OF TRIUMPH
            , new String([0xF0, 0x9F, 0x98, 0xA5] as byte[]) //DISAPPOINTED BUT RELIEVED FACE
            , new String([0xF0, 0x9F, 0x98, 0xA8] as byte[]) //FEARFUL FACE
            , new String([0xF0, 0x9F, 0x98, 0xA9] as byte[]) //WEARY FACE
            , new String([0xF0, 0x9F, 0x98, 0xAA] as byte[]) //SLEEPY FACE
            , new String([0xF0, 0x9F, 0x98, 0xAB] as byte[]) //TIRED FACE
            , new String([0xF0, 0x9F, 0x98, 0xAD] as byte[]) //LOUDLY CRYING FACE
            , new String([0xF0, 0x9F, 0x98, 0xB0] as byte[]) //FACE WITH OPEN MOUTH AND COLD SWEAT
            , new String([0xF0, 0x9F, 0x98, 0xB1] as byte[]) //FACE SCREAMING IN FEAR
            , new String([0xF0, 0x9F, 0x98, 0xB2] as byte[]) //ASTONISHED FACE
            , new String([0xF0, 0x9F, 0x98, 0xB3] as byte[]) //FLUSHED FACE
            , new String([0xF0, 0x9F, 0x98, 0xB5] as byte[]) //DIZZY FACE
            , new String([0xF0, 0x9F, 0x98, 0xB7] as byte[]) //FACE WITH MEDICAL MASK
            , new String([0xF0, 0x9F, 0x98, 0xB8] as byte[]) //GRINNING CAT FACE WITH SMILING EYES
            , new String([0xF0, 0x9F, 0x98, 0xB9] as byte[]) //CAT FACE WITH TEARS OF JOY
            , new String([0xF0, 0x9F, 0x98, 0xBA] as byte[]) //SMILING CAT FACE WITH OPEN MOUTH
            , new String([0xF0, 0x9F, 0x98, 0xBB] as byte[]) //SMILING CAT FACE WITH HEART-SHAPED EYES
            , new String([0xF0, 0x9F, 0x98, 0xBC] as byte[]) //CAT FACE WITH WRY SMILE
            , new String([0xF0, 0x9F, 0x98, 0xBD] as byte[]) //KISSING CAT FACE WITH CLOSED EYES
            , new String([0xF0, 0x9F, 0x98, 0xBE] as byte[]) //POUTING CAT FACE
            , new String([0xF0, 0x9F, 0x98, 0xBF] as byte[]) //CRYING CAT FACE
            , new String([0xF0, 0x9F, 0x99, 0x80] as byte[]) //WEARY CAT FACE
            , new String([0xF0, 0x9F, 0x99, 0x85] as byte[]) //FACE WITH NO GOOD GESTURE
            , new String([0xF0, 0x9F, 0x99, 0x86] as byte[]) //FACE WITH OK GESTURE
            , new String([0xF0, 0x9F, 0x99, 0x87] as byte[]) //PERSON BOWING DEEPLY
            , new String([0xF0, 0x9F, 0x99, 0x88] as byte[]) //SEE-NO-EVIL MONKEY
            , new String([0xF0, 0x9F, 0x99, 0x89] as byte[]) //HEAR-NO-EVIL MONKEY
            , new String([0xF0, 0x9F, 0x99, 0x8A] as byte[]) //SPEAK-NO-EVIL MONKEY
            , new String([0xF0, 0x9F, 0x99, 0x8B] as byte[]) //HAPPY PERSON RAISING ONE HAND
            , new String([0xF0, 0x9F, 0x99, 0x8C] as byte[]) //PERSON RAISING BOTH HANDS IN CELEBRATION
            , new String([0xF0, 0x9F, 0x99, 0x8D] as byte[]) //PERSON FROWNING
            , new String([0xF0, 0x9F, 0x99, 0x8E] as byte[]) //PERSON WITH POUTING FACE
            , new String([0xF0, 0x9F, 0x99, 0x8F] as byte[]) //PERSON WITH FOLDED HANDS
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
