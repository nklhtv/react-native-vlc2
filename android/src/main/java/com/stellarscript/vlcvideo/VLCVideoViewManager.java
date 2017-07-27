package com.stellarscript.vlcvideo;

import android.support.annotation.NonNull;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nullable;

final class VLCVideoViewManager extends SimpleViewManager<VLCVideoView> {

    private static final String REACT_CLASS = "RCT" + VLCVideoView.class.getSimpleName();
    private static final String REACT_REGISTRATION_NAME = "registrationName";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    @Nullable
    public Map<String, Integer> getCommandsMap() {
        final Map<String, Integer> commands = MapBuilder.newHashMap();

        commands.put(VLCVideoProps.PLAY_COMMAND_NAME, VLCVideoProps.PLAY_COMMAND_ID);
        commands.put(VLCVideoProps.PAUSE_COMMAND_NAME, VLCVideoProps.PAUSE_COMMAND_ID);
        commands.put(VLCVideoProps.SEEK_COMMAND_NAME, VLCVideoProps.SEEK_COMMAND_ID);

        return commands;
    }

    @Override
    @Nullable
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        final Map<String, Object> events = MapBuilder.newHashMap();

        events.put(VLCVideoEvents.ON_BUFFERING_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_BUFFERING_EVENT));
        events.put(VLCVideoEvents.ON_PLAYING_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_PLAYING_EVENT));
        events.put(VLCVideoEvents.ON_PAUSED_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_PAUSED_EVENT));
        events.put(VLCVideoEvents.ON_END_REACHED_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_END_REACHED_EVENT));
        events.put(VLCVideoEvents.ON_ERROR_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_ERROR_EVENT));
        events.put(VLCVideoEvents.ON_TIME_CHANGED_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_TIME_CHANGED_EVENT));
        events.put(VLCVideoEvents.ON_SEEK_PERFORMED_EVENT, MapBuilder.of(REACT_REGISTRATION_NAME, VLCVideoEvents.ON_SEEK_PERFORMED_EVENT));

        return events;
    }

    @Nullable
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

        return constants;
    }

    @Override
    protected VLCVideoView createViewInstance(@NonNull final ThemedReactContext themedReactContext) {
        return new VLCVideoView(themedReactContext);
    }

    @Override
    public void receiveCommand(@NonNull final VLCVideoView videoView, final int commandId, @Nullable final ReadableArray args) {
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
                    final int seekTime = args.getInt(VLCVideoProps.SEEK_COMMAND_TIME_ARGUMENT_INDEX);
                    videoView.seek(seekTime);
                }
                break;
        }
    }

    @ReactProp(name = VLCVideoProps.MEDIA_PROP)
    public void loadMedia(@NonNull final VLCVideoView videoView, @Nullable final ReadableMap media) {
        if (media == null ||
                !media.hasKey(VLCVideoProps.MEDIA_SOURCE_URL_PROP) ||
                media.isNull(VLCVideoProps.MEDIA_SOURCE_URL_PROP) ||
                media.getType(VLCVideoProps.MEDIA_SOURCE_URL_PROP) != ReadableType.String) {
            return;
        }

        final String sourceUrl = media.getString(VLCVideoProps.MEDIA_SOURCE_URL_PROP);

        int startTime = VLCVideoProps.MEDIA_START_TIME_DEFAULT_VALUE;
        if (media.hasKey(VLCVideoProps.MEDIA_START_TIME_PROP) &&
                !media.isNull(VLCVideoProps.MEDIA_START_TIME_PROP) &&
                media.getType(VLCVideoProps.MEDIA_START_TIME_PROP) == ReadableType.Number) {
            startTime = media.getInt(VLCVideoProps.MEDIA_START_TIME_PROP);
        }

        boolean autoplay = VLCVideoProps.MEDIA_AUTOPLAY_DEFAULT_VALUE;
        if (media.hasKey(VLCVideoProps.MEDIA_AUTOPLAY_PROP) &&
                !media.isNull(VLCVideoProps.MEDIA_AUTOPLAY_PROP) &&
                media.getType(VLCVideoProps.MEDIA_AUTOPLAY_PROP) == ReadableType.Boolean) {
            autoplay = media.getBoolean(VLCVideoProps.MEDIA_AUTOPLAY_PROP);
        }

        videoView.loadMedia(sourceUrl, startTime, autoplay);
    }

    @ReactProp(name = VLCVideoProps.KEY_CONTROL_ENABLED_PROP)
    public void setKeyControlEnabled(@NonNull final VLCVideoView videoView, final boolean keyControlEnabled) {
//        if (keyControlEnabled) {
//            videoView.setFocusable(true);
//            videoView.requestFocus();
//            videoView.setOnKeyListener(videoView);
//        } else {
//            videoView.setFocusable(false);
//            videoView.clearFocus();
//            videoView.setOnKeyListener(null);
//        }
    }

}
