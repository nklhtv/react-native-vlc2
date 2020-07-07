package com.stremio.vlcvideo;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.SurfaceView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.ThemedReactContext;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.text.MessageFormat;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_REWIND;
import static android.view.KeyEvent.KEYCODE_SPACE;

public final class VLCVideoView extends SurfaceView {

    private static final String MEDIA_ERROR_MESSAGE = "VLC encountered an error with this media.";
    private static final String CHANNEL_ID_RESOURCE_NAME = "react_native_vlc2_channel_id";
    private static final String SMALL_ICON_RESOURCE_NAME = "react_native_vlc2_small_icon";
    private static final String LARGE_ICON_RESOURCE_NAME = "react_native_vlc2_large_icon";
    private static final String PLAY_ICON_RESOURCE_NAME = "react_native_vlc2_play_icon";
    private static final String PAUSE_ICON_RESOURCE_NAME = "react_native_vlc2_pause_icon";
    private static final String PLAY_INTENT_ACTION = "VLCVideo:Play";
    private static final String PAUSE_INTENT_ACTION = "VLCVideo:Pause";
    private static final int D_PAD_SEEK_TIME = 30000;

    public static final int PLAYBACK_NOTIFICATION_ID = 11740;

    private String mTitle;
    private boolean mPlayInBackground;
    private boolean mPlaybackStarted;
    private boolean mIsSeekRequested;
    private final ThemedReactContext mThemedReactContext;
    private final LibVLC mLibVLC;
    private final VLCVideoCallbackManager mCallbackManager;
    private final VLCVideoEventEmitter mEventEmitter;
    private final MediaPlayer mMediaPlayer;

