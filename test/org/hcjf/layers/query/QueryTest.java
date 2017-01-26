package org.hcjf.layers.query;

import junit.framework.Test;

import java.util.*;

/**
 * @author javaito
 * @mail javaito@gmail.com
 */
public class QueryTest {

    public static void main(String[] args) {
        List<TestModel> data = new ArrayList<>();

        Random random = new Random();
        GregorianCalendar calendar = new GregorianCalendar();
        for (int i = 0; i < 20; i++) {
            TestModel model1 = new TestModel();
            model1.setName("Model" + i);
            model1.setNumber((int)(1000 * random.nextDouble()));
            calendar.set(Calendar.YEAR, 2014);
            calendar.set(Calendar.MONTH, (int)(11 * random.nextDouble()));
            calendar.set(Calendar.DAY_OF_MONTH, (int)(28 * random.nextDouble()));
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            model1.setDate(calendar.getTime());

            data.add(model1);
        }

        System.out.println(Arrays.toString(data.toArray()));

        calendar.set(Calendar.YEAR, 2014);
        calendar.set(Calendar.MONTH, 5);
        calendar.set(Calendar.DAY_OF_MONTH, 15);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Query query = new Query();
        query.addOrderField("name");
        query.greaterThan("date", calendar.getTime()).smallerThanOrEqual("number", 500);

//        System.out.println(Arrays.toString(query.evaluate(data).toArray()));

        query = Query.compile("SELECT * FROM car JOIN CarModel ON CarModel.carId = car.id WHERE car.id = 34 OR car.id >= 40");
        System.out.printf("");

    }

    public static class TestModel {

        private UUID id;
        private Integer number;
        private String name;
        private Date date;

        public TestModel() {
            id = UUID.randomUUID();
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return "{" + name + ", " + number + ", " + date + "}";
        }
    }
}
