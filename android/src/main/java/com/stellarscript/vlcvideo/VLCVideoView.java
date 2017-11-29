package com.stellarscript.vlcvideo;

import android.net.Uri;
import android.view.Gravity;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.uimanager.ThemedReactContext;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.text.MessageFormat;
import java.util.ArrayList;

public final class VLCVideoView extends FrameLayout {

    private static final String HARDWARE_ACCELERATION_ERROR_MESSAGE = "VLC encountered an error with hardware acceleration.";
    private static final String MEDIA_ERROR_MESSAGE = "VLC encountered an error with this media.";

    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;
    private boolean mIsSeekRequested;
    private final ThemedReactContext mThemedReactContext;
    private final VLCVideoEventEmitter mEventEmitter;
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
            if (!mMediaPlayer.isReleased()) {
                mMediaPlayer.pause();
                VLCVideoView.this.detachVLCVoutViews();
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
                    break;
                case MediaPlayer.Event.EncounteredError:
                    mEventEmitter.emitOnError(MEDIA_ERROR_MESSAGE, true);
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
    private final IVLCVout.Callback mVoutCallback = new IVLCVout.Callback() {

        @Override
        public void onNewLayout(final IVLCVout vout, final int width, final int height, final int visibleWidth, final int visibleHeight, final int sarNum, final int sarDen) {
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
        public void onSurfacesCreated(final IVLCVout vout) {
        }

        @Override
        public void onSurfacesDestroyed(final IVLCVout vout) {
        }

        @Override
        public void onHardwareAccelerationError(final IVLCVout vout) {
            mEventEmitter.emitOnError(HARDWARE_ACCELERATION_ERROR_MESSAGE, true);
        }

    };

    public VLCVideoView(final ThemedReactContext themedReactContext) {
        super(themedReactContext);

        mThemedReactContext = themedReactContext;

        mEventEmitter = new VLCVideoEventEmitter(VLCVideoView.this, mThemedReactContext);

        final ArrayList<String> libVLCOptions = new ArrayList<>();
        libVLCOptions.add("-vvv");
        libVLCOptions.add("--http-reconnect");
        mLibVLC = new LibVLC(mThemedReactContext, libVLCOptions);

        mMediaPlayer = new MediaPlayer(mLibVLC);

        mVideoView = new SurfaceView(mThemedReactContext);
        addView(mVideoView);
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
        mLibVLC.release();
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            changeSurfaceLayout();
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
        mIsSeekRequested = true;
        mEventEmitter.emitOnSeekRequested(time);
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

        final int surfaceWidth;
        final int surfaceHeight;
        if (parentAspectRatio < videoAspectRatio) {
            surfaceWidth = (int) Math.ceil(parentWidth * mVideoWidth / mVideoVisibleWidth);
            surfaceHeight = (int) Math.ceil((parentWidth / videoAspectRatio) * mVideoHeight / mVideoVisibleHeight);
        } else {
            surfaceWidth = (int) Math.ceil((parentHeight * videoAspectRatio) * mVideoWidth / mVideoVisibleWidth);
            surfaceHeight = (int) Math.ceil(parentHeight * mVideoHeight / mVideoVisibleHeight);
        }

        final FrameLayout.LayoutParams surfaceLayoutParams = (FrameLayout.LayoutParams) mVideoView.getLayoutParams();
        surfaceLayoutParams.width  = surfaceWidth;
        surfaceLayoutParams.height = surfaceHeight;
        surfaceLayoutParams.gravity = Gravity.CENTER;
        mVideoView.setLayoutParams(surfaceLayoutParams);

        VLCVideoView.this.measure(parentWidth, parentHeight);
        VLCVideoView.this.layout(parentLeft, parentTop, parentLeft + parentWidth, parentTop + parentHeight);
    }

}
