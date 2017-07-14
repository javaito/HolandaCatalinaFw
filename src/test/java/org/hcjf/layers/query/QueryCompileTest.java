package org.hcjf.layers.query;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public class QueryCompileTest {

    @Test()
    public void testCompile() {
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
