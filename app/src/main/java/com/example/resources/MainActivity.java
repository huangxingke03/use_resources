package com.example.resources;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private String data = "{\n" +
            "    \"name\": \"BeJson\",\n" +
            "    \"url\": \"http://www.bejson.com\",\n" +
            "    \"page\": 88,\n" +
            "    \"isNonProfit\": true,\n" +
            "    \"address\": {\n" +
            "        \"street\": \"科技园路.\",\n" +
            "        \"city\": \"江苏苏州\",\n" +
            "        \"country\": \"中国\"\n" +
            "    },\n" +
            "    \"links\": [\n" +
            "        {\n" +
            "            \"name\": \"Google\",\n" +
            "            \"url\": \"http://www.google.com\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"Baidu\",\n" +
            "            \"url\": \"http://www.baidu.com\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"name\": \"SoSo\",\n" +
            "            \"url\": \"http://www.SoSo.com\"\n" +
            "        }\n" +
            "   ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.kotlin).setOnClickListener((v) -> {
            startActivity(new Intent(MainActivity.this, KotlinActivity.class));
        });
//        FileUtil.print();
    }
}