package org.hcjf.layers.view;

import org.hcjf.utils.Introspection;
import org.hcjf.view.ViewComponent;
import org.hcjf.view.ViewComponentContainer;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public abstract class ViewCrudLayer<O extends Object> extends ViewLayer implements ViewCrudLayerInterface {

    public ViewCrudLayer(String implName) {
        super(implName);
    }

    //public ViewComponent getList(@ViewActionParameter(name = "OBJECTS") List<O> objects) {
    @ViewAction("LIST")
    public ViewComponent getList() {
        ViewComponentContainer result = getMainContainer();
        result.addComponent(getComponentList(getResourceType()));
        return result;
    }

    @ViewAction("CRUD")
    public ViewComponent getCrud() {
        ViewComponentContainer result = getMainContainer();
        getInvokers(getResourceType()).stream().filter(invoker -> isIncluded(invoker)).forEach(invoker -> {
            result.addComponent(getFieldComponent(invoker));
        });
        return result;
    }

    @Override
    protected final Class<? extends ViewLayer> getImplementationClass() {
        return ViewCrudLayer.class;
    }

    protected abstract ViewComponentContainer getMainContainer();

    protected abstract ViewComponent getFieldComponent(Introspection.Invoker invoker);

    protected abstract ViewComponent getComponentList(Class<O> resourceClass);

    protected Collection<? extends Introspection.Invoker> getInvokers(Class<O> resourceClass) {
        return Introspection.getSetters(resourceClass).values();
    }

    protected abstract boolean isIncluded(Introspection.Invoker invoker);

    /**
     * This method return the resource class of the layer.
     * @return Resource class.
     */
    public final Class<O> getResourceType() {
        Class<O> resourceClass = (Class<O>)
                ((ParameterizedType)getClass().getGenericSuperclass()).
                        getActualTypeArguments()[0];
        return resourceClass;
    }

}
