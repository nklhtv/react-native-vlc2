import { NativeModules, DeviceEventEmitter } from 'react-native';

const VLCCastingModule = NativeModules.VLCCastingModule;

const addOnRenderersChangeListener = (cb) => {
    DeviceEventEmitter.addListener(VLCCastingModule.ON_RENDERERS_CHANGE, cb);
};

const removeOnRenderersChangeListener = (cb) => {
    DeviceEventEmitter.removeListener(VLCCastingModule.ON_RENDERERS_CHANGE, cb);
};

const getRenderers = (cb) => {
    VLCCastingModule.getRenderers(cb);
};

export default {
    getRenderers,
    addOnRenderersChangeListener,
    removeOnRenderersChangeListener
};
