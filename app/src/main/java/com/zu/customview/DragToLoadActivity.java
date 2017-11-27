package com.zu.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.zu.customview.view.DragLoadView;

import java.util.HashMap;
import java.util.LinkedList;

public class DragToLoadActivity extends AppCompatActivity {

    private LinkedList<HashMap<String, Integer>> data = new LinkedList<>();

    private ListView listView;
    private DragLoadView upDragLoadView;
    private DragLoadView downDragLoadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_to_load);
        createData(5);
        initViews();

    }

    private void initViews()
    {
        listView = (ListView)findViewById(R.id.DragToLoad_listView);
        upDragLoadView = (DragLoadView)findViewById(R.id.DragToLoad_upDragLoadView);
        downDragLoadView = (DragLoadView)findViewById(R.id.DragToLoad_downDragLoadView);

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
