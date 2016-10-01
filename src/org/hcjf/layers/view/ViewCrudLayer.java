package org.hcjf.layers.view;

import org.hcjf.utils.Introspection;
import org.hcjf.view.ViewComponent;
import org.hcjf.view.ViewComponentContainer;
import org.hcjf.view.containers.LinearLayout;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public abstract class ViewCrudLayer<O extends Object> extends ViewLayer {

    public ViewCrudLayer(String implName) {
        super(implName);
    }

    @ViewAction("LIST")
    public ViewComponent getList(@ViewActionParameter(name = "OBJECTS") List<O> objects) {
        LinearLayout result = new LinearLayout("");
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

    /**
     * This method return the resource class of the layer.
     *
     * @return Resource class.
     */
    private final Class<O> getResourceType() {
        Class<O> resourceClass = (Class<O>)
                ((ParameterizedType) getClass().getGenericSuperclass()).
                        getActualTypeArguments()[0];
        return resourceClass;
    }

    protected abstract ViewComponentContainer getMainContainer();

    protected abstract ViewComponent getFieldComponent(Introspection.Invoker invoker);

    protected Collection<? extends Introspection.Invoker> getInvokers(Class<O> resourceClass) {
        return Introspection.getSetters(resourceClass).values();
    }

    protected abstract boolean isIncluded(Introspection.Invoker invoker);

}
