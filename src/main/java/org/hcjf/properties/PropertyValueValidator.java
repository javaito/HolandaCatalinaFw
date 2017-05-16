package org.hcjf.properties;

/**
 * This interface define the validators for the values of the properties.
 * @author javaito
 *
 */
public interface PropertyValueValidator<O extends Object> {

    /**
     * This method is to validate the value to be returned
     * @param value Value to be returned
     * @return True if the value is valid and false if the value is not valid.
     */
    public boolean validate(O value);

}
