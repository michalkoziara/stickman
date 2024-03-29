package com.litkaps.stickman.mediaviewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.litkaps.stickman.R;

import io.reactivex.rxjava3.schedulers.Schedulers;

public class VideoViewActivity extends AppCompatActivity {

    private static final String TAG = "VideoPlayActivity";

    private final MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mPlayPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_filled_24);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                case PlaybackStateCompat.STATE_STOPPED:
                    mPlayPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_filled_24);
                    break;
                case PlaybackStateCompat.STATE_BUFFERING:
                case PlaybackStateCompat.STATE_NONE:
                case PlaybackStateCompat.STATE_ERROR:
                case PlaybackStateCompat.STATE_CONNECTING:
                case PlaybackStateCompat.STATE_FAST_FORWARDING:
                case PlaybackStateCompat.STATE_REWINDING:
                case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                default:
                    break;
            }
        }
    };

    private MediaPlayer mMediaPlayer;
    private Uri mVideoUri;
    private ImageButton mPlayPauseButton;
    private SurfaceView mSurfaceView;

    private MediaControllerCompat mController;
    private MediaControllerCompat.TransportControls mControllerTransportControls;

    private PlaybackStateCompat.Builder mPBuilder;
    private MediaSessionCompat mSession;

    private class MediaSessionCallback extends MediaSessionCompat.Callback implements
            SurfaceHolder.Callback,
            MediaPlayer.OnCompletionListener,
            AudioManager.OnAudioFocusChangeListener {

        private final Context mContext;
        private final AudioManager mAudioManager;
        private final IntentFilter mNoisyIntentFilter;
        private final AudioBecommingNoisy mAudioBecommingNoisy;

        public MediaSessionCallback(Context context) {
            super();

            mContext = context;
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            mAudioBecommingNoisy = new AudioBecommingNoisy();
            mNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            mSurfaceView.getHolder().addCallback(this);
        }

        private class AudioBecommingNoisy extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                mediaPause();
            }
        }

        @Override
        public void onPlay() {
            super.onPlay();

            mediaPlay();
        }

        @Override
        public void onPause() {
            super.onPause();

            mediaPause();
        }

        @Override
        public void onStop() {
            super.onStop();

            releaseResources();
        }

        private void releaseResources() {
            mSession.setActive(false);
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }

        private void mediaPlay() {
            if (mMediaPlayer != null) {
                registerReceiver(mAudioBecommingNoisy, mNoisyIntentFilter);
                int requestAudioFocusResult = mAudioManager.requestAudioFocus(
                        this,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN
                );

                if (requestAudioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mSession.setActive(true);
                    mPBuilder.setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_STOP);
                    mPBuilder.setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            mMediaPlayer.getCurrentPosition(),
                            1.0f,
                            SystemClock.elapsedRealtime()
                    );
                    mSession.setPlaybackState(mPBuilder.build());
                    mMediaPlayer.start();
                }
            }
        }

        private void mediaPause() {
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
                mPBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_STOP);
                mPBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                        mMediaPlayer.getCurrentPosition(), 1.0f, SystemClock.elapsedRealtime());
                mSession.setPlaybackState(mPBuilder.build());
                mAudioManager.abandonAudioFocus(this);
                unregisterReceiver(mAudioBecommingNoisy);
            }
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
            Schedulers.io().createWorker().schedule(() -> {
                mMediaPlayer = MediaPlayer.create(mContext, mVideoUri, surfaceHolder);
                mMediaPlayer.setOnCompletionListener(this);
            });
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

        }

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mPBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_STOP);
            mPBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                    mediaPlayer.getCurrentPosition(), 1.0f, SystemClock.elapsedRealtime());
            mSession.setPlaybackState(mPBuilder.build());
        }

        @Override
        public void onAudioFocusChange(int audioFocusChanged) {
            switch (audioFocusChanged) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS:
                    mediaPause();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    mediaPlay();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_video);

        mPlayPauseButton = findViewById(R.id.video_play_pause_button);
        mSurfaceView = findViewById(R.id.videoSurfaceView);

        Intent callingIntent = this.getIntent();
        if (callingIntent != null) {
            mVideoUri = callingIntent.getData();
        }

        mSession = new MediaSessionCompat(this, TAG);
        mSession.setCallback(new MediaSessionCallback(this));
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mPBuilder = new PlaybackStateCompat.Builder();
        mController = new MediaControllerCompat(this, mSession);
        mControllerTransportControls = mController.getTransportControls();

        findViewById(R.id.video_play_pause_button).setOnClickListener(this::playPauseClick);
    }

    public void playPauseClick(View view) {
        if (mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            mControllerTransportControls.pause();
        } else if (mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED ||
                mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_STOPPED ||
                mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) {
            mControllerTransportControls.play();
        }
    }

    @Override
    protected void onStop() {
        mController.unregisterCallback(mControllerCallback);
        if (mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ||
                mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
            mControllerTransportControls.stop();
        }

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mController.registerCallback(mControllerCallback);
        mPBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mSession.setPlaybackState(mPBuilder.build());
    }

    @Override
    protected void onPause() {
        if (mController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            mControllerTransportControls.pause();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSession.release();
    }
}