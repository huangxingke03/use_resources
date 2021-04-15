package com.example.resources.video;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;

import com.example.resources.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerControlView;

import java.util.Formatter;
import java.util.Locale;


public class VideoPlayerController extends PlayerControlView implements View.OnClickListener {

    private static final String TAG = "PlayerController";
    private final View backView;
    private final ImageView voiceView;
    private final ImageView resizeView;
    private final ImageView voice2View;
    private final ImageView resize2View;
    private float lastVolume;
    private SimpleExoPlayer player;
    private View fullscreenControlView;
    private View smallWindowControlView;
    private View pauseView;
    private ImageView playView;
    private ComponentListener componentListener;
    private com.google.android.exoplayer2.ControlDispatcher controlDispatcher;
    private ImageView exoPlayView;

    private TextView mediaPosition;
    private StringBuilder formatBuilder;
    private Formatter formatter;
    private String mFrom;
    private String mPackage;
    private int enterFullScreenRes = R.drawable.ic_svg_fullscreen;
    private int exitFullScreenRes = R.drawable.ic_svg_shrink;

    public VideoPlayerController(Context context) {
        this(context, null, 0, null);
    }

    public VideoPlayerController(Context context, AttributeSet attrs) {
        this(context, attrs, 0, attrs);
    }

    public VideoPlayerController(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, attrs);
    }

    public VideoPlayerController(Context context, AttributeSet attrs, int defStyleAttr, AttributeSet playbackAttrs) {
        super(context, attrs, defStyleAttr, playbackAttrs);
        fullscreenControlView = findViewById(R.id.fullscreen_control);
        smallWindowControlView = findViewById(R.id.small_window_control);
        backView = findViewById(R.id.exo_back);
        voiceView = findViewById(R.id.exo_voice);
        resizeView = findViewById(R.id.exo_shrink);
        voice2View = findViewById(R.id.btn_voice);
        resize2View = findViewById(R.id.btn_screen_resize);
        if (voiceView != null) {
            voiceView.setTag(false);
            voiceView.setOnClickListener(this);
        }
        if (resizeView != null) {
            resizeView.setTag(false);
        }
        if (voice2View != null) {
            voice2View.setOnClickListener(this);
        }
        exoPlayView = findViewById(R.id.exo_play);
        playView = findViewById(R.id.btn_play);
        if (playView != null) {
            playView.setOnClickListener(this);
        }
        pauseView = findViewById(R.id.btn_pause);
        if (pauseView != null) {
            pauseView.setOnClickListener(this);
        }
        componentListener = new ComponentListener();

        this.formatBuilder = new StringBuilder();
        this.formatter = new Formatter(this.formatBuilder, Locale.getDefault());
        mediaPosition = smallWindowControlView.findViewById(R.id.media_position);
        if (mediaPosition != null) {
            ExoPositionView exoPositionView = findViewById(R.id.exo_position);
            exoPositionView.setOnTextChangeListener(new ExoPositionView.OnTextChangeListener() {
                @Override
                public void onTextChanged(CharSequence text) {
                    mediaPosition.setText(text);
                }
            });
        }
    }

    public void setAnalyticsInfo(String from, String pkg) {
        mFrom = from;
        mPackage = pkg;
    }

    public void showControl(boolean fullscreen) {
        if (fullscreen) {
            fullscreenControlView.setVisibility(View.VISIBLE);
            smallWindowControlView.setVisibility(View.GONE);
        } else {
            fullscreenControlView.setVisibility(View.GONE);
            smallWindowControlView.setVisibility(View.VISIBLE);
        }
    }

    public void showControl() {
        fullscreenControlView.setVisibility(View.GONE);
        smallWindowControlView.setVisibility(View.VISIBLE);
    }

    public void hideControl() {
        fullscreenControlView.setVisibility(View.GONE);
        smallWindowControlView.setVisibility(View.GONE);
    }

    public void setOnBackClickListener(View.OnClickListener onClickListener) {
        if (backView != null) {
            backView.setOnClickListener(onClickListener);
        }
    }

    public void setOnShrinkClickListener(View.OnClickListener onClickListener) {
        if (resizeView != null) {
            resizeView.setOnClickListener(onClickListener);
        }
        if (resize2View != null) {
            resize2View.setOnClickListener(onClickListener);
        }
    }

    public void setFullscreenSize(boolean fullscreen) {
        if (fullscreen) {
            resizeView.setImageResource(exitFullScreenRes);
            resize2View.setImageResource(exitFullScreenRes);
            resizeView.setTag(true);
        } else {
            resizeView.setImageResource(enterFullScreenRes);
            resize2View.setImageResource(enterFullScreenRes);
            resizeView.setTag(false);
        }
    }

    public boolean isFullscreen() {
        return (boolean) resizeView.getTag();
    }

    @Override
    public void setPlayer(Player player) {
        super.setPlayer(player);
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(componentListener);
        }
        if (player instanceof SimpleExoPlayer) {
            this.player = (SimpleExoPlayer) player;
        }
        if (this.player != null) {
            this.player.addListener(componentListener);
        }
        updatePlayPauseButton();
    }

    public void voiceToggle() {
        if (voiceView != null) {
            boolean tag = (boolean) voiceView.getTag();
            if (tag) {
                voiceOff();
            } else {
                voiceOn();
            }
        }
    }

    public void voiceOff() {
        if (voiceView != null && player != null) {
            voiceView.setTag(false);
            lastVolume = player.getVolume();
            player.setVolume(0f);
            voiceView.setImageResource(R.drawable.icon_volume_white_off);
            if (voice2View != null) {
                voice2View.setImageResource(R.drawable.icon_volume_white_off);
            }
        }
    }

    public void voiceOn() {
        if (voiceView != null && player != null) {
            voiceView.setTag(true);
            if (lastVolume > 0) {
                player.setVolume(lastVolume);
            }
            voiceView.setImageResource(R.drawable.icon_volume_white_on);
            if (voice2View != null) {
                voice2View.setImageResource(R.drawable.icon_volume_white_on);
            }
        }
    }

    @Override
    public void setControlDispatcher(@Nullable ControlDispatcher controlDispatcher) {
        super.setControlDispatcher(controlDispatcher);
        this.controlDispatcher = controlDispatcher;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_voice || id == R.id.exo_voice) {
            voiceToggle();
        } else if (id == R.id.btn_play) {
            if (player.getPlaybackState() == Player.STATE_ENDED) {
                controlDispatcher.dispatchSeekTo(player, player.getCurrentWindowIndex(), C.TIME_UNSET);
            } else if (controlDispatcher != null) {
                controlDispatcher.dispatchSetPlayWhenReady(player, true);
            }
        } else if (id == R.id.btn_pause) {
            long current = player.getCurrentPosition() / 1000;
            long total = player.getDuration() / 1000;
            if (controlDispatcher != null) {
                controlDispatcher.dispatchSetPlayWhenReady(player, false);
            }
        }
    }

    private boolean isAttachedToWindow;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
    }

    private boolean isPlaying() {
        return player != null
                && player.getPlaybackState() != Player.STATE_ENDED
                && player.getPlaybackState() != Player.STATE_IDLE
                && player.getPlayWhenReady();
    }

    private void updatePlayPauseButton() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        boolean playing = isPlaying();
        if (playView != null) {
            playView.setVisibility(playing ? View.GONE : View.VISIBLE);
        }
        if (pauseView != null) {
            pauseView.setVisibility(!playing ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void show() {
        super.show();
        updatePlayPauseButton();
    }

    public void hideFullScreenBtn() {
        resize2View.setVisibility(View.GONE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) voice2View.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        voice2View.setLayoutParams(params);
    }

    public void showVoiceBtn() {
        voice2View.setVisibility(View.VISIBLE);
    }

    public void initFullScreenIconRes(int enterRes, int exitRes) {
        this.enterFullScreenRes = enterRes;
        this.exitFullScreenRes = exitRes;
        setFullscreenSize(isFullscreen());
    }

    private final class ComponentListener extends Player.DefaultEventListener {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            updatePlayPauseButton();
            boolean playEnd = playbackState == Player.STATE_ENDED;
            playView.setImageResource(playEnd ? R.drawable.zs_svg_sendagain : R.drawable.ic_svg_play);
            exoPlayView.setImageResource(playEnd ? R.drawable.zs_svg_sendagain : R.drawable.ic_svg_play);
        }
    }
}
