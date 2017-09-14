package org.hcjf.layers.locale;

import org.hcjf.layers.Layer;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.ServiceSession;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This class use the java resource bundle api to translate the text
 * for the specific locale.
 * @author javaito
 */
public abstract class PropertiesFileLocaleLayer extends Layer implements LocaleLayerInterface {

    public PropertiesFileLocaleLayer(String implName) {
        super(implName);
    }

    /**
     * Changes the parameter string with the value into the bundle file indexed
     * by the parameter text.
     * @param text Text to translate.
     * @return Translated text or the same text if the are any error.
     */
    @Override
    public String translate(String text) {
        String result = text;
        try {
            result = ResourceBundle.getBundle(getImplName(), ServiceSession.getCurrentIdentity().getLocale()).getString(text);
        } catch (Exception ex) {
            Log.w(SystemProperties.get(SystemProperties.Locale.LOG_TAG),
                    "Unable to translate text (%s)", ex, text);
        }
        return result;
    }
}
