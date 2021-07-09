import { NativeModules, DeviceEventEmitter } from 'react-native';

const VLCCastingModule = NativeModules.VLCCastingModule;

const addOnRenderersChangeListener = (cb) => {
    DeviceEventEmitter.addListener(VLCCastingModule.ON_RENDERERS_CHANGE, cb);
};

const removeOnRenderersChangeListener = (cb) => {
    DeviceEventEmitter.removeListener(VLCCastingModule.ON_RENDERERS_CHANGE, cb);
};

const addOnSelectedRendererChangeListener = (cb) => {
    DeviceEventEmitter.addListener(VLCCastingModule.ON_SELECTED_RENDERER_CHANGE, cb);
};

const removeOnSelectedRendererChangeListener = (cb) => {
    DeviceEventEmitter.removeListener(VLCCastingModule.ON_SELECTED_RENDERER_CHANGE, cb);
};

const getRenderers = (cb) => {
    VLCCastingModule.getRenderers(cb);
};

const getSelectedRenderer = (cb) => {
    VLCCastingModule.getSelectedRenderer(cb);
};

const setSelectedRenderer = (renderer) => {
    VLCCastingModule.setSelectedRenderer(renderer);
};

export default {
    getRenderers,
    getSelectedRenderer,
    setSelectedRenderer,
    addOnRenderersChangeListener,
    removeOnRenderersChangeListener,
    addOnSelectedRendererChangeListener,
    removeOnSelectedRendererChangeListener
};
