package com.labs.dm.gpssurfacemeasurement;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private LocationManager locationManager;
    private Button button;
    private Button cleanBtn;
    private List<Position> list = new ArrayList();
    private TextView textView;
    private TextView counter;
    private TextView result;
    private TextView log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        counter = (TextView) findViewById(R.id.textView);
        cleanBtn = (Button) findViewById(R.id.button2);
        result = (TextView) findViewById(R.id.result);
        log = (TextView) findViewById(R.id.log);
        button.setOnClickListener(this);
        cleanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.clear();
                //  counter.setText(list.size());
                result.setText("");
                log.setText("");
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public double polygonArea(Position... point) {
        if (point.length < 3) {
            return 0;
        }
        log.setText("");

        Position[] ref = new Position[point.length];

        ref[0] = new Position(0, 0);
        log.append(ref[0].toString());
        log.append("\n");

        for (int i = 1; i < ref.length; i++) {
            double bearing = bearing(point[i - 1], point[i]);
            double distance = calculateDistance(point[i - 1], point[i]);

            double x = Math.cos(bearing) * distance;
            double y = Math.sin(bearing) * distance;

            Position p = new Position(x, y);
            log.append(p.toString());
            log.append("\n");
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

    @Override
    public void onClick(View v) {
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        list.add(new Position(locationGPS.getLongitude(), locationGPS.getLatitude()));
        double sum = 0;
        //   counter.setText(list.size());
        if (list.size() > 2) {
            sum = polygonArea(list.toArray(new Position[0]));
        }

        result.setText(String.format("%.3f", (sum)) + " m2");
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

}
