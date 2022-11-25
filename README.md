# react-native-vlc2

Move over to [Stremio/react-native-vlc2](https://github.com/Stremio/react-native-vlc2)

## iOS is not supported yet

```JSX
import React, { Component } from 'react';
import VLCVideo from 'react-native-vlc2';

class Player extends Component {
    play() {
        this.refs.video && this.refs.video.play();
    }

    pause() {
        this.refs.video && this.refs.video.pause();
    }

    seek(time) {
        this.refs.video && this.refs.video.seek(time);
    }

    render() {
        return (
            <VLCVideo
                ref={'video'}
                style={{ flex: 1 }}
                sourceUrl={'http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4'}
                autoplay={true}
                startTime={0}
            //   onSeekRequested={this.onSeekRequested}
            //   onBuffering={this.onBuffering}
            //   onPlaying={this.onPlaying}
            //   onPaused={this.onPaused}
            //   onEndReached={this.onEndReached}
            //   onError={this.onError}
            //   onTimeChanged={this.onTimeChanged}
            //   onSeekPerformed={this.onSeekPerformed}
            />
        );
    }
}
```
