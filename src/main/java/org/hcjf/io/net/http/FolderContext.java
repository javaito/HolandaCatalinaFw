package org.hcjf.io.net.http;

import org.hcjf.encoding.MimeType;
import org.hcjf.errors.Errors;
import org.hcjf.log.Log;
import org.hcjf.properties.SystemProperties;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class publish some local folder in the web environment.
 * @author javaito
 */
public class FolderContext extends Context {

    private final Path baseFolder;
    private final String name;
    private String defaultFile;
    private final String[] names;
    private final MessageDigest messageDigest;

    public FolderContext(String name, Path baseFolder, String defaultFile) {
        super(START_CONTEXT + URI_FOLDER_SEPARATOR + name + END_CONTEXT);

        if(baseFolder == null) {
            throw new NullPointerException(Errors.getMessage(Errors.ORG_HCJF_IO_NET_HTTP_1));
        }

        if(!baseFolder.toFile().exists()) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.ORG_HCJF_IO_NET_HTTP_2));
        }

        if(verifyJatFormat(baseFolder)) {
            try {
                baseFolder = unzipJar(baseFolder);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to unzip jar file", e);
            }
        } else if(verifyZipFormat(baseFolder)) {
            try {
                baseFolder = unzip(baseFolder);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to unzip file", e);
            }
        }

        if(defaultFile != null) {
            File file = baseFolder.resolve(defaultFile).toFile();
            if (file.exists()) {
                if (file.isDirectory()) {
                    baseFolder = baseFolder.resolve(defaultFile);
                } else {
                    this.defaultFile = defaultFile;
                }
            } else {
                Log.w(SystemProperties.get(SystemProperties.Net.Http.Folder.LOG_TAG), "Default file doesn't exist %s", defaultFile);
            }
        }

        this.name = name;
        this.baseFolder = baseFolder;
        this.names = name.split(URI_FOLDER_SEPARATOR);
        try {
            this.messageDigest = MessageDigest.getInstance(
                    SystemProperties.get(SystemProperties.Net.Http.DEFAULT_FILE_CHECKSUM_ALGORITHM));
        } catch (Exception ex) {
            throw new IllegalArgumentException(Errors.getMessage(Errors.ORG_HCJF_IO_NET_HTTP_9), ex);
        }
    }

    public FolderContext(String name, Path baseFolder) {
        this(name, baseFolder, null);
    }

    /**
     * Verify if the specific path is a zip file or not.
     * @param path Specific path.
     * @return True if the path point to the zip file.
     */
    private boolean verifyZipFormat(Path path) {
        boolean result = false;
        try {
            new ZipFile(path.toFile()).getName();
            result = true;
        } catch (Exception ex){}
        return result;
    }

    /**
     * Verify if the specific path is a jar file or not.
     * @param path Specific path.
     * @return True if the path point to the jar file.
     */
    private boolean verifyJatFormat(Path path) {
        boolean result = false;
        try {
            new JarFile(path.toFile()).getName();
            result = true;
        } catch (Exception ex) {}
        return result;
    }

    /**
     * Unzip the specific file and create a temporal folder with all the content and returns
     * the new base folder for the context.
     * @param zipFilePath Specific file.
     * @return New base folder.
     */
    private Path unzip(Path zipFilePath) throws IOException {
        ZipFile zipFile = new ZipFile(zipFilePath.toFile());
        Path tempFolder = Files.createTempDirectory(
                SystemProperties.getPath(SystemProperties.Net.Http.Folder.ZIP_CONTAINER),
                SystemProperties.get(SystemProperties.Net.Http.Folder.ZIP_TEMP_PREFIX));
        tempFolder.toFile().deleteOnExit();
        int errors;
        Set<String> processedNames = new TreeSet<>();
        do {
            errors = 0;
            Enumeration<? extends ZipEntry> entryEnumeration = zipFile.entries();
            while (entryEnumeration.hasMoreElements()) {
                ZipEntry zipEntry = entryEnumeration.nextElement();
                if(!processedNames.contains(zipEntry.getName())) {
                    try {
                        if (zipEntry.isDirectory()) {
                            Files.createDirectory(tempFolder.resolve(zipEntry.getName()));
                        } else {
                            Path file = Files.createFile(tempFolder.resolve(zipEntry.getName()));
                            try (InputStream inputStream = zipFile.getInputStream(zipEntry);
                                 FileOutputStream fileOutputStream = new FileOutputStream(file.toFile())) {
                                byte[] buffer = new byte[2048];
                                int readSize = inputStream.read(buffer);
                                while (readSize >= 0) {
                                    fileOutputStream.write(buffer, 0, readSize);
                                    fileOutputStream.flush();
                                    readSize = inputStream.read(buffer);
                                }
                            }
                        }
                    } catch (IOException ex) {
                        errors++;
                    }
                    processedNames.add(zipEntry.getName());
                }
            }
        } while(errors > 0);
        return tempFolder;
    }

    /**
     * Unzip the specific file and create a temporal folder with all the content and returns
     * the new base folder for the context.
     * @param jarFilePath Specific file.
     * @return New base folder.
     */
    private Path unzipJar(Path jarFilePath) throws IOException {
        JarFile jarFile = new JarFile(jarFilePath.toFile());
        Path tempFolder = Files.createTempDirectory(
                SystemProperties.getPath(SystemProperties.Net.Http.Folder.JAR_CONTAINER),
                SystemProperties.get(SystemProperties.Net.Http.Folder.JAR_TEMP_PREFIX));
        tempFolder.toFile().deleteOnExit();
        int errors;
        Set<String> processedNames = new TreeSet<>();
        do {
            errors = 0;
            Enumeration<? extends ZipEntry> entryEnumeration = jarFile.entries();
            while (entryEnumeration.hasMoreElements()) {
                ZipEntry zipEntry = entryEnumeration.nextElement();
                if(!processedNames.contains(zipEntry.getName())) {
                    try {
                        if (zipEntry.isDirectory()) {
                            Files.createDirectory(tempFolder.resolve(zipEntry.getName()));
                        } else {
                            Path file = Files.createFile(tempFolder.resolve(zipEntry.getName()));
                            try (InputStream inputStream = jarFile.getInputStream(zipEntry);
                                 FileOutputStream fileOutputStream = new FileOutputStream(file.toFile())) {
                                byte[] buffer = new byte[2048];
                                int readSize = inputStream.read(buffer);
                                while (readSize >= 0) {
                                    fileOutputStream.write(buffer, 0, readSize);
                                    fileOutputStream.flush();
                                    readSize = inputStream.read(buffer);
                                }
                            }
                        }
                    } catch (IOException ex) {
                        errors++;
                    }
                    processedNames.add(zipEntry.getName());
                }
            }
        } while(errors > 0);
        return tempFolder;
    }

    /**
     * This method is called when there comes a http package addressed to this
     * context.
     *
     * @param request All the request information.
     * @return Return an object with all the response information.
     */
    @Override
    public HttpResponse onContext(HttpRequest request) {
        List<String> elements = request.getPathParts();
        for(String forbidden : SystemProperties.getList(SystemProperties.Net.Http.Folder.FORBIDDEN_CHARACTERS)) {
            for(String element : elements) {
                if (element.contains(forbidden)) {
                    throw new IllegalArgumentException(Errors.getMessage(Errors.ORG_HCJF_IO_NET_HTTP_3, forbidden, request.getContext()));
                }
            }
        }

        //The first value is a empty value, and the second value is the base public context.
        Path path = baseFolder.toAbsolutePath();
        boolean emptyElement = true;
        for(String element : elements) {
            if(!element.isEmpty() && Arrays.binarySearch(names, element) < 0) {
                path = path.resolve(element);
                emptyElement = false;
            }
        }
        if(emptyElement && defaultFile != null) {
            path = path.resolve(defaultFile);
        }

        HttpResponse response = new HttpResponse();

        File file = path.toFile();
        if(file.exists()) {
            if (file.isDirectory()) {
                StringBuilder list = new StringBuilder();
                for(File subFile : file.listFiles()) {
                    list.append(String.format(SystemProperties.get(SystemProperties.Net.Http.Folder.DEFAULT_HTML_ROW),
                            path.relativize(baseFolder).resolve(request.getContext()).resolve(subFile.getName()).toString(),
                            subFile.getName()));
                }
                String htmlBody = String.format(SystemProperties.get(SystemProperties.Net.Http.Folder.DEFAULT_HTML_BODY), list.toString());
                String document = String.format(SystemProperties.get(SystemProperties.Net.Http.Folder.DEFAULT_HTML_DOCUMENT), file.getName(), htmlBody);
                byte[] body = document.getBytes();
                response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
                response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.HTML));
                response.setResponseCode(HttpResponseCode.OK);
                response.setBody(body);
            } else {
                byte[] body;
                String checksum;
                try {
                    body = Files.readAllBytes(file.toPath());
                    synchronized (this) {
                        checksum = new String(Base64.getEncoder().encode(messageDigest.digest(body)));
                        messageDigest.reset();
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(Errors.getMessage(Errors.ORG_HCJF_IO_NET_HTTP_4, Paths.get(request.getContext(), file.getName())), ex);
                }

                Integer responseCode = HttpResponseCode.OK;
                HttpHeader ifNonMatch = request.getHeader(HttpHeader.IF_NONE_MATCH);
                if(ifNonMatch != null) {
                    if(checksum.equals(ifNonMatch.getHeaderValue())) {
                        responseCode = HttpResponseCode.NOT_MODIFIED;
                    }
                }

                String[] nameExtension = file.getName().split(SystemProperties.get(SystemProperties.Net.Http.Folder.FILE_EXTENSION_REGEX));
                String extension = nameExtension.length == 2 ? nameExtension[1] : MimeType.BIN;
                response.setResponseCode(responseCode);
                MimeType mimeType = MimeType.fromSuffix(extension);
                response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, mimeType == null ? MimeType.BIN : mimeType.toString()));
                response.addHeader(new HttpHeader(HttpHeader.E_TAG, checksum));
                response.addHeader(new HttpHeader(HttpHeader.LAST_MODIFIED,
                        SystemProperties.getDateFormat(SystemProperties.Net.Http.RESPONSE_DATE_HEADER_FORMAT_VALUE).
                                format(new Date(file.lastModified()))));

                if(responseCode.equals(HttpResponseCode.OK)) {
                    HttpHeader acceptEncodingHeader = request.getHeader(HttpHeader.ACCEPT_ENCODING);
                    if(acceptEncodingHeader != null) {
                        boolean notAcceptable = true;
                        for(String group : acceptEncodingHeader.getGroups()) {
                            if (group.equalsIgnoreCase(HttpHeader.GZIP) || group.equalsIgnoreCase(HttpHeader.DEFLATE)) {
                                try (ByteArrayOutputStream out = new ByteArrayOutputStream(); GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out)) {
                                    gzipOutputStream.write(body);
                                    gzipOutputStream.flush();
                                    gzipOutputStream.finish();
                                    body = out.toByteArray();
                                    response.addHeader(new HttpHeader(HttpHeader.CONTENT_ENCODING, HttpHeader.GZIP));
                                    notAcceptable = false;
                                    break;
                                } catch (Exception ex) {
                                    //TODO: Log.w();
                                }
                            } else if (group.equalsIgnoreCase(HttpHeader.IDENTITY)) {
                                response.addHeader(new HttpHeader(HttpHeader.CONTENT_ENCODING, HttpHeader.IDENTITY));
                                notAcceptable = false;
                                break;
                            }
                        }

                        if (notAcceptable) {
                            response.setResponseCode(HttpResponseCode.NOT_ACCEPTABLE);
                        }
                    }

                    if(responseCode.equals(HttpResponseCode.OK)) {
                        response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
                        response.setBody(body);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException(Errors.getMessage(Errors.ORG_HCJF_IO_NET_HTTP_5, request.getContext()));
        }

        return response;
    }

}
