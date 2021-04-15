package com.example.resources.video;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction;
import com.google.android.exoplayer2.source.dash.offline.DashDownloadAction;
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadAction;
import com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloadAction;
import com.google.android.exoplayer2.upstream.DataSink;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSinkFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

public class PlayerHelper {
    private static PlayerHelper INSTANCE;
    private static final Object lockObj = new Object();

    public static final String FLAVOR = "noExtensions";
    private static final String DOWNLOAD_ACTION_FILE = "actions";
    private static final String DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";
    private static final int MAX_SIMULTANEOUS_DOWNLOADS = 2;
    private static final DownloadAction.Deserializer[] DOWNLOAD_DESERIALIZERS =
            new DownloadAction.Deserializer[] {
                    DashDownloadAction.DESERIALIZER,
                    HlsDownloadAction.DESERIALIZER,
                    SsDownloadAction.DESERIALIZER,
                    ProgressiveDownloadAction.DESERIALIZER
            };

    protected String userAgent;

    private File downloadDirectory;
    private Cache downloadCache;
    private DownloadManager downloadManager;
    private DownloadTracker downloadTracker;

    private Context mContext;

    private PlayerHelper() {
        String[] exts = new String[]{""};
        Uri[] uris = new Uri[]{Uri.parse("")};
    }

    public static PlayerHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (lockObj) {
                if (INSTANCE == null) {
                    INSTANCE = new PlayerHelper();
                }
            }
        }
        return INSTANCE;
    }

    public void initContext(Context context) {
        mContext = context.getApplicationContext();
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = Util.getUserAgent(mContext, "BSAMAgent");
        }
    }

    /** Returns a {@link DataSource.Factory}. */
    public DataSource.Factory buildDataSourceFactory(TransferListener<? super DataSource> listener) {
        DefaultDataSourceFactory upstreamFactory =
                new DefaultDataSourceFactory(mContext, listener, buildHttpDataSourceFactory(listener));
        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache());
    }

    /** Returns a {@link HttpDataSource.Factory}. */
    public HttpDataSource.Factory buildHttpDataSourceFactory(
            TransferListener<? super DataSource> listener) {
        return new DefaultHttpDataSourceFactory(userAgent, listener);
    }

    /** Returns whether extension renderers should be used. */
    public boolean useExtensionRenderers() {
        return "withExtensions".equals(FLAVOR);
    }

    public DownloadManager getDownloadManager() {
        initDownloadManager();
        return downloadManager;
    }

    public DownloadTracker getDownloadTracker() {
        initDownloadManager();
        return downloadTracker;
    }

    private synchronized void initDownloadManager() {
        if (downloadManager == null) {
            DownloaderConstructorHelper downloaderConstructorHelper =
                    new DownloaderConstructorHelper(
                            getDownloadCache(), buildHttpDataSourceFactory(/* listener= */ null));
            downloadManager =
                    new DownloadManager(
                            downloaderConstructorHelper,
                            MAX_SIMULTANEOUS_DOWNLOADS,
                            DownloadManager.DEFAULT_MIN_RETRY_COUNT,
                            new File(getDownloadDirectory(), DOWNLOAD_ACTION_FILE),
                            DOWNLOAD_DESERIALIZERS);
            downloadTracker =
                    new DownloadTracker(
                            /* context= */ mContext,
                            buildDataSourceFactory(/* listener= */ null),
                            new File(getDownloadDirectory(), DOWNLOAD_TRACKER_ACTION_FILE),
                            DOWNLOAD_DESERIALIZERS);
            downloadManager.addListener(downloadTracker);
        }
    }

    private synchronized Cache getDownloadCache() {
        if (downloadCache == null) {
            File downloadContentDirectory = new File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor());
        }
        return downloadCache;
    }

    private File getDownloadDirectory() {
        if (downloadDirectory == null) {
            downloadDirectory = mContext.getExternalFilesDir(null);
            if (downloadDirectory == null) {
                downloadDirectory = mContext.getFilesDir();
            }
        }
        return downloadDirectory;
    }

    private static CacheDataSourceFactory buildReadOnlyCacheDataSource(
            DefaultDataSourceFactory upstreamFactory, Cache cache) {
        DataSink.Factory cacheWriteDataSinkFactory = new CacheDataSinkFactory(cache, Long.MAX_VALUE);
        return new CacheDataSourceFactory(
                cache,
                upstreamFactory,
                new FileDataSourceFactory(),
                cacheWriteDataSinkFactory,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                /* eventListener= */ null);
    }
}
