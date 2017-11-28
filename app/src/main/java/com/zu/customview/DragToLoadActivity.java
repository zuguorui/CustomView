package com.zu.customview;

import android.os.Handler;
import android.os.Message;
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

    private boolean isRefreshing = false;
    private boolean isLoading = false;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    });

    private DragLoadView.OnDragListener upListener = new DragLoadView.OnDragListener() {
        @Override
        public void onDrag(float process) {

        }

        @Override
        public void onDragRelease(float process) {
            if(process > 0.5f && !isRefreshing)
            {
                isRefreshing = true;
                upDragLoadView.loadStart();

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        upDragLoadView.loadComplete(true);
                        isRefreshing = false;
                    }
                }, 1000);
            }
        }

        @Override
        public void onDragStart() {

        }
    };

    private DragLoadView.OnDragListener downListsner = new DragLoadView.OnDragListener() {
        @Override
        public void onDrag(float process) {

        }

        @Override
        public void onDragRelease(float process) {
            if(process > 0.5f && !isLoading)
            {
                isLoading = true;
                downDragLoadView.loadStart();

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        downDragLoadView.loadComplete(true);
                        isLoading = false;
                    }
                }, 1000);
            }
        }

        @Override
        public void onDragStart() {

        }
    };

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


        upDragLoadView.setOnDragListener(upListener);
        downDragLoadView.setOnDragListener(downListsner);
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
