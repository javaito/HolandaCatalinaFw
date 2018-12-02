package org.hcjf.io.net.http;

import org.hcjf.layers.Layer;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.ReadRowsLayerInterface;
import org.hcjf.layers.query.JoinableMap;
import org.hcjf.layers.query.Queryable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SimpleRestEndPoint {

    public static void main(String[] args) {
        Layers.publishLayer(Bag.class);
        HttpResponse response = new HttpResponse();
        response.setBody("Hello world".getBytes());
        HttpServer.create(9090, new RestContext("base"));
    }

    public static class Bag extends Layer implements ReadRowsLayerInterface {

        private static final List<JoinableMap> db;

        static {
            db = new ArrayList<>();
            db.add(new JoinableMap(Map.of("id", 1, "items", 20, "owner", "Javier")));
            db.add(new JoinableMap(Map.of("id", 2, "items", 0, "owner", "Ignacio")));
            db.add(new JoinableMap(Map.of("id", 3, "items", 15, "owner", "Matias")));
            db.add(new JoinableMap(Map.of("id", 4, "items", 108, "owner", "Matias")));
            db.add(new JoinableMap(Map.of("id", 5, "items", 35, "owner", "Cecilia")));
            db.add(new JoinableMap(Map.of("id", 6, "items", 100, "owner", "Mariano")));
        }

        @Override
        public String getImplName() {
            return "bag";
        }

        @Override
        public Collection<JoinableMap> readRows(Queryable queryable) {
            return queryable.evaluate(db);
        }
    }
}
