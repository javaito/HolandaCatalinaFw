package org.hcjf.utils;

import org.hcjf.layers.Layers;
import org.hcjf.layers.locale.LocaleLayerInterface;
import org.hcjf.properties.SystemProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Message manager
 * @author javaito
 * @email javaito@gmail.com
 */
public class Messages {

    private final Map<String, String> defaultMessages;

    protected Messages() {
        defaultMessages = new HashMap<>();

        //Publishing default layers
        Layers.publishLayer(SystemProperties.getClass(SystemProperties.HCJF_DEFAULT_LOCALE_LAYER_IMPLEMENTATION));
    }

    /**
     * Returnt he message associated to the error code.
     * @param messageCode Message code.
     * @param params Parameters to complete the message.
     * @return Message complete and translated.
     */
    protected String getInternalMessage(String messageCode, Object... params) {
        String result = defaultMessages.get(messageCode);

        if(result == null) {
            result = messageCode;
        } else {
            result = Layers.get(LocaleLayerInterface.class,
                    SystemProperties.get(SystemProperties.HCJF_DEFAULT_LOCALE_LAYER_IMPLEMENTATION_NAME)).translate(result);
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
