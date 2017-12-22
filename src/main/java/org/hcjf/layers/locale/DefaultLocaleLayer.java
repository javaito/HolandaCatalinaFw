package org.hcjf.layers.locale;

import org.hcjf.layers.Layer;

/**
 * This is the default implementation
 * @author javaito
 */
public class DefaultLocaleLayer extends Layer implements LocaleLayerInterface {

    public DefaultLocaleLayer() {
    }

    @Override
    public String getImplName(){
        return getClass().getName();
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
