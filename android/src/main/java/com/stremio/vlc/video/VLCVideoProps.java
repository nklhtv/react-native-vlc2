package com.stremio.vlc.video;

public final class VLCVideoProps {

    public static final String MEDIA_PROP = "media";
    public static final String MEDIA_SOURCE_URL_PROP = "sourceUrl";
    public static final String MEDIA_START_TIME_PROP = "startTime";
    public static final int MEDIA_START_TIME_DEFAULT_VALUE = 0;
    public static final String MEDIA_AUTOPLAY_PROP = "autoplay";
    public static final boolean MEDIA_AUTOPLAY_DEFAULT_VALUE = true;
    public static final String MEDIA_HW_DECODER_ENABLED_PROP = "hwDecoderEnabled";
    public static final boolean MEDIA_HW_DECODER_ENABLED_DEFAULT_VALUE = true;
    public static final String MEDIA_TITLE_PROP = "title";
    public static final String MEDIA_TITLE_DEFAULT_VALUE = "";
    public static final String KEY_CONTROL_ENABLED_PROP = "keyControlEnabled";
    public static final boolean KEY_CONTROL_ENABLED_DEFAULT_VALUE = false;
    public static final String PLAY_IN_BACKGROUND_PROP = "playInBackground";
    public static final boolean PLAY_IN_BACKGROUND_DEFAULT_VALUE = false;

    public static final String PLAY_COMMAND_NAME = "play";
    public static final int PLAY_COMMAND_ID = 1;
    public static final String PAUSE_COMMAND_NAME = "pause";
    public static final int PAUSE_COMMAND_ID = 2;
    public static final String SEEK_COMMAND_NAME = "seek";
    public static final int SEEK_COMMAND_ID = 3;
    public static final String SET_SUBTITLE_TRACK_COMMAND_NAME = "setSubtitleTrack";
    public static final int SET_SUBTITLE_TRACK_COMMAND_ID = 4;
    public static final String SET_AUDIO_TRACK_COMMAND_NAME = "setAudioTrack";
    public static final int SET_AUDIO_TRACK_COMMAND_ID = 5;
    public static final int SEEK_COMMAND_TIME_ARGUMENT_INDEX = 0;

}
