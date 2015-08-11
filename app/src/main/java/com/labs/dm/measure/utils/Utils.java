package com.labs.dm.measure.utils;

import android.location.Location;
import android.widget.TextView;

import com.labs.dm.measure.domain.Position;

import java.util.Collection;

/**
 * Created by daniel on 2015-07-14.
 */
public class Utils {

    public static Position toPosition(Location location) {
        return new Position(location.getLatitude(), location.getLongitude());
    }

    public static double calculateDistance(Location src, Location dest) {
        return calculateDistance(new Position(src.getLatitude(), src.getLongitude()), new Position(dest.getLatitude(), dest.getLongitude()));
    }

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

    public static double polygonArea(TextView log, Position... point) {
        Position[] ref = new Position[point.length];

        ref[0] = new Position(0, 0);
        if (log != null) {
            log.setText("");
            log.append(String.format("%.2f,\t%.2f", ref[0].getLatitude(), ref[0].getLongitude()));
            log.append("\n");
        }

        if (point.length == 2) {
            return calculateDistance(point[0], point[1]);
        } else if (point.length < 3) {
            return 0;
        }

        for (int i = 1; i < ref.length; i++) {
            double bearing = bearing(point[i - 1], point[i]);
            double distance = calculateDistance(point[i - 1], point[i]);

            double x = Math.cos(bearing) * distance;
            double y = Math.sin(bearing) * distance;

            Position p = new Position(x, y);
            if (log != null) {
                log.append(String.format("%.2f,\t%.2f", x, y));
                log.append("\n");
            }
            ref[i] = p;
        }

        double sum = 0;
        for (int i = 1; i < ref.length - 1; i++) {
            double item = ref[i].getLatitude() * (ref[i + 1].getLongitude() - ref[i - 1].getLongitude());
            sum += item;
        }
        sum += ref[0].getLatitude() * (ref[1].getLongitude() - ref[ref.length - 1].getLongitude());
        sum += ref[ref.length - 1].getLatitude() * (ref[0].getLongitude() - ref[ref.length - 2].getLongitude());
        return Math.abs(0.5d * sum);
    }

    public static String getUnits(Collection c) {
        return (c.size() < 3 ? " m" : " m2");
    }
}
