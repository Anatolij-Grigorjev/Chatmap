package lt.mediapark.chatmap

class Picture {

    static constraints = {
        data nullable: false
    }

    byte[] data
    String name
    Double lat
    Double lng

}
