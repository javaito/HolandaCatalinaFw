package org.hcjf.io.net.http.datasources;

import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.layers.query.Queryable;
import org.hcjf.service.ServiceConsumer;

import java.util.Map;

public class DataSourceServiceConsumer implements ServiceConsumer {

    private final Map<String, Object> rawDataSources;
    private final Queryable.DataSource dataSource;
    private Map<String, Object> result;
    private Throwable throwable;

    public DataSourceServiceConsumer(Map<String, Object> rawDataSources) {
        this(rawDataSources, null);
    }

    public DataSourceServiceConsumer(Map<String, Object> rawDataSources, Queryable.DataSource dataSource) {
        this.rawDataSources = rawDataSources;
        this.dataSource = dataSource;
    }

    public Map<String, Object> getRawDataSources() {
        return rawDataSources;
    }

    public Queryable.DataSource getDataSource() {
        return dataSource;
    }

    public synchronized void setThrowable(Throwable throwable) {
        this.throwable = throwable;
        this.notifyAll();
    }

    public synchronized void setResult(Map<String,Object> result) {
        this.result = result;
        this.notifyAll();
    }

    public synchronized Map<String,Object> getResult() {
        if(result == null && throwable == null) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }

        if(throwable != null){
            throw new HCJFRuntimeException("Get data source fail", throwable);
        }

        return result;
    }
}
