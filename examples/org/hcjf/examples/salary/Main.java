package org.hcjf.examples.salary;

import org.hcjf.io.net.http.HttpServer;
import org.hcjf.io.net.http.RestContext;
import org.hcjf.layers.Layers;

public class Main {

    public static void main(String[] args) {
        Layers.publishLayer(EmployeeResource.class);
        Layers.publishLayer(SalaryResource.class);
        HttpServer server = new HttpServer(9090);
        server.addContext(new RestContext("/api"));
        server.start();
    }

}
