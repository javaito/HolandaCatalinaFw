package org.hcjf.utils;

import org.hcjf.layers.Layers;
import org.hcjf.layers.locale.LocaleLayerInterface;
import org.hcjf.properties.SystemProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Message manager
 * @author javaito
 *
 */
public class Messages {

    private final Map<String, String> defaultMessages;

    protected Messages() {
        defaultMessages = new HashMap<>();

        //Publishing default layers
        Layers.publishLayer(SystemProperties.getClass(SystemProperties.Locale.DEFAULT_LOCALE_LAYER_IMPLEMENTATION));
    }

    /**
     * Returnt he message associated to the error code.
     * @param messageCode Message code.
     * @param localeLayerName Implementation name of the locale layer.
     * @param params Parameters to complete the message.
     * @return Message complete and translated.
     */
    protected String getInternalMessage(String messageCode, String localeLayerName, Object... params) {
        String result = null;

        if(localeLayerName != null) {
            try {
                result = Layers.get(LocaleLayerInterface.class, localeLayerName).translate(messageCode);
            } catch (Exception ex) {
            }
        }

        if(result == null) {
            result = defaultMessages.get(messageCode);
            if(result == null) {
                result = messageCode;
            }
        }

        return String.format(result, params);
    }

    /**
     * Add the default value associated to error code.
     * @param errorCode Error code.
     * @param defaultMessage Default message.
     */
    protected void addInternalDefault(String errorCode, String defaultMessage) {
        defaultMessages.put(errorCode, defaultMessage);
    }

}
