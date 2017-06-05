package com.cairongcai.cuslistview;

import android.graphics.Color;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cairongcai.cuslistview.view.RefreshListview;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RefreshListview cus_listview;
    private ArrayList<String> datalist;
    private Myadapter myadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标头,必须在加载页面前调用，不然程序调用
            setContentView(R.layout.activity_main);
            cus_listview = (RefreshListview) findViewById(R.id.cus_listview);
         cus_listview.setRefreshListener(new RefreshListview.onRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread()
                {
                    @Override
                    public void run() {
                        SystemClock.sleep(2000);
                        datalist.add(0,"我是下拉出来的数据");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myadapter.notifyDataSetChanged();
                                cus_listview.onRefreshComplete();
                            }
                        });
                    }
                }.start();

            }

            @Override
            public void onLoadmore() {
                new Thread()
                {
                    @Override
                    public void run() {
                        SystemClock.sleep(2000);
                        datalist.add("我是加载出来的数据1");
                        datalist.add("我是加载出来的数据2");
                        datalist.add("我是加载出来的数据3");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myadapter.notifyDataSetChanged();
                                cus_listview.onRefreshComplete();
                            }
                        });
                    }

                }.start();

            }
        });
        datalist = new ArrayList<String>();
        for (int i = 0; i < 30; i++) {
            datalist.add("这是一条listview数据");
        }
        myadapter = new Myadapter();
        cus_listview.setAdapter(myadapter);
        }



    class Myadapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return datalist.size();
        }

        @Override
        public Object getItem(int position) {
            return datalist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView=new TextView(parent.getContext());
            textView.setText(datalist.get(position));
            textView.setTextSize(18f);
            textView.setTextColor(Color.BLACK);

            return textView;
        }
    }
}
