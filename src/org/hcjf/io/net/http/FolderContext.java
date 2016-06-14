package org.hcjf.io.net.http;

import java.io.File;
import java.nio.file.Path;

/**
 * Created by javaito on 8/6/2016.
 */
public abstract class FolderContext extends Context {

    private static final String START_CONTEXT = "^";
    private static final String URI_FOLDER_SEPARATOR = "/";
    private static final String[] FORBIDDEN_CHARACTERS = {".", "~"};

    private final Path baseFolder;

    public FolderContext(String name, Path baseFolder) {
        super(START_CONTEXT + name);

        if(baseFolder == null) {
            throw new NullPointerException("Folder location can't be null");
        }

        if(!baseFolder.toFile().exists()) {
            throw new IllegalArgumentException("The base folder doesn't exist");
        }

        this.baseFolder = baseFolder;
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
        for(String forbidden : FORBIDDEN_CHARACTERS) {
            if(request.getContext().contains(forbidden)) {
                throw new IllegalArgumentException("Forbidden path (" + forbidden + "):" + request.getContext());
            }
        }
        String[] elements = request.getContext().split(URI_FOLDER_SEPARATOR);
        //The first value is a empty value, and the second value is the base public context.
        Path path = baseFolder.toAbsolutePath();
        for(String element : elements) {
            path = path.resolve(element);
        }

        File file = path.toFile();


        return null;
    }



    /**
     * This method is called when there are any error on the context execution.
     *
     * @param request   All the request information.
     * @param throwable Throwable object, could be null.
     * @return Return an object with all the response information.
     */
    @Override
    protected HttpResponse onError(HttpRequest request, Throwable throwable) {
        return null;
    }

}
