package com.cs.android190703openapi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AllItemActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> list;

    // ListView의 Data를 재출력 해주는 Handler
    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_item);

        listView = (ListView)findViewById(R.id.itemlist);
        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(AllItemActivity.this, android.R.layout.simple_list_item_1, list);

        listView.setAdapter(adapter);

        Thread th = new Thread(){
            @Override
            public void run(){
                String json="";
                try{
                    String addr = "http://192.168.0.105:8080/item/allitem";
                    URL url = new URL(addr);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();

                    // StringBuilder는 자신에게 작업을 수행 - 수정 가능
                    // String은 자신에게 작업을 수행하지 못합니다. - 읽기 전용
                    // 부분적으로 읽어가면서 추가할 때는 StringBuilder 사용
                    // String을 사용하게 되면 계속 새로운 곳에 복사하면서 작업 합니다.
                    StringBuilder sb = new StringBuilder();

                    // BufferedReader는 입출력을 모아서 처리하기 위하여 사용
                    // Buffer를 사용하지 않으면 입출력할 때 마다 Native Method를 호출하여 입출력 효율이 떨어집니다.
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    while (true) {
                        String line = br.readLine();
                        if(line == null){
                            break;
                        }
                        sb.append(line);
                    }
                    json = sb.toString();
                    br.close();
                    con.disconnect();

                }catch (Exception e){
                    Log.e("Download Exception", e.getMessage());
                }
                //Log.e("json", json);

                try {
                    JSONArray items = new JSONArray(json);
                    int len = items.length();
                    for(int i=0 ; i<len ; i=i+1){
                        JSONObject item = items.getJSONObject(i);
                        list.add(item.getString("itemname"));
                    }
                    handler.sendEmptyMessage(0);
                }catch (Exception e){
                    Log.e("Parsing Exception", e.getMessage());
                }
            }
        };
        th.start();

        // List View의 Cell을 선택했을 때 Event 처리
        listView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                // 다음에 출력할 Intent 만들기
                Intent intent = new Intent(AllItemActivity.this, DetailItemActivity.class);

                // Data 설정
                intent.putExtra("itemid", position+1);

                // 화면 출력
                startActivity(intent);
            }
        });
    }
}
