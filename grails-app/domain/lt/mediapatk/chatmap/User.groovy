package lt.mediapatk.chatmap

class User {

    static constraints = {
        name nullable: false
        gender nullable: false
        emoji nullable: false
        deviceToken unique: true
    }

    String deviceToken
    String name
    String emoji
    Gender gender
}
