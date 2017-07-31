package com.stellarscript.vlcvideo;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.text.MessageFormat;
import java.util.ArrayList;

public final class VLCVideoView extends FrameLayout {

    private static final String TAG = VLCVideoView.class.getSimpleName();
    private static final String HARDWARE_ACCELERATION_ERROR_MESSAGE = "VLC encountered an error with hardware acceleration.";
    private static final String MEDIA_ERROR_MESSAGE = "VLC encountered an error with this media.";

    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;
    private boolean mSeekRequested;
    private final ThemedReactContext mThemedReactContext;
    private final RCTEventEmitter mEventEmitter;
    private final LibVLC mLibVLC;
    private final MediaPlayer mMediaPlayer;
    private final SurfaceView mVideoView;
    private final LifecycleEventListener mLifecycleEventListener = new LifecycleEventListener() {

        @Override
        public void onHostResume() {
            VLCVideoView.this.attachVLCVoutViews();
        }

        @Override
        public void onHostPause() {
            mMediaPlayer.pause();
            VLCVideoView.this.detachVLCVoutViews();
        }

        @Override
        public void onHostDestroy() {
        }

    };
    private final MediaPlayer.EventListener mMediaPlayerEventListener = new MediaPlayer.EventListener() {

        @Override
        public void onEvent(@NonNull final MediaPlayer.Event mediaEvent) {
            final WritableMap event = Arguments.createMap();
            String eventName = VLCVideoEvents.UNHANDLED_EVENT;

            switch (mediaEvent.type) {
                case MediaPlayer.Event.EndReached:
                    eventName = VLCVideoEvents.ON_END_REACHED_EVENT;
                    break;
                case MediaPlayer.Event.EncounteredError:
                    eventName = VLCVideoEvents.ON_ERROR_EVENT;
                    event.putBoolean(VLCVideoEvents.ON_ERROR_IS_CRITICAL_PROP, true);
                    event.putString(VLCVideoEvents.ON_ERROR_MESSAGE_PROP, MEDIA_ERROR_MESSAGE);
                    break;
                case MediaPlayer.Event.Paused:
                    eventName = VLCVideoEvents.ON_PAUSED_EVENT;
                    break;
                case MediaPlayer.Event.TimeChanged:
                    final double currentTime = mMediaPlayer.getTime();
                    eventName = VLCVideoEvents.ON_TIME_CHANGED_EVENT;
                    event.putDouble(VLCVideoEvents.ON_TIME_CHANGED_TIME_PROP, currentTime);
                    break;
                case MediaPlayer.Event.Playing:
                    final double duration = mMediaPlayer.getLength();
                    eventName = VLCVideoEvents.ON_PLAYING_EVENT;
                    event.putDouble(VLCVideoEvents.ON_PLAYING_DURATION_PROP, duration);
                    break;
                case MediaPlayer.Event.Buffering:
                    final double buffering = mediaEvent.getBuffering();
                    eventName = VLCVideoEvents.ON_BUFFERING_EVENT;
                    event.putDouble(VLCVideoEvents.ON_BUFFERING_BUFFERING_PROP, buffering);
                    break;
            }

            if (!eventName.equals(VLCVideoEvents.UNHANDLED_EVENT)) {
                mEventEmitter.receiveEvent(VLCVideoView.this.getId(), eventName, event);
                if (mSeekRequested && eventName.equals(VLCVideoEvents.ON_TIME_CHANGED_EVENT)) {
                    mSeekRequested = false;
                    mEventEmitter.receiveEvent(VLCVideoView.this.getId(), VLCVideoEvents.ON_SEEK_PERFORMED_EVENT, null);
                }
            }
        }

    };
    private final IVLCVout.Callback mVoutCallback = new IVLCVout.Callback() {

        @Override
        public void onNewLayout(@NonNull final IVLCVout vout, final int width, final int height, final int visibleWidth, final int visibleHeight, final int sarNum, final int sarDen) {
            if (width * height == 0) {
                return;
            }

            mVideoWidth = width;
            mVideoHeight = height;
            mVideoVisibleWidth  = visibleWidth;
            mVideoVisibleHeight = visibleHeight;
            mSarNum = sarNum;
            mSarDen = sarDen;
            VLCVideoView.this.changeSurfaceLayout();
        }

        @Override
        public void onSurfacesCreated(@NonNull final IVLCVout vout) {
        }

        @Override
        public void onSurfacesDestroyed(@NonNull final IVLCVout vout) {
        }

        @Override
        public void onHardwareAccelerationError(@NonNull final IVLCVout vout) {
            final WritableMap event = Arguments.createMap();
            event.putBoolean(VLCVideoEvents.ON_ERROR_IS_CRITICAL_PROP, true);
            event.putString(VLCVideoEvents.ON_ERROR_MESSAGE_PROP, HARDWARE_ACCELERATION_ERROR_MESSAGE);
            mEventEmitter.receiveEvent(VLCVideoView.this.getId(), VLCVideoEvents.ON_ERROR_EVENT, event);
        }

    };

