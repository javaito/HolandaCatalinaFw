package org.hcjf.layers.query;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.Layer;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.ReadRowsLayerInterface;
import org.hcjf.utils.Strings;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class QueryRunningFromFileDataSources {

    private static Path invoicePath;
    private static Path detailPath;

    @Test
    public void testCsv() {

        invoicePath = Path.of(getClass().getClassLoader().getResource("invoice.csv").getFile());
        detailPath = Path.of(getClass().getClassLoader().getResource("detail.csv").getFile());

        Layers.publishLayer(InvoiceResource.class);
        Layers.publishLayer(DetailResource.class);

        Collection<JoinableMap> resultSet = Query.evaluate("SELECT * FROM demo.csv.invoice");
        System.out.println();

        resultSet = Query.evaluate("SELECT * FROM demo.csv.invoice JOIN demo.csv.detail ON demo.csv.invoice.id = demo.csv.detail.idInvoice");
        System.out.println();

        resultSet = Query.evaluate("SELECT * FROM demo.csv.invoice JOIN demo.csv.detail ON demo.csv.invoice.id = demo.csv.detail.idInvoice group by demo.csv.detail.idInvoice");
        System.out.println();

    }

    public static class InvoiceResource extends Layer implements ReadRowsLayerInterface {

        @Override
        public String getImplName() {
            return "demo.csv.invoice";
        }

        @Override
        public Collection<JoinableMap> readRows(Queryable queryable) {
            try {
                return queryable.evaluate(readCsv(Files.readAllBytes(invoicePath)));
            }catch (Exception ex){
                throw new HCJFRuntimeException("", ex);
            }
        }
    }

    public static class DetailResource extends Layer implements ReadRowsLayerInterface {

        @Override
        public String getImplName() {
            return "demo.csv.detail";
        }

        @Override
        public Collection<JoinableMap> readRows(Queryable queryable) {
            try {
                return queryable.evaluate(readCsv(Files.readAllBytes(detailPath)));
            }catch (Exception ex){
                throw new HCJFRuntimeException("", ex);
            }
        }
    }

    private static Collection<JoinableMap> readCsv(byte[] file) {
        Collection<JoinableMap> result = new ArrayList<>();

        try (InputStream in = new ByteArrayInputStream(file);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in))) {

            String line = bufferedReader.readLine();
            String[] labels = line.split(","); //TODO: Create a manifest file to read if the separator is ','
            String[] values;
            JoinableMap row;
            while ((line = bufferedReader.readLine()) != null) {
                values = line.split(",");
                row = new JoinableMap();
                for (int i = 0; i < labels.length; i++) {
                    if (!values[i].isBlank()) {
                        row.put(labels[i], Strings.deductInstance(values[i]));
                    }
                }
                result.add(row);
            }
        } catch (Exception ex) {
            throw new HCJFRuntimeException("Unable to read csv file", ex);
        }

        return result;
    }

}
