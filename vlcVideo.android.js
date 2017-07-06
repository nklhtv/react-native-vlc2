import React, { Component, PropTypes } from 'react';
import { View, UIManager, requireNativeComponent, findNodeHandle } from 'react-native';

const EMPTY_SOURCE_URL = 'EMPTY_SOURCE_URL';
const RCTVLCVideoViewConstants = UIManager.RCTVLCVideoView.Constants;

class VLCVideo extends Component {
    constructor(props) {
        super(props);

        this.callbacks = {
            [RCTVLCVideoViewConstants.ON_MEDIA_CHANGED]: this._invokeEventCallback.bind(this, 'onMediaChanged'),
            [RCTVLCVideoViewConstants.ON_BUFFERING]: this._invokeEventCallback.bind(this, 'onBuffering'),
            [RCTVLCVideoViewConstants.ON_PLAYING]: this._invokeEventCallback.bind(this, 'onPlaying'),
            [RCTVLCVideoViewConstants.ON_PAUSED]: this._invokeEventCallback.bind(this, 'onPaused'),
            [RCTVLCVideoViewConstants.ON_STOPPED]: this._invokeEventCallback.bind(this, 'onStopped'),
            [RCTVLCVideoViewConstants.ON_END_REACHED]: this._invokeEventCallback.bind(this, 'onEndReached'),
            [RCTVLCVideoViewConstants.ON_ERROR]: this._invokeEventCallback.bind(this, 'onError'),
            [RCTVLCVideoViewConstants.ON_TIME_CHANGED]: this._invokeEventCallback.bind(this, 'onTimeChanged')
        };

        this.state = {
            sourceUrl: props.sourceUrl,
            autoplay: props.autoplay,
            startTime: props.startTime,
            keyControlEnabled: props.keyControlEnabled
        };
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.sourceUrl !== this.state.sourceUrl) {
            this.setState({ sourceUrl: nextProps.sourceUrl });
        }

        if (nextProps.autoplay !== this.state.autoplay) {
            this.setState({ autoplay: nextProps.autoplay });
        }

        if (nextProps.startTime !== this.state.startTime) {
            this.setState({ startTime: nextProps.startTime });
        }

        if (nextProps.keyControlEnabled !== this.state.keyControlEnabled) {
            this.setState({ keyControlEnabled: nextProps.keyControlEnabled });
        }
    }

    shouldComponentUpdate(nextProps, nextState) {
        return nextState.sourceUrl !== this.state.sourceUrl ||
            nextState.keyControlEnabled !== this.state.keyControlEnabled ||
            nextProps.style !== this.props.style;
    }
    
    render() {
        const media = {
            sourceUrl: this.state.sourceUrl,
            autoplay: this.state.autoplay,
            startTime: this.state.startTime
        };

        return (
            <RCTVLCVideoView 
                ref={(root) => this._root = root}
                style={this.props.style}
                keyControlEnabled={this.state.keyControlEnabled}
                media={media}
                {...this.callbacks}
            />
        );
    }

    _invokeEventCallback(eventName, event) {
        if (typeof this.props[eventName] === 'function') {
            this.props[eventName](event.nativeEvent);
        }
    }

    _getViewHandle() {
        return findNodeHandle(this._root);
    }

    seek(time) {
        if (typeof time !== 'number' || isNaN(time) || time < 0) {
            time = 0;
        }

        UIManager.dispatchViewManagerCommand(
            this._getViewHandle(),
            UIManager.RCTVLCVideoView.Commands.seek,
            time
        );
    }

    play() {
        UIManager.dispatchViewManagerCommand(
            this._getViewHandle(),
            UIManager.RCTVLCVideoView.Commands.play,
            null
        );
    }

    pause() {
        UIManager.dispatchViewManagerCommand(
            this._getViewHandle(),
            UIManager.RCTVLCVideoView.Commands.pause,
            null
        );
    }

}

VLCVideo.propTypes = {
    ...View.propTypes,
    sourceUrl: PropTypes.string.isRequired,
    autoplay: PropTypes.bool.isRequired,
    startTime: PropTypes.number.isRequired,
    keyControlEnabled: PropTypes.bool.isRequired,
    onMediaChanged: PropTypes.func,
    onBuffering: PropTypes.func,
    onPlaying: PropTypes.func,
    onPaused: PropTypes.func,
    onStopped: PropTypes.func,
    onEndReached: PropTypes.func,
    onError: PropTypes.func,
    onTimeChanged: PropTypes.func
};

VLCVideo.defaultProps = {
    sourceUrl: EMPTY_SOURCE_URL,
    autoplay: true,
    startTime: 0,
    keyControlEnabled: false
};

const RCTVLCVideoViewInterface = {
    name: 'VLCVideo',
    propTypes: {
        ...View.propTypes,
        keyControlEnabled: PropTypes.bool.isRequired,
        onMediaChanged: PropTypes.func,
        onBuffering: PropTypes.func,
        onPlaying: PropTypes.func,
        onPaused: PropTypes.func,
        onStopped: PropTypes.func,
        onEndReached: PropTypes.func,
        onError: PropTypes.func,
        onTimeChanged: PropTypes.func
    }
};

const RCTVLCVideoView = requireNativeComponent('RCTVLCVideoView', RCTVLCVideoViewInterface, {
    nativeOnly: {
        media: true,
        keyControlEnabled: true
    }
});

export default VLCVideo;