    public VLCVideoView(@NonNull final ThemedReactContext themedReactContext) {
        super(themedReactContext);

        mThemedReactContext = themedReactContext;
        mThemedReactContext.addLifecycleEventListener(mLifecycleEventListener);

        mEventEmitter = mThemedReactContext.getJSModule(RCTEventEmitter.class);

        final ArrayList<String> libVLCOptions = new ArrayList<>();
        libVLCOptions.add("-vvv");
        libVLCOptions.add("--http-reconnect");
        mLibVLC = new LibVLC(mThemedReactContext, libVLCOptions);

        mMediaPlayer = new MediaPlayer(mLibVLC);
        mMediaPlayer.setEventListener(mMediaPlayerEventListener);

        LayoutInflater.from(mThemedReactContext).inflate(R.layout.video, VLCVideoView.this);
        mVideoView = (SurfaceView) findViewById(R.id.videoView);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachVLCVoutViews();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        detachVLCVoutViews();
        mThemedReactContext.removeLifecycleEventListener(mLifecycleEventListener);
        mMediaPlayer.release();
        mLibVLC.release();
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            changeSurfaceLayout();
        }
    }

    public void loadMedia(@NonNull final String sourceUrl, final int startTime, final boolean autoplay) {
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
        mMediaPlayer.setTime(time);
        mMediaPlayer.play();
        mSeekRequested = true;
        final WritableMap event = Arguments.createMap();
        event.putDouble(VLCVideoEvents.ON_SEEK_REQUESTED_TIME_PROP, time);
        mEventEmitter.receiveEvent(VLCVideoView.this.getId(), VLCVideoEvents.ON_SEEK_REQUESTED_EVENT, event);
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

    private void attachVLCVoutViews() {
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.addCallback(mVoutCallback);
        if (!vout.areViewsAttached()) {
            vout.setVideoView(mVideoView);
            vout.attachViews();
        }
    }

    private void detachVLCVoutViews() {
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(mVoutCallback);
        if (vout.areViewsAttached()) {
            vout.detachViews();
        }
    }

    private void changeSurfaceLayout() {
        if (mVideoVisibleWidth * mVideoVisibleHeight == 0 || mVideoWidth * mVideoHeight == 0 || mSarDen == 0) {
            return;
        }

        final int parentWidth = VLCVideoView.this.getWidth();
        final int parentHeight = VLCVideoView.this.getHeight();
        final int parentLeft = VLCVideoView.this.getLeft();
        final int parentTop = VLCVideoView.this.getTop();

        if (parentWidth * parentHeight == 0) {
            return;
        }

        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.setWindowSize(parentWidth, parentHeight);

        final double videoVisibleWidth = mVideoVisibleWidth * (double)mSarNum / (double)mSarDen;
        final double videoAspectRatio = videoVisibleWidth / mVideoVisibleHeight;
        final double parentAspectRatio = (double)parentWidth / (double)parentHeight;

        int surfaceWidth, surfaceHeight;
        if (parentAspectRatio < videoAspectRatio) {
            surfaceWidth = (int) Math.ceil(parentWidth * mVideoWidth / mVideoVisibleWidth);
            surfaceHeight = (int) Math.ceil((parentWidth / videoAspectRatio) * mVideoHeight / mVideoVisibleHeight);
        } else {
            surfaceWidth = (int) Math.ceil((parentHeight * videoAspectRatio) * mVideoWidth / mVideoVisibleWidth);
            surfaceHeight = (int) Math.ceil(parentHeight * mVideoHeight / mVideoVisibleHeight);
        }

        final ViewGroup.LayoutParams surfaceLayoutParams = mVideoView.getLayoutParams();
        surfaceLayoutParams.width  = surfaceWidth;
        surfaceLayoutParams.height = surfaceHeight;
        mVideoView.setLayoutParams(surfaceLayoutParams);

        VLCVideoView.this.measure(parentWidth, parentHeight);
        VLCVideoView.this.layout(parentLeft, parentTop, parentLeft + parentWidth, parentTop + parentHeight);
    }

}
