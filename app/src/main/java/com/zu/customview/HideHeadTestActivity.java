package com.zu.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.HashMap;
import java.util.LinkedList;

public class HideHeadTestActivity extends AppCompatActivity {

    private ImageView imageView;
    private ListView listView;
    private LinkedList<HashMap<String, Integer>> data = new LinkedList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_head_test);
        createData(50);
        initViews();
    }

    private void initViews()
    {

        listView = (ListView)findViewById(R.id.HideHeadActivity_listView);
        listView.setNestedScrollingEnabled(true);
        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.list_item, new String[]{"name"}, new int[]{R.id.ListItem_index});
        listView.setAdapter(adapter);
    }

    private void createData(int count)
    {
        data.clear();
        for(int i = 0; i < count; i++)
        {
            HashMap<String, Integer> s = new HashMap<>();
            s.put("name", i);
            data.push(s);
        }
    }
}
