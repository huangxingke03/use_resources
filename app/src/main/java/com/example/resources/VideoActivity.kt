package com.example.resources

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blackshark.market.core.view.video.IVideoActivity
import com.blackshark.market.core.view.video.VideoPlayerManager

class VideoActivity : AppCompatActivity(),IVideoActivity {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
    }

    override fun getPlayManager(): VideoPlayerManager {
        TODO("Not yet implemented")
    }
}