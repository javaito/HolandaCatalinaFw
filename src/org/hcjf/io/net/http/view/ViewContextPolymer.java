package org.hcjf.io.net.http.view;

import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.io.net.http.layered.LayeredRequest;
import org.hcjf.io.net.http.layered.LayeredResponse;
import org.hcjf.layers.view.ViewLayerInterface;
import org.hcjf.view.ViewComponent;
import org.hcjf.view.ViewComponentContainer;
import org.hcjf.view.components.Button;
import org.hcjf.view.components.Input;
import org.hcjf.view.containers.Card;
import org.hcjf.view.containers.ToolbarLayout;
import org.hcjf.view.parameters.componentParameters.ComponentParameter;
import org.hcjf.view.parameters.componentParameters.TypeParameter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class ViewContextPolymer extends ViewContext<ViewLayerInterface> {
    private static String DEFAULT_VIEW_LAYER = "default";
    private static String CRUD_VIEW_LAYER = "personal";
    public ViewContextPolymer(String groupName, String resourceName) {
        super(groupName, resourceName);
    }

    @Override
    protected Object get(LayeredRequest layeredRequest) {
        Object result = null;
        ViewLayerInterface viewLayerInterface = null;
        try {
            viewLayerInterface = getLayerInterface(layeredRequest.getResourceName());
        } catch (Exception e){

        }
        if(viewLayerInterface == null){
            //Default
            viewLayerInterface = getLayerInterface(CRUD_VIEW_LAYER);
            if(viewLayerInterface != null){
                /*HashMap<String,Object> params = new HashMap<>();
                List<Object> objects = new ArrayList<>();
                objects.add(new String("UNO"));
                params.put("OBJECTS",objects);
                result = viewLayerInterface.onAction("LIST",params);*/
                result = viewLayerInterface.onAction("CRUD",null);
            }
        } else {
            result = viewLayerInterface.onAction("CRUD",null);
        }

        return result;
    }

    @Override
    protected LayeredResponse encode(Object object, LayeredRequest request) {
        ViewComponent component = (ViewComponent) object;
        HttpResponse httpResponse = new HttpResponse();
        if(component instanceof ViewComponentContainer){
            httpResponse.setBody(getRepresentation((ViewComponentContainer) component).getBytes());
        } else {
            //Error
        }
        LayeredResponse response = new LayeredResponse(httpResponse);
        return response;
    }

    protected String getStyle(){
        return "body {" +
            "margin: 0;" +
                "background-color: #eee;" +
        "}" +
        "app-header {" +
        "background-color: #4285f4;" +
        "color: #fff;" +
        "}" +
        "app-header paper-icon-button {" +
        "--paper-icon-button-ink-color: white;" +
        "}";
    }


    public String getRepresentation(ViewComponentContainer viewComponentContainer){
        StringBuilder html = new StringBuilder();
        StringBuilder head = new StringBuilder();
        StringBuilder body = new StringBuilder();
        String bodyRepresentation;
        TreeSet<String> imports = new TreeSet<>();

        bodyRepresentation = getComponentContainerRepresentation(viewComponentContainer, imports);

        html.append("<!doctype html>");
        html.append("<html lang=\"en\">");

        head.append("<head>");
        head.append("<title>");
        head.append(viewComponentContainer.getName());
        head.append("</title>");
        head.append("<meta charset=\"utf-8\">");
        head.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        head.append("<link rel=\"manifest\" href=\"/manifest.json\">");
        head.append("<base href=\"../../home/bower_components/\">");
        head.append("<script src=\"webcomponentsjs/webcomponents-lite.js\"></script>");

        for (String importComponent : imports) {
            head.append("<link rel=\"import\" href=\"");
            head.append(importComponent);
            head.append("\">");
        }

        head.append("<style is=\"custom-style\">");
        head.append(getStyle());
        head.append("</style>");

        head.append("</head>");
        html.append(head);
        body.append("<body>");
        body.append(bodyRepresentation);
        body.append("</body>");
        html.append(body);
        html.append("</html>");

        return html.toString();
    }

    public String getComponentContainerRepresentation(ViewComponentContainer viewComponentContainer, TreeSet<String> imports){
        StringBuilder componentsRepresentation = new StringBuilder();

        String result = null;

        for(ViewComponent component : viewComponentContainer.getComponents()){
            if(component instanceof ViewComponent) {
                componentsRepresentation.append(getComponentRepresentation(component, imports));
            } else if (component instanceof ViewComponentContainer) {
                componentsRepresentation.append(getComponentContainerRepresentation((ViewComponentContainer) component, imports));
            }
        }

        String methodName = "get" + viewComponentContainer.getClass().getSimpleName() + "ContainerRepresentation";
        Method method = null;

        try {
            method = this.getClass().getDeclaredMethod(methodName, viewComponentContainer.getClass(), StringBuilder.class,imports.getClass());
        } catch (NoSuchMethodException e){
            e.printStackTrace();
        }

        if(method != null){
            try {
                result = (String) method.invoke(this,viewComponentContainer,componentsRepresentation,imports);
            } catch (IllegalArgumentException e){
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            result = componentsRepresentation.toString();
        }

        return result;
    }

    public String getComponentRepresentation(ViewComponent viewComponent, TreeSet<String> imports){
        String result = "";
        String methodName = "get" + viewComponent.getClass().getSimpleName() + "Representation";
        Method method = null;

        try {
            method = this.getClass().getDeclaredMethod(methodName, viewComponent.getClass(), imports.getClass());
        } catch (NoSuchMethodException e){
            e.printStackTrace();
        }

        if(method != null){
            try {
                result = (String) method.invoke(this,viewComponent,imports);
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

    private String getButtonRepresentation(Button button, TreeSet imports){
        imports.add("paper-button/paper-button.html");
        StringBuilder component = new StringBuilder();
        component.append("<paper-button raised>");
        component.append(button.getName());
        component.append("</paper-button>");
        return component.toString();
    }

    private String getInputRepresentation(Input input, TreeSet imports){
        imports.add("paper-input/paper-input.html");
        StringBuilder component = new StringBuilder();
        component.append("<paper-input label=\"");
        component.append(input.getName());
        component.append("\"");
        for(ComponentParameter parameter : input.getParameters()){
            if(parameter instanceof TypeParameter){
                if(parameter.getValue().equals("password")){
                    component.append(" type=\"password\"");
                }
            }
        }
        component.append(">");
        component.append("</paper-input>");
        return component.toString();
    }


    private String getCardContainerRepresentation(Card card, StringBuilder componentsRepresentation, TreeSet imports){

        imports.add("paper-card/paper-card.html");

        StringBuilder component = new StringBuilder();

        component.append("<paper-card heading=\"");
        component.append(card.getName());
        component.append("\">");
        component.append("<div class=\"card-content\">");
        component.append(componentsRepresentation);
        component.append("</div>");
        component.append("</paper-card>");

        return component.toString();
    }


    private String getToolbarLayoutContainerRepresentation(ToolbarLayout toolbarLayout, StringBuilder componentsRepresentation, TreeSet imports){

        imports.add("iron-icons/iron-icons.html");
        imports.add("paper-icon-button/paper-icon-button.html");
        imports.add("app-layout/app-header/app-header.html");
        imports.add("app-layout/app-scroll-effects/app-scroll-effects.html");
        imports.add("app-layout/app-toolbar/app-toolbar.html");
        imports.add("app-layout/app-header-layout/app-header-layout.html");

        StringBuilder component = new StringBuilder();
        component.append("<app-header-layout>");

        component.append("<app-header effects=\"waterfall\" condenses reveals>");
        component.append("<app-toolbar>");
        component.append("<paper-icon-button icon=\"menu\"></paper-icon-button>");
        component.append("<div main-title></div>");
        component.append("<paper-icon-button icon=\"search\"></paper-icon-button>");
        component.append("</app-toolbar>");
        component.append("<app-toolbar></app-toolbar>");
        component.append("<app-toolbar>");
        component.append("<div spacer main-title>My Drive</div>");
        component.append("</app-toolbar>");
        component.append("</app-header>");

        component.append(componentsRepresentation);

        component.append("</app-header-layout>");

        return component.toString();
    }
}
