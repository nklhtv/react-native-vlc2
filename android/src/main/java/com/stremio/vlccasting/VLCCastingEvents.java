package com.stremio.vlccasting;

final class VLCCastingEvents {

    private static final String EVENT_NAME_PREFIX = VLCCastingModule.class.getSimpleName();

    static final String ON_SELECTED_RENDERER_CHANGE = getFullEventName("onSelectedRendererChange");
    static final String ON_RENDERERS_CHANGE = getFullEventName("onRenderersChange");

    static final String RENDERER_NAME_PROP = "name";
    static final String RENDERER_DISPLAY_NAME_PROP = "displayName";
    static final String ON_SELECTED_RENDERER_CHANGE_RENDERER_PROP = "renderer";
    static final String ON_RENDERERS_CHANGE_RENDERERS_PROP = "renderers";

    private static String getFullEventName(final String eventName) {
        return EVENT_NAME_PREFIX.concat(eventName);
    }

}
