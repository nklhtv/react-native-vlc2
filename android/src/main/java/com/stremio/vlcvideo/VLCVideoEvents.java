package com.stremio.vlcvideo;

final class VLCVideoEvents {

    private static final String EVENT_NAME_PREFIX = VLCVideoView.class.getSimpleName();

    static final String ON_BUFFERING_EVENT = getFullEventName("onBuffering");
    static final String ON_PLAYING_EVENT = getFullEventName("onPlaying");
    static final String ON_PAUSED_EVENT = getFullEventName("onPaused");
    static final String ON_END_REACHED_EVENT = getFullEventName("onEndReached");
    static final String ON_ERROR_EVENT = getFullEventName("onError");
    static final String ON_TIME_CHANGED_EVENT = getFullEventName("onTimeChanged");
    static final String ON_SEEK_REQUESTED_EVENT = getFullEventName("onSeekRequested");
    static final String ON_SEEK_PERFORMED_EVENT = getFullEventName("onSeekPerformed");
    static final String ON_SUBTITLE_TRACKS_CHANGED_EVENT = getFullEventName("onSubtitleTracksChanged");
    static final String ON_AUDIO_TRACKS_CHANGED_EVENT = getFullEventName("onAudioTracksChanged");
    static final String ON_SELECTED_SUBTITLE_TRACK_ID_CHANGED_EVENT = getFullEventName("onSelectedSubtitleTrackIdChanged");
    static final String ON_SELECTED_AUDIO_TRACK_ID_CHANGED_EVENT = getFullEventName("onAudioTrackIdChanged");

    static final String MESSAGE_PROP = "message";
    static final String IS_CRITICAL_PROP = "isCritical";
    static final String TIME_PROP = "time";
    static final String DURATION_PROP = "duration";
    static final String BUFFERING_PROP = "buffering";
    static final String SUBTITLE_TRACKS_PROP = "subtitleTracks";
    static final String AUDIO_TRACKS_PROP = "audioTracks";
    static final String TRACK_ID_PROP = "id";
    static final String TRACK_NAME_PROP = "name";

    private static String getFullEventName(final String eventName) {
        return EVENT_NAME_PREFIX.concat(eventName);
    }

}
