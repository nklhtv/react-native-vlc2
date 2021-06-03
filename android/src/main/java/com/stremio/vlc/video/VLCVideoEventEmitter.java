package com.stremio.vlc.video;

import android.view.View;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import org.videolan.libvlc.MediaPlayer.TrackDescription;

public final class VLCVideoEventEmitter {

    private final View mVideoView;
    private final RCTEventEmitter mEventEmitter;

    public VLCVideoEventEmitter(final View videoView, final ThemedReactContext themedReactContext) {
        mVideoView = videoView;
        mEventEmitter = themedReactContext.getJSModule(RCTEventEmitter.class);
    }

    public void emitOnBuffering(final double buffering) {
        final WritableMap event = Arguments.createMap();
        event.putDouble(VLCVideoEvents.BUFFERING_PROP, buffering);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_BUFFERING_EVENT, event);
    }

    public void emitOnPlaying(final double duration) {
        final WritableMap event = Arguments.createMap();
        event.putDouble(VLCVideoEvents.DURATION_PROP, duration);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_PLAYING_EVENT, event);
    }

    public void emitOnPaused() {
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_PAUSED_EVENT, null);
    }

    public void emitOnEndReached() {
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_END_REACHED_EVENT, null);
    }

    public void emitOnError(final String message, final boolean isCritical) {
        final WritableMap event = Arguments.createMap();
        event.putString(VLCVideoEvents.MESSAGE_PROP, message);
        event.putBoolean(VLCVideoEvents.IS_CRITICAL_PROP, isCritical);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_ERROR_EVENT, event);
    }

    public void emitOnTimeChanged(final double time) {
        final WritableMap event = Arguments.createMap();
        event.putDouble(VLCVideoEvents.TIME_PROP, time);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_TIME_CHANGED_EVENT, event);
    }

    public void emitOnSeekRequested(final double time) {
        final WritableMap event = Arguments.createMap();
        event.putDouble(VLCVideoEvents.TIME_PROP, time);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_SEEK_REQUESTED_EVENT, event);
    }

    public void emitOnSeekPerformed() {
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_SEEK_PERFORMED_EVENT, null);
    }

    public void emitOnSubtitleTracksChanged(final TrackDescription[] tracks) {
        final WritableArray eventTracks = Arguments.createArray();
        if (tracks != null) {
            for (final TrackDescription track : tracks) {
                if (track.name.toLowerCase().contains("disable")) {
                    continue;
                }

                final WritableMap eventTrack = Arguments.createMap();
                eventTrack.putInt(VLCVideoEvents.TRACK_ID_PROP, track.id);
                eventTrack.putString(VLCVideoEvents.TRACK_NAME_PROP, track.name);
                eventTracks.pushMap(eventTrack);
            }
        }

        final WritableMap event = Arguments.createMap();
        event.putArray(VLCVideoEvents.SUBTITLE_TRACKS_PROP, eventTracks);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_SUBTITLE_TRACKS_CHANGED_EVENT, event);
    }

    public void emitOnAudioTracksChanged(final TrackDescription[] tracks) {
        final WritableArray eventTracks = Arguments.createArray();
        if (tracks != null) {
            for (final TrackDescription track : tracks) {
                if (track.name.toLowerCase().contains("disable")) {
                    continue;
                }

                final WritableMap eventTrack = Arguments.createMap();
                eventTrack.putInt(VLCVideoEvents.TRACK_ID_PROP, track.id);
                eventTrack.putString(VLCVideoEvents.TRACK_NAME_PROP, track.name);
                eventTracks.pushMap(eventTrack);
            }
        }

        final WritableMap event = Arguments.createMap();
        event.putArray(VLCVideoEvents.AUDIO_TRACKS_PROP, eventTracks);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_AUDIO_TRACKS_CHANGED_EVENT, event);
    }

    public void emitOnSelectedSubtitleTrackIdChanged(final int id) {
        final WritableMap event = Arguments.createMap();
        event.putInt(VLCVideoEvents.TRACK_ID_PROP, id);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_SELECTED_SUBTITLE_TRACK_ID_CHANGED_EVENT, event);
    }

    public void emitOnSelectedAudioTrackIdChanged(final int id) {
        final WritableMap event = Arguments.createMap();
        event.putInt(VLCVideoEvents.TRACK_ID_PROP, id);
        mEventEmitter.receiveEvent(mVideoView.getId(), VLCVideoEvents.ON_SELECTED_AUDIO_TRACK_ID_CHANGED_EVENT, event);
    }
}
