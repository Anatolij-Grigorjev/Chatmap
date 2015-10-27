package lt.mediapark.chatmap.utils

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

/**
 * Created by anatolij on 26/08/15.
 */
@Log4j
@CompileStatic
class Converter {

    /**
     * Attempts to transform whatever was passed here into a {@link Long},
     * mostly for valid ID processing.<br/>
     * This method attempts the following, in order:<br/>
     * <ol>
     *     <li>If the passed object is <code>null</code>, then <code>null</code> is returned</li>
     *     <li>If the passed object is actually an instance of <code>Long</code> without knowing it,
     *     then it is cast to <code>Long</code> and returned</li>
     *     <li>If the passed object is a {@link String}, then {@link Long#parseLong} is invoked to handle it</li>
     *     <li>If the passed object is any non-Long form of {@link Number}, then {@link Number#longValue()} is
     *     invoked to coerce it</li>
     *     <li>If the class of the object is not <code>Long</code> but is still assignable from it, the object is
     *     cast to <code>Long</code> and returned.</li>
     * </ol>
     * When all of the above fail, a <i>WARN</i> level log message is printed, containing the uncoerceable value and
     * its class name, recorded for posterity. <code>null</code> is returned.
     * @param obj Whatever thing needs to be coerced to a <code>Long</code>
     * @return the closest thing to a <code>Long</code> you can extract from the object passed in.
     */
    public static Long coerceToLong(def obj) {
        if (obj == null) return null
        Long res = null
        if (!(obj instanceof Long)) {
            if (obj instanceof String) {
                res = Long.parseLong(obj)
            } else if (obj instanceof Number) {
                res = obj.longValue()
            } else if (obj.class.isAssignableFrom(Long.class)) {
                res = (Long) obj
            } else {
                log.warn("Proceeding with id that could not be coerced into Long! ID: ${obj}, " +
                        "class: ${obj.class.name}")
            }
        } else {
            res = (Long) obj
        }
        return res
    }

}
