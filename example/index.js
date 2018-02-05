import React, { Component } from 'react';
import { AppRegistry } from 'react-native';
import VLCVideo from 'react-native-vlc2';

class App extends Component {
    render() {
        return (
            <VLCVideo
                ref={'video'}
                style={{flex: 1, backgroundColor: 'green'}}
                sourceUrl={'http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4'}
                autoplay={true}
                startTime={0}
            />
        );
    }
}

AppRegistry.registerComponent('example', () => App);
