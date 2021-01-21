package org.hcjf.layers.query;

import org.hcjf.bson.BsonDecoder;
import org.hcjf.bson.BsonDocument;
import org.hcjf.bson.BsonEncoder;
import org.hcjf.layers.Layer;
import org.hcjf.layers.Layers;
import org.hcjf.layers.crud.ReadRowsLayerInterface;
import org.hcjf.layers.query.evaluators.Equals;
import org.hcjf.layers.query.evaluators.In;
import org.hcjf.layers.query.functions.BaseQueryFunctionLayer;
import org.hcjf.layers.query.functions.QueryFunctionLayerInterface;
import org.hcjf.layers.query.model.QueryField;
import org.hcjf.properties.SystemProperties;
import org.hcjf.utils.Introspection;
import org.hcjf.utils.JsonUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author javaito
 */
public class QueryRunningTest {

    private static final String CHARACTER = "character";
    private static final String CHARACTER_2 = "character2";
    private static final String Z_CHARACTER = "zCharacter";
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
    private static final Map<UUID, JoinableMap> simpsonCharacters2 = new HashMap<>();
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
            simpsonCharacters2.put((UUID) map.get(ID), map);

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
            simpsonCharacters2.put((UUID) map.get(ID), map);

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
            simpsonCharacters2.put((UUID) map.get(ID), map);

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
            simpsonCharacters2.put((UUID) map.get(ID), map);

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
            simpsonCharacters2.put((UUID) map.get(ID), map);

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
            simpsonCharacters2.put((UUID) map.get(ID), map);

            map = new JoinableMap(ADDRESS);
            addressId = UUID.randomUUID();
            map.put(ADDRESS_ID, addressId);
            map.put(STREET, "Chubut");
            map.put(NUMBER, 2321);
            simpsonAddresses.put(addressId, map);

