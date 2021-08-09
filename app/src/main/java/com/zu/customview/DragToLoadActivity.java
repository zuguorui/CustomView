package com.zu.customview;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

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
    private float startLoadGate = 0.7f;

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
            if(process > startLoadGate && !isRefreshing)
            {
                isRefreshing = true;
                upDragLoadView.loadStart();

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        upDragLoadView.loadComplete(true);
                        isRefreshing = false;
                    }
                }, 3000);
                return;
            }
            if(process <= startLoadGate)
            {
                upDragLoadView.loadCancel();
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
            if(process > startLoadGate && !isLoading)
            {
                isLoading = true;
                downDragLoadView.loadStart();

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        downDragLoadView.loadComplete(true);
                        isLoading = false;
                    }
                }, 3000);
                return;
            }
            if(process <= startLoadGate)
            {
                downDragLoadView.loadCancel();
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
        createData(50);
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
