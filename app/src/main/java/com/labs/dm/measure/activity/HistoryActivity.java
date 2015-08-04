package com.labs.dm.measure.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.labs.dm.measure.R;
import com.labs.dm.measure.db.DBManager;
import com.labs.dm.measure.domain.Measurement;
import com.labs.dm.measure.domain.Position;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import static android.widget.AdapterView.OnItemClickListener;

public class HistoryActivity extends Activity {

    private static final int CODE = 1;
    private ListView view;
    private DBManager db;
    private PopupMenu popUp;

    @Override
    protected void onStart() {
        super.onStart();
        List<Map<String, String>> list = db.list();
        String[] from = {"created", "id"};
        int[] to = {R.id.itemTextView};
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item, from, to);
        view.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        db = new DBManager(this, "maps");
        view = (ListView) findViewById(R.id.listView);

        view.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, final long id) {
                final Map<String, String> map = (Map) parent.getAdapter().getItem((int) id);

                popUp = new PopupMenu(HistoryActivity.this, view);
                popUp.getMenuInflater().inflate(R.menu.popup_menu, popUp.getMenu());

                popUp.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.share:
                                try {
                                    share(map.get("id"));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case R.id.delete:
                                delete(map.get("id"));
                                break;
                            case R.id.showmap:
                                showMap();
                                break;
                        }
                        Toast.makeText(HistoryActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                popUp.show();
            }
        });
    }

    private void delete(String id) {
        db.delete(Integer.valueOf(id));
    }

    private void showMap() {
    }


    private String filename;

    private void share(String id) throws IOException {
        Measurement measurement = db.getMeasurement(id);
        List<Position> list = db.getPoints(id);
        File sd = Environment.getExternalStorageDirectory();
        filename = "track_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(measurement.getCreated()) + ".xml";
        File f = new File(sd, filename);
        FileWriter write = new FileWriter(f);
        XmlSerializer xml = Xml.newSerializer();

        xml.setOutput(write);
        xml.startDocument("UTF-8", true);

        xml.startTag("", "measurement");
        xml.attribute("", "created", String.valueOf(measurement.getCreated()));
        xml.endTag("", "measurement");
        xml.startTag("", "positions");
        for (Position position : list) {
            xml.startTag("", "pos");
            xml.attribute("", "lat", String.valueOf(position.getLatitude()));
            xml.attribute("", "lon", String.valueOf(position.getLongitude()));
            xml.endTag("", "pos");
        }
        xml.endTag("", "positions");
        xml.endDocument();

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Uri uri = Uri.fromFile(f);

        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("application/octet-stream");
        startActivityForResult(Intent.createChooser(shareIntent, "Send to..."), CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE) {
            File sd = Environment.getExternalStorageDirectory();
            File f = new File(sd, filename);
            f.delete();
        }
    }
}
