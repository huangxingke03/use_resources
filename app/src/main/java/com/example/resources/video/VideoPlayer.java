package com.example.resources.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.blankj.utilcode.util.NetworkUtils;
import com.example.resources.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.dash.manifest.RepresentationKey;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParser;
import com.google.android.exoplayer2.source.hls.playlist.RenditionKey;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.SsManifestParser;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.StreamKey;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;

import java.util.List;


public class VideoPlayer implements PlaybackPreparer {
    private static final String TAG = "VideoPlayer";

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private MediaSource mediaSource;
    private DefaultTrackSelector trackSelector;
    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private TrackGroupArray lastSeenTrackGroupArray;

    private boolean startAutoPlay;
    private int startWindow;
    private long startPosition;

    private PlayerView playerView;
    private Context mContext;
    private Uri mUri;
    private View mFlowTipLayout;
    private boolean PLAY_USE_FLOW = false;
    private PlayerControlView controllerView;
    private FrameLayout contentFrame;
    private boolean mNetType = true; //true wifi,false flow
    private NetReceiver mNetReceiver;
    private String mFrom;
    private String mPackage;

    private com.google.android.exoplayer2.ControlDispatcher controlDispatcher;

    public VideoPlayer(Context context, PlayerView view) {
        this(context, view, null);
    }
    public VideoPlayer(Context context, PlayerView view, View flowTipLayout) {
        mContext = context;
        playerView = view;
//        playerView.setUseController(false);
        mediaDataSourceFactory = buildDataSourceFactory(true);
        trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().build();
        controlDispatcher = new com.google.android.exoplayer2.DefaultControlDispatcher();
        clearStartPosition();
        mFlowTipLayout = flowTipLayout;
        if (mFlowTipLayout != null && !PLAY_USE_FLOW) {
            mFlowTipLayout.findViewById(R.id.flow_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PLAY_USE_FLOW = true;
                    if (contentFrame != null) {
                        contentFrame.setBackground(null);
                    }
                    if (player != null && player.getPlaybackState() == Player.STATE_READY) {
                        if (mFlowTipLayout != null && mFlowTipLayout.getVisibility() == View.VISIBLE) {
                            if (contentFrame != null) {
                                contentFrame.setBackground(null);
                            }
                            if (controllerView != null) {
                                VideoPlayerController controller = (VideoPlayerController) controllerView;
                                controller.showControl(controller.isFullscreen());
                                controllerView.show();
                            }
                            mFlowTipLayout.setVisibility(View.GONE);
                        }
                        mediaPlay();
                    } else {
                        initializePlayer(mUri);
                    }
                }
            });
        }
        contentFrame = playerView.getOverlayFrameLayout();
        registerNetChangeReceiver();
    }

    public ControlDispatcher getControlDispatcher() {
        return controlDispatcher;
    }

    public void setAnalyticsInfo(String from, String pkg) {
        mFrom = from;
        mPackage = pkg;
    }

    public void initializePlayer(Uri uri) {
        if (uri == null) {
            return;
        }
        mUri = uri;
        if (!checkNetwork()) {
            return;
        }
        if (player == null) {
            TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(mContext, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);

            trackSelector = new DefaultTrackSelector(trackSelectionFactory);
            trackSelector.setParameters(trackSelectorParameters);
            lastSeenTrackGroupArray = null;

            LoadControl loadControl = new DefaultLoadControl();

            player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
            player.addListener(new PlayerEventListener());
            player.setPlayWhenReady(startAutoPlay);

            playerView.setPlayer(player);
            playerView.setPlaybackPreparer(this);
            mediaSource = buildMediaSource(uri);
        }
        boolean haveStartPosition = startWindow != C.INDEX_UNSET;
        if (haveStartPosition) {
            player.seekTo(startWindow, startPosition);
        }
        player.prepare(mediaSource, !haveStartPosition, false);
//        if (mOnPlayerUICallBack != null) {
//            mOnPlayerUICallBack.updateButtonVisibilities();
//        }
    }
    private boolean checkNetwork() {
        if (NetworkUtils.getWifiEnabled()) {
            if (PLAY_USE_FLOW) {
                if (mFlowTipLayout != null) {
                    mFlowTipLayout.setVisibility(View.GONE);
                }
                if (controllerView != null) {
                    ((VideoPlayerController) controllerView).showControl();
                    controllerView.show();
                }
                return true;
            }
            return true;
        } else if (NetworkUtils.isMobileData()) {
            if (PLAY_USE_FLOW) {
                if (mFlowTipLayout != null) {
                    mFlowTipLayout.setVisibility(View.GONE);
                }
                if (controllerView != null) {
                    ((VideoPlayerController) controllerView).showControl();
                    controllerView.show();
                }
                return true;
            } else if (mFlowTipLayout != null) {
                if (controllerView != null) {
                    ((VideoPlayerController) controllerView).hideControl();
                    controllerView.hide();
                }
                if (contentFrame != null) {
                    contentFrame.setBackgroundResource(android.R.color.black);
                }
                mFlowTipLayout.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(mContext, R.string.network_error_tips, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void onNetStatusChanged(boolean wifiEnable) {
        if (wifiEnable) {
            if (mFlowTipLayout != null && mFlowTipLayout.getVisibility() == View.VISIBLE) {
                if (contentFrame != null) {
                    contentFrame.setBackground(null);
                }
                if (controllerView != null) {
                    VideoPlayerController controller = (VideoPlayerController) controllerView;
                    controller.showControl(controller.isFullscreen());
                    controllerView.show();
                }
                mFlowTipLayout.setVisibility(View.GONE);
            }
            if (player == null) {
                initializePlayer(mUri);
            }
        } else {
            if (isPlaying() && !PLAY_USE_FLOW) {
                mediaPause();
                if (controllerView != null) {
                    ((VideoPlayerController) controllerView).hideControl();
                    controllerView.hide();
                }
                if (contentFrame != null) {
                    contentFrame.setBackgroundResource(android.R.color.black);
                }
                mFlowTipLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void preparePlayback() {
        initializePlayer(mUri);
    }

    private MediaSource buildMediaSource(Uri uri) {
        return buildMediaSource(uri, null);
    }

    @SuppressWarnings("unchecked")
    private MediaSource buildMediaSource(Uri uri, @Nullable String overrideExtension) {
        @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new DashManifestParser(), (List<RepresentationKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new SsManifestParser(), (List<StreamKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory)
                        .setPlaylistParser(
                                new FilteringManifestParser<>(
                                        new HlsPlaylistParser(), (List<RenditionKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return PlayerHelper.getInstance().buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private List<?> getOfflineStreamKeys(Uri uri) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
        return PlayerHelper.getInstance().getDownloadTracker().getOfflineStreamKeys(uri);
    }

    private void keepScreenOn(boolean keep) {
        if (playerView != null) {
            playerView.setKeepScreenOn(keep);
        }
    }

    private class PlayerEventListener extends Player.DefaultEventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.v(TAG, "onPlayerStateChanged, playWhenReady:" + playWhenReady + " playbackState:" + playbackState);
            switch (playbackState) {
                case Player.STATE_IDLE: {
                    if (mOnPlayerUICallBack != null) {
                        mOnPlayerUICallBack.idle(playWhenReady);
                        keepScreenOn(true);
                    }
                    break;
                }
                case Player.STATE_BUFFERING: {
                    if (mOnPlayerUICallBack != null) {
                        mOnPlayerUICallBack.buffering(playWhenReady);
                    }
                    break;
                }
                case Player.STATE_READY: {
                    if (mOnPlayerUICallBack != null) {
                        mOnPlayerUICallBack.playReady(playWhenReady);
                    }
                    break;
                }
                case Player.STATE_ENDED: {
                    long current = player.getCurrentPosition()/1000;
                    long total = player.getDuration()/1000;
                    if (mOnPlayerUICallBack != null) {
                        mOnPlayerUICallBack.playEnd(playWhenReady);
                    }
                    keepScreenOn(false);
                    break;
                }
            }
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            Log.v(TAG, "onPositionDiscontinuity, reason:" + reason);
            if (player != null && player.getPlaybackError() != null) {
                // The user has performed a seek whilst in the error state. Update the resume position so
                // that if the user then retries, playback resumes from the position to which they seeked.
                updateStartPosition();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            if (isBehindLiveWindow(e)) {
                clearStartPosition();
                initializePlayer(mUri);
            } else {
                updateStartPosition();

//                if (mOnPlayerUICallBack != null) {
//                    mOnPlayerUICallBack.updateButtonVisibilities();
//                    mOnPlayerUICallBack.showControls();
//                }
            }
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.v(TAG, "onTracksChanged");
//            if (mOnPlayerUICallBack != null) {
//                mOnPlayerUICallBack.updateButtonVisibilities();
//            }
            if (trackGroups != lastSeenTrackGroupArray) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_video);
                    }
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_audio);
                    }
                }
                lastSeenTrackGroupArray = trackGroups;
            }
        }
    }

    private void showToast(@StringRes int id) {

    }

    private void updateTrackSelectorParameters() {
        if (trackSelector != null) {
            trackSelectorParameters = trackSelector.getParameters();
        }
    }

    public long getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : 0L;
    }

    public boolean isPlaying() {
        return player != null
                && player.getPlaybackState() != Player.STATE_ENDED
                && player.getPlaybackState() != Player.STATE_IDLE
                && player.getPlayWhenReady();
    }

    public void setVolume(float audioVolume) {
        player.setVolume(audioVolume);
    }

    public float getVolume() {
        return player.getVolume();
    }

    public void mediaPlayPause() {
        controlDispatcher.dispatchSetPlayWhenReady(player, !player.getPlayWhenReady());
    }

    public void mediaPlay() {
        controlDispatcher.dispatchSetPlayWhenReady(player, true);
    }

    public void mediaPause() {
        controlDispatcher.dispatchSetPlayWhenReady(player, false);
    }

    public void mediaReplay() {
        controlDispatcher.dispatchSeekTo(player, 0, 0);
    }

    public void setControllerView(PlayerControlView controllerView) {
        this.controllerView = controllerView;
    }

    public void releasePlayer() {
        mContext.unregisterReceiver(mNetReceiver);
        keepScreenOn(false);
        if (player != null) {
            updateTrackSelectorParameters();
            updateStartPosition();
            player.release();
            player = null;
            mediaSource = null;
            trackSelector = null;
        }
        controllerView = null;
    }

    private void updateStartPosition() {
        if (player != null) {
            startAutoPlay = player.getPlayWhenReady();
            startWindow = player.getCurrentWindowIndex();
            startPosition = Math.max(0, player.getContentPosition());
        }
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private void clearStartPosition() {
        startAutoPlay = true;
        startWindow = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }

    private OnPlayerUICallBack mOnPlayerUICallBack;

    public void setOnPlayerUICallBack(OnPlayerUICallBack inf) {
        mOnPlayerUICallBack = inf;
    }

    public interface OnPlayerUICallBack {
        void idle(boolean playWhenReady);

        void buffering(boolean playWhenReady);

        void playEnd(boolean playWhenReady);

        void playReady(boolean playWhenReady);
    }

    private void registerNetChangeReceiver() {
        mNetType = NetworkUtils.getWifiEnabled();
        mNetReceiver = new NetReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.ethernet.ETHERNET_STATE_CHANGED");
        filter.addAction("android.net.ethernet.STATE_CHANGE");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mNetReceiver, filter);
    }

    class NetReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mNetType != NetworkUtils.getWifiEnabled()) {
                mNetType = NetworkUtils.getWifiEnabled();
                onNetStatusChanged(mNetType);
            }
        }
    }
}
