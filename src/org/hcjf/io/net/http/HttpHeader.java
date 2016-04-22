package org.hcjf.io.net.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a http header and contains all
 * the components of the headers.
 * @author javaito
 * @email javaito@gmail.com
 */
public class HttpHeader {

    private static final char HEADER_ASIGNATION = ':';
    private static final char HEADER_PARAMETER_DELIMITER = ';';
    private static final char HEADER_PARAMETER_DELIMITER2 = ' ';
    private static final char HEADER_PARAMETERS_SEPARATOR = ',';
    private static final char HEADER_PARAMETER_ASIGNATION = '=';
    private static final char HEADER_SCAPE_CHARACTER = '\"';

    private static int HEADER_NAME = 0;
    private static int HEADER_VALUE = 1;
    private static int HEADER_PARAMETER = 2;

    private String headerName;
    private String headerValue;
    private final List<String> parameterNames;
    private final Map<String, String> parameters;

    public HttpHeader(String header) {
        this.parameters = new HashMap();
        this.parameterNames = new ArrayList<>();
        parseHeader(header);
    }

    public HttpHeader(String headerName, String headerValue) {
        this.parameters = new HashMap();
        this.parameterNames = new ArrayList<>();
        try {
            parseHeader(headerName + ": " + headerValue);
        } catch (Exception ex) {
            this.headerName = headerName;
            this.headerValue = headerValue;
        }
    }

    /**
     * Parse the http header and create all the header's components.
     * @param header Header http.
     */
    private void parseHeader(String header) {
        try {
            boolean scape = false;
            String currentParameterName = "";
            StringBuilder buffer = new StringBuilder();
            int state = HEADER_NAME;
            for(char currentChar : header.toCharArray()) {
                switch(currentChar) {
                    case HEADER_ASIGNATION: {
                        if(state == HEADER_NAME) {
                            this.headerName = buffer.toString().trim();
                            buffer.delete(0, buffer.length());
                            state = HEADER_VALUE;
                        } else {
                            buffer.append(currentChar);
                        }
                        break;
                    }
                    case HEADER_PARAMETER_DELIMITER:
                    case HEADER_PARAMETER_DELIMITER2: {
                        if(state == HEADER_VALUE && buffer.length() > 0) {
                            this.headerValue = buffer.toString().trim();
                            buffer.delete(0, buffer.length());
                            state = HEADER_PARAMETER;
                        } else if(!scape && state == HEADER_PARAMETER && (buffer.length() > 0)) {
                            if(currentParameterName.isEmpty()) {
                                addParameter(buffer.toString(), "");
                            } else {
                                addParameter(currentParameterName, buffer.toString());
                            }
                            currentParameterName = "";
                            buffer.delete(0, buffer.length());
                        } else {
                            buffer.append(currentChar);
                        }
                        break;
                    }
                    case HEADER_PARAMETERS_SEPARATOR: {
                        if(!scape && state == HEADER_PARAMETER && (buffer.length() > 0)) {
                            if(currentParameterName.isEmpty()) {
                                addParameter(buffer.toString(), "");
                            } else {
                                addParameter(currentParameterName, buffer.toString());
                            }
                            currentParameterName = "";
                            buffer.delete(0, buffer.length());
                        } else {
                            buffer.append(currentChar);
                        }
                        break;
                    }
                    case HEADER_PARAMETER_ASIGNATION: {
                        if(!scape && state == HEADER_PARAMETER) {
                            currentParameterName = buffer.toString();
                            buffer.delete(0, buffer.length());
                        } else {
                            buffer.append(currentChar);
                        }
                        break;
                    }
                    case HEADER_SCAPE_CHARACTER: {
                        scape = !scape;
                        break;
                    }
                    default: {
                        buffer.append(currentChar);
                    }
                }
            }

            if(state == HEADER_VALUE) {
                this.headerValue = buffer.toString().trim();
            } else if(state == HEADER_PARAMETER) {
                if(currentParameterName.isEmpty()) {
                    addParameter(buffer.toString(), "");
                } else {
                    addParameter(currentParameterName, buffer.toString());
                }
            }
        } catch (Exception ex){
            throw new IllegalArgumentException("Unable to parse http header: " + header, ex);
        }
    }

    /**
     * Add the new field into the header.
     * @param parameterName Name of the field.
     * @param parameterValue Value fo the field.
     */
    public void addParameter(String parameterName, String parameterValue){
        parameterName = parameterName.trim();
        if(!parameterName.isEmpty()){
            parameters.put(parameterName, parameterValue.trim());
            parameterNames.add(parameterName);
        }
    }

    /**
     * Return the value of the named field.
     * @param parameterName Name of the field..
     * @return Value of the field.
     */
    public String getParameter(String parameterName){
        return parameters.get(parameterName);
    }

    /**
     * Find the value of the field that is in the place indicated for the index.
     * The firs value is the index cero.
     * @param index Index to indicate the field.
     * @return Return the value of the founded field.
     */
    public String getParameter(int index) {
        String result = null;
        String name = null;
        String value = null;
        if(index >=0 && index < parameterNames.size()) {
            name = parameterNames.get(index);
            value = parameters.get(name);
            //When the parameter comes without name in the header then it's value
            //is storage like a key and the place of value is null.
            if(value.isEmpty()){
                result = name;
            }else{
                result = value;
            }
        }
        return result;
    }

    /**
     * Check if the parameter exists into the fields.
     * @param parameterName Field name.
     * @return Return true if the parameter exists and false if not exists.
     */
    public boolean contains(String parameterName) {
        return parameters.containsKey(parameterName);
    }

    /**
     * Retur all the parameter's names.
     * @return List with the names.
     */
    public List<String> getParametersName(){
        return parameterNames;
    }
    /**
     * Devuelve el nombre del header.
     * @return Nombre del header.
     */
    public String getHeaderName() {
        return headerName;
    }

    /**
     * Return the header name.
     * @return Header name.
     */
    public String getHeaderValue() {
        return headerValue;
    }

    /**
     * Print the header with the http header standard format.
     * @return Header's print.
     */
    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Print the header with the http header standard format.
     * @param includeHeaderName
     * @return Header's print.
     */
    public String toString(boolean includeHeaderName) {
        StringBuilder result = new StringBuilder();
        if(includeHeaderName) {
            result.append(getHeaderName());
            result.append(HEADER_ASIGNATION).append(" ");
        }
        result.append(getHeaderValue());
        if(parameterNames.size() > 0) {
            result.append(HEADER_PARAMETER_DELIMITER);
            String separator = "";
            String value;
            for(String name : parameterNames){
                result.append(separator);
                result.append(name);
                value = parameters.get(name);
                if(!value.isEmpty()){
                    result.append(HEADER_PARAMETER_ASIGNATION);
                    result.append(HEADER_SCAPE_CHARACTER);
                    result.append(value);
                    result.append(HEADER_SCAPE_CHARACTER);
                }
                separator = ",";
            }
        }
        return result.toString();
    }

}
