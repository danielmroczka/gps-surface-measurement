package com.labs.dm.gpssurfacemeasurement;

/**
 * Created by daniel on 2015-07-14.
 */
public class Utils {
    public static double calculateDistance(Position src, Position dest) {

        double latDistance = Math.toRadians(src.getLatitude() - dest.getLatitude());
        double lngDistance = Math.toRadians(src.getLongitude() - dest.getLongitude());

        double a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2))
                + (Math.cos(Math.toRadians(src.getLatitude())))
                * (Math.cos(Math.toRadians(dest.getLatitude())))
                * (Math.sin(lngDistance / 2))
                * (Math.sin(lngDistance / 2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 1000d * 6371 * c;
    }

    public static double bearing(Position src, Position dest) {
        double latitude1 = Math.toRadians(src.getLatitude());
        double latitude2 = Math.toRadians(dest.getLatitude());
        double longDiff = Math.toRadians(dest.getLongitude() - src.getLongitude());
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }
}
