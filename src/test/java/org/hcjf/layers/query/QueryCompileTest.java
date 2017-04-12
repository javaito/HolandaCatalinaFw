package org.hcjf.layers.query;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Javier Quiroga.
 * @email javier.quiroga@sitrack.com
 */
public class QueryCompileTest {

    @Test()
    public void test() {
        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field != 5");
            Assert.assertNotNull(query);
        } catch (Exception ex) {
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


}