    private final VLCVideoCallbackManager.OnKeyDownCallback mOnKeyDownCallback = new VLCVideoCallbackManager.OnKeyDownCallback() {
        @Override
        public boolean onKeyDown(final int keyCode, final KeyEvent keyEvent) {
            final int action = keyEvent.getAction();
            if (action == ACTION_DOWN) {
                switch (keyCode) {
                    case KEYCODE_SPACE:
                    case KEYCODE_MEDIA_PLAY_PAUSE:
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.pause();
                        } else {
                            mMediaPlayer.play();
                        }
                        return true;
                    case KEYCODE_MEDIA_FAST_FORWARD:
                    case KEYCODE_MEDIA_REWIND:
                        if (mMediaPlayer.isSeekable()) {
                            final int multiplier = keyCode == KEYCODE_MEDIA_REWIND ? -1 : 1;
                            final long seekTime = Math.max(Math.min(mMediaPlayer.getTime() + (multiplier * D_PAD_SEEK_TIME), mMediaPlayer.getLength() - 1000), 0);
                            VLCVideoView.this.seek(seekTime);
                        }
                        return true;
                }
            }
            return false;
        }
    };
    private final VLCVideoCallbackManager.IntentCallback mIntentCallback = new VLCVideoCallbackManager.IntentCallback() {

        @Override
        public boolean onNewIntent(final Intent intent) {
            final String action = intent != null && intent.getAction() != null ? intent.getAction() : "";
            switch (action) {
                case PLAY_INTENT_ACTION:
                    VLCVideoView.this.attachVLCVoutViews();
                    mMediaPlayer.play();
                    return true;
                case PAUSE_INTENT_ACTION:
                    VLCVideoView.this.attachVLCVoutViews();
                    mMediaPlayer.pause();
                    return true;
                default:
                    return false;
            }
        }
    };
    private final LifecycleEventListener mLifecycleEventListener = new LifecycleEventListener() {

        @Override
        public void onHostResume() {
            VLCVideoView.this.attachVLCVoutViews();
        }

        @Override
        public void onHostPause() {
            try {
                if (!mMediaPlayer.isReleased() && !mPlayInBackground) {
                    mMediaPlayer.pause();
                }
            } catch (final Throwable e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onHostDestroy() {
        }

    };
    private final MediaPlayer.EventListener mMediaPlayerEventListener = new MediaPlayer.EventListener() {

        @Override
        public void onEvent(final MediaPlayer.Event mediaEvent) {
            final int eventType = mediaEvent.type;
            switch (eventType) {
                case MediaPlayer.Event.EndReached:
                    mEventEmitter.emitOnEndReached();
                    VLCVideoView.this.stop();
                    VLCVideoView.this.clearPlaybackNotification();
                    break;
                case MediaPlayer.Event.EncounteredError:
                    mEventEmitter.emitOnError(MEDIA_ERROR_MESSAGE, true);
                    VLCVideoView.this.stop();
                    VLCVideoView.this.clearPlaybackNotification();
                    break;
                case MediaPlayer.Event.Paused:
                    mEventEmitter.emitOnPaused();
                    VLCVideoView.this.updatePlaybackNotification();
                    break;
                case MediaPlayer.Event.TimeChanged:
                    final double time = mMediaPlayer.getTime();
                    mEventEmitter.emitOnTimeChanged(time);
                    if (mIsSeekRequested) {
                        mIsSeekRequested = false;
                        mEventEmitter.emitOnSeekPerformed();
                    }
                    break;
                case MediaPlayer.Event.Playing:
                    if (!mPlaybackStarted) {
                        mPlaybackStarted = true;
                        mMediaPlayer.setSpuTrack(-1);
                    }
                    final double duration = mMediaPlayer.getLength();
                    mEventEmitter.emitOnPlaying(duration);
                    final int subtitleTrackId = mMediaPlayer.getSpuTrack();
                    mEventEmitter.emitOnSelectedSubtitleTrackIdChanged(subtitleTrackId);
                    final int audioTrackId = mMediaPlayer.getAudioTrack();
                    mEventEmitter.emitOnSelectedAudioTrackIdChanged(audioTrackId);
                    final MediaPlayer.TrackDescription[] subtitleTracks = mMediaPlayer.getSpuTracks();
                    mEventEmitter.emitOnSubtitleTracksChanged(subtitleTracks);
                    final MediaPlayer.TrackDescription[] audioTracks = mMediaPlayer.getAudioTracks();
                    mEventEmitter.emitOnAudioTracksChanged(audioTracks);
                    VLCVideoView.this.updatePlaybackNotification();
                    break;
                case MediaPlayer.Event.Buffering:
                    final double buffering = mediaEvent.getBuffering();
                    mEventEmitter.emitOnBuffering(buffering);
                    break;
            }
        }

    };

    public VLCVideoView(final ThemedReactContext themedReactContext, final LibVLC libVLC, final VLCVideoCallbackManager callbackManager) {
        super(themedReactContext);

        mThemedReactContext = themedReactContext;
        mLibVLC = libVLC;
        mCallbackManager = callbackManager;
        mEventEmitter = new VLCVideoEventEmitter(VLCVideoView.this, mThemedReactContext);
        mMediaPlayer = new MediaPlayer(mLibVLC);

        setBackgroundResource(R.drawable.video_view_background);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        VLCVideoView.this.attachVLCVoutViews();
        if (mCallbackManager != null) {
            mCallbackManager.addCallback(mIntentCallback);
            mCallbackManager.addCallback(mOnKeyDownCallback);
        }

        mThemedReactContext.addLifecycleEventListener(mLifecycleEventListener);
        mMediaPlayer.setEventListener(mMediaPlayerEventListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        VLCVideoView.this.clearPlaybackNotification();
        VLCVideoView.this.detachVLCVoutViews();
        if (mCallbackManager != null) {
            mCallbackManager.removeCallback(mIntentCallback);
            mCallbackManager.removeCallback(mOnKeyDownCallback);
        }

        mThemedReactContext.removeLifecycleEventListener(mLifecycleEventListener);
        mMediaPlayer.setEventListener(null);
        try {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            final int width = right - left;
            final int height = bottom - top;
            if (width * height == 0) {
                return;
            }

            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setWindowSize(width, height);
        }
    }

    public void loadMedia(final String sourceUrl, final long startTime, final boolean autoplay, final boolean hwDecoderEnabled, final String title) {
        if (sourceUrl == null || sourceUrl.isEmpty()) {
            return;
        }

        final Uri newSourceUri = Uri.parse(sourceUrl);
        final Media oldMedia = mMediaPlayer.getMedia();
        if (oldMedia != null) {
            final Uri oldSourceUri = oldMedia.getUri();
            if (oldSourceUri.compareTo(newSourceUri) == 0) {
                return;
            }
        }

        VLCVideoView.this.stop();
        final Media newMedia = new Media(mLibVLC, newSourceUri);
        newMedia.setHWDecoderEnabled(hwDecoderEnabled, false);

        if (startTime > 0) {
            final long startTimeInSeconds = startTime / 1000;
            final String startTimeOption = MessageFormat.format(":start-time={0}", String.valueOf(startTimeInSeconds));
            newMedia.addOption(startTimeOption);
            final String subtitleTrackOption = MessageFormat.format(":sub-track-id={0}", String.valueOf(Integer.MAX_VALUE));
            newMedia.addOption(subtitleTrackOption);
        }

        mTitle = title;
        mMediaPlayer.setMedia(newMedia);
        if (autoplay) {
            mMediaPlayer.play();
        }

        mEventEmitter.emitOnSelectedSubtitleTrackIdChanged(mMediaPlayer.getSpuTrack());
        mEventEmitter.emitOnSelectedAudioTrackIdChanged(mMediaPlayer.getAudioTrack());
        mEventEmitter.emitOnSubtitleTracksChanged(mMediaPlayer.getSpuTracks());
        mEventEmitter.emitOnAudioTracksChanged(mMediaPlayer.getAudioTracks());
        VLCVideoView.this.updatePlaybackNotification();
    }

    public void play() {
        mMediaPlayer.play();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void requestSeek(final long time) {
        mIsSeekRequested = true;
        mEventEmitter.emitOnSeekRequested(time);
    }

    public void seek(final long time) {
        requestSeek(time);
        mMediaPlayer.setTime(time);
        mMediaPlayer.play();
    }

    public void setSubtitleTrack(final int id) {
        mMediaPlayer.setSpuTrack(id);
        mEventEmitter.emitOnSelectedSubtitleTrackIdChanged(mMediaPlayer.getSpuTrack());
    }

    public void setAudioTrack(final int id) {
        mMediaPlayer.setAudioTrack(id);
        mEventEmitter.emitOnSelectedAudioTrackIdChanged(mMediaPlayer.getAudioTrack());
    }

    public void setPlayInBackground(final boolean playInBackground) {
        mPlayInBackground = playInBackground;
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public boolean isSeekable() {
        return mMediaPlayer.isSeekable();
    }

    public long getTime() {
        return mMediaPlayer.getTime();
    }

    public long getDuration() {
        return mMediaPlayer.getLength();
    }

    private void stop() {
        mPlaybackStarted = false;
        mIsSeekRequested = false;
        mMediaPlayer.stop();
        mMediaPlayer.setSpuTrack(-1);
        mMediaPlayer.setAudioTrack(-1);
    }

    private void attachVLCVoutViews() {
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        if (!vout.areViewsAttached()) {
            vout.setVideoView(VLCVideoView.this);
            vout.attachViews();
        }
    }

    private void detachVLCVoutViews() {
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        if (vout.areViewsAttached()) {
            vout.detachViews();
        }
    }

    private void updatePlaybackNotification() {
        try {
            final String channelId = getResources().getString(getResources().getIdentifier(CHANNEL_ID_RESOURCE_NAME, "string", mThemedReactContext.getPackageName()));
            final int smallIconResId = getResources().getIdentifier(SMALL_ICON_RESOURCE_NAME, "drawable", mThemedReactContext.getPackageName());
            final int largeIconResId = getResources().getIdentifier(LARGE_ICON_RESOURCE_NAME, "drawable", mThemedReactContext.getPackageName());
            final int playIconResId = getResources().getIdentifier(PLAY_ICON_RESOURCE_NAME, "drawable", mThemedReactContext.getPackageName());
            final int pauseIconResId = getResources().getIdentifier(PAUSE_ICON_RESOURCE_NAME, "drawable", mThemedReactContext.getPackageName());
            final Bitmap lergeIconBitmap = BitmapFactory.decodeResource(getResources(), largeIconResId);
            final Intent playbackIntent = new Intent(mThemedReactContext, mThemedReactContext.getCurrentActivity().getClass());
            final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mThemedReactContext, channelId)
                    .setContentTitle(mTitle != null ? mTitle : "")
                    .setSmallIcon(smallIconResId)
                    .setLargeIcon(lergeIconBitmap)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(false);
            if (mMediaPlayer.isPlaying()) {
                playbackIntent.setAction(PAUSE_INTENT_ACTION);
                notificationBuilder.addAction(
                        new NotificationCompat.Action.Builder(
                                pauseIconResId,
                                "Pause",
                                PendingIntent.getActivity(
                                        mThemedReactContext,
                                        PLAYBACK_NOTIFICATION_ID,
                                        playbackIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                )
                        ).build()
                );
            } else {
                playbackIntent.setAction(PLAY_INTENT_ACTION);
                notificationBuilder.addAction(
                        new NotificationCompat.Action.Builder(
                                playIconResId,
                                "Play",
                                PendingIntent.getActivity(
                                        mThemedReactContext,
                                        PLAYBACK_NOTIFICATION_ID,
                                        playbackIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                )
                        ).build()
                );
            }

            NotificationManagerCompat.from(mThemedReactContext).notify(PLAYBACK_NOTIFICATION_ID, notificationBuilder.build());
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

    private void clearPlaybackNotification() {
        NotificationManagerCompat.from(mThemedReactContext).cancel(PLAYBACK_NOTIFICATION_ID);
    }
}
