package org.hcjf.layers.query;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * @author javaito
 */
public class QueryCompileTest {

    @Test()
    public void testCompile() {

        try {
            Query query = Query.compile("SELECT 2+2*field1 as suma FROM resource WHERE resource.field != log(5)+2 AND resource.field = '2017-07-07 22:15:32'");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }


        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field != 5 AND resource.field = '2017-07-07 22:15:32'");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field != 5 AND resource.field = 6");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field != 5 AND resource.field = 6 OR resource.field <> 7");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field != 5 AND resource.field = 6 OR resource.field <> 7 LIMIT 10");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
            Assert.assertEquals(query.getLimit().intValue(), 10);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        try {
            Query query = Query.compile("SELECT * FROM resource JOIN resource1 ON resource.id = resource1.id WHERE resource.field != 5 AND resource.field = 6 OR resource.field <> 7 LIMIT 10");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
            Assert.assertEquals(query.getLimit().intValue(), 10);
            Assert.assertNotNull(query.getJoins());
            Assert.assertEquals(query.getJoins().size(), 1);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        try {
            Query query = Query.compile("SELECT * FROM resource JOIN resource1 ON resource.id = resource1.id WHERE resource.field != 5 AND resource.field = 6 OR resource.field <> 7 GROUP BY field2 LIMIT 10");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
            Assert.assertEquals(query.getLimit().intValue(), 10);
            Assert.assertNotNull(query.getJoins());
            Assert.assertEquals(query.getJoins().size(), 1);
            Assert.assertEquals(query.getGroupParameters().size(), 1);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        try {
            Query query = Query.compile("SELECT * FROM resource JOIN resource1 ON resource.id = resource1.id WHERE resource.field != 5 AND resource.field = 6 OR resource.field <> 7 GROUP BY field2 START 50 LIMIT 10");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
            Assert.assertEquals(query.getLimit().intValue(), 10);
            Assert.assertEquals(query.getStart().intValue(), 50);
            Assert.assertNotNull(query.getJoins());
            Assert.assertEquals(query.getJoins().size(), 1);
            Assert.assertEquals(query.getGroupParameters().size(), 1);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = 'POINT (23.34 34.98)'");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getMessage());
        }

        try {
            Query query = Query.compile("SELECT *  FROM holder WHERE nombre LIKE '%MKR%' OR dominio LIKE " +
                    "'%MKR%' AND activo = '1' AND holderid IN " +
                    "(92928,124291,11278,119441,104341,45460,111255,15513,15001,3358,12447,22047," +
                    "88740,15528,21033,3755,115506,57397,123447,10427,120639,120638,120641,120640," +
                    "120642,30533,58697,17483,40395,106188,7246,43598,23889,23891,81366,15321,23130," +
                    "5594,93018,81628,3041,20065,103654,78824,49257,19050,99818,87530,71405,39792,18930," +
                    "3827,16246,43261,47230) LIMIT 20");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
        } catch (Exception ex){
            Assert.fail(ex.getMessage());
        }
    }

    /**
     * Test the numeric value parsing
     */
    @Test
    public void testCompileNumericDataType() {
        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = 5");
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRawValue() instanceof Number);
        } catch (Exception ex) {
            Assert.fail("Unable to parse integer number");
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = 5.3");
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRawValue() instanceof Number);
        } catch (Exception ex) {
            Assert.fail("Unable to parse decimal number");
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = -0.00023");
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRawValue() instanceof Number);
        } catch (Exception ex) {
            Assert.fail("Unable to parse negative decimal number");
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = -2.3E-4");
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRawValue() instanceof Number);
        } catch (Exception ex) {
            Assert.fail("Unable to parse negative scientific number with negative exponent");
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = -2.3E4");
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRawValue() instanceof Number);
        } catch (Exception ex) {
            Assert.fail("Unable to parse negative scientific number");
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = 2.3E4");
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRawValue() instanceof Number);
        } catch (Exception ex) {
            Assert.fail("Unable to parse scientific number");
        }
    }

    /**
     * Test uuid type
     */
    @Test
    public void testCompileNumericUUIDType() {
        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = 2821c2b9-c485-4550-8dd8-6ec83033fa84");
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRawValue() instanceof UUID);
        } catch (Exception ex) {
            Assert.fail("Unable to parse UUID data type");
        }
    }

}
