package com.labs.dm.gpssurfacemeasurement;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

public class HistoryActivity extends Activity {

    private DBManager db;

    @Override
    protected void onStart() {
        super.onStart();
        List<Map<String, String>> list = db.list();

        //SimpleAdapter adapter = new SimpleAdapter(this, )
        String[] from = {"created"};
        int[] to = {R.id.itemTextView};
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item, from, to);
        ListView view = (ListView) findViewById(R.id.listView);
        view.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        db = new DBManager(this, "maps");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
