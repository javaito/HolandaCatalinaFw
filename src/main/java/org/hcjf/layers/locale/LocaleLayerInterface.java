package org.hcjf.layers.locale;

import org.hcjf.layers.LayerInterface;

/**
 * This interface provides some methods access the locale information
 * based on the locale of the service session.
 * @author javaito
 */
public interface LocaleLayerInterface extends LayerInterface {

    /**
     * Translate some text to the specific language.
     * @param text Text to translate.
     * @return Returns translated text.
     */
    public String translate(String text);

}
