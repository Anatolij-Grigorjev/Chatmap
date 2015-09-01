package lt.mediapark.chatmap

/**
 * Created by anatolij on 26/08/15.
 */
public enum Gender {

    M('M'), F('F')

    private String literal

    private Gender(String literal) {
        this.literal = literal
    }

    static boolean isValid(String genderString) {
        values().any { it.literal == genderString }
    }

    public String getLiteral() {
        return literal
    }
}