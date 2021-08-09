package com.zu.customview;

import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ListViewTest extends AppCompatActivity {

    ListView listView;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0:
                    Toast.makeText(ListViewTest.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return true;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_test);
        listView = (ListView)findViewById(R.id.ListViewTest_listView);
        listView.setAdapter(mAdapter);
        listView.setClickable(true);
        listView.setDrawSelectorOnTop(true);
        listView.setSelected(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message message = Message.obtain(mHandler, 0);
                message.obj = position + " clicked";
                message.sendToTarget();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Message message = Message.obtain(mHandler, 0);
                message.obj = position + " long clicked";
                message.sendToTarget();
                return true;
            }
        });

    }

    BaseAdapter mAdapter = new BaseAdapter() {
        int count = 100;
        @Override
        public int getCount() {
            return count;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHandler handler;
            if(convertView == null)
            {
//                handler = new ViewHandler();
//                convertView = ViewGroup.inflate(ListViewTest.this, R.layout.list_item_simple_file, null);
//                handler.button = (Button)convertView.findViewById(R.id.list_item_simple_file_button);
//                handler.imageView = (ImageView)convertView.findViewById(R.id.list_item_simple_file_icon);
//                handler.textView = (TextView)convertView.findViewById(R.id.list_item_simple_file_name);
//                convertView.setTag(handler);
                ImageView i = new ImageView(ListViewTest.this);
                i.setImageResource(R.drawable.chuyin);
                i.setMaxHeight(30);
                i.setScaleType(ImageView.ScaleType.FIT_XY);
                convertView = i;
            }else
            {
                handler = (ViewHandler) convertView.getTag();
            }
//            handler.textView.setText(position + "");
            return convertView;
        }

        class ViewHandler{
            public ImageView imageView;
            public TextView textView;
            public Button button;
        }
    };
}
