package com.blackshark.market.core.view.video

import android.animation.Animator
import android.app.Activity
import android.graphics.PointF
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.example.resources.R
import com.example.resources.video.BaseVideoViewHolder
import com.example.resources.video.VideoPlayer
import com.example.resources.video.VideoPlayerController
import com.google.android.exoplayer2.ui.PlayerView
import java.lang.ref.WeakReference

class VideoPlayerManager(private val activity: Activity, private val playerContainer: ViewGroup) {

    companion object {
        private const val TAG = "VideoPlayerManager"
    }

    var mVideoPlayerController: VideoPlayerController? = null
    var mPlayerRootView: View? = null
    var mPlayerView: PlayerView? = null
    var mFlowTipView: View? = null
    var mVideoPlayer: VideoPlayer? = null
    private val mTmpScaleCenter: PointF = PointF()
    private var mScaleX: Float = 1f
    private var mScaleY: Float = 1f
    private var mPlayerHolderRfs: WeakReference<BaseVideoViewHolder?> = WeakReference(null)

    init {
        initPlayerView()
    }

    private fun initPlayerView() {
        mPlayerRootView = View.inflate(activity, R.layout.layout_player, null)
        val layoutParams: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        mPlayerRootView?.layoutParams = layoutParams
        mPlayerView = mPlayerRootView?.findViewById(R.id.player_view)
        mFlowTipView = mPlayerRootView?.findViewById(R.id.flow_tip_container)
        mVideoPlayerController = mPlayerView?.findViewById(R.id.exo_controller)
        mVideoPlayerController?.setFullscreenSize(false)
        mVideoPlayerController?.showControl(false)

        mVideoPlayerController?.setOnShrinkClickListener {
            val fullscreen: Boolean? = mVideoPlayerController?.isFullscreen
            if (fullscreen!!) {
                exitFullScreen()
                mVideoPlayerController?.showControl(false)
                mVideoPlayerController?.setFullscreenSize(false)
            } else {
                enterFullscreen()
                mVideoPlayerController?.showControl(true)
                mVideoPlayerController?.setFullscreenSize(true)
            }
        }

        mPlayerRootView?.setOnClickListener {
            //            mVideoPlayer?.mediaPlayPause()
        }

        mVideoPlayerController?.setOnBackClickListener {
            exitFullScreen()
            mVideoPlayerController?.showControl(false)
            mVideoPlayerController?.setFullscreenSize(false)
        }

        val displayMetrics: DisplayMetrics = activity.applicationContext.resources.displayMetrics
        val itemWidth: Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 240f, displayMetrics) - 6f;
        val itemHeight: Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150f, displayMetrics) - 12f;
        mScaleX = itemWidth / displayMetrics.widthPixels
        mScaleY = itemHeight / displayMetrics.heightPixels
    }

    fun getPlayerView(uri: String, from: String, pkg: String): View? {
        mVideoPlayer = VideoPlayer(activity.applicationContext, mPlayerView, mFlowTipView)
        mVideoPlayer?.setOnPlayerUICallBack(PlayCallBack())
        mVideoPlayer?.setAnalyticsInfo(from, pkg)
        mVideoPlayerController?.setControlDispatcher(mVideoPlayer?.controlDispatcher)
        mVideoPlayerController?.setFullscreenSize(false)
        mVideoPlayerController?.showControl(false)
        mVideoPlayer?.setControllerView(mVideoPlayerController)
        mVideoPlayer?.initializePlayer(Uri.parse(uri))
        return mPlayerRootView
    }

    fun releaseAndRemovePlayerView() {
        releaseVideoPlayer()
        if (mPlayerRootView?.parent != null) {
            (mPlayerRootView?.parent as ViewGroup).removeView(mPlayerRootView)
        }
        playerContainer.visibility = View.GONE
    }

    private fun releaseVideoPlayer() {
        var pkg: String? = null
        if (mVideoPlayer != null) {
            mVideoPlayer?.releasePlayer()
            mVideoPlayer = null
        }
    }

    fun checkAndReleaseVideo() {
        if (mVideoPlayer != null && mVideoPlayer?.isPlaying!!) {
            releaseAndRemovePlayerView()
        }
    }

    private fun enterFullscreen() {
        val local: IntArray = IntArray(2)
        mPlayerRootView?.getLocationOnScreen(local)
        mTmpScaleCenter.x = local[0] / (1f - mScaleX)
        mTmpScaleCenter.y = local[1] / (1f - mScaleY)

        if (mPlayerRootView?.parent != null) {
            (mPlayerRootView?.parent as ViewGroup).removeView(mPlayerRootView)
        }
        playerContainer.visibility = View.VISIBLE
        playerContainer.addView(mPlayerRootView, 0)
        val isPlaying = isPlaying()
        if (isPlaying) {
            mVideoPlayer?.mediaPause()
        }
        mPlayerRootView?.pivotX = mTmpScaleCenter.x
        mPlayerRootView?.pivotY = mTmpScaleCenter.y
        mPlayerRootView?.scaleX = mScaleX
        mPlayerRootView?.scaleY = mScaleY

        mPlayerRootView?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(200L)?.setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (isPlaying) {
                    mVideoPlayer?.mediaPlay()
                }
            }
        })?.start()
    }

    fun exitFullScreen() {
        val isPlaying = isPlaying()
        if (isPlaying) {
            mVideoPlayer?.mediaPause()
        }
        mPlayerRootView?.animate()?.scaleX(mScaleX)?.scaleY(mScaleY)?.setDuration(200L)?.setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                mPlayerRootView?.scaleX = 1f
                mPlayerRootView?.scaleY = 1f
                restorePlayerViewToListView(isPlaying)
            }
        })?.start()
    }

    private fun restorePlayerViewToListView(isPlaying: Boolean) {
        if (mPlayerRootView?.parent != null) {
            (mPlayerRootView?.parent as ViewGroup).removeView(mPlayerRootView)
        }
        addPlayerViewToListView(mPlayerRootView)
        playerContainer.visibility = View.GONE
        if (isPlaying) {
            mVideoPlayer?.mediaPlay()
        }
    }

    fun isPlaying(): Boolean {
        return mVideoPlayer != null && mVideoPlayer?.isPlaying!!
    }

    inner class PlayCallBack : VideoPlayer.OnPlayerUICallBack {
        override fun idle(playWhenReady: Boolean) {
            Log.v(TAG, "idle, playWhenReady:$playWhenReady")
        }

        override fun buffering(playWhenReady: Boolean) {
            Log.v(TAG, "buffering, playWhenReady:$playWhenReady")
        }

        override fun playEnd(playWhenReady: Boolean) {
            Log.v(TAG, "playEnd, playWhenReady:$playWhenReady")
        }

        override fun playReady(playWhenReady: Boolean) {
            Log.v(TAG, "playReady, playWhenReady:$playWhenReady")
        }
    }

    private fun addPlayerViewToListView(view: View?) {
        getVideoViewHolder()?.let { holderRfs ->
            if (view != null) {
                holderRfs.getPlayContainer()?.visibility = View.VISIBLE
                holderRfs.getPlayContainer()?.addView(view)
            }
        }
    }

    fun onBackPressed(): Boolean {
        if (playerContainer.visibility == View.VISIBLE) {
            val fullscreen = mVideoPlayerController?.isFullscreen?: false
            if (fullscreen) {
                exitFullScreen()
                mVideoPlayerController?.showControl(false)
                mVideoPlayerController?.setFullscreenSize(false)
                return true
            }
        }
        return false
    }

    fun playVideoInViewHolder(holder: BaseVideoViewHolder, url: String, from: String, pkg: String) {
        Log.i(TAG, "playVideoInViewHolder:::url = $url")
        checkAndReleaseVideo()
        val view = getPlayerView(url, from, pkg)
        if (view?.parent != null) {
            (view.parent as ViewGroup).removeView(view)
        }
        mVideoPlayerController?.setAnalyticsInfo(from, pkg)
        mVideoPlayerController?.voiceOff()
        mPlayerHolderRfs = WeakReference(holder)
        holder.getPlayContainer().visibility = View.VISIBLE
        holder.getPlayContainer().addView(view)
    }

    fun getVideoViewHolder(): BaseVideoViewHolder? {
        return mPlayerHolderRfs.get()
    }
}