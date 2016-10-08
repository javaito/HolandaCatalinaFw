package org.hcjf.layers.view;

import org.hcjf.layers.Layer;
import org.hcjf.utils.Introspection;
import org.hcjf.view.ViewComponent;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

/**
 * @mail armedina@gmail.com
 */
public abstract class ViewLayer extends Layer implements ViewLayerInterface {
    public ViewLayer(String implName) {
        super(implName);
    }

    @Override
    public ViewComponent onAction(String action, HashMap params) {
        ViewComponent result = null;
        Map<String, ViewAccessor> viewAccessorMap = Introspection.getInvokers(getImplementationClass(), new ViewActionFilter());
        if(viewAccessorMap.containsKey(action)){
            result = viewAccessorMap.get(action).invokeAccessor(this, params);
        }
        return result;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ViewAction {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface ViewActionParameter {
        String name();
    }


    protected Class<? extends ViewLayer> getImplementationClass(){
        return ViewLayer.class;
    }

    public class ViewAccessor extends Introspection.Invoker{

        public ViewAccessor(Class implementationClass, Method method) {
            super(implementationClass, method);
        }

        public ViewComponent invokeAccessor(Object instance, HashMap<String, Object> params){
            ViewComponent result = null;
            Map annotatedParams = null;
            if(params != null){
                if(!params.isEmpty()){

                }

            }

            try {
                result = (ViewComponent) invoke(instance,new Object[] {});
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    public class ViewActionFilter implements Introspection.InvokerFilter<ViewAccessor>{

        @Override
        public Introspection.InvokerEntry<ViewAccessor> filter(Method method) {
            Introspection.InvokerEntry result = null;
            if(method.isAnnotationPresent(ViewAction.class)){
                String methodName = method.getAnnotation(ViewAction.class).value();
                result = new Introspection.InvokerEntry(methodName, new ViewAccessor(this.getClass(),method));
            }
            return result;
        }
    }
}
