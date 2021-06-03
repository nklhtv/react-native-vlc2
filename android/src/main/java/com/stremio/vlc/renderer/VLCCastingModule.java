package com.stremio.vlc.renderer;

import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.RendererDiscoverer;
import org.videolan.libvlc.RendererItem;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public final class VLCCastingModule extends ReactContextBaseJavaModule {

    private static final String REACT_CLASS = VLCCastingModule.class.getSimpleName();

    private final LibVLC mLibVLC;
    private final Set<RendererDiscoverer> mDiscoverers = new HashSet<>();
    private final Set<RendererItem> mRenderers = new HashSet<>();
    private final ObservableField<RendererItem> mSelectedRenderer;
    private final RendererDiscoverer.EventListener mDiscovererEventListener = new RendererDiscoverer.EventListener() {
        @Override
        public void onEvent(final RendererDiscoverer.Event event) {
            final RendererItem renderer = event.getItem();
            switch (event.type) {
                case RendererDiscoverer.Event.ItemAdded: {
                    mRenderers.add(renderer);
                    break;
                }
                case RendererDiscoverer.Event.ItemDeleted: {
                    mRenderers.remove(renderer);
                    if (renderer == mSelectedRenderer.get()) {
                        mSelectedRenderer.set(null);
                    }
                    break;
                }
            }

            eminOnRenderersChange();
        }
    };
    private final Observable.OnPropertyChangedCallback mSelectedRendererListener = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(final Observable sender, final int id) {
            emitOnSelectedRendererChange();
        }
    };

    private DeviceEventManagerModule.RCTDeviceEventEmitter mDeviceEventEmitter;

    public VLCCastingModule(final ReactApplicationContext reactContext, final LibVLC libVLC, final ObservableField<RendererItem> selectedRenderer) {
        super(reactContext);
        mLibVLC = libVLC;
        mSelectedRenderer = selectedRenderer;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = MapBuilder.newHashMap();

        constants.put("ON_RENDERERS_CHANGE", VLCCastingEvents.ON_RENDERERS_CHANGE);
        constants.put("ON_SELECTED_RENDERER_CHANGE", VLCCastingEvents.ON_SELECTED_RENDERER_CHANGE);

        return constants;
    }

    @Override
    public void initialize() {
        super.initialize();
        mDeviceEventEmitter = getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        mSelectedRenderer.addOnPropertyChangedCallback(mSelectedRendererListener);
        for (final RendererDiscoverer.Description discovererDescription : RendererDiscoverer.list(mLibVLC)) {
            final RendererDiscoverer discoverer = new RendererDiscoverer(mLibVLC, discovererDescription.name);
            mDiscoverers.add(discoverer);
            discoverer.setEventListener(mDiscovererEventListener);
            startDiscoverer(discoverer);
        }
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        mSelectedRenderer.removeOnPropertyChangedCallback(mSelectedRendererListener);
        for (final RendererDiscoverer discoverer : mDiscoverers) {
            discoverer.setEventListener(null);
            if (!discoverer.isReleased()) {
                discoverer.stop();
            }
        }
    }

    @ReactMethod
    public void getRenderers(final Callback callback) {
        final WritableMap event = createRenderersEvent();
        callback.invoke(event);
    }

    @ReactMethod
    public void getSelectedRenderer(final Callback callback) {
        final WritableMap event = createSelectedRendererEvent();
        callback.invoke(event);
    }

    @ReactMethod
    public void setSelectedRenderer(final String rendererDisplayName) {
        for (final RendererItem renderer : mRenderers) {
            if (renderer.displayName.equals(rendererDisplayName)) {
                mSelectedRenderer.set(renderer);
                return;
            }
        }

        mSelectedRenderer.set(null);
    }

    private void emitOnSelectedRendererChange() {
        final WritableMap event = createSelectedRendererEvent();
        mDeviceEventEmitter.emit(VLCCastingEvents.ON_SELECTED_RENDERER_CHANGE, event);
    }

    private void eminOnRenderersChange() {
        final WritableMap event = createRenderersEvent();
        mDeviceEventEmitter.emit(VLCCastingEvents.ON_RENDERERS_CHANGE, event);
    }

    private WritableMap createSelectedRendererEvent() {
        final WritableMap event = Arguments.createMap();
        final RendererItem renderer = mSelectedRenderer.get();
        if (renderer != null) {
            event.putString(VLCCastingEvents.ON_SELECTED_RENDERER_CHANGE_RENDERER_PROP, renderer.displayName);
        } else {
            event.putNull(VLCCastingEvents.ON_SELECTED_RENDERER_CHANGE_RENDERER_PROP);
        }
        return event;
    }

    private WritableMap createRenderersEvent() {
        final WritableMap event = Arguments.createMap();
        final WritableArray renderersValue = Arguments.createArray();
        for (final RendererItem renderer : mRenderers) {
            renderersValue.pushString(renderer.displayName);
        }
        event.putArray(VLCCastingEvents.ON_RENDERERS_CHANGE_RENDERERS_PROP, renderersValue);
        return event;
    }

    private void startDiscoverer(final RendererDiscoverer discoverer) {
        final Timer timer = new Timer();
        final AtomicInteger retriesCounter = new AtomicInteger(5);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final int retriesLeft = retriesCounter.decrementAndGet();
                if (discoverer.isReleased() || discoverer.start() || retriesLeft == 0) {
                    timer.cancel();
                }
            }
        }, 0, 1000);
    }
}
