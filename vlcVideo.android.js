import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { View, UIManager, requireNativeComponent, findNodeHandle } from 'react-native';

class VLCVideo extends Component {
    constructor(props) {
        super(props);

        this.callbacks = {
            [RCTVLCVideoViewConstants.ON_SEEK_REQUESTED]: this._invokeEventCallback.bind(this, 'onSeekRequested'),
            [RCTVLCVideoViewConstants.ON_BUFFERING]: this._invokeEventCallback.bind(this, 'onBuffering'),
            [RCTVLCVideoViewConstants.ON_PLAYING]: this._invokeEventCallback.bind(this, 'onPlaying'),
            [RCTVLCVideoViewConstants.ON_PAUSED]: this._invokeEventCallback.bind(this, 'onPaused'),
            [RCTVLCVideoViewConstants.ON_END_REACHED]: this._invokeEventCallback.bind(this, 'onEndReached'),
            [RCTVLCVideoViewConstants.ON_ERROR]: this._invokeEventCallback.bind(this, 'onError'),
            [RCTVLCVideoViewConstants.ON_AUDIO_DIGITAL_OUTPUT_ERROR]: this._invokeEventCallback.bind(this, 'onAudioDigitalOutputError'),
            [RCTVLCVideoViewConstants.ON_TIME_CHANGED]: this._invokeEventCallback.bind(this, 'onTimeChanged'),
            [RCTVLCVideoViewConstants.ON_SEEK_PERFORMED]: this._invokeEventCallback.bind(this, 'onSeekPerformed'),
            [RCTVLCVideoViewConstants.ON_SUBTITLE_TRACKS_CHANGED]: this._invokeEventCallback.bind(this, 'onSubtitleTracksChanged'),
            [RCTVLCVideoViewConstants.ON_AUDIO_TRACKS_CHANGED]: this._invokeEventCallback.bind(this, 'onAudioTracksChanged'),
            [RCTVLCVideoViewConstants.ON_SELECTED_SUBTITLE_TRACK_ID_CHANGED]: this._invokeEventCallback.bind(this, 'onSelectedSubtitleTrackIdChanged'),
            [RCTVLCVideoViewConstants.ON_SELECTED_AUDIO_TRACK_ID_CHANGED]: this._invokeEventCallback.bind(this, 'onSelectedAudioTrackIdChanged'),
            [RCTVLCVideoViewConstants.ON_SCALE_TYPE_CHANGED_EVENT]: this._invokeEventCallback.bind(this, 'onScaleTypeChanged')

        };
    }

    shouldComponentUpdate(nextProps, nextState) {
        return nextProps.sourceUrl !== this.props.sourceUrl ||
            nextProps.keyControlEnabled !== this.props.keyControlEnabled ||
            nextProps.playInBackground !== this.props.playInBackground ||
            nextProps.audioDigitalOutput !== this.props.audioDigitalOutput ||
            nextProps.style !== this.props.style;
    }

    _assignRoot = (root) => {
        this._root = root;
    }

    _getViewHandle = () => {
        return findNodeHandle(this._root);
    }

    _invokeEventCallback = (eventName, event) => {
        if (typeof this.props[eventName] === 'function') {
            this.props[eventName](event.nativeEvent);
        }
    }

    seek = (time) => {
        if (typeof time !== 'number' || isNaN(time) || time < 0) {
            time = 0;
        }

        UIManager.dispatchViewManagerCommand(
            this._getViewHandle(),
            UIManager.RCTVLCVideoView.Commands.seek,
            [time]
        );
    }

    play = () => {
        UIManager.dispatchViewManagerCommand(
            this._getViewHandle(),
            UIManager.RCTVLCVideoView.Commands.play,
            null
        );
    }

    pause = () => {
        UIManager.dispatchViewManagerCommand(
            this._getViewHandle(),
            UIManager.RCTVLCVideoView.Commands.pause,
            null
        );
    }

    setSubtitleTrack = (id) => {
        if (typeof id !== 'number' || isNaN(id)) {
            id = -1;
        }

        UIManager.dispatchViewManagerCommand(
            this._getViewHandle(),
            UIManager.RCTVLCVideoView.Commands.setSubtitleTrack,
            [id]
        );
    }

    setAudioTrack = (id) => {
        if (typeof id !== 'number' || isNaN(id)) {
            id = -1;
        }

        UIManager.dispatchViewManagerCommand(
            this._getViewHandle(),
            UIManager.RCTVLCVideoView.Commands.setAudioTrack,
            [id]
        );
    }

