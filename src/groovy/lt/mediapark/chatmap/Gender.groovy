package lt.mediapark.chatmap

/**
 * Created by anatolij on 26/08/15.
 */
public enum Gender {

    M, F

    static boolean isValid(String genderString) {
        values().any { it.toString() == genderString }
    }
}