            map = new JoinableMap(CHARACTER);
            map.put(ID, UUID.randomUUID());
            map.put(NAME, "Nedward");
            map.put(LAST_NAME, "Flanders");
            map.put(NICKNAME, "Ned");
            map.put(BIRTHDAY, dateFormat.parse("1975-03-14"));
            map.put(WEIGHT, 82.0);
            map.put(HEIGHT, 1.70);
            map.put(GENDER, Gender.MALE);
            simpsonCharacters.put((UUID) map.get(ID), map);
            simpsonCharacters2.put((UUID) map.get(ID), map);
        } catch (Exception ex){}

        Layers.publishLayer(CharacterResource.class);
        Layers.publishLayer(Character2Resource.class);
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
                case CHARACTER_2: {
                    for(JoinableMap map : simpsonCharacters2.values()) {
                        result.add(new JoinableMap(map));
                    }
                    break;
                }
                case Z_CHARACTER: {
                    Collection<JoinableMap> resultSet = simpsonCharacters.values();
                    String json = JsonUtils.toJsonTree(resultSet).toString();
                    Collection<Map<String,Object>> newResultSet = (Collection<Map<String, Object>>) JsonUtils.createObject(json);
                    for(Map<String,Object> map : newResultSet) {
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
    public void functionInWhere() {
        Query query = Query.compile("SELECT * FROM character WHERE parseDate('yyyy-MM-dd HH:mm:ss', dateFormat(birthday, 'yyyy-MM-dd 00:00:00')) < '2020-01-01 00:00:00'");
        Collection<JoinableMap> resultSet = Query.evaluate(query);
        System.out.println();
    }

    @Test
    public void innerQuery() {
        Query query = Query.compile("SELECT * FROM character WHERE (SELECT name FROM character2 WHERE name like 'Homer') like name");
        Collection<JoinableMap> resultSet = Query.evaluate(query);
        System.out.println();

        query = Query.compile("SELECT * FROM character WHERE concat((SELECT name FROM character2 WHERE name like 'Homer'), ' Jay') like name");
        resultSet = Query.evaluate(query);
        System.out.println();

        query = Query.compile("select addressId from address where street like 'Evergreen Terrace' limit 1");
        resultSet = Query.evaluate(query);
        System.out.println();

        query = Query.compile("SELECT name FROM character WHERE addressId = (select addressId from address where street like 'Evergreen Terrace' limit 1)");
        resultSet = Query.evaluate(query);
        System.out.println();

        query = Query.compile("SELECT * FROM character WHERE (SELECT name FROM character2 WHERE addressId = (select addressId from address where street like 'Terrace' limit 1) limit 1) like name");
        resultSet = Query.evaluate(query);
        System.out.println();


        query = Query.compile("select addressId from address where street like 'Terrace' limit 1");
        resultSet = Query.evaluate(query);
        System.out.println();


        query = Query.compile("SELECT * FROM character WHERE addressId in (select addressId from address)");
        resultSet = Query.evaluate(query);
        System.out.println();
    }

    @Test
    public void subQueryAsParam() {
        Query query = Query.compile("SELECT * FROM character WHERE addressId = (SELECT addressId FROM address where street like 'Evergreen')");
        Collection<JoinableMap> resultSet = Query.evaluate(query);
        System.out.println();
    }

    @Test
    public void aggregateFunction() {
        Query query = Query.compile("SELECT addressId, aggregateProduct(weight) as aggregateWeight FROM character group by addressId");
        Collection<JoinableMap> resultSet = Query.evaluate(query);
        query = Query.compile("SELECT addressId, aggregateSum(weight, false) as aggregateWeight, aggregateSum(height, false) as aggregateHeight, aggregateEvalExpression(aggregateWeight - aggregateHeight) as result FROM character group by addressId");
        Collection<JoinableMap> resultSet1 = Query.evaluate(query);
        query = Query.compile("SELECT addressId, aggregateEvalExpression(sum(weight) / 2) as aggregateWeight FROM character group by addressId");
        Collection<JoinableMap> resultSet2 = Query.evaluate(query);
        System.out.println();
        query = Query.compile("SELECT addressId, aggregateEvalExpression(sum(weight) / 2) as aggregateWeight, aggregateContext(numberFormat('$#,###.00', aggregateWeight)) as weightFormatted FROM character group by addressId");
        Collection<JoinableMap> resultSet3 = Query.evaluate(query);
        System.out.println();
    }

    @Test
    public void checkToString() {
        Query query = Query.compile("SELECT * FROM character WHERE @underlying('field')");
        String queryToString = query.toString();
        Assert.assertTrue(queryToString.contains("@underlying('field')"));
    }

    @Test
    public void queryDynamicResource() {
        Query query1 = Query.compile("SELECT * FROM character JOIN address ON address.addressId = character.addressId where lastName like 'Simpson'");
        Collection<JoinableMap> resultSet1 = Query.evaluate(query1);
        Query query2 = Query.compile("SELECT * FROM (SELECT * FROM character JOIN address ON address.addressId = character.addressId where lastName like 'Simpson') as ch WHERE weight > 16");
        Collection<JoinableMap> resultSet2 = Query.evaluate(query2);
        System.out.printf("");
        Query query3 = Query.compile("SELECT * FROM (SELECT * FROM (SELECT * FROM character WHERE toString(gender) = 'FEMALE') as ch1 where lastName like 'Simpson') as ch JOIN address ON address.addressId = ch.addressId WHERE weight > 16");
        Collection<JoinableMap> resultSet3 = Query.evaluate(query3);
        System.out.println();
        Query query4 = Query.compile("SELECT * FROM (SELECT *, bsonParse(body) AS bodyDecoded  FROM character where isNotNull(body)).bodyDecoded as body");
        Collection<JoinableMap> resultSet4 = Query.evaluate(query4);
        Assert.assertEquals(Introspection.resolve(resultSet4.stream().findFirst().get(), "field1"), "string");
        Query query5 = Query.compile("SELECT field1 FROM (SELECT *, bsonParse(body) AS bodyDecoded  FROM character where isNotNull(body)).bodyDecoded as body");
        Collection<JoinableMap> resultSet5 = Query.evaluate(query5);
        Assert.assertEquals(resultSet5.stream().findFirst().get().size(), 1);
        Query query6 = Query.compile("SELECT * FROM character JOIN (select * from address) as add ON add.addressId = character.addressId WHERE weight > 16");
        Collection<JoinableMap> resultSet6 = Query.evaluate(query6);
        System.out.println();
    }

    @Test
    public void underlyingFunction() {
        Query query = Query.compile("SELECT * FROM character WHERE @underlying()");
        Collection<JoinableMap> resultSet = Query.evaluate(query);
        Assert.assertEquals(resultSet.size(), 7);

        query = Query.compile("SELECT * FROM character WHERE @underlying() and @underlying2()");
        resultSet = Query.evaluate(query);
        Assert.assertEquals(resultSet.size(), 7);

        query = Query.compile("SELECT * FROM character WHERE (@underlying() or @underlying2())");
        resultSet = Query.evaluate(query);
        Assert.assertEquals(resultSet.size(), 7);
    }

    @Test
    public void nullValues() {
        Query query = Query.compile("SELECT * FROM character WHERE  name = null");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 0);

        query = Query.compile("SELECT * FROM character WHERE  nickname like 'Bart'");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 1);

        query = Query.compile("SELECT * FROM character WHERE  nickname = null");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 2);

        query = Query.compile("SELECT * FROM character WHERE  nickname != null");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 5);
    }

    @Test
    public void distinct() {
        Query query = Query.compile("SELECT lastName, distinct(lastName) FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 5);

        query = Query.compile("SELECT count(lastName) as value1, distinct(lastName), count(lastName) as value2 FROM character group by a");
        resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void placeHolder() {
        Query query = Query.compile("SELECT name FROM character WHERE name = ?");
        ParameterizedQuery parameterizedQuery = query.getParameterizedQuery().add("Margaret Abigail");
        Collection<JoinableMap> resultSet = parameterizedQuery.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void startAndLimit() {
        Query query = Query.compile("SELECT name FROM character ORDER BY name");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);

        Collection<JoinableMap> firstResultSet = resultSet;
        JoinableMap first = resultSet.stream().findFirst().get();
        JoinableMap last = resultSet.stream().skip(resultSet.stream().count() - 1).findFirst().get();

        query = Query.compile("SELECT * FROM character ORDER BY name LIMIT 1");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.stream().findFirst().get().get("name"), first.get("name"));

        query = Query.compile("SELECT * FROM character ORDER BY name DESC LIMIT 1");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.stream().findFirst().get().get("name"), last.get("name"));

        query = Query.compile("SELECT * FROM character ORDER BY name LIMIT 1000");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), firstResultSet.size());

        query = Query.compile("SELECT * FROM character ORDER BY name START 0 LIMIT 1000");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), firstResultSet.size());

        query = Query.compile("SELECT * FROM character ORDER BY name START 2 LIMIT 1000");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), firstResultSet.size()-2);

        query = Query.compile("SELECT * FROM character ORDER BY name START 2 LIMIT 2");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 2);
    }

    @Test
    public void debug() {
        Query query = Query.compile("" +
                "SELECT address.street, lastName, concat(name), stringJoin('@', name), sum(weight), addressId FROM character " +
                "JOIN address on address.addressId = character.addressId " +
                "WHERE character.lastName like 'simp'");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void join() {
        Query query = Query.compile("SELECT address.street, concat(name), stringJoin('@', name), sum(weight), addressId FROM character JOIN address ON address.addressId = character.addressId GROUP BY addressId");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonAddresses.size() - 1);

        query = Query.compile("SELECT name, count(character.name), address.street, concat(name), stringJoin('@', name), sum(weight), addressId FROM character JOIN address ON address.addressId = character.addressId GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonAddresses.size() - 1);

        query = Query.compile("SELECT * FROM character RIGHT JOIN address ON address.addressId = character.addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonCharacters.size());

        query = Query.compile("SELECT * FROM character LEFT JOIN address ON address.addressId = character.addressId");
        resultSet = query.evaluate(dataSource);

        resultSet.stream().findFirst().get().get("street");

        Assert.assertEquals(resultSet.size(), simpsonCharacters.size());

        query = Query.compile("SELECT * FROM character FULL JOIN address ON address.addressId = character.addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonCharacters.size() + 1);

        query = Query.compile("SELECT * FROM character JOIN address ON address.addressId = character.addressId where isNotNull(nickname)");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 4);

        query = Query.compile("SELECT * FROM character JOIN address ON address.addressId = character.addressId where isNotNull(character.nickname)");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 4);

        query = Query.compile("SELECT * FROM character JOIN address ON address.addressId = character.addressId where isNotNull(nickname) and street = 'Evergreen Terrace'");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 3);

        query = Query.compile("SELECT * FROM character JOIN character2 ON character.id = character2.id");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonCharacters.size());

        query = Query.compile("SELECT * FROM character JOIN character2 ON true");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonCharacters.size() * simpsonCharacters.size());

        query = Query.compile("SELECT * FROM character JOIN character2 ON character.id = character2.id JOIN address ON address.addressId = character.addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonCharacters.size() - 1);

        query = Query.compile("SELECT * FROM character JOIN character2 ON character.id = character2.id LEFT JOIN address ON address.addressId = character.addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonCharacters.size());

        query = Query.compile("SELECT * FROM character JOIN character2 ON character.id = character2.id LEFT JOIN address ON address.addressId = character.addressId where character2.name like 'Home'");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 1);

        query = Query.compile("SELECT * FROM character JOIN character2 ON character.lastName like character2.lastName JOIN address ON address.addressId = character.addressId");
        resultSet = query.evaluate(dataSource);

        query = Query.compile("SELECT * FROM character JOIN character2 ON character.lastName like character2.lastName JOIN address ON address.addressId = character.addressId");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT name, aggregateSum(weight) FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT name, aggregateSum(weight) FROM character GROUP BY addressId UNION SELECT name, aggregateSum(weight) FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT * FROM character JOIN (SELECT * FROM character) AS ch ON character.id = ch.id");
        resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void testIf() {
        Query query = Query.compile("SELECT * FROM (SELECT *, if(equals(weight, 82),'si','no') as mmm FROM character) AS ch where mmm = 'si'");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 2);
    }

    @Test
    public void testArithmetic() {
        Query query = Query.compile("SELECT *, weight * 2 as wt FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void testPutAggregateFunction() {
        Query query = Query.compile("SELECT *, put('.', 'copy', .) FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
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

        query = Query.compile("SELECT count(weight) AS size FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 3);

        query = Query.compile("SELECT count(weight) AS size FROM character WHERE isNotNull(addressId) GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 2);

        query = Query.compile("SELECT aggregateSum(weight, false) AS sum FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 3);

        query = Query.compile("SELECT aggregateProduct(weight, false) AS product FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 3);

        query = Query.compile("SELECT aggregateMean(weight, 'arithmetic', false) AS mean FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 3);

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

        query = Query.compile("SELECT weight, -2  *  weight AS superWeight, pow(max(weight, 50.1) ,2) AS smartWeight FROM character");
        resultSet = query.evaluate(dataSource);
        for(Joinable obj : resultSet) {
            Double dweight = Introspection.resolve(obj, "weight");
            dweight = -2 * dweight;
            BigDecimal weight = new BigDecimal(dweight);
            BigDecimal superWeight = Introspection.resolve(obj, "superWeight");
            Assert.assertEquals(superWeight, weight);
        }

        query = Query.compile("SELECT name FROM character");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.iterator().next().size(), 1);

        query = Query.compile("SELECT *, name as nombre FROM character");
        resultSet = query.evaluate(dataSource);
        JoinableMap first = resultSet.iterator().next();
        Assert.assertEquals(first.get("nombre"), first.get("name"));

        query = Query.compile("SELECT street, concat(name), stringJoin('@', name), sum(weight), addressId FROM character JOIN address ON address.addressId = character.addressId GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonAddresses.size() - 1);

        query = Query.compile("SELECT bsonParse(body) AS body FROM character WHERE name LIKE 'Bartolomeo'");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(((Map<String,Object>)resultSet.iterator().next().get(BODY)).get("field1"), "string");

        query = Query.compile("SELECT get(bsonParse(body),'field1') AS body FROM character WHERE name LIKE 'Bartolomeo'");
        resultSet = query.evaluate(dataSource);

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

        query = Query.compile("SELECT addressId, aggregateSum(weight) AS sum FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        System.out.println(JsonUtils.toJsonTree(resultSet).toString());

        query = Query.compile("SELECT name, if(weight + 10 > 100, 'gordo', 'flaco') AS es FROM character");
        resultSet = query.evaluate(dataSource);
        for(JoinableMap map : resultSet) {
            Assert.assertNotNull(map.get("es"));
        }

        query = Query.compile("SELECT name, if(weight + 10 > 0, 'gordo', size(null)) AS es FROM character");
        resultSet = query.evaluate(dataSource);

        try {
            query = Query.compile("SELECT name, if(weight + 10 > 0, size(null), 'flaco') AS es FROM character");
            resultSet = query.evaluate(dataSource);
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(true);
        }

        query = Query.compile("SELECT name, if(equals(name, 'Homer Jay'), 'gordo', 'flaco') AS es FROM character");
        resultSet = query.evaluate(dataSource);

        query = Query.compile("SELECT name, case(name, 'Homer Jay', 'gordo', 'Marjorie Jaqueline', 'flaco', 'mmm!') AS es FROM character");
        resultSet = query.evaluate(dataSource);

        query = Query.compile("SELECT lastName, count(weight) as size, aggregateMin(weight) as min, aggregateMax(weight) as max, aggregateSum(weight, false) as sum, aggregateMean(weight, 'arithmetic', false) as arithmeticMean, aggregateMean(weight, 'harmonic', false) as harmonicMean FROM character group by lastName");
        resultSet = query.evaluate(dataSource);
        System.out.println(JsonUtils.toJsonTree(resultSet).toString());
        System.out.println();

        query = Query.compile("SELECT name, lastName, nickname FROM character");
        resultSet = query.evaluate(dataSource);
        System.out.println(JsonUtils.toJsonTree(resultSet).toString());
        System.out.println();

        query = Query.compile("SELECT name, lastName, nickname, new('literal') as literal FROM character");
        resultSet = query.evaluate(dataSource);
        System.out.println(JsonUtils.toJsonTree(resultSet).toString());
        System.out.println();

        query = Query.compile("SELECT name, lastName, nickname, new('2019-01-01 00:00:00') as literal FROM character");
        resultSet = query.evaluate(dataSource);
        Assert.assertTrue(resultSet.stream().findFirst().get().get("literal") instanceof Date);
        System.out.println(JsonUtils.toJsonTree(resultSet).toString());
        System.out.println();

        query = Query.compile("SELECT name, lastName, nickname, getMillisecondUnixEpoch(new('2019-01-01 00:00:00')) as literal FROM character");
        resultSet = query.evaluate(dataSource);
        Assert.assertTrue(resultSet.stream().findFirst().get().get("literal") instanceof Long);

        query = Query.compile("SELECT length(name) as length, length(name) + 5 as lengthName FROM character");
        resultSet = query.evaluate(dataSource);

        BsonDocument bsonDocument = new BsonDocument(resultSet.stream().findFirst().get());
        byte[] doc = BsonEncoder.encode(bsonDocument);

        bsonDocument = BsonDecoder.decode(doc);
        Map<String,Object> map = bsonDocument.toMap();
    }

    @Test
    public void queryInsideAggregationContext() {
        Query query = Query.compile("SELECT (SELECT * FROM .) as names FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();


        query = Query.compile("SELECT aggregateContext((SELECT * FROM . WHERE lastName like 'simp')) as names FROM character");
        resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void disjoint() {
        Query query = Query.compile("SELECT * FROM character DISJOINT BY lastName");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("select (SELECT count() FROM disjointResultSet WHERE height >= 1.30) as mayores, (SELECT count() FROM disjointResultSet WHERE height < 1.30) as menores FROM (SELECT * FROM character DISJOINT BY lastName) as data");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("select (SELECT name, lastName FROM disjointResultSet WHERE height >= 1.30) as mayores, (SELECT name, lastName FROM disjointResultSet WHERE height < 1.30) as menores FROM (SELECT * FROM character DISJOINT BY lastName) as data");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("select (SELECT name as nombre, lastName FROM disjointResultSet WHERE height >= 1.30) as mayores, (SELECT name, lastName FROM disjointResultSet WHERE height < 1.30) as menores FROM (SELECT * FROM character DISJOINT BY lastName) as data");
        resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void mathExpression() {
        Query query = Query.compile("SELECT *, (100 - 2) / 50 as value FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void testSize() {
        Query query = Query.compile("SELECT aggregateContext(size(name)) as names FROM character group by a");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT * FROM character join address on address.addressId = character.addressId group by a");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT name, aggregateContext(size(name)) as names FROM character left join address on address.addressId = character.addressId group by a");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.stream().findFirst().get().get("names"), 7);

        query = Query.compile("SELECT size(name) as names FROM (SELECT name FROM character left join address on address.addressId = character.addressId group by a) as date");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.stream().findFirst().get().get("names"), 7);

        query = Query.compile("SELECT aggregateContext(size(name)) as names FROM character right join address on address.addressId = character.addressId group by a");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.stream().findFirst().get().get("names"), 6);

        query = Query.compile("SELECT aggregateContext(size(name)) as names FROM character left join address on address.addressId = character.addressId group by addressId");
        resultSet = query.evaluate(dataSource);
    }

    @Test
    public void shellTest() {
        Query query = Query.compile("SELECT java('name.toString().length() > 10',*) as var FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void newObjectTest() {
        Query query = Query.compile("SELECT newMap('key', 'value', 'name', name) as var FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        for(JoinableMap map : resultSet) {
            Assert.assertTrue(Introspection.resolve(map, "var") instanceof Map);
        }
        System.out.println();
    }

    @Test
    public void newArrayTest() {
        Query query = Query.compile("SELECT newArray('name', name) as var FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        for(JoinableMap map : resultSet) {
            Assert.assertTrue(Introspection.resolve(map, "var") instanceof Collection);
        }
        System.out.println();
    }

    @Test
    public void alwaysTrue() {
        Query query = Query.compile("SELECT * FROM character WHERE true");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonCharacters.size());
    }

    @Test
    public void dateTimeZone() {
        Query query = Query.compile("SELECT dateFormat(birthday, 'GMT', 'yyyy/MM/dd HH:mm:ssZ') as gmt, dateFormat(birthday, 'America/Santiago', 'yyyy/MM/dd HH:mm:ssZ') as santiago FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void toDate() {
        Query query = Query.compile("SELECT birthday, toDate(plusDays(birthday,1)) as birthdayPlusOne FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void isNullTest() {
        Query query = Query.compile("SELECT isNull(noField) as n FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonCharacters.size());
        Assert.assertTrue(Introspection.resolve(resultSet.stream().findFirst().get(), "n"));
    }

    @Test
    public void testOrder() {
        Query query = Query.compile("SELECT * FROM character ORDER BY name limit 1");
        Collection<JoinableMap> resultSet = Query.evaluate(query);
        System.out.println();
    }

    @Test
    public void testGeoFunctions() {

        Query query = Query.compile("SELECT name, geoAsText(geoUnion('POINT(-33.2569 -65.2548)', 'MULTIPOINT ((10 40), (40 30), (20 20), (30 10))')) as gulf FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);

        query = Query.compile("SELECT name, geoUnion('POINT(-33.2569 -65.2548)', 'MULTIPOINT ((10 40), (40 30), (20 20), (30 10))') as gulf FROM character");
        resultSet = query.evaluate(dataSource);

        query = Query.compile("SELECT name, new('hola') as gulf FROM character");
        resultSet = query.evaluate(dataSource);

        query = Query.compile("SELECT geodesicDistance('POINT(-68.820792 -32.892190)', 'POINT(-68.820910 -32.894325)') FROM character WHERE true");
        resultSet = query.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), simpsonCharacters.size());

        System.out.println();
    }

    @Test
    public void testToStringFunction() {
        Query query = Query.compile("SELECT toString(name) FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);

        query = Query.compile("SELECT toString(number) as numberAsString FROM address");
        resultSet = query.evaluate(dataSource);
        for(Map<String,Object> obj : resultSet) {
            Assert.assertEquals(obj.get("numberAsString").getClass(), String.class);
        }

        query = Query.compile("SELECT * FROM (SELECT number as A_NUMBER FROM address) as add WHERE toString(A_NUMBER) = '2321'");
        resultSet = query.evaluate(dataSource);

        query = Query.compile("SELECT toString(name) as NAME, indexOf(name, 'a') as whereIsA, subString(name, 1) as subString1, subString(name, 1, 3) as subString2 FROM character");
        resultSet = query.evaluate(dataSource);
        for(Map<String,Object> obj : resultSet) {
            Assert.assertEquals(((String)obj.get("NAME")).indexOf("a"), ((Number)obj.get("whereIsA")).intValue());
            Assert.assertEquals(((String)obj.get("NAME")).substring(1), obj.get("subString1"));
            Assert.assertEquals(((String)obj.get("NAME")).substring(1, 3), obj.get("subString2"));
        }
    }

    @Test
    public void arithmeticTest() {
        Query query = Query.compile("SELECT * FROM (SELECT *, getMillisecondUnixEpoch(now()), getMillisecondUnixEpoch(birthday), getMillisecondUnixEpoch(now()) - getMillisecondUnixEpoch(birthday) as ageMillis FROM character JOIN address ON address.addressId = character.addressId where lastName like 'Simpson') as ch WHERE weight > 16");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void testDateFunctions() {
        Query query = Query.compile("SELECT *, new('2020-03-01 00:25:12') as newDate FROM character");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT *, new('2020-03-01T00:25:12') as newDate, parseDate('yyyy-MM-dd\\'T\\'HH:mm:ss', '2020-03-01T00:25:12') as parseDate FROM character");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        String s = "{\n" +
                "    \"endDate\": 1582724862291,\n" +
                "    \"events\": [],\n" +
                "    \"loginCode\": \"359459076768998\",\n" +
                "    \"odometerEnd\": 792.8828323183975,\n" +
                "    \"odometerStart\": 0,\n" +
                "    \"processId\": \"0a259971-ca87-4175-b3dd-b0c3235ec828\",\n" +
                "    \"propertyId\": \"1\",\n" +
                "    \"propertyName\": \"Finca Bodega Salentein SRL\",\n" +
                "    \"startDate\": 1582724839702,\n" +
                "    \"trackingCoords\": [\n" +
                "        -65.25090605020523,\n" +
                "        -26.873189827241074,\n" +
                "        -65.25777423681663,\n" +
                "        -26.873070930388632,\n" +
                "        -65.25792522077234,\n" +
                "        -26.874055487787768\n" +
                "    ],\n" +
                "    \"trackingDistance\": 0.7928828323183975\n" +
                "}";
        Object obj =  JsonUtils.createObject(s);
        System.out.println();

        query = Query.compile("SELECT *, dateFormat('2020-03-01 00:25:12','UTC','America/Santiago','yyyy/MM/dd HH:mm:ss') as newDate FROM character");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT *, dateFormat('2020-03-01 00:25:12','UTC','America/Santiago','yyyy/MM/dd HH:mm:ss') as newDate FROM character");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT *, new('2020-03-01 00:25:12') as dateBefore, dateTransition('2020-03-01 00:25:12','UTC','America/Santiago') as date FROM character");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT *, new('2020-03-01 00:25:12') as dateBefore, dateTransition('2020-03-01 00:25:12','Asia/Ho_Chi_Minh','America/Santiago') as date FROM character");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT *, new('2020-03-01 00:25:12') as dateBefore, dateTransition(now(),'Asia/Ho_Chi_Minh','America/Santiago') as date FROM character");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT *, new('2020-03-01 00:25:12') as dateBefore, toDate(dateTransition('2020-03-01 00:25:12','America/Santiago','Asia/Ho_Chi_Minh')) as date FROM character");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        query = Query.compile("SELECT *, new('2020-03-01 00:25:12') as dateBefore, now('Asia/Ho_Chi_Minh') as date FROM character");
        resultSet = query.evaluate(dataSource);
        System.out.println();

        System.out.println((ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)));
        System.out.println(ZonedDateTime.parse("2020-09-09T16:06:14.838904-03:00[America/Argentina/Buenos_Aires]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        System.out.println(ZonedDateTime.parse("2020-09-09T16:06:14-03:00[America/Argentina/Buenos_Aires]", DateTimeFormatter.ISO_ZONED_DATE_TIME));
        System.out.println(ZonedDateTime.parse("2020-09-09T16:06:14-03:00", DateTimeFormatter.ISO_ZONED_DATE_TIME));
    }

    @Test
    public void testCollectionFunctions() {
        Query query = Query.compile("SELECT *, aggregateContext(sort(name)) as sortedNames FROM character GROUP BY addressId");
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        for(JoinableMap row : resultSet) {
            if(((Collection)row.get("sortedNames")).size() > 1) {
                Assert.assertEquals(((Collection)row.get("sortedNames")).stream().findFirst().get(), "Bartolomeo Jay");
            }
        }

        query = Query.compile("SELECT *, aggregateContext(first(sort(name))) as firstSortedName FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        for(JoinableMap row : resultSet) {
            Assert.assertTrue(row.get("firstSortedName") instanceof String);
        }

        query = Query.compile("SELECT *, aggregateContext(last(sort(name))) as firstSortedName FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        for(JoinableMap row : resultSet) {
            Assert.assertTrue(row.get("firstSortedName") instanceof String);
        }

        query = Query.compile("SELECT *, aggregateContext(limit(name, 3)) as limitedNames FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        for(JoinableMap row : resultSet) {
            Assert.assertTrue(row.get("limitedNames") instanceof Collection);
            Assert.assertTrue(((Collection)row.get("limitedNames")).size() <= 3);
        }

        query = Query.compile("SELECT *, aggregateContext(skip(name, 3)) as limitedNames FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        for(JoinableMap row : resultSet) {
            Assert.assertTrue(row.get("limitedNames") instanceof Collection);
        }

        query = Query.compile("SELECT *, aggregateContext(limit(skip(sort(name), 2), 1)) as limitedNames FROM character GROUP BY addressId");
        resultSet = query.evaluate(dataSource);
        for(JoinableMap row : resultSet) {
            Assert.assertTrue(row.get("limitedNames") instanceof Collection);
        }
    }

    @Test
    public void subParameterizedQueryTest() {
        String sql = "SELECT * FROM (SELECT * FROM character WHERE name like ?) as hc where lastName like ?";
        Query query = Query.compile(sql);
        ParameterizedQuery parameterizedQuery = query.getParameterizedQuery();
        parameterizedQuery.add("Homer");
        parameterizedQuery.add("Simp");
        Collection<JoinableMap> resultSet = parameterizedQuery.evaluate(dataSource);
        System.out.println();


        sql = "SELECT length, instanceOf(length) as instanceOf FROM (SELECT if(equals(length(name),0),'huy!','bah!') as length FROM zCharacter) as hc";
        query = Query.compile(sql);
        resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void testEnlargedObjectWithJoin() {
        String sql = "select if(equals(age,0),false,if(false,if(equals(name,'FJHG20'),false,true),if(equals(name,'FJHG20'),true,false))) as isDisabled, " +
                " addressId, name, " +
                " length(name) as l, " +
                " toString(l) as sl, " +
                " instanceOf(sl), " +
                " if(equals(l,0), true, false) as eq, instanceOf(eq) as eqt, " +
                " instanceOf(sl) as iosl, " +
                " isDisabled, instanceOf(isDisabled) as ioid from character" +
                " join address on character.addressId = address.addressId";
        Query query = Query.compile(sql);
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void testReplaceableValues() {
        Query query = Query.compile("SELECT * FROM character WHERE lastName like ?");
        ParameterizedQuery parameterizedQuery = query.getParameterizedQuery();
        parameterizedQuery.add("simp");
        Collection<JoinableMap> resultSet = parameterizedQuery.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 5);

        query = Query.compile("SELECT * FROM character WHERE lastName like ?");
        parameterizedQuery = query.getParameterizedQuery();
        parameterizedQuery.add("simp");
        resultSet = parameterizedQuery.evaluate(dataSource);
        Assert.assertEquals(resultSet.size(), 5);
    }

    @Test
    public void testConditionalValue() {
        String sql = "SELECT (length(name) > 5 AND length(lastName) > 10) as two, name, lastName FROM character";
        Query query = Query.compile(sql);
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();

        sql = "SELECT if((length(name) > 5 AND length(lastName) > 10), 'son nombre largos ;)','no son nombres largos :(') as two, name, lastName FROM character";
        query = Query.compile(sql);
        resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void testAggregateSum() {
        String sql = "SELECT aggregateSum(weight) FROM character";
        Query query = Query.compile(sql);
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();

        sql = "SELECT aggregateSum(weight, false) FROM character";
        query = Query.compile(sql);
        resultSet = query.evaluate(dataSource);
        System.out.println();

        sql = "SELECT aggregateSum(weight, false, true) FROM character";
        query = Query.compile(sql);
        resultSet = query.evaluate(dataSource);
        System.out.println();

        sql = "SELECT *, put('.', 'idCopy', id) FROM character";
        query = Query.compile(sql);
        resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void testAggregateMean() {
        String sql = "SELECT aggregateMean(weight) FROM character";
        Query query = Query.compile(sql);
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();

        sql = "SELECT aggregateMean(weight, 'arithmetic', false) FROM character";
        query = Query.compile(sql);
        resultSet = query.evaluate(dataSource);
        System.out.println();

        sql = "SELECT aggregateMean(weight, 'harmonic') FROM character";
        query = Query.compile(sql);
        resultSet = query.evaluate(dataSource);
        System.out.println();

        sql = "SELECT aggregateMean(weight, 'median', false) FROM character";
        query = Query.compile(sql);
        resultSet = query.evaluate(dataSource);
        System.out.println();

        sql = "SELECT aggregateMean(weight, 'median') FROM character";
        query = Query.compile(sql);
        resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void testJsonResource() {
        String sql = "SELECT * FROM '[{\"id\":1,\"value\":32.56},{\"id\":2,\"value\":85.32}]' as data";
        Query query = Query.compile(sql);
        Collection<JoinableMap> resultSet = query.evaluate(dataSource);
        System.out.println();
    }

    @Test
    public void testGroupByAndCount() {
        String json = "[\n" +
                "  {\n" +
                "    \"fechaEvento\": \"2020-04-29 00:00:00\",\n" +
                "    \"fletero\": 96263,\n" +
                "    \"secuenciaPlanificada\": 26,\n" +
                "    \"descripcionRechazo\": \"PRODUCTOS EN MAL ESTADO\",\n" +
                "    \"centroDistribucion\": 7,\n" +
                "    \"nroPedido\": 274683090291,\n" +
                "    \"_updateAccount\": \"ea0bffc7-40f3-5252-bbd8-61ce1d0c220d\",\n" +
                "    \"nombreUen\": \"COMERCIAL CCU S.A.\",\n" +
                "    \"idCliente\": 780476,\n" +
                "    \"codCamion\": \"020\",\n" +
                "    \"horaEvento\": \"2020-04-29 19:02:41\",\n" +
                "    \"id\": \"070a5528-4c30-4129-aa63-ca7ac5942421\",\n" +
                "    \"estadoEntrega\": \"2\",\n" +
                "    \"uen\": 96,\n" +
                "    \"lineas\": [\n" +
                "      {\n" +
                "        \"cantidadFacturada\": 1,\n" +
                "        \"producto\": 1007,\n" +
                "        \"cantidad\": 1,\n" +
                "        \"codigoRechazo\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"cantidadFacturada\": 1,\n" +
                "        \"producto\": 1600,\n" +
                "        \"cantidad\": 0,\n" +
                "        \"codigoRechazo\": 180\n" +
                "      },\n" +
                "      {\n" +
                "        \"cantidadFacturada\": 1,\n" +
                "        \"producto\": 1603,\n" +
                "        \"cantidad\": 0,\n" +
                "        \"codigoRechazo\": 180\n" +
                "      },\n" +
                "      {\n" +
                "        \"cantidadFacturada\": 1,\n" +
                "        \"producto\": 1728,\n" +
                "        \"cantidad\": 1,\n" +
                "        \"codigoRechazo\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"cantidadFacturada\": 2,\n" +
                "        \"producto\": 870206,\n" +
                "        \"cantidad\": 2,\n" +
                "        \"codigoRechazo\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"cantidadFacturada\": 1,\n" +
                "        \"producto\": 953,\n" +
                "        \"cantidad\": 1,\n" +
                "        \"codigoRechazo\": 0\n" +
                "      }\n" +
                "    ],\n" +
                "    \"codRechazo\": 180,\n" +
                "    \"numeroFactura\": 107008125,\n" +
                "    \"planilla\": 7770646,\n" +
                "    \"_creationDate\": \"2020-04-29 23:02:54\",\n" +
                "    \"_eventTracking\": {\n" +
                "      \"dispatchDate\": \"2020-04-29 23:02:54\",\n" +
                "      \"eventTriggerId\": \"74b2b470-23b1-4c8e-a105-619227b5a2de\",\n" +
                "      \"from\": {\n" +
                "        \"path\": \"/event/flow/conditional\",\n" +
                "        \"headers\": {\n" +
                "          \"Authorization\": \"Apikey 7e388459f3994390b4b9fc2eb03346aa\",\n" +
                "          \"Accept-Charset\": \"big5, big5-hkscs, euc-jp, euc-kr, gb18030, gb2312, gbk, ibm-thai, ibm00858, ibm01140, ibm01141, ibm01142, ibm01143, ibm01144, ibm01145, ibm01146, ibm01147, ibm01148, ibm01149, ibm037, ibm1026, ibm1047, ibm273, ibm277, ibm278, ibm280, ibm284, ibm285, ibm290, ibm297, ibm420, ibm424, ibm437, ibm500, ibm775, ibm850, ibm852, ibm855, ibm857, ibm860, ibm861, ibm862, ibm863, ibm864, ibm865, ibm866, ibm868, ibm869, ibm870, ibm871, ibm918, iso-2022-cn, iso-2022-jp, iso-2022-jp-2, iso-2022-kr, iso-8859-1, iso-8859-13, iso-8859-15, iso-8859-2, iso-8859-3, iso-8859-4, iso-8859-5, iso-8859-6, iso-8859-7, iso-8859-8, iso-8859-9, jis_x0201, jis_x0212-1990, koi8-r, koi8-u, shift_jis, tis-620, us-ascii, utf-16, utf-16be, utf-16le, utf-32, utf-32be, utf-32le, utf-8, windows-1250, windows-1251, windows-1252, windows-1253, windows-1254, windows-1255, windows-1256, windows-1257, windows-1258, windows-31j, x-big5-hkscs-2001, x-big5-solaris, x-compound_text, x-euc-jp-linux, x-euc-tw, x-eucjp-open, x-ibm1006, x-ibm1025, x-ibm1046, x-ibm1097, x-ibm1098, x-ibm1112, x-ibm1122, x-ibm1123, x-ibm1124, x-ibm1166, x-ibm1364, x-ibm1381, x-ibm1383, x-ibm300, x-ibm33722, x-ibm737, x-ibm833, x-ibm834, x-ibm856, x-ibm874, x-ibm875, x-ibm921, x-ibm922, x-ibm930, x-ibm933, x-ibm935, x-ibm937, x-ibm939, x-ibm942, x-ibm942c, x-ibm943, x-ibm943c, x-ibm948, x-ibm949, x-ibm949c, x-ibm950, x-ibm964, x-ibm970, x-iscii91, x-iso-2022-cn-cns, x-iso-2022-cn-gb, x-iso-8859-11, x-jis0208, x-jisautodetect, x-johab, x-macarabic, x-maccentraleurope, x-maccroatian, x-maccyrillic, x-macdingbat, x-macgreek, x-machebrew, x-maciceland, x-macroman, x-macromania, x-macsymbol, x-macthai, x-macturkish, x-macukraine, x-ms932_0213, x-ms950-hkscs, x-ms950-hkscs-xp, x-mswin-936, x-pck, x-sjis_0213, x-utf-16le-bom, x-utf-32be-bom, x-utf-32le-bom, x-windows-50220, x-windows-50221, x-windows-874, x-windows-949, x-windows-950, x-windows-iso2022jp\",\n" +
                "          \"X-Cloud-Trace-Context\": \"fdaf2e7b43098c68ba4c4df048704bd1/11180262256834346684\",\n" +
                "          \"Accept\": \"text/plain, application/json, application/*+json, */*\",\n" +
                "          \"User-Agent\": \"Java1.7.0_131\",\n" +
                "          \"X-Forwarded-Proto\": \"http\",\n" +
                "          \"Connection\": \"Keep-Alive\",\n" +
                "          \"X-Forwarded-For\": \"200.111.67.30, 34.95.78.57\",\n" +
                "          \"Host\": \"api.sitrack.io\",\n" +
                "          \"Content-Length\": \"901\",\n" +
                "          \"Content-Type\": \"application/json; charset=utf-8\",\n" +
                "          \"Via\": \"1.1 google\"\n" +
                "        },\n" +
                "        \"protocol\": \"HTTP\",\n" +
                "        \"method\": \"POST\",\n" +
                "        \"parameters\": {}\n" +
                "      }\n" +
                "    },\n" +
                "    \"fechaReparto\": \"2020-04-29 00:00:00\",\n" +
                "    \"_creationAccount\": \"ea0bffc7-40f3-5252-bbd8-61ce1d0c220d\",\n" +
                "    \"usuarioCamion\": 0,\n" +
                "    \"_lastUpdate\": \"2020-04-29 23:02:54\",\n" +
                "    \"_permissions\": [],\n" +
                "    \"_id\": \"070a5528-4c30-4129-aa63-ca7ac5942421\",\n" +
                "    \"codCarga\": 1,\n" +
                "    \"referencia\": 7770646\n" +
                "  }\n" +
                "]";




        Collection<Map<String,Object>> data = (Collection<Map<String, Object>>) JsonUtils.createObject(json);

        Query query1 = Query.compile("select lineas from store.dynamic.ccu.delivery where planilla = 7770646 and isNotNull(fletero) and numeroFactura=107008125");
        Collection<Map<String,Object>> resultSet1 = query1.evaluate(data);

        Query query = Query.compile("select aggregateSum(cantidadFacturada, false) as totalFacturado, aggregateSum(cantidad, false) as totalEntregado, aggregateContext(cantidadFacturada) as facturado, aggregateContext(cantidad) as entregado from (select lineas from store.dynamic.ccu.delivery where planilla = 7770646 and isNotNull(fletero) and numeroFactura=107008125).lineas as data group by a");

        Collection<Map<String,Object>> resultSet = query.evaluate(data);
        Map<String,Object> firstObject = resultSet.stream().findFirst().get();
        Assert.assertEquals(((Collection)firstObject.get("facturado")).size(), 6);
        Assert.assertEquals(((Collection)firstObject.get("entregado")).size(), 6);

        Assert.assertEquals(((BigDecimal)firstObject.get("totalFacturado")).intValue(), 7);
        Assert.assertEquals(((BigDecimal)firstObject.get("totalEntregado")).intValue(), 5);
    }

    @Test
    public void testCodCamion() throws Exception {

        System.out.println("085".hashCode());
        System.out.println("07T".hashCode());

        String json = new String(Files.readAllBytes(Path.of("/home/javaito/Descargas/codCamion.json")));
        List<Map<String,Object>> datasource = (List<Map<String, Object>>) JsonUtils.createObject(json);
        String sql = "select codCamion from data group by codCamion";
        Query query = Query.compile(sql);
        Collection rs = query.evaluate(datasource);
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

    public static class Character2Resource extends Layer implements ReadRowsLayerInterface {

        @Override
        public String getImplName() {
            return "character2";
        }

        @Override
        public Collection<JoinableMap> readRows(Queryable queryable) {
            return queryable.evaluate(simpsonCharacters2.values());
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
