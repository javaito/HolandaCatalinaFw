package org.hcjf.utils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class ResolveTest {

    private static Map<String,Object> model;
    private static Date date;
    private static UUID id;

    @BeforeClass
    public static void init() {
        id = UUID.randomUUID();
        date = new Date();

        model = new HashMap<>();
        model.put("stringMapField", "string");
        model.put("integerMapField", 2);
        model.put("dateMapField", date);
        model.put("uuidMapField", id);

        TestingClass testingClass = new TestingClass();
        testingClass.setStringField("string");
        testingClass.setIntegerField(2);
        testingClass.setDateField(date);
        testingClass.setUuidField(id);
        testingClass.setStringArrayField(new String[]{"string0","string1"});
        testingClass.setStringListField(List.of("string0","string1"));
        testingClass.setObjectMapField(Map.of("innerString", "string", "innerInteger", 1));

        model.put("testingClassMapField", testingClass);
        model.put("testingClassCollectionMapField", List.of(testingClass, testingClass));
    }

    @Test
    public void resolveTest() {
        Assert.assertEquals(Introspection.resolve(model, "dateMapField"), date);
        Assert.assertEquals(Introspection.resolve(model, "uuidMapField"), id);
        Assert.assertEquals(Introspection.resolve(model, "testingClassMapField.stringField"), "string");
        Assert.assertEquals(Introspection.resolve(model, "testingClassMapField.stringArrayField.1"), "string1");
        Assert.assertEquals(Introspection.resolve(model, "testingClassCollectionMapField.0.stringArrayField.1"), "string1");
    }

    public static class TestingClass {

        private String stringField;
        private Integer integerField;
        private Date dateField;
        private UUID uuidField;
        private String[] stringArrayField;
        private List<String> stringListField;
        private Map<String,Object> objectMapField;

        public String getStringField() {
            return stringField;
        }

        public void setStringField(String stringField) {
            this.stringField = stringField;
        }

        public Integer getIntegerField() {
            return integerField;
        }

        public void setIntegerField(Integer integerField) {
            this.integerField = integerField;
        }

        public Date getDateField() {
            return dateField;
        }

        public void setDateField(Date dateField) {
            this.dateField = dateField;
        }

        public UUID getUuidField() {
            return uuidField;
        }

        public void setUuidField(UUID uuidField) {
            this.uuidField = uuidField;
        }

        public String[] getStringArrayField() {
            return stringArrayField;
        }

        public void setStringArrayField(String[] stringArrayField) {
            this.stringArrayField = stringArrayField;
        }

        public List<String> getStringListField() {
            return stringListField;
        }

        public void setStringListField(List<String> stringListField) {
            this.stringListField = stringListField;
        }

        public Map<String, Object> getObjectMapField() {
            return objectMapField;
        }

        public void setObjectMapField(Map<String, Object> objectMapField) {
            this.objectMapField = objectMapField;
        }
    }
}
