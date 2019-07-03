package com.cs.android190703openapi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.helper.HttpConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText keyword;
    ListView listview;

    ArrayAdapter<String> adapter;
    ArrayList<String> list;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        keyword = (EditText)findViewById(R.id.keyword);
        listview = (ListView)findViewById(R.id.listview);
        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        Button saerch = (Button)findViewById(R.id.searchbtn);

        saerch.setOnClickListener(view->{
            // Data를 Download 받아야 하므로 Thread를 이용 합니다.
            Thread th = new Thread(){
                @Override
                public void run() {
                    // 주소 생성
                    String key = keyword.getText().toString().trim();

                    // 문자열을 가져온 결과 저장
                    String json = null;
                    try {
                        // 주소 만들기
                        // 한글 Encoding
                        key = URLEncoder.encode(key, "utf-8");
                        String addr = "https://dapi.kakao.com/v3/search/book?target=title&query=" + key;
                        URL url = new URL(addr);

                        // 연결
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();

                        // Header 만들기
                        con.setRequestProperty("Authorization", "KakaoAK a3605fea0b596b579e7644d2a8ba9738");

                        // Option 설정
                        con.setConnectTimeout(30000);
                        con.setUseCaches(false);

                        // 요청에 정상적으로 응답을 하면
                        if (con.getResponseCode() == 200) {
                            // Data를 문자열로 읽기 위한 Stream 생성
                            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                            StringBuilder sb = new StringBuilder();

                            // Data 읽기
                            while (true) {
                                String line = br.readLine();
                                if (line == null) {
                                    break;
                                }
                                sb.append(line);
                            }
                            json = sb.toString();
                            br.close();
                            con.disconnect();
                        }

                    } catch (Exception e) {
                        Log.e("Download Exception", e.getMessage());
                    }
                    Log.e("JSON", json);
                    try {
                        // JSON Parsing - Price와 Title

                        // 문자열을 JSON 객체로 변환
                        JSONObject documents = new JSONObject(json);

                        // Documents Key에 해당하는 배열 가져오기
                        JSONArray books = documents.getJSONArray("documents");

                        // 배열 순회
                        list.clear();
                        for (int i = 0; i < books.length(); i = i + 1) {
                            JSONObject book = books.getJSONObject(i);
                            list.add(book.getString("title") + ":" + book.getInt("price"));
                        }

                        // Handler 호출
                        handler.sendEmptyMessage(0);

                        // Keyboard를 화면에서 제거하기 위한 Code
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(keyword.getWindowToken(), 0);

                    } catch (Exception e) {
                        Log.e("Parsing Exception", e.getMessage());
                    }
                }

            };
            th.start();
        });
    }
}
