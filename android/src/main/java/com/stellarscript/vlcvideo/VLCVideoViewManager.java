package com.stellarscript.vlcvideo;

import android.view.View;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import org.videolan.libvlc.LibVLC;

import java.util.Map;

final class VLCVideoViewManager extends SimpleViewManager<VLCVideoView> {

    private static final String REACT_CLASS = "RCT" + VLCVideoView.class.getSimpleName();
    private static final String REACT_REGISTRATION_NAME = "registrationName";

    private final View.OnKeyListener mOnKeyListener;
    private final LibVLC mLibVLC;
    private final VLCVideoCallbackManager mCallbackManager;

    public VLCVideoViewManager(final View.OnKeyListener onKeyListener, final LibVLC libVLC, final VLCVideoCallbackManager callbackManager) {
        mOnKeyListener = onKeyListener;
        mLibVLC = libVLC;
        mCallbackManager = callbackManager;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        final Map<String, Integer> commands = MapBuilder.newHashMap();

        commands.put(VLCVideoProps.PLAY_COMMAND_NAME, VLCVideoProps.PLAY_COMMAND_ID);
        commands.put(VLCVideoProps.PAUSE_COMMAND_NAME, VLCVideoProps.PAUSE_COMMAND_ID);
        commands.put(VLCVideoProps.SEEK_COMMAND_NAME, VLCVideoProps.SEEK_COMMAND_ID);
        commands.put(VLCVideoProps.SET_SUBTITLE_TRACK_COMMAND_NAME, VLCVideoProps.SET_SUBTITLE_TRACK_COMMAND_ID);
        commands.put(VLCVideoProps.SET_AUDIO_TRACK_COMMAND_NAME, VLCVideoProps.SET_AUDIO_TRACK_COMMAND_ID);

        return commands;
    }

    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        final Map<String, Object> events = MapBuilder.newHashMap();

