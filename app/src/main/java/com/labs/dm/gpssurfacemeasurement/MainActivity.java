package com.labs.dm.gpssurfacemeasurement;

import android.app.Activity;
import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private LocationManager locationManager;
    private Location lastLocation;
    private long mLastLocationMillis;
    private Button button;
    private Button cleanBtn;
    private Button undoButton;
    private List<Position> list = new ArrayList();
    private TextView counter;
    private TextView result;
    private TextView log;
    private TextView estimate;

    public void setGpsFix(boolean gpsFix) {
        this.gpsFix = gpsFix;
        button.setEnabled(gpsFix);
    }

    private boolean gpsFix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.captureBtn);
        undoButton = (Button) findViewById(R.id.undoButton);
        counter = (TextView) findViewById(R.id.countView);
        cleanBtn = (Button) findViewById(R.id.clearBtn);
        result = (TextView) findViewById(R.id.result);
        estimate = (TextView) findViewById(R.id.estimate);
        log = (TextView) findViewById(R.id.log);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                list.add(new Position(locationGPS.getLongitude(), locationGPS.getLatitude()));
                undoButton.setEnabled(true);
                calculate();
            }
        });
        cleanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoButton.setEnabled(false);
                list.clear();
                estimate.setText("0.0");
                result.setText("0.0");
                log.setText("");
                counter.setText("0");
            }
        });
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (list.size() > 0) {
                    list.remove(list.size() - 1);
                    calculate();
                } else {
                    undoButton.setEnabled(false);
                }
            }
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                if (location == null) return;
                mLastLocationMillis = SystemClock.elapsedRealtime();
                lastLocation = location;

                if (gpsFix) {
                    List<Position> l = new ArrayList<>(list);
                    l.add(new Position(location.getLongitude(), location.getLatitude()));
                    double sum = 0;
                    if (l.size() > 1) {
                        sum = polygonArea(l.toArray(new Position[l.size()]));
                    }

                    estimate.setText(String.format("%.3f", (sum)) + (l.size() < 3 ? " m" : " m2"));
                }

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

        GpsStatus.Listener listener = new GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {

                if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                    if (lastLocation != null) {
                        setGpsFix((SystemClock.elapsedRealtime() - mLastLocationMillis) < 2000 && lastLocation.getAccuracy() < 100);
                    }
                }
            }
        };

        locationManager.addGpsStatusListener(listener);
    }

    private void calculate() {
        double sum = 0;
        if (list.size() > 1) {
            sum = polygonArea(list.toArray(new Position[list.size()]));
        }

        result.setText(String.format("%.3f", (sum)) + (list.size() < 3 ? " m" : " m2"));
        counter.setText(String.valueOf(list.size()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public double polygonArea(Position... point) {

        log.setText("");

        Position[] ref = new Position[point.length];

        ref[0] = new Position(0, 0);
        log.append(ref[0].toString());
        log.append("\n");

        if (point.length == 2) {
            return Utils.calculateDistance(point[0], point[1]);
        } else if (point.length < 3) {
            return 0;
        }

        for (int i = 1; i < ref.length; i++) {
            double bearing = Utils.bearing(point[i - 1], point[i]);
            double distance = Utils.calculateDistance(point[i - 1], point[i]);

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

}
