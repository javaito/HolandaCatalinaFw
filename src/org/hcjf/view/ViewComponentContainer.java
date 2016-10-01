package org.hcjf.view;

import java.util.LinkedList;
import java.util.List;


/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class ViewComponentContainer extends ViewComponent {

    private final LinkedList<ViewComponent> viewComponentsList = new LinkedList<>();

    public ViewComponentContainer(String name) {
        super(name);
    }

    public void addComponent(ViewComponent component){
        this.viewComponentsList.add(component);
    }

    public List<ViewComponent> getComponents(){
        return viewComponentsList;
    }

}
