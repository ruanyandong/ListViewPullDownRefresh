package com.example.ai.listviewxialashuaxin;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * litView下拉刷新
 */

public class MainActivity extends AppCompatActivity implements ReFlashListView.IReflashListener{
    private List<String> data=new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private ReFlashListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        arrayAdapter=new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1,data);
        listView=(ReFlashListView) findViewById(R.id.list_view);
        listView.setInterface(this);
        listView.setAdapter(arrayAdapter);
    }

    private void initData(){
        for(int i=0;i<20;i++){
            data.add(""+i);
        }
    }

    private void getMoredata(){
        for(int i=20;i<30;i++){
            data.add(""+i);
        }
    }

    @Override
    public void onReflash(){
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //获取到最新数据
                getMoredata();
                //通知界面显示
                arrayAdapter.notifyDataSetChanged();
                //通知listview刷新数据完毕
                listView.reflashComplete();
            }
        },2000);

    }
}
