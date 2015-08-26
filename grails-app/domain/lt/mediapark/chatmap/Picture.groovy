package lt.mediapark.chatmap

class Picture {

    static constraints = {
        data maxSize: 4096 * 2160, nullable: false
    }

    byte[] data
    String name
    Double lat
    Double lng

}
