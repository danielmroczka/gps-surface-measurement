package com.labs.dm.gpssurfacemeasurement;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

import static android.widget.AdapterView.OnItemClickListener;

public class HistoryActivity extends Activity {

    private DBManager db;
    ListView view;

    @Override
    protected void onStart() {
        super.onStart();
        List<Map<String, String>> list = db.list();

        //SimpleAdapter adapter = new SimpleAdapter(this, )
        String[] from = {"created"};
        int[] to = {R.id.itemTextView};
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item, from, to);
        view.setAdapter(adapter);
    }

    boolean click = true;
    PopupMenu popUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        db = new DBManager(this, "maps");
        view = (ListView) findViewById(R.id.listView);
        popUp = new PopupMenu(HistoryActivity.this, view);
        popUp.inflate(R.menu.popup_menu);
        view.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (click) {
                    popUp.show();
                    System.out.println("onItemClick" + position);
                    click = false;
                } else {
                    popUp.dismiss();
                    click = true;
                }
            }
        });
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
}
