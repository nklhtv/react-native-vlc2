import React, { Component } from 'react';
import { View, AppRegistry } from 'react-native';
import VLCVideo from 'react-native-vlc2';

class App extends Component {
    render() {
        return (
            <View style={{flex: 1, backgroundColor: 'green'}}>
                <VLCVideo
                    style={{flex: 1}}
                    sourceUrl={'http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4'}
                    autoplay={true}
                    startTime={0}
                />
                <VLCVideo
                    style={{flex: 1}}
                    sourceUrl={'http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4'}
                    autoplay={true}
                    startTime={0}
                />
            </View>
        );
    }
}

AppRegistry.registerComponent('example', () => App);
