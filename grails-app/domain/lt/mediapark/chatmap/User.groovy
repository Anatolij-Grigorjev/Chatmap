package lt.mediapark.chatmap

class User {

    static constraints = {
        gender nullable: false
        emoji nullable: false
        deviceToken unique: true
    }

    String deviceToken
    String name
    String emoji
    Gender gender
    Double lat
    Double lng
    Boolean wantsNotifications = Boolean.FALSE

    boolean hasLocation() {
        lat && lng
    }
}
