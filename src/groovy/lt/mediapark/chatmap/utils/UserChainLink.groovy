package lt.mediapark.chatmap.utils

import groovy.transform.CompileStatic
import lt.mediapark.chatmap.User

/**
 * Created by anatolij on 28/08/15.
 */
@CompileStatic
class UserChainLink {

    User user
    boolean isCenter
    Map<User, Double> connections
    Double avgDist

    public UserChainLink(User user) {
        this.user = user
        isCenter = false
        connections = [:]
        avgDist = BigDecimal.ZERO.doubleValue()
    }


    public void setConnections(Map<User, Double> connections) {
        this.connections = connections
        if (connections) {
            this.avgDist = (((Double) this.connections.values().sum()) / ((Double) this.connections.size()))?.doubleValue()
        } else {
            this.avgDist = Double.MAX_VALUE
        }
    }


}
