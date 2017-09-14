package org.hcjf.io.net.http;

import org.hcjf.encoding.MimeType;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Bytes;
import org.hcjf.utils.Strings;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * This particular kind of package contains the request information.
 * @author javaito
 */
public class HttpRequest extends HttpPackage {

    private static final int METHOD_INDEX = 0;
    private static final int REQUEST_PATH_INDEX = 1;
    private static final int VERSION_INDEX = 2;

    private String path;
    private String context;
    private HttpMethod method;
    private final Map<String, String> parameters;
    private final Map<String, AttachFile> attachFiles;
    private final List<String> pathParts;

    public HttpRequest(String requestPath, HttpMethod method) {
        this.path = requestPath;
        this.method = method;
        this.parameters = new HashMap<>();
        this.attachFiles = new HashMap<>();
        this.pathParts = new ArrayList<>();
    }

    protected HttpRequest(HttpRequest httpRequest) {
        super(httpRequest);
        this.path = httpRequest.path;
        this.method = httpRequest.method;
        this.parameters = httpRequest.parameters;
        this.attachFiles = httpRequest.attachFiles;
        this.pathParts = httpRequest.pathParts;
    }

    public HttpRequest() {
        this(null, null);
    }

    /**
     * Return the request path
     * @return Request path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the request path.
     * @param path Request path.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Return the request context.
     * @return Request context.
     */
    public String getContext() {
        return context;
    }

    /**
     * Set the request context.
     * @param context Request context.
     */
    public void setContext(String context) {
        this.context = context;
        this.path = context;
    }

    /**
     * Return the request method.
     * @return Request method.
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * Set the request method.
     * @param method Request method.
     */
    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    /**
     * Return the request parameters.
     * @return Request parameters.
     */
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Return the parameter indexed by the argument name.
     * @param parameterName Name of the founding parameter.
     * @return Return the parameter.
     */
    public String getParameter(String parameterName) {
        return parameters.get(parameterName);
    }

    /**
     * Returns the map with all the files attached to the request.
     * @return Map with all the files attached.
     */
    public Map<String, AttachFile> getAttachFiles() {
        return Collections.unmodifiableMap(attachFiles);
    }

    /**
     * Returns the file attached for the specific name.
     * @param attachName Name of the parameter to attach the file.
     * @return Attached file.
     */
    public AttachFile getAtachFile(String attachName) {
        return attachFiles.get(attachName);
    }

    /**
     * Add parameter.
     * @param parameterName Parameter name.
     * @param parameterValue Parameter value.
     */
    public void addHttpParameter(String parameterName, String parameterValue) {
        parameters.put(parameterName, parameterValue);
    }

    /**
     * Return a list with all the parts of the request path.
     * If the path is /path1/path2/pathN, then the method response with
     * a list ad [path1,path2,pathN]
     * @return List with all the parts of the request path.
     */
    public List<String> getPathParts() {
        return pathParts;
    }

    /**
     * This method process the body of the complete request.
     * @param body Body of the request.
     */
    @Override
    protected void processBody(byte[] body) {
        HttpHeader contentType = getHeader(HttpHeader.CONTENT_TYPE);
        if(contentType != null &&
                contentType.getHeaderValue().startsWith(HttpHeader.APPLICATION_X_WWW_FORM_URLENCODED)) {
            parseHttpParameters(new String(body));
        } else if(contentType != null &&
                contentType.getHeaderValue().startsWith(HttpHeader.MULTIPART_FORM_DATA)) {
            parseHttpMultipartBody();
        }
    }

    /**
     * Process the first line of the request.
     * @param firstLine First line of the request.
     */
    @Override
    protected void processFirstLine(String firstLine) {
        String[] parts = firstLine.split(LINE_FIELD_SEPARATOR);

        method = HttpMethod.valueOf(parts[METHOD_INDEX]);
        path = parts[REQUEST_PATH_INDEX];
        setHttpVersion(parts[VERSION_INDEX]);

        //Check if there are parameters into request
        if(path.indexOf(HTTP_FIELD_START) >= 0) {
            context = path.substring(0, path.indexOf(HTTP_FIELD_START));
            parseHttpParameters(path.substring(path.indexOf(HTTP_FIELD_START) + 1));
        } else {
            context = path;
        }

        for(String pathPart : context.split(HTTP_CONTEXT_SEPARATOR)) {
            if(!pathPart.isEmpty()) {
                pathParts.add(pathPart);
            }
        }
    }

