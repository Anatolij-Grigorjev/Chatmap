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
        avgDist = 0.0
    }


    public void setConnections(Map<User, Double> connections) {
        this.connections = connections
        this.avgDist = this.connections.values().sum() / this.connections.size()
    }


}
