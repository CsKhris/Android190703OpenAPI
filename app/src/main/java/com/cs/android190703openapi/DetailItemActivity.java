package com.cs.android190703openapi;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailItemActivity extends AppCompatActivity {

    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            Bitmap bitmap = (Bitmap)msg.obj;
            ImageView imageView = (ImageView)findViewById(R.id.imgview);
            imageView.setImageBitmap(bitmap);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_item);

        Button backbtn = (Button)findViewById(R.id.backbtn);
        backbtn.setOnClickListener(view -> {
            // 현재 화면 제거
            finish();
        });

        // 앞에서 넘겨준 Data 가져오기
        int itemid = getIntent().getIntExtra("itemid", 1);

        Thread th = new Thread(){
            @Override
            public void run(){
                String addr = "http://192.168.0.105:8080/item/getitem?itemid=" + itemid;

                String json = null;
                try{
                    URL url = new URL(addr);

                    HttpURLConnection con = (HttpURLConnection)url.openConnection();

                    StringBuilder sb = new StringBuilder();

                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    while (true){
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

                try{
                    JSONObject item = new JSONObject(json);
                    String itemname = item.getString("itemname");
                    int price = item.getInt("price");
                    String description = item.getString("description");
                    String pictureurl = item.getString("pictureurl");

                    TextView itemn = (TextView)findViewById(R.id.itemname);
                    itemn.setText(itemname);
                    TextView pri = (TextView)findViewById(R.id.price);
                    pri.setText(price + "");
                    TextView des = (TextView)findViewById(R.id.description);
                    des.setText(description);

                    // Image를 Download 받아서 Handler에게 전송
                    URL imageURL = new URL("http://192.168.0.105:8080/item/img/" + pictureurl);

                    Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openStream());

                    Message msg = new Message();
                    msg.obj = bitmap;
                    handler.sendMessage(msg);

                    ImageView imageView = (ImageView)findViewById(R.id.imgview);
                    imageView.setImageBitmap(bitmap);

                }catch (Exception e){
                    Log.e("Parsing Exception", e.getMessage());
                }
            }
        };
        th.start();
    }
}
