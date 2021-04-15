package com.example.resources

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class KotlinActivity : AppCompatActivity() {
    private val data = "{\n" +
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
            "    ]\n" +
            "}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin)
    }
}