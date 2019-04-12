package org.hcjf.layers.query;

import org.hcjf.bson.BsonDocument;
import org.hcjf.bson.BsonEncoder;
import org.hcjf.layers.Layer;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.ReadRowsLayerInterface;
import org.hcjf.layers.query.functions.BaseQueryFunctionLayer;
import org.hcjf.layers.query.functions.QueryFunctionLayerInterface;
import org.hcjf.properties.SystemProperties;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author javaito
 */
public class QueryRunningTest {

    private static final String CHARACTER = "character";
    private static final String ADDRESS = "address";

    private static final String ID = "id";

    private static final String NAME = "name";
    private static final String LAST_NAME = "lastName";
    private static final String NICKNAME = "nickname";
    private static final String BIRTHDAY = "birthday";
    private static final String WEIGHT = "weight";
    private static final String HEIGHT = "height";
    private static final String GENDER = "gender";
    private static final String ADDRESS_ID = "addressId";
    private static final String BODY = "body";

    private static final String STREET = "street";
    private static final String NUMBER = "number";

    private static final Map<UUID, JoinableMap> simpsonCharacters = new HashMap<>();
    private static final Map<UUID, JoinableMap> simpsonAddresses = new HashMap<>();
    private static final TestDataSource dataSource = new TestDataSource();

    @BeforeClass
    public static void config() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            JoinableMap map = new JoinableMap(ADDRESS);
            UUID addressId = UUID.randomUUID();
            map.put(ADDRESS_ID, addressId);
            map.put(STREET, "Evergreen Terrace");
            map.put(NUMBER, 742);
            simpsonAddresses.put(addressId, map);

            map = new JoinableMap(CHARACTER);
            map.put(ID, UUID.randomUUID());
            map.put(NAME, "Homer Jay");
            map.put(LAST_NAME, "Simpson");
            map.put(BIRTHDAY, dateFormat.parse("1981-02-19"));
            map.put(WEIGHT, 108.0);
            map.put(HEIGHT, 1.83);
            map.put(GENDER, Gender.MALE);
            map.put(ADDRESS_ID, addressId);
            simpsonCharacters.put((UUID) map.get(ID), map);

            map = new JoinableMap(CHARACTER);
            map.put(ID, UUID.randomUUID());
            map.put(NAME, "Marjorie Jaqueline");
            map.put(LAST_NAME, "Bouvier Simpson");
            map.put(NICKNAME, "Marge");
            map.put(BIRTHDAY, dateFormat.parse("1981-03-14"));
            map.put(WEIGHT, 67.0);
            map.put(HEIGHT, 1.65);
            map.put(GENDER, Gender.FEMALE);
            map.put(ADDRESS_ID, addressId);
            simpsonCharacters.put((UUID) map.get(ID), map);

            BsonDocument document = new BsonDocument();
            document.put("field1", "string");
            document.put("field2", new Date());
            document.put("field3", 5);

            map = new JoinableMap(CHARACTER);
            map.put(ID, UUID.randomUUID());
            map.put(NAME, "Bartolomeo Jay");
            map.put(LAST_NAME, "Simpson Bouvier");
            map.put(NICKNAME, "Bart");
            map.put(BIRTHDAY, dateFormat.parse("2007-03-14"));
            map.put(WEIGHT, 45.0);
            map.put(HEIGHT, 1.20);
            map.put(GENDER, Gender.MALE);
            map.put(ADDRESS_ID, addressId);
            map.put(BODY, BsonEncoder.encode(document));
            simpsonCharacters.put((UUID) map.get(ID), map);

            map = new JoinableMap(CHARACTER);
            map.put(ID, UUID.randomUUID());
            map.put(NAME, "Lisa Marie");
            map.put(LAST_NAME, "Simpson Bouvier");
            map.put(BIRTHDAY, dateFormat.parse("2009-07-20"));
            map.put(WEIGHT, 37.0);
            map.put(HEIGHT, 1.05);
            map.put(GENDER, Gender.FEMALE);
            map.put(ADDRESS_ID, addressId);
            simpsonCharacters.put((UUID) map.get(ID), map);

