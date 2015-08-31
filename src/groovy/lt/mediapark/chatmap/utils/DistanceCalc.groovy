package lt.mediapark.chatmap.utils

import groovy.transform.CompileStatic
import lt.mediapark.chatmap.User

@CompileStatic
class DistanceCalc {

    /**
     * Globe distance metric used to navigate using longitude and latitude
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static Double getHaversineDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        Double R = 6371 * 1000; // Radius of the earth in m
        Double dLat = degToRad(lat2 - lat1);  //calc in rad
        Double dLon = degToRad(lng2 - lng1);
        Double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(degToRad(lat1)) * Math.cos(degToRad(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2)
        ;
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double d = R * c; // Distance in m
        return d;
    }

    public static Double getHaversineDistance(User user1, User user2) {
        if (!user1 || !user2) {
            return Double.MAX_VALUE
        }
        if (!user1.lng || !user1.lat || !user2.lat || !user2.lng) {
            return Double.MAX_VALUE
        }
        return getHaversineDistance(user1.lat, user1.lng, user2.lat, user2.lng)
    }


    private static Double degToRad(Double deg) {
        return deg * (Math.PI / 180);
    }

    /**
     * Sometimes quicker arithmetic approximation of the Haversine distance, used as an optimization
     * metric
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static long getMetricDistance(double lat1, double lng1, double lat2, double lng2) {
        //( (((target.lat - entity.lat)*2)**2) + (target.lng - entity.lng)**2)**0.5*60000|round
        Math.round(
                Math.pow((Math.pow(2 * lat2 - 2 * lat1, 2) + Math.pow(lng2 - lng1, 2)), 0.5) * 60000
        )
    }

}
