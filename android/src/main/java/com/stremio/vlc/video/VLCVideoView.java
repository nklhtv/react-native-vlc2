package com.stremio.vlc.video;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.SurfaceView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.ThemedReactContext;
import com.stremio.vlc.R;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.RendererItem;
import org.videolan.libvlc.interfaces.IMedia;
import org.videolan.libvlc.interfaces.IVLCVout;
import org.videolan.libvlc.util.DisplayManager;

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

    private static final int HW_ACCELERATION_AUTOMATIC = -1;
    private static final int HW_ACCELERATION_DISABLED = 0;
    private static final int HW_ACCELERATION_DECODING = 1;
    private static final int HW_ACCELERATION_FULL = 2;

    private static final String PLAY_INTENT_ACTION = "VLCVideo:Play";
    private static final String PAUSE_INTENT_ACTION = "VLCVideo:Pause";

    private static final int D_PAD_SEEK_TIME = 15000;

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
    private final ObservableField<RendererItem> mSelectedRenderer;
    private final DisplayManager mDisplayManager;

    private final VLCVideoCallbackManager.OnKeyDownCallback mOnKeyDownCallback = new VLCVideoCallbackManager.OnKeyDownCallback() {
        @Override
        public boolean onKeyDown(final int keyCode, final KeyEvent keyEvent) {
            if (mMediaPlayer.isReleased()) {
                return false;
            }
            final int action = keyEvent.getAction();
            final int repeatCount = keyEvent.getRepeatCount();
            if (action == ACTION_DOWN && repeatCount % 4 == 0) {
                switch (keyCode) {
                    case KEYCODE_SPACE:
                    case KEYCODE_MEDIA_PLAY_PAUSE:
                        if (repeatCount > 0) {
                            return false;
                        }

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
            if (mMediaPlayer.isReleased()) {
                return false;
            }
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
            if (!mMediaPlayer.isReleased()) {
                VLCVideoView.this.attachVLCVoutViews();
            }
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
                    VLCVideoView.this.unloadMedia();
                    VLCVideoView.this.clearPlaybackNotification();
                    break;
                case MediaPlayer.Event.EncounteredError:
                    mEventEmitter.emitOnError(MEDIA_ERROR_MESSAGE, true);
                    VLCVideoView.this.unloadMedia();
                    VLCVideoView.this.clearPlaybackNotification();
                    break;
                case MediaPlayer.Event.Paused:
                    mEventEmitter.emitOnPaused();
                    VLCVideoView.this.updatePlaybackNotification();
                    break;
                case MediaPlayer.Event.TimeChanged:
                    final long time = mMediaPlayer.getTime();
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
                    final long duration = mMediaPlayer.getLength();
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
                    final long buffering = (long) mediaEvent.getBuffering();
                    mEventEmitter.emitOnBuffering(buffering);
                    break;
            }
        }

    };
    private final Observable.OnPropertyChangedCallback mRendererListener = new Observable.OnPropertyChangedCallback() {

        @Override
        public void onPropertyChanged(final Observable sender, final int id) {
            if (!mMediaPlayer.isReleased()) {
                mMediaPlayer.setRenderer(mSelectedRenderer.get());
            }
        }

    };

    public VLCVideoView(final ThemedReactContext themedReactContext, final LibVLC libVLC, final VLCVideoCallbackManager callbackManager, final ObservableField<RendererItem> selectedRenderer) {
        super(themedReactContext);

        mThemedReactContext = themedReactContext;
        mLibVLC = libVLC;
        mCallbackManager = callbackManager;
        mEventEmitter = new VLCVideoEventEmitter(VLCVideoView.this, mThemedReactContext);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        mDisplayManager = new DisplayManager(mThemedReactContext.getCurrentActivity(), null, false, false, false);
        mSelectedRenderer = selectedRenderer;

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
        mSelectedRenderer.addOnPropertyChangedCallback(mRendererListener);
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
        if (!mMediaPlayer.isReleased()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }

        mSelectedRenderer.removeOnPropertyChangedCallback(mRendererListener);
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed && !mMediaPlayer.isReleased()) {
            final int width = right - left;
            final int height = bottom - top;
            if (width * height == 0) {
                return;
            }

            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setWindowSize(width, height);
        }
    }

    public void loadMedia(final String sourceUrl, final long startTime, final boolean autoplay, final int hwDecoderMode, final String title) {
        if (sourceUrl == null || sourceUrl.isEmpty() || mMediaPlayer.isReleased()) {
            return;
        }

        final Uri newSourceUri = Uri.parse(sourceUrl);
        final IMedia oldMedia = mMediaPlayer.getMedia();
        if (oldMedia != null) {
            final Uri oldSourceUri = oldMedia.getUri();
            if (oldSourceUri.compareTo(newSourceUri) == 0) {
                return;
            }
        }

        VLCVideoView.this.unloadMedia();
        final Media newMedia = new Media(mLibVLC, newSourceUri);

        switch (hwDecoderMode) {
            case HW_ACCELERATION_DISABLED:
                newMedia.setHWDecoderEnabled(false, false);
                break;
            case HW_ACCELERATION_FULL:
                newMedia.setHWDecoderEnabled(true, true);
                break;
            case HW_ACCELERATION_AUTOMATIC:
                break;
            case HW_ACCELERATION_DECODING:
            default:
                newMedia.setHWDecoderEnabled(true, true);
                newMedia.addOption(":no-mediacodec-dr");
                newMedia.addOption(":no-omxil-dr");
                break;
        }

        if (startTime > 0) {
            final long startTimeInSeconds = startTime / 1000;
            final String startTimeOption = MessageFormat.format(":start-time={0}", String.valueOf(startTimeInSeconds));
            newMedia.addOption(startTimeOption);
            final String subtitleTrackOption = MessageFormat.format(":sub-track-id={0}", String.valueOf(Integer.MAX_VALUE));
            newMedia.addOption(subtitleTrackOption);
        }

        if (!autoplay) {
            newMedia.addOption(":start-paused");
        }

        mTitle = title;
        mMediaPlayer.setRenderer(mSelectedRenderer.get());
        mMediaPlayer.setMedia(newMedia);
        mMediaPlayer.play();

        mEventEmitter.emitOnSelectedSubtitleTrackIdChanged(mMediaPlayer.getSpuTrack());
        mEventEmitter.emitOnSelectedAudioTrackIdChanged(mMediaPlayer.getAudioTrack());
        mEventEmitter.emitOnSubtitleTracksChanged(mMediaPlayer.getSpuTracks());
        mEventEmitter.emitOnAudioTracksChanged(mMediaPlayer.getAudioTracks());
        VLCVideoView.this.updatePlaybackNotification();
    }

    public void play() {
        if (!mMediaPlayer.isReleased()) {
            mMediaPlayer.play();
        }
    }

    public void pause() {
        if (!mMediaPlayer.isReleased()) {
            mMediaPlayer.pause();
        }
    }

    public void requestSeek(final long time) {
        if (!mMediaPlayer.isReleased()) {
            mIsSeekRequested = true;
            mEventEmitter.emitOnSeekRequested(time);
        }
    }

    public void seek(final long time) {
        if (!mMediaPlayer.isReleased()) {
            requestSeek(time);
            mMediaPlayer.setTime(time);
            mMediaPlayer.play();
        }
    }

    public void setSubtitleTrack(final int id) {
        if (!mMediaPlayer.isReleased()) {
            mMediaPlayer.setSpuTrack(id);
            mEventEmitter.emitOnSelectedSubtitleTrackIdChanged(mMediaPlayer.getSpuTrack());
        }
    }

    public void setAudioTrack(final int id) {
        if (!mMediaPlayer.isReleased()) {
            mMediaPlayer.setAudioTrack(id);
            mEventEmitter.emitOnSelectedAudioTrackIdChanged(mMediaPlayer.getAudioTrack());
        }
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

    private void unloadMedia() {
        mPlaybackStarted = false;
        mIsSeekRequested = false;
        mMediaPlayer.stop();
        mMediaPlayer.setSpuTrack(-1);
        mMediaPlayer.setAudioTrack(-1);
    }

    private void attachVLCVoutViews() {
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        if (!vout.areViewsAttached()) {
            if(!mDisplayManager.isOnRenderer) {
                mMediaPlayer.attachViews(VLCVideoView.this, mDisplayManager, false, false);
            }
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
