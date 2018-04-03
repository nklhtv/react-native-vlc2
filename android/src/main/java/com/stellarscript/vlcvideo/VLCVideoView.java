package com.stellarscript.vlcvideo;

import android.net.Uri;
import android.view.SurfaceView;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.ThemedReactContext;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.text.MessageFormat;

public final class VLCVideoView extends SurfaceView {

    private static final String MEDIA_ERROR_MESSAGE = "VLC encountered an error with this media.";

    private boolean mIsSeekRequested;
    private final ThemedReactContext mThemedReactContext;
    private final LibVLC mLibVLC;
    private final VLCVideoEventEmitter mEventEmitter;
    private final MediaPlayer mMediaPlayer;
    private final LifecycleEventListener mLifecycleEventListener = new LifecycleEventListener() {

        @Override
        public void onHostResume() {
            VLCVideoView.this.attachVLCVoutViews();
        }

        @Override
        public void onHostPause() {
            try {
                if (!mMediaPlayer.isReleased()) {
                    mMediaPlayer.pause();
                    VLCVideoView.this.detachVLCVoutViews();
                }
            } catch (final Exception e) {
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
                    break;
                case MediaPlayer.Event.EncounteredError:
                    mEventEmitter.emitOnError(MEDIA_ERROR_MESSAGE, true);
                    VLCVideoView.this.stop();
                    break;
                case MediaPlayer.Event.Paused:
                    mEventEmitter.emitOnPaused();
                    break;
                case MediaPlayer.Event.TimeChanged:
                    final double currentTime = mMediaPlayer.getTime();
                    mEventEmitter.emitOnTimeChanged(currentTime);
                    if (mIsSeekRequested) {
                        mIsSeekRequested = false;
                        mEventEmitter.emitOnSeekPerformed();
                    }
                    break;
                case MediaPlayer.Event.Playing:
                    final double duration = mMediaPlayer.getLength();
                    mEventEmitter.emitOnPlaying(duration);
                    break;
                case MediaPlayer.Event.Buffering:
                    final double buffering = mediaEvent.getBuffering();
                    mEventEmitter.emitOnBuffering(buffering);
                    break;
            }
        }

    };

    public VLCVideoView(final ThemedReactContext themedReactContext, final LibVLC libVLC) {
        super(themedReactContext);

        mThemedReactContext = themedReactContext;
        mLibVLC = libVLC;
        mEventEmitter = new VLCVideoEventEmitter(VLCVideoView.this, mThemedReactContext);
        mMediaPlayer = new MediaPlayer(mLibVLC);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mThemedReactContext.addLifecycleEventListener(mLifecycleEventListener);
        mMediaPlayer.setEventListener(mMediaPlayerEventListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        detachVLCVoutViews();
        mThemedReactContext.removeLifecycleEventListener(mLifecycleEventListener);
        mMediaPlayer.setEventListener(null);
        mMediaPlayer.stop();
        mMediaPlayer.release();
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

    public void loadMedia(final String sourceUrl, final int startTime, final boolean autoplay) {
        if (sourceUrl.isEmpty()) {
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

        final Media newMedia = new Media(mLibVLC, newSourceUri);
        newMedia.setHWDecoderEnabled(true, false);

        if (startTime > 0) {
            final int startTimeInSeconds = startTime / 1000;
            final String startTimeOption = MessageFormat.format(":start-time={0}", String.valueOf(startTimeInSeconds));
            newMedia.addOption(startTimeOption);
        }

        stop();
        mMediaPlayer.setMedia(newMedia);

        if (autoplay) {
            mMediaPlayer.play();
        }
    }

    public void play() {
        mMediaPlayer.play();
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void seek(final int time) {
        mIsSeekRequested = true;
        mEventEmitter.emitOnSeekRequested(time);
        mMediaPlayer.setTime(time);
        mMediaPlayer.play();
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

    private void stop() {
        mIsSeekRequested = false;
        mMediaPlayer.stop();
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

}
