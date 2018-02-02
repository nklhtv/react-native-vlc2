import React, { Component } from 'react';
import { AppRegistry, View } from 'react-native';
import VLCVideo from 'react-native-vlc2';

class App extends Component {
    render() {
        return (
            <View style={{flex: 1, backgroundColor: 'green'}}>
                <VLCVideo
                    ref={'video'}
                    style={{ height: 100, width: '100%' }}
                    sourceUrl={'http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4'}
                    autoplay={true}
                    startTime={0}
                />
            </View>
        );
    }
}

AppRegistry.registerComponent('example', () => App);
