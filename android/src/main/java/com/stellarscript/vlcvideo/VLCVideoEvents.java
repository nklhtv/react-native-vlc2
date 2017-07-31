package com.stellarscript.vlcvideo;

import android.support.annotation.NonNull;

final class VLCVideoEvents {

    private static final String EVENT_NAME_PREFIX = VLCVideoView.class.getSimpleName();

    static final String UNHANDLED_EVENT = "unhandledEvent";

    static final String ON_BUFFERING_EVENT = getFullEventName("onBuffering");
    static final String ON_PLAYING_EVENT = getFullEventName("onPlaying");
    static final String ON_PAUSED_EVENT = getFullEventName("onPaused");
    static final String ON_END_REACHED_EVENT = getFullEventName("onEndReached");
    static final String ON_ERROR_EVENT = getFullEventName("onError");
    static final String ON_TIME_CHANGED_EVENT = getFullEventName("onTimeChanged");
    static final String ON_SEEK_REQUESTED_EVENT = getFullEventName("onSeekRequested");
    static final String ON_SEEK_PERFORMED_EVENT = getFullEventName("onSeekPerformed");

    static final String ON_ERROR_MESSAGE_PROP = "message";
    static final String ON_ERROR_IS_CRITICAL_PROP = "isCritical";
    static final String ON_TIME_CHANGED_TIME_PROP = "time";
    static final String ON_PLAYING_DURATION_PROP = "duration";
    static final String ON_BUFFERING_BUFFERING_PROP = "buffering";
    static final String ON_SEEK_REQUESTED_TIME_PROP = "time";

    private static String getFullEventName(@NonNull final String eventName) {
        return EVENT_NAME_PREFIX.concat(eventName);
    }

}