            map = new JoinableMap(CHARACTER);
            map.put(ID, UUID.randomUUID());
            map.put(NAME, "Margaret Abigail");
            map.put(LAST_NAME, "Simpson Bouvier");
            map.put(NICKNAME, "Maggie");
            map.put(BIRTHDAY, dateFormat.parse("2015-09-02"));
            map.put(WEIGHT, 15.0);
            map.put(HEIGHT, 0.75);
            map.put(GENDER, Gender.FEMALE);
            map.put(ADDRESS_ID, addressId);
            simpsonCharacters.put((UUID) map.get(ID), map);

            map = new JoinableMap(ADDRESS);
            addressId = UUID.randomUUID();
            map.put(ADDRESS_ID, addressId);
            map.put(STREET, "Buenos Aires");
            map.put(NUMBER, 1025);
            simpsonAddresses.put(addressId, map);

            map = new JoinableMap(CHARACTER);
            map.put(ID, UUID.randomUUID());
            map.put(NAME, "Maurice Lester");
            map.put(LAST_NAME, "Szyslak");
            map.put(NICKNAME, "Moe");
            map.put(BIRTHDAY, dateFormat.parse("1975-03-14"));
            map.put(WEIGHT, 82.0);
            map.put(HEIGHT, 1.70);
            map.put(GENDER, Gender.MALE);
            map.put(ADDRESS_ID, addressId);
            simpsonCharacters.put((UUID) map.get(ID), map);
        } catch (Exception ex){}

        Layers.publishLayer(CharacterResource.class);
        Layers.publishLayer(AddressResource.class);
    }

    public enum Gender {

        MALE,

        FEMALE

    }

    private static class TestDataSource implements Queryable.DataSource<JoinableMap> {

        @Override
        public Collection<JoinableMap> getResourceData(Queryable queryable) {
            Collection<JoinableMap> result = new HashSet<>();

            switch (queryable.getResourceName()) {
                case CHARACTER: {
                    for(JoinableMap map : simpsonCharacters.values()) {
                        result.add(new JoinableMap(map));
                    }
                    break;
                }
                case ADDRESS: {
                    for(JoinableMap map : simpsonAddresses.values()) {
                        result.add(new JoinableMap(map));
                    }
                    break;
                }
                default:{
                    throw new IllegalArgumentException("Resource not found " + queryable.getResourceName());
                }
            }

            return result;
        }

    }

    @Test
    public void select() {
        SystemProperties.get(SystemProperties.Service.SYSTEM_SESSION_NAME);

        Query query = Query.compile("SELECT * FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonCharacters.size());

        query = Query.compile("SELECT * FROM credentials WHERE methodName = 'user-password' AND get(fields, 'javaito') = '1234'");
        System.out.println();

        query = Query.compile("SELECT * FROM character ORDER BY addressId, name DESC");
        resultSet = query.evaluate(dataSource);

        query = Query.compile("SELECT count(*) FROM character");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.iterator().next().get("count(*)"), simpsonCharacters.size());

        query = Query.compile("SELECT count(*) AS size FROM character");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.iterator().next().get("size"), simpsonCharacters.size());

        query = Query.compile("SELECT count('weight') AS size FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 2);

        query = Query.compile("SELECT aggregateSum('weight') AS sum FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 2);

        query = Query.compile("SELECT aggregateProduct('weight') AS product FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 2);

        query = Query.compile("SELECT aggregateMean('weight') AS mean FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 2);

        query = Query.compile("SELECT now(), getYear(birthday), periodInDays(birthday), getMonth(birthday) FROM character");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonCharacters.size());

        query = Query.compile("SELECT * FROM character GROUP BY getMonth(birthday)");
        resultSet = query.evaluate(dataSource);

        query = Query.compile("SELECT weight, 2  *  weight AS superWeight, pow(max(integerValue(weight), integerValue(50.1)) ,2) AS smartWeight FROM character");
        resultSet = query.evaluate(dataSource);
        for(JoinableMap joinableMap : resultSet) {
            if(((Number)joinableMap.get(WEIGHT)).doubleValue() > 50) {
                Assert.assertTrue(((Number)joinableMap.get("smartWeight")).doubleValue() > 2500);
            } else {
                Assert.assertTrue(((Number)joinableMap.get("smartWeight")).intValue() == 2500);
            }
        }

        query = Query.compile("SELECT weight, 2  *  weight AS superWeight, pow(max(weight, 50.1) ,2) AS smartWeight FROM character");
        resultSet = query.evaluate(dataSource);

        query = Query.compile("SELECT name FROM character");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.iterator().next().size(), 1);

        query = Query.compile("SELECT *, name as nombre FROM character");
        resultSet = query.evaluate(dataSource);
        JoinableMap first = resultSet.iterator().next();
        Assert.assertEquals(first.get("nombre"), first.get("name"));

        query = Query.compile("SELECT street, concat(name), stringJoin('&', name), sum(weight), addressId FROM character JOIN address ON address.addressId = character.addressId GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonAddresses.size());

        query = Query.compile("SELECT bsonParse(body) AS body FROM character WHERE name LIKE 'Bartolomeo'");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(((Map<String,Object>)resultSet.iterator().next().get(BODY)).get("field1"), "string");

        query = Query.compile("SELECT bsonParse(body) AS body FROM character WHERE name LIKE ?");
        ParameterizedQuery parameterizedQuery = query.getParameterizedQuery();
        resultSet = parameterizedQuery.add("Bartolomeo").evaluate(dataSource);
        Assert.assertEquals(((Map<String,Object>)resultSet.iterator().next().get(BODY)).get("field1"), "string");

        query = Query.compile("SELECT * FROM character WHERE weight > ? AND weight < ?");
        parameterizedQuery = query.getParameterizedQuery();
        resultSet = parameterizedQuery.add(40).add(100).evaluate(dataSource);
        for(JoinableMap row : resultSet){
            Assert.assertTrue((double)row.get("weight") > 40 && (double)row.get("weight") < 100);
        }

        resultSet = parameterizedQuery.add(40).add(80).evaluate(dataSource);
        for(JoinableMap row : resultSet){
            Assert.assertTrue((double)row.get("weight") > 40 && (double)row.get("weight") < 80);
        }

        query = Query.compile("SELECT * FROM character WHERE weight >= ? AND weight <= ? AND true");
        parameterizedQuery = query.getParameterizedQuery();
        resultSet = parameterizedQuery.add(40).add(108).evaluate(dataSource);

        query = Query.compile("SELECT * FROM character WHERE weight < ? OR weight > ?");
        query.toString();
        parameterizedQuery = query.getParameterizedQuery();
        resultSet = parameterizedQuery.add(40).add(100).evaluate(dataSource);
        for(JoinableMap row : resultSet){
            Assert.assertTrue((double)row.get("weight") < 40 || (double)row.get("weight") > 100);
        }

        Layers.publishLayer(CustomFunction.class);

        query = Query.compile("SELECT name, customFunction(integerValue(weight)) FROM character");
        resultSet = query.evaluate(dataSource);

        query = Query.compile("SELECT dateFormat(birthday, 'YYYY--MM--dd HH::mm::ss') as year FROM character");
        resultSet = query.evaluate(dataSource);
        Assert.assertTrue(resultSet.iterator().next().get("year") instanceof String);

        query = Query.compile("SELECT addressId, aggregateSum('weight') AS sum FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);

        query = Query.compile("SELECT name, if(weight + 10 > 100, 'gordo', 'flaco') AS es FROM character");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT lastName, count('weight') as size, aggregateMin('weight') as min, aggregateMax('weight') as max, aggregateSum('weight') as sum, aggregateMean('weight') as arithmeticMean, aggregateMean('weight', 'harmonic') as harmonicMean FROM character group by lastName");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT *, reference('lipigas.Order', 'addressId', addressId) as address FROM character");
        resultSet = Query.evaluate(query);
        System.out.println();
    }

    public static class CustomFunction extends BaseQueryFunctionLayer implements QueryFunctionLayerInterface {

        public CustomFunction() {
            super("customFunction");
        }

        @Override
        public Object evaluate(String functionName, Object... parameters) {
            return ((Integer)parameters[0]) % 2 == 0 ? "P" : "I";
        }
    }

    public static class CharacterResource extends Layer implements ReadRowsLayerInterface {

        @Override
        public String getImplName() {
            return "character";
        }

        @Override
        public Collection<JoinableMap> readRows(Queryable queryable) {
            return queryable.evaluate(simpsonCharacters.values());
        }
    }

    public static class AddressResource extends Layer implements ReadRowsLayerInterface {

        @Override
        public String getImplName() {
            return "address";
        }

        @Override
        public Collection<JoinableMap> readRows(Queryable queryable) {
            return queryable.evaluate(simpsonAddresses.values());
        }
    }
}
