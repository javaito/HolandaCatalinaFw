package org.hcjf.layers.locale;

import org.hcjf.layers.Layer;
import org.hcjf.properties.SystemProperties;

import java.util.Locale;

/**
 * This is the default implementation
 * @author javaito
 */
public class DefaultLocaleLayer extends Layer implements LocaleLayerInterface {

    public DefaultLocaleLayer() {
        super(SystemProperties.get(SystemProperties.Locale.DEFAULT_LOCALE_LAYER_IMPLEMENTATION_NAME));
    }

    /**
     * Do nothing with the text parameter.
     * @param text Text to translate.
     * @return Return the same text.
     */
    @Override
    public String translate(String text) {
        return text;
    }

}
