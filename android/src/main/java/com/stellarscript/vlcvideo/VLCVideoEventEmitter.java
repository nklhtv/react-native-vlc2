package com.stellarscript.vlcvideo;

import android.support.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

final class VLCVideoEventEmitter {

    private final VLCVideoView mVideoView;
    private final RCTEventEmitter mEventEmitter;

    public VLCVideoEventEmitter(@NonNull final VLCVideoView videoView, @NonNull final ThemedReactContext themedReactContext) {
        mVideoView = videoView;
        mEventEmitter = themedReactContext.getJSModule(RCTEventEmitter.class);
    }

    public void emitOnBuffering(final double buffering) {
        final WritableMap event = Arguments.createMap();
        event.putDouble(VLCVideoEvents.ON_BUFFERING_BUFFERING_PROP, buffering);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_BUFFERING_EVENT, event);
    }

    public void emitOnPlaying(final double duration) {
        final WritableMap event = Arguments.createMap();
        event.putDouble(VLCVideoEvents.ON_PLAYING_DURATION_PROP, duration);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_PLAYING_EVENT, event);
    }

    public void emitOnPaused() {
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_PAUSED_EVENT, null);
    }

    public void emitOnEndReached() {
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_END_REACHED_EVENT, null);
    }

    public void emitOnError(@NonNull final String message, final boolean isCritical) {
        final WritableMap event = Arguments.createMap();
        event.putString(VLCVideoEvents.ON_ERROR_MESSAGE_PROP, message);
        event.putBoolean(VLCVideoEvents.ON_ERROR_IS_CRITICAL_PROP, isCritical);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_ERROR_EVENT, event);
    }

    public void emitOnTimeChanged(final double currentTime) {
        final WritableMap event = Arguments.createMap();
        event.putDouble(VLCVideoEvents.ON_TIME_CHANGED_TIME_PROP, currentTime);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_TIME_CHANGED_EVENT, event);
    }

    public void emitOnSeekRequested(final double time) {
        final WritableMap event = Arguments.createMap();
        event.putDouble(VLCVideoEvents.ON_SEEK_REQUESTED_TIME_PROP, time);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_SEEK_REQUESTED_EVENT, event);
    }

    public void emitOnSeekPerformed() {
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_SEEK_PERFORMED_EVENT, null);
    }

}
