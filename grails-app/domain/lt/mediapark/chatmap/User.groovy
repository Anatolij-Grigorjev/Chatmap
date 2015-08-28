package lt.mediapark.chatmap

class User {

    static constraints = {
        gender nullable: false
        emoji nullable: false
        deviceToken unique: true
    }

    static transients = ['isCenter']


    String deviceToken
    String name
    String emoji
    Gender gender
    Double lat
    Double lng
    Boolean wantsNotifications = Boolean.FALSE
    Boolean isCenter = false

    boolean hasLocation() {
        lat && lng
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof User)) return false

        User user = (User) o

        if (deviceToken != user.deviceToken) return false
        if (emoji != user.emoji) return false
        if (gender != user.gender) return false
        if (id != user.id) return false
        if (name != user.name) return false
        if (version != user.version) return false
        if (wantsNotifications != user.wantsNotifications) return false

        return true
    }

    int hashCode() {
        int result
        result = (deviceToken != null ? deviceToken.hashCode() : 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + emoji.hashCode()
        result = 31 * result + gender.hashCode()
        result = 31 * result + wantsNotifications.hashCode()
        result = 31 * result + (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        return result
    }
}
