package lt.mediapark.chatmap.utils

import lt.mediapark.chatmap.User

/**
 * Created by anatolij on 28/08/15.
 */
class UserChainLink {

    User user
    boolean isCenter
    Map<Long, Double> connections
    Double avgDist

    public UserChainLink(User user) {
        this.user = user
        isCenter = false
        connections = [:]
        avgDist = BigDecimal.ZERO.doubleValue()
    }


    public void setConnections(Map<Long, Double> connections) {
        this.connections = connections
        if (connections) {
            this.avgDist = this.connections.values().sum() / this.connections.size()
        } else {
            this.avgDist = Double.MAX_VALUE
        }
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof UserChainLink)) return false

        UserChainLink that = (UserChainLink) o

        if (user != that.user) return false

        return true
    }

    int hashCode() {
        return user.hashCode()
    }


    @Override
    public String toString() {
        return "UserChainLink{" +
                "user=" + user +
                '}';
    }
}