    private void parseHttpMultipartBody() {
        HttpHeader contentType = getHeader(HttpHeader.CONTENT_TYPE);
        String boundary = AttachFile.BOUNDARY_START + contentType.getParameter(HttpHeader.MULTIPART_FORM_DATA, HttpHeader.BOUNDARY);

        String stringLine;
        byte[] line;
        byte[] file;
        HttpHeader lineContentDisposition;
        HttpHeader lineContentType;
        MimeType mimeType = null;
        String name = null;
        String fileName = null;
        AttachFile attachFile;
        for(byte[] part : Bytes.split(getBody(), boundary.getBytes())) {
            if(part.length == 0){
                continue;
            }

            if(Arrays.equals(part, AttachFile.BOUNDARY_START.getBytes())) {
                break;
            } else {
                List<Integer> indexes = Bytes.allIndexOf(part, STRING_LINE_SEPARATOR.getBytes());
                Integer startIndex = 0;
                for(Integer index : indexes) {
                    line = new byte[index - startIndex];
                    System.arraycopy(part, startIndex, line, 0, line.length);
                    stringLine = new String(line).trim();
                    if(stringLine.isEmpty()) {
                        startIndex = index + 2;
                        continue;
                    }
                    if(stringLine.startsWith(HttpHeader.CONTENT_DISPOSITION)) {
                        lineContentDisposition = new HttpHeader(stringLine);
                        for(String headerPart : lineContentDisposition.getHeaderValue().split(AttachFile.FIELDS_SEPARATOR)){
                            if(headerPart.trim().startsWith(AttachFile.NAME_FIELD)) {
                                name = headerPart.substring(headerPart.indexOf(Strings.ASSIGNATION) + 1).trim();
                            } if(headerPart.trim().startsWith(AttachFile.FILE_NAME_FIELD)) {
                                fileName = headerPart.substring(headerPart.indexOf(Strings.ASSIGNATION) + 1).trim();
                            }
                        }
                        startIndex = index + 2;
                    } else if(stringLine.startsWith(HttpHeader.CONTENT_TYPE)) {
                        lineContentType = new HttpHeader(stringLine);
                        mimeType = MimeType.fromString(lineContentType.getHeaderValue());
                        startIndex = index + 2;
                    } else if(stringLine.trim().isEmpty()) {
                        break;
                    }
                }

                file = new byte[part.length - startIndex - 2];
                System.arraycopy(part, startIndex, file, 0, file.length - 2);

                if(fileName != null) {
                    attachFile = new AttachFile(name, fileName, mimeType == null ? MimeType.APPLICATION_X_BINARY : mimeType, file);
                    attachFiles.put(name, attachFile);
                } else {
                    parameters.put(name, new String(file));
                }
            }
        }
    }

    /**
     * This method split the parameters body string in each http parameter
     * using the http standard syntax.
     * @param parametersBody String that contains all the parameters.
     */
    private void parseHttpParameters(String parametersBody) {
        String[] params = parametersBody.split(HTTP_FIELD_SEPARATOR);

        String charset = null;
        HttpHeader contentType = getHeader(HttpHeader.CONTENT_TYPE);
        if(contentType != null) {
            //The content-type header should not have more than one group
            //In this case we use the first group.
            charset = contentType.getParameter(
                    contentType.getGroups().iterator().next(), HttpHeader.PARAM_CHARSET);
        }
        if(charset == null) {
            charset = SystemProperties.getDefaultCharset();
        }

        String key;
        String value;
        for(String param : params) {
            if(param.indexOf(HTTP_FIELD_ASSIGNATION) < 0) {
                key = param;
                value = null;
            } else {
                String[] keyValue = param.split(HTTP_FIELD_ASSIGNATION);
                key = keyValue[0];
                value = keyValue.length==2 ? keyValue[1] : null;
            }

            try {
                parameters.put(URLDecoder.decode(key, charset), value == null ? null : URLDecoder.decode(value, charset));
            } catch (UnsupportedEncodingException e) {
                Log.w(SystemProperties.get(SystemProperties.Net.Http.LOG_TAG), "Unable to decode http parameter, %s:%s", key, value);
                parameters.put(key, value);
            }
        }
    }

    /**
     * Return the string that represent the protocol of the package.
     * @return Protocol description.
     */
    private String toStringProtocolHeader() {
        StringBuilder builder = new StringBuilder();

        builder.append(getMethod().toString()).append(LINE_FIELD_SEPARATOR).
                append(getPath()).append(LINE_FIELD_SEPARATOR).
                append(getHttpVersion()).append(STRING_LINE_SEPARATOR);
        for(HttpHeader header : getHeaders()) {
            builder.append(header).append(STRING_LINE_SEPARATOR);
        }
        builder.append(STRING_LINE_SEPARATOR);
        return builder.toString();
    }

    /**
     * Return the bytes that represent the string of the protocol name.
     * @return Protocol name bytes.
     */
    @Override
    public final byte[] getProtocolHeader() {
        return toStringProtocolHeader().getBytes();
    }

    /**
     * Creates the string representation of the package.
     * @return String representation of the package.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(toStringProtocolHeader());
        if(getBody() != null) {
            int maxLength = SystemProperties.getInteger(SystemProperties.Net.Http.INPUT_LOG_BODY_MAX_LENGTH);
            if(maxLength > 0) {
                if (getBody().length > maxLength) {
                    builder.append(new String(getBody(), 0, maxLength));
                    builder.append(" ... [").append(getBody().length - maxLength).append(" more]");
                } else {
                    builder.append(new String(getBody()));
                }
            }
        }

        return builder.toString();
    }

    /**
     * This class represents a file attached into the request.
     */
    public static class AttachFile {

        private static final String NAME_FIELD = "name";
        private static final String FILE_NAME_FIELD = "filename";
        private static final String FIELDS_SEPARATOR = ";";
        private static final String BOUNDARY_START = "--";

        private final String name;
        private final String fileName;
        private final MimeType mimeType;
        private final byte[] file;

        public AttachFile(String name, String fileName, MimeType mimeType, byte[] file) {
            this.name = name;
            this.fileName = fileName;
            this.mimeType = mimeType;
            this.file = file;
        }

        /**
         * Returns the name of the block of data.
         * @return Name of the block of data.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the name of the file.
         * @return File name.
         */
        public String getFileName() {
            return fileName;
        }

        public MimeType getMimeType() {
            return mimeType;
        }

        /**
         * Returns the content of the file.
         * @return File content.
         */
        public byte[] getFile() {
            return file;
        }
    }
}
