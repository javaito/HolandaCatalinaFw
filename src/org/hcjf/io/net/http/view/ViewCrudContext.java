package org.hcjf.io.net.http.view;

import org.hcjf.io.net.http.HttpRequest;
import org.hcjf.layers.view.ViewCrudLayerInterface;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class ViewCrudContext extends  ViewContext<ViewCrudLayerInterface, ViewRequest, ViewResponse>{

    public ViewCrudContext(String groupName, String resourceName) {
        super(groupName, resourceName);
    }

    @Override
    protected ViewRequest decode(HttpRequest request) {

        //Verificar ACTION CRUD
        return null;
    }



    @Override
    protected ViewResponse encode(Object object, ViewRequest request) {
        return null;
    }

}
