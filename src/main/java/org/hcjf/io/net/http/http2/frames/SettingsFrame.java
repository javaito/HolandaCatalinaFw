package org.hcjf.io.net.http.http2.frames;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsFrame extends Http2Frame {

    private static final Integer SETTING_PAYLOAD_LENGTH = 6;

    private Map<Short,Integer> settings;
    private List<Short> order;

    public SettingsFrame(Integer id, Byte flags, Integer length) {
        super(id, flags, length, Type.SETTINGS);
        settings = new HashMap<>();
    }

    public SettingsFrame(Integer id, Byte flags, SettingsFrame settingsFrame) {
        super(id, flags, settingsFrame.getLength(), Type.SETTINGS);
        this.settings = new HashMap<>();
        this.settings.putAll(settingsFrame.settings);
        this.order = new ArrayList<>();
        this.order.addAll(settingsFrame.order);
    }

    public static SettingsFrame createDefaultSettingsFrame(Integer id) {
        SettingsFrame settingsFrame = new SettingsFrame(id, (byte)0, 1);
        settingsFrame.setMaxHeaderListSize(Settings.SETTINGS_MAX_HEADER_LIST_SIZE.getDefaultValue());
        settingsFrame.setMaxConcurrentStreams(Settings.SETTINGS_MAX_CONCURRENT_STREAMS.getDefaultValue());
        settingsFrame.setMaxFrameSize(Settings.SETTINGS_MAX_FRAME_SIZE.getDefaultValue());
        settingsFrame.setInitialWindowsSize(Settings.SETTINGS_INITIAL_WINDOW_SIZE.getDefaultValue());
        settingsFrame.setHeaderTableSize(Settings.SETTINGS_HEADER_TABLE_SIZE.getDefaultValue());
        //settingsFrame.setEnablePush(Settings.SETTINGS_ENABLE_PUSH.getDefaultValue());
        return settingsFrame;
    }

    @Override
    protected Integer recalculateLength() {
        Integer length = settings.size() * SETTING_PAYLOAD_LENGTH;
        return length;
    }

    @Override
    protected void processPayload() {
        order = new ArrayList<>();
        ByteBuffer payload = getPayload();
        while(payload.limit() > payload.position()) {
            Short settingId = (short) (((payload.get() & 0x0F) << 8) | (payload.get() & 0xFF));
            Integer settingValue = ((payload.get() & 0xFF) << 24) | ((payload.get() & 0xFF) << 16) | ((payload.get() & 0xFF) << 8) | (payload.get() & 0xFF);
            setSetting(settingId, settingValue);
            order.add(settingId);
        }
        System.out.println();
    }

    @Override
    protected ByteBuffer serializePayload(ByteBuffer fixedBuffer) {
        if(order != null) {
            for (Short id : order) {
                fixedBuffer.putShort(id);
                fixedBuffer.putInt(settings.get(id));
            }
        } else {
            for (Short id : settings.keySet()) {
                fixedBuffer.putShort(id);
                fixedBuffer.putInt(settings.get(id));
            }
        }
        return fixedBuffer;
    }

    private void setSetting(Short id, Integer value) {
        settings.put(id, value);
    }

    public Integer getHeaderTableSize() {
        return settings.get(Settings.SETTINGS_HEADER_TABLE_SIZE.id);
    }

    public void setHeaderTableSize(Integer headerTableSize) {
        settings.put(Settings.SETTINGS_HEADER_TABLE_SIZE.id, headerTableSize);
    }

    public Integer getEnablePush() {
        return settings.get(Settings.SETTINGS_ENABLE_PUSH.id);
    }

    public void setEnablePush(Integer enablePush) {
        settings.put(Settings.SETTINGS_ENABLE_PUSH.id, enablePush);
    }

    public Integer getMaxConcurrentStreams() {
        return settings.get(Settings.SETTINGS_MAX_CONCURRENT_STREAMS.id);
    }

    public void setMaxConcurrentStreams(Integer maxConcurrentStreams) {
        settings.put(Settings.SETTINGS_MAX_CONCURRENT_STREAMS.id, maxConcurrentStreams);
    }

    public Integer getInitialWindowsSize() {
        return settings.get(Settings.SETTINGS_INITIAL_WINDOW_SIZE.id);
    }

    public void setInitialWindowsSize(Integer initialWindowsSize) {
        settings.put(Settings.SETTINGS_INITIAL_WINDOW_SIZE.id, initialWindowsSize);
    }

    public Integer getMaxFrameSize() {
        return settings.get(Settings.SETTINGS_MAX_FRAME_SIZE.id);
    }

    public void setMaxFrameSize(Integer maxFrameSize) {
        settings.put(Settings.SETTINGS_MAX_FRAME_SIZE.id, maxFrameSize);
    }

    public Integer getMaxHeaderListSize() {
        return settings.get(Settings.SETTINGS_MAX_HEADER_LIST_SIZE.id);
    }

    public void setMaxHeaderListSize(Integer maxHeaderListSize) {
        settings.put(Settings.SETTINGS_MAX_HEADER_LIST_SIZE.id, maxHeaderListSize);
    }

    public enum Settings {

        SETTINGS_HEADER_TABLE_SIZE((short)0x1, 4096),

        SETTINGS_ENABLE_PUSH((short)0x2, 1),

        SETTINGS_MAX_CONCURRENT_STREAMS((short)0x3, 0),

        SETTINGS_INITIAL_WINDOW_SIZE((short)0x4, 65535),

        SETTINGS_MAX_FRAME_SIZE((short)0x5, 16384),

        SETTINGS_MAX_HEADER_LIST_SIZE((short)0x6, Integer.MAX_VALUE);

        private final short id;
        private final int defaultValue;

        Settings(short id, int defaultValue) {
            this.id = id;
            this.defaultValue = defaultValue;
        }

        public short getId() {
            return id;
        }

        public int getDefaultValue() {
            return defaultValue;
        }
    }
}
