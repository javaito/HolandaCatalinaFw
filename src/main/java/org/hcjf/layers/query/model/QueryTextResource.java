package org.hcjf.layers.query.model;

public class QueryTextResource extends QueryResource {

    private String text;

    public QueryTextResource(String resourceName, String text) {
        super(resourceName);
        this.text = text;
    }

    public String getText() {
        return text;
    }

}
