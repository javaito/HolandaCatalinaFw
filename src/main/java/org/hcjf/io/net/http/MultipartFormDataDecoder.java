package org.hcjf.io.net.http;

import org.hcjf.encoding.MimeType;
import org.hcjf.layers.Layer;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Bytes;
import org.hcjf.utils.Strings;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * This layer implementation contains the logic to decode a body
 * using the form url encoding standard method, that correspond with
 * the content type header 'multipart/form-data'
 * @author javaito
 */
public class MultipartFormDataDecoder extends Layer implements RequestBodyDecoderLayer {

    private static final String NAME_FIELD = "name";
    private static final String FILE_NAME_FIELD = "filename";
    private static final String FIELDS_SEPARATOR = ";";
    private static final String BOUNDARY_START = "--";

    public MultipartFormDataDecoder() {
        super(HttpHeader.MULTIPART_FORM_DATA);
    }

    @Override
    public Map<String, Object> decode(HttpRequest request) {
        Map<String,Object> parameters = new HashMap<>();
        HttpHeader contentType = request.getHeader(HttpHeader.CONTENT_TYPE);
        String boundary = BOUNDARY_START + contentType.getParameter(HttpHeader.MULTIPART_FORM_DATA, HttpHeader.BOUNDARY);

        String stringLine;
        byte[] line;
        byte[] file;
        HttpHeader lineContentDisposition;
        HttpHeader lineContentType;
        MimeType mimeType = null;
        String name = null;
        String fileName = null;
        HttpRequest.AttachFile attachFile;
        ArrayList<HttpRequest.AttachFile> attachFiles;
        for(byte[] part : Bytes.split(request.getBody(), boundary.getBytes())) {
            if(part.length == 0){
                continue;
            }

            if(Arrays.equals(part, BOUNDARY_START.getBytes())) {
                break;
            } else {
                mimeType = null;
                name = null;
                fileName = null;
                List<Integer> indexes = Bytes.allIndexOf(part, HttpPackage.STRING_LINE_SEPARATOR.getBytes());
                String charset = null;
                Integer startIndex = 0;
                for(Integer index : indexes) {
                    line = new byte[index - startIndex];
                    System.arraycopy(part, startIndex, line, 0, line.length);
                    stringLine = new String(line).trim();
                    if(stringLine.isEmpty()) {
                        startIndex = index + HttpPackage.STRING_LINE_SEPARATOR.getBytes().length;
                        continue;
                    }
                    if(stringLine.startsWith(HttpHeader.CONTENT_DISPOSITION)) {
                        lineContentDisposition = new HttpHeader(stringLine);
                        for(String headerPart : lineContentDisposition.getHeaderValue().split(FIELDS_SEPARATOR)){
                            if(headerPart.trim().startsWith(NAME_FIELD)) {
                                name = headerPart.substring(headerPart.indexOf(Strings.ASSIGNATION) + 1).trim().replace("\"", Strings.EMPTY_STRING);
                            } if(headerPart.trim().startsWith(FILE_NAME_FIELD)) {
                                fileName = headerPart.substring(headerPart.indexOf(Strings.ASSIGNATION) + 1).trim().replace("\"", Strings.EMPTY_STRING);
                            }
                        }
                        startIndex = index + HttpPackage.STRING_LINE_SEPARATOR.getBytes().length;
                    } else if(stringLine.startsWith(HttpHeader.CONTENT_TYPE)) {
                        lineContentType = new HttpHeader(stringLine);
                        mimeType = MimeType.fromString(lineContentType.getHeaderValue());
                        startIndex = index + HttpPackage.STRING_LINE_SEPARATOR.getBytes().length;
                        charset = contentType.getParameter(
                                contentType.getGroups().iterator().next(), HttpHeader.PARAM_CHARSET);
                    } else if(stringLine.trim().isEmpty()) {
                        break;
                    }
                }

                if(charset == null) {
                    charset = SystemProperties.getDefaultCharset();
                }

                if(name != null) {
                    if (part.length - startIndex >= HttpPackage.STRING_LINE_SEPARATOR.getBytes().length) {
                        file = new byte[part.length - startIndex - HttpPackage.STRING_LINE_SEPARATOR.getBytes().length];
                        System.arraycopy(part, startIndex, file, 0, file.length);
                    } else {
                        file = new byte[0];
                    }

                    if (fileName != null) {
                        attachFile = new HttpRequest.AttachFile(name, fileName, mimeType == null ? MimeType.APPLICATION_X_BINARY : mimeType, file);
                        if(parameters.containsKey(name) && parameters.get(name) instanceof ArrayList) {
                            attachFiles = (ArrayList<HttpRequest.AttachFile>) parameters.get(name);
                            attachFiles.add(attachFile);
                        } else if(parameters.containsKey(name) && parameters.get(name) instanceof HttpRequest.AttachFile) {
                            attachFiles = new ArrayList<>();
                            attachFiles.add((HttpRequest.AttachFile) parameters.get(name));
                            attachFiles.add(attachFile);
                            parameters.put(name, attachFiles);
                        } else {
                            parameters.put(name, attachFile);
                        }
                    } else {
                        String value = new String(file);
                        try {
                            parameters.put(name, URLDecoder.decode(value, charset));
                        } catch (UnsupportedEncodingException e) {
                            Log.w(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Unable to decode http parameter, %s:%s", name, value);
                            parameters.put(name, value);
                        }
                    }
                }
            }
        }
        return parameters;
    }

}