    changeScaleType = () => {
        UIManager.dispatchViewManagerCommand(
            this._getViewHandle(),
            UIManager.RCTVLCVideoView.Commands.changeScaleType,
            null
        );
    }

    render() {
        const media = {
            sourceUrl: this.props.sourceUrl,
            autoplay: this.props.autoplay,
            startTime: this.props.startTime,
            title: this.props.title,
            hwDecoderMode: this.props.hwDecoderMode
        };

        return (
            <RCTVLCVideoView
                ref={this._assignRoot}
                style={this.props.style}
                keyControlEnabled={this.props.keyControlEnabled}
                playInBackground={this.props.playInBackground}
                audioDigitalOutput={this.props.audioDigitalOutput}
                media={media}
                {...this.callbacks}
            />
        );
    }

}

VLCVideo.propTypes = {
    style: PropTypes.oneOfType([PropTypes.object, PropTypes.number, PropTypes.array]),
    sourceUrl: PropTypes.string.isRequired,
    autoplay: PropTypes.bool.isRequired,
    startTime: PropTypes.number.isRequired,
    title: PropTypes.string.isRequired,
    hwDecoderMode: PropTypes.number.isRequired,
    keyControlEnabled: PropTypes.bool.isRequired,
    playInBackground: PropTypes.bool.isRequired,
    audioDigitalOutput: PropTypes.bool.isRequired,
    onSeekRequested: PropTypes.func,
    onBuffering: PropTypes.func,
    onPlaying: PropTypes.func,
    onPaused: PropTypes.func,
    onEndReached: PropTypes.func,
    onError: PropTypes.func,
    onAudioDigitalOutputError: PropTypes.func,
    onTimeChanged: PropTypes.func,
    onSeekPerformed: PropTypes.func,
    onSubtitleTracksChanged: PropTypes.func,
    onAudioTracksChanged: PropTypes.func,
    onSelectedSubtitleTrackIdChanged: PropTypes.func,
    onSelectedAudioTrackIdChanged: PropTypes.func,
    onScaleTypeChanged: PropTypes.func
};

VLCVideo.defaultProps = {
    autoplay: true,
    startTime: 0,
    title: '',
    hwDecoderMode: 1,
    keyControlEnabled: false,
    playInBackground: false,
    audioDigitalOutput: false
};

const RCTVLCVideoViewConstants = UIManager.RCTVLCVideoView.Constants;

const RCTVLCVideoViewInterface = {
    name: 'VLCVideo',
    propTypes: {
        ...View.propTypes,
        media: PropTypes.object.isRequired,
        keyControlEnabled: PropTypes.bool.isRequired,
        playInBackground: PropTypes.bool.isRequired,
        audioDigitalOutput: PropTypes.bool.isRequired,
        [RCTVLCVideoViewConstants.ON_SEEK_REQUESTED]: PropTypes.func,
        [RCTVLCVideoViewConstants.ON_BUFFERING]: PropTypes.func,
        [RCTVLCVideoViewConstants.ON_PLAYING]: PropTypes.func,
        [RCTVLCVideoViewConstants.ON_PAUSED]: PropTypes.func,
        [RCTVLCVideoViewConstants.ON_END_REACHED]: PropTypes.func,
        [RCTVLCVideoViewConstants.ON_ERROR]: PropTypes.func,
        [RCTVLCVideoViewConstants.ON_AUDIO_DIGITAL_OUTPUT_ERROR]: PropTypes.func,
        [RCTVLCVideoViewConstants.ON_TIME_CHANGED]: PropTypes.func,
        [RCTVLCVideoViewConstants.ON_SEEK_PERFORMED]: PropTypes.func,
        [RCTVLCVideoViewConstants.ON_SUBTITLE_TRACKS_CHANGED]: PropTypes.func,
        [RCTVLCVideoViewConstants.ON_AUDIO_TRACKS_CHANGED]: PropTypes.func,
        [RCTVLCVideoViewConstants.ON_SELECTED_SUBTITLE_TRACK_ID_CHANGED]: PropTypes.func,
        [RCTVLCVideoViewConstants.ON_SELECTED_AUDIO_TRACK_ID_CHANGED]: PropTypes.func,
        [RCTVLCVideoViewConstants.ON_SCALE_TYPE_CHANGED_EVENT]: PropTypes.func
    }
};

const RCTVLCVideoView = requireNativeComponent('RCTVLCVideoView', RCTVLCVideoViewInterface, {
    nativeOnly: {
        media: true,
        keyControlEnabled: true,
        playInBackground: true,
        audioDigitalOutput: true
    }
});

export default VLCVideo;
