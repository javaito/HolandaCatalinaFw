package org.hcjf.layers.view;

import org.hcjf.view.ViewComponent;
import org.hcjf.view.ViewComponentContainer;
import org.hcjf.view.components.Input;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class ViewLayerDefault extends ViewLayer implements ViewLayerInterface{

    public ViewLayerDefault() {
        super("default");
    }

    public ViewLayerDefault(String implName) {
        super(implName);
    }


    public Object crud(String resource) {

        ViewComponentContainer container = new ViewComponentContainer(resource);

        try {
            Class classResource = Class.forName(resource);
            Method methods[] = classResource.getDeclaredMethods();
            String methodName;
            Method method;
            for(int i=0;i<methods.length;i++){
                method = methods[i];
                methodName = method.getName();
                if(methodName.startsWith("get")){
                    container.addComponent(getViewComponent(method));
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return container;
    }

    private ViewComponent getViewComponent(Method method){
        ViewComponent result;
        Annotation annotations[] = method.getDeclaredAnnotations();
        Annotation annotation = null;
        Annotation methodAnnotation = new Annotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }
        };

        for (int i = 0; i < annotations.length; i++) {
            if(annotations[i].equals(methodAnnotation)){
                annotation = annotations[i];
                break;
            }
        }

        if(annotation == null){
            result = getViewComponentByMethod(method);
        } else {
            result = getViewComponentByAnnotation(annotation);
        }
        return result;
    }

    private ViewComponent getViewComponentByAnnotation(Annotation annotation){
        return null;
    }

    private ViewComponent getViewComponentByMethod(Method method){
        String name = method.getName().substring(3);
        Class returnType = method.getReturnType();
        ViewComponent result = getViewComponentByClassSimpleName(returnType.getSimpleName());
        result.setName(name);
        return result;
    }

    private ViewComponent getViewComponentByClassSimpleName(String classSimpleName ){
        ViewComponent result = null;

        String methodName = "get" + classSimpleName + "ViewComponent";
        Method method = null;

        try {
            method = this.getClass().getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e){
            e.printStackTrace();
        }

        if(method != null){
            try {
                result = (ViewComponent) method.invoke(this);
            } catch (IllegalArgumentException e){
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private ViewComponent getStringViewComponent(){
        ViewComponent result;
        result = new Input("");
        return result;
    }

    private ViewComponent getIntegerViewComponent(){
        ViewComponent result;
        result = new Input("");
        return result;
    }
}
