package com.labs.dm.measure.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.labs.dm.measure.R;
import com.labs.dm.measure.db.DBManager;
import com.labs.dm.measure.domain.Position;
import com.labs.dm.measure.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
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
    private TextView distance;
    private LocationListener ll;
    private boolean gpsFix;

    public void setGpsFix(boolean gpsFix) {
        this.gpsFix = gpsFix;
        button.setEnabled(gpsFix);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.captureBtn);
        undoButton = (Button) findViewById(R.id.undoButton);
        cleanBtn = (Button) findViewById(R.id.clearBtn);

        counter = (TextView) findViewById(R.id.countView);
        result = (TextView) findViewById(R.id.result);
        estimate = (TextView) findViewById(R.id.estimate);
        distance = (TextView) findViewById(R.id.distance);
        log = (TextView) findViewById(R.id.log);
        log.setTextColor(Color.YELLOW);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                list.add(new Position(locationGPS.getLatitude(), locationGPS.getLongitude()));
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
                distance.setText("0.0");
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

        ll = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                if (location == null) {
                    return;
                }
                mLastLocationMillis = SystemClock.elapsedRealtime();
                lastLocation = location;

                if (gpsFix) {

                    if (list.size() > 0) {
                        List<Position> tempList = new ArrayList<>(list);
                        tempList.add(new Position(location.getLatitude(), location.getLongitude()));
                        double sum = Utils.polygonArea(log, tempList.toArray(new Position[tempList.size()]));

                        double lastDistance = Utils.calculateDistance(list.get(list.size() - 1), Utils.toPosition(location));
                        distance.setText(String.format("%.3f", (lastDistance)) + " m");
                        estimate.setText(String.format("%.3f", (sum)) + Utils.getUnits(list));
                    } else {
                        distance.setText("0.0");
                        estimate.setText("0.0");
                    }
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
        };

        locationManager.addGpsStatusListener(new GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {

                if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                    if (lastLocation != null) {
                        setGpsFix((SystemClock.elapsedRealtime() - mLastLocationMillis) < 2000 && lastLocation.getAccuracy() < 100);
                    }
                }
            }
        });
    }

    private void calculate() {
        double sum = 0;
        if (list.size() > 1) {
            sum = Utils.polygonArea(log, list.toArray(new Position[list.size()]));
        }

        result.setText(String.format("%.3f", (sum)) + Utils.getUnits(list));
        counter.setText(String.valueOf(list.size()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), 1);
                return true;
            case R.id.save:
                saveTrack();
                return true;
            case R.id.history:
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveTrack() {
        DBManager db = new DBManager(this, "maps");
        db.save(list);
        Toast.makeText(this, "Saved track with " + list.size() + " items", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(ll);
        Log.i(TAG, "onPause, done");
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, ll);
    }
}
