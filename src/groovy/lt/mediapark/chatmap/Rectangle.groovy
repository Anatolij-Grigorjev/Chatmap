package lt.mediapark.chatmap

/**
 * Created by anatolij on 26/08/15.
 */
class Rectangle {
    //rectangle lowe-left point
    double x1, y1
    //rectangle upper right point
    double x2, y2

    public Rectangle(double ax1, double ay1, double ax2, double ay2) {
        def X = [ax1, ax2].sort()
        def Y = [ay1, ay2].sort()

        (x1, x2) = X
        (y1, y2) = Y
    }

    boolean hasPoint(double x, double y) {
        (x1 <= x && x2 >= x) && (y1 <= y && y2 >= y)
    }

}