        events.put(VLCVideoEvents.ON_BUFFERING_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_BUFFERING_EVENT));
        events.put(VLCVideoEvents.ON_PLAYING_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_PLAYING_EVENT));
        events.put(VLCVideoEvents.ON_PAUSED_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_PAUSED_EVENT));
        events.put(VLCVideoEvents.ON_END_REACHED_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_END_REACHED_EVENT));
        events.put(VLCVideoEvents.ON_ERROR_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_ERROR_EVENT));
        events.put(VLCVideoEvents.ON_TIME_CHANGED_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_TIME_CHANGED_EVENT));
        events.put(VLCVideoEvents.ON_SEEK_PERFORMED_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_SEEK_PERFORMED_EVENT));
        events.put(VLCVideoEvents.ON_SEEK_REQUESTED_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_SEEK_REQUESTED_EVENT));
        events.put(VLCVideoEvents.ON_SUBTITLE_TRACKS_CHANGED_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_SUBTITLE_TRACKS_CHANGED_EVENT));
        events.put(VLCVideoEvents.ON_AUDIO_TRACKS_CHANGED_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_AUDIO_TRACKS_CHANGED_EVENT));

        return events;
    }

    @Override
    public Map<String, Object> getExportedViewConstants() {
        final Map<String, Object> constants = MapBuilder.newHashMap();

        constants.put("ON_BUFFERING", VLCVideoEvents.ON_BUFFERING_EVENT);
        constants.put("ON_PLAYING", VLCVideoEvents.ON_PLAYING_EVENT);
        constants.put("ON_PAUSED", VLCVideoEvents.ON_PAUSED_EVENT);
        constants.put("ON_END_REACHED", VLCVideoEvents.ON_END_REACHED_EVENT);
        constants.put("ON_ERROR", VLCVideoEvents.ON_ERROR_EVENT);
        constants.put("ON_TIME_CHANGED", VLCVideoEvents.ON_TIME_CHANGED_EVENT);
        constants.put("ON_SEEK_PERFORMED", VLCVideoEvents.ON_SEEK_PERFORMED_EVENT);
        constants.put("ON_SEEK_REQUESTED", VLCVideoEvents.ON_SEEK_REQUESTED_EVENT);
        constants.put("ON_SUBTITLE_TRACKS_CHANGED", VLCVideoEvents.ON_SUBTITLE_TRACKS_CHANGED_EVENT);
        constants.put("ON_AUDIO_TRACKS_CHANGED", VLCVideoEvents.ON_AUDIO_TRACKS_CHANGED_EVENT);

        return constants;
    }

    @Override
    protected VLCVideoView createViewInstance(final ThemedReactContext themedReactContext) {
        return new VLCVideoView(themedReactContext, mLibVLC, mCallbackManager);
    }

    @Override
    public void receiveCommand(final VLCVideoView videoView, final int commandId, final ReadableArray args) {
        switch (commandId) {
            case VLCVideoProps.PLAY_COMMAND_ID:
                videoView.play();
                break;
            case VLCVideoProps.PAUSE_COMMAND_ID:
                videoView.pause();
                break;
            case VLCVideoProps.SEEK_COMMAND_ID:
                if (args != null &&
                        args.size() > 0 &&
                        !args.isNull(VLCVideoProps.SEEK_COMMAND_TIME_ARGUMENT_INDEX) &&
                        args.getType(VLCVideoProps.SEEK_COMMAND_TIME_ARGUMENT_INDEX) == ReadableType.Number) {
                    final long seekTime = (long) args.getDouble(VLCVideoProps.SEEK_COMMAND_TIME_ARGUMENT_INDEX);
                    videoView.seek(seekTime);
                }
                break;
            case VLCVideoProps.SET_SUBTITLE_TRACK_COMMAND_ID:
                if (args != null &&
                        args.size() > 0 &&
                        !args.isNull(0) &&
                        args.getType(0) == ReadableType.Number) {
                    final int id = (int) args.getInt(0);
                    videoView.setSubtitlesTrack(id);
                }
                break;
            case VLCVideoProps.SET_AUDIO_TRACK_COMMAND_ID:
                if (args != null &&
                        args.size() > 0 &&
                        !args.isNull(0) &&
                        args.getType(0) == ReadableType.Number) {
                    final int id = (int) args.getInt(0);
                    videoView.setAudioTrack(id);
                }
                break;
        }
    }

    @ReactProp(name = VLCVideoProps.MEDIA_PROP)
    public void loadMedia(final VLCVideoView videoView, final ReadableMap media) {
        if (media == null ||
                !media.hasKey(VLCVideoProps.MEDIA_SOURCE_URL_PROP) ||
                media.isNull(VLCVideoProps.MEDIA_SOURCE_URL_PROP) ||
                media.getType(VLCVideoProps.MEDIA_SOURCE_URL_PROP) != ReadableType.String) {
            return;
        }

        final String sourceUrl = media.getString(VLCVideoProps.MEDIA_SOURCE_URL_PROP);

        final long startTime;
        if (media.hasKey(VLCVideoProps.MEDIA_START_TIME_PROP) &&
                !media.isNull(VLCVideoProps.MEDIA_START_TIME_PROP) &&
                media.getType(VLCVideoProps.MEDIA_START_TIME_PROP) == ReadableType.Number) {
            startTime = (long) media.getDouble(VLCVideoProps.MEDIA_START_TIME_PROP);
        } else {
            startTime = VLCVideoProps.MEDIA_START_TIME_DEFAULT_VALUE;
        }

        final boolean autoplay;
        if (media.hasKey(VLCVideoProps.MEDIA_AUTOPLAY_PROP) &&
                !media.isNull(VLCVideoProps.MEDIA_AUTOPLAY_PROP) &&
                media.getType(VLCVideoProps.MEDIA_AUTOPLAY_PROP) == ReadableType.Boolean) {
            autoplay = media.getBoolean(VLCVideoProps.MEDIA_AUTOPLAY_PROP);
        } else {
            autoplay = VLCVideoProps.MEDIA_AUTOPLAY_DEFAULT_VALUE;
        }

        final boolean hwDecoderEnabled;
        if (media.hasKey(VLCVideoProps.MEDIA_HW_DECODER_ENABLED_PROP) &&
                !media.isNull(VLCVideoProps.MEDIA_HW_DECODER_ENABLED_PROP) &&
                media.getType(VLCVideoProps.MEDIA_HW_DECODER_ENABLED_PROP) == ReadableType.Boolean) {
            hwDecoderEnabled = media.getBoolean(VLCVideoProps.MEDIA_HW_DECODER_ENABLED_PROP);
        } else {
            hwDecoderEnabled = VLCVideoProps.MEDIA_HW_DECODER_ENABLED_DEFAULT_VALUE;
        }

        final String title;
        if (media.hasKey(VLCVideoProps.MEDIA_TITLE_PROP) &&
                !media.isNull(VLCVideoProps.MEDIA_TITLE_PROP) &&
                media.getType(VLCVideoProps.MEDIA_TITLE_PROP) == ReadableType.String) {
            title = media.getString(VLCVideoProps.MEDIA_TITLE_PROP);
        } else {
            title = VLCVideoProps.MEDIA_TITLE_DEFAULT_VALUE;
        }

        videoView.loadMedia(sourceUrl, startTime, autoplay, hwDecoderEnabled, title);
    }

    @ReactProp(name = VLCVideoProps.KEY_CONTROL_ENABLED_PROP, defaultBoolean = VLCVideoProps.KEY_CONTROL_ENABLED_DEFAULT_VALUE)
    public void setKeyControlEnabled(final VLCVideoView videoView, final boolean keyControlEnabled) {
        if (keyControlEnabled && mOnKeyListener != null) {
            videoView.setOnKeyListener(mOnKeyListener);
            videoView.setFocusable(true);
            videoView.requestFocus();
        } else {
            videoView.setOnKeyListener(null);
            videoView.setFocusable(false);
            videoView.clearFocus();
        }
    }

    @ReactProp(name = VLCVideoProps.PLAY_IN_BACKGROUND_PROP, defaultBoolean = VLCVideoProps.PLAY_IN_BACKGROUND_DEFAULT_VALUE)
    public void setPlayInBackground(final VLCVideoView videoView, final boolean playInBackground) {
        videoView.setPlayInBackground(playInBackground);
    }

}
