package org.hcjf.layers.locale;

import org.hcjf.layers.LayerInterface;

/**
 * This interface provides some methods access the locale information
 * based on the locale of the service session.
 * @author javaito
 * @email javaito@gmail.com
 */
public interface LocaleLayerInterface extends LayerInterface {

    /**
     * Translate some text.
     * @param text Text to translate.
     * @return Return the translated text.
     */
    public String translate(String text);

}
