package org.hcjf.layers.query;

import org.hcjf.layers.query.evaluators.FieldEvaluator;
import org.hcjf.layers.query.model.QueryDynamicResource;
import org.hcjf.utils.Strings;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

/**
 * @author javaito
 */
public class QueryCompileTest {

    @Test
    public void testInWhereOneValue() {
        String queryAsString = "SELECT * FROM resource WHERE id IN (774cb3ea-c372-4fb3-b00d-8b7a9648a321)";
        Query query = Query.compile(queryAsString);
        System.out.println();

        queryAsString = "SELECT * FROM resource WHERE id IN ('hola-mundo+3456')";
        query = Query.compile(queryAsString);
        System.out.println();

        queryAsString = "SELECT * FROM resource WHERE id IN (36589)";
        query = Query.compile(queryAsString);
        System.out.println();
    }

    @Test
    public void dynamicResourceCompile() {
        try {
            Query query = Query.compile("SELECT * FROM (SELECT * FROM resource limit 10) as resource");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
            Assert.assertTrue(query.getResource() instanceof QueryDynamicResource);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        try {
            Query query = Query.compile("SELECT * FROM (SELECT * FROM resource limit 10) as resource JOIN resource1 ON resource.id = resource1.id WHERE resource.field != 5 AND resource.field = 6 OR resource.field <> 7 LIMIT 10");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
            Assert.assertEquals(query.getLimit().intValue(), 10);
            Assert.assertNotNull(query.getJoins());
            Assert.assertEquals(query.getJoins().size(), 1);
            Assert.assertTrue(query.getResource() instanceof QueryDynamicResource);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        try {
            Query query = Query.compile("SELECT * FROM resource JOIN (SELECT * FROM resource limit 10) as resource1 ON resource.id = resource1.id WHERE resource.field != 5 AND resource.field = 6 OR resource.field <> 7 LIMIT 10");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
            Assert.assertEquals(query.getLimit().intValue(), 10);
            Assert.assertNotNull(query.getJoins());
            Assert.assertEquals(query.getJoins().size(), 1);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail(ex.getMessage());
        }

        try {
            Query query = Query.compile("SELECT * FROM resource JOIN (SELECT * FROM resource limit 10).path as resource1 ON resource.id = resource1.id WHERE resource.field != 5 AND resource.field = 6 OR resource.field <> 7 LIMIT 10");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
            Assert.assertEquals(query.getLimit().intValue(), 10);
            Assert.assertNotNull(query.getJoins());
            Assert.assertEquals(query.getJoins().size(), 1);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testJoinCompile() {
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
    }
;
    @Test
    public void testWhereFunctions(){
        String q = "select * from store.dynamic.ccu.planning where fecFacturacion = parseDate('yyyy-MM-dd HH:mm:ss',dateFormat('2020-02-20 03:00:00','yyyy-MM-dd 00:00:00'))";
        Query query = Query.compile(q);
        System.out.println(query.toString());
        System.out.println();
    }

    @Test
    public void testTwoIfs() {
        String q = "select periodInMinutes(_lastUpdate, if(isNotNull(checkInDate), checkInDate, now())) as period,checkinTime,\n" +
                "if(isNotNull(checkInDate), checkInDate, '') as ingreso, checkInDate, _lastUpdate as checkOutDate,\n" +
                "if(isNotNull(photo1Id),concat(concat('<a href=\"',photo1Id),'\" target=\"_blank\"> ##image </a>'),'') as Foto1,\n" +
                "if(isNotNull(photo2Id),concat(concat('<a href=\"',photo2Id),'\" target=\"_blank\"> ##image </a>'),'') as Foto2,\n" +
                "consulta.CD as CD, planilla,patente,\n" +
                "consulta.codCamion as codCamion,consulta.codCarga as codCarga,\n" +
                "dateFormat('America/Santiago', _lastUpdate, 'dd/MM/yyyy HH:mm:ss') as horario,\n" +
                "if(plateManualEntry,'##keyboard','') as ingresoPatente,\n" +
                "if(planningManualEntry,'##keyboard','') as ingresoPlanilla,\n" +
                "if(isNotNull(platePhotoId),concat(concat('<a href=\"',platePhotoId),'\" target=\"_blank\"> ##image </a>'),'') as FotoPatente,\n" +
                "store.dynamic.ccu.checkout.loginCode as IMEI\n" +
                "from store.dynamic.ccu.checkout JOIN (select centroDistribucion as CD,\n" +
                "codCamion, codCarga, planilla as stringPlanilla\n" +
                "from store.dynamic.ccu.planilla\n" +
                "where centroDistribucion = 7 and _creationDate >= '2020-03-09 03:00:00' and _creationDate <= '2020-03-10 02:59:59') as consulta ON store.dynamic.ccu.checkout.planilla = consulta.stringPlanilla\n" +
                "left join\n" +
                "(select getMillisecondUnixEpoch(_creationDate) as checkinTime,\n" +
                "_creationDate as checkInDate,\n" +
                "planilla as checkinPlanilla\n" +
                "from store.dynamic.ccu.checkin where _creationDate >= '2020-03-09 03:00:00' and _creationDate <= '2020-03-10 02:59:59') as consultachkin on planilla = checkinPlanilla\n" +
                "where _creationDate >= '2020-03-09 03:00:00' and _creationDate <= '2020-03-10 02:59:59' order by _creationDate desc";

        Query query = Query.compile(q);
        System.out.println();
    }

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
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field != 5 AND resource.field = '2017-07-07 22:15:32' AND true AND someFunction()");
            query = Query.compile(query.toString());
            Assert.assertNotNull(query);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        try {
            Query query = Query.compile("SELECT * FROM dataaccumulator where perioddate >= '2017-08-01 00:00:00' and dataaccumulatorid = 'lastOdometerByDay' limit 100");
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

        try {
            Query query = Query.compile("SELECT  cliente.clienteid as clienteid ,  cliente.nombre as nombre ,  cliente.rubroid as rubroid ,  cliente.documentotipoid as documentotipoid ,  cliente.documentonro as documentonro ,\n" +
                    " cliente.direccionid as direccionid ,  cliente.clienteestadoid as clienteestadoid ,  cliente.clavepublica as clavepublica ,  cliente.clientetipoid as clientetipoid ,\n" +
                    " cliente.norestacredito as norestacredito ,  cliente.contid as contid ,  cliente.notifholderresponsable as notifholderresponsable ,  cliente.parquevehicular as parquevehicular ,\n" +
                    " cliente.listaprecioid as listaprecioid ,  cliente.descuento as descuento ,  cliente.mail as mail ,  cliente.grupo as grupo ,  pais.desc_es as pais ,  direccion.calle as calle ,\n" +
                    " direccion.localidad as localidad ,  direccion.provincia as provincia ,  direccion.cp as cp ,  clienteestado.desc_es as estado ,  rubro.desc_es as rubro ,  clientetipo.desc_es as tipo ,\n" +
                    " documentotipo.desc_es as documentotipo\n" +
                    " FROM cliente\n" +
                    " LEFT JOIN direccion  ON (direccion.direccionid=cliente.direccionid)\n" +
                    " LEFT JOIN pais  ON (pais.paisid=direccion.paisid)\n" +
                    " INNER JOIN clienteestado  ON (clienteestado.clienteestadoid=cliente.clienteestadoid)\n" +
                    " INNER JOIN rubro  ON (rubro.rubroid=cliente.rubroid)\n" +
                    " INNER JOIN clientetipo  ON (clientetipo.clientetipoid=cliente.clientetipoid)\n" +
                    " INNER JOIN documentotipo  ON (documentotipo.documentotipoid=cliente.documentotipoid)\n" +
                    " WHERE (  cliente.clienteid = 309 )");
            System.out.println();
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        try {
            Query.compile("SELECT  clientedeuda.fechavencimiento as fechavencimiento ,  clientedeuda.comprobantetipo as comprobantetipo ,  clientedeuda.comprobantenro as comprobantenro ,  clientedeuda.importe as importe ,  moneda.desc_es as desc_es\n" +
                    " FROM clientedeuda INNER JOIN moneda  ON (moneda.monedaid=clientedeuda.monedaid)\n" +
                    " INNER JOIN cliente  ON (cliente.clienteid=clientedeuda.clienteid)   WHERE ( clientedeuda.clienteid = 309 )");
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
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRightValue() instanceof Number);
        } catch (Exception ex) {
            Assert.fail("Unable to decode integer number");
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = 5.3");
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRightValue() instanceof Number);
        } catch (Exception ex) {
            Assert.fail("Unable to decode decimal number");
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = -0.00023");
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRightValue() instanceof Number);
        } catch (Exception ex) {
            Assert.fail("Unable to decode negative decimal number");
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = -2.3E-4");
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRightValue() instanceof Number);
        } catch (Exception ex) {
            Assert.fail("Unable to decode negative scientific number with negative exponent");
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = -2.3E4");
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRightValue() instanceof Number);
        } catch (Exception ex) {
            Assert.fail("Unable to decode negative scientific number");
        }

        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = 2.3E4");
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRightValue() instanceof Number);
        } catch (Exception ex) {
            Assert.fail("Unable to decode scientific number");
        }
    }

    /**
     * Test uuid type
     */
    @Test
    public void testCompileNumericUUIDType() {
        try {
            Query query = Query.compile("SELECT * FROM resource WHERE resource.field = 2821c2b9-c485-4550-8dd8-6ec83033fa84");
            Assert.assertTrue(((FieldEvaluator) query.getEvaluators().iterator().next()).getRightValue() instanceof UUID);
        } catch (Exception ex) {
            Assert.fail("Unable to decode UUID data type");
        }
    }

    @Test
    public void testCompileCache() {
        long startTime = System.currentTimeMillis();
        Query query1 = Query.compile("SELECT * FROM resource50", false);
        long firstTime = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        Query query2 = Query.compile("SELECT * FROM resource50", false);
        long secondTime = System.currentTimeMillis() - startTime;

        System.out.printf("First time: %d \r\n", firstTime);
        System.out.printf("Second time: %d \r\n", secondTime);
        Assert.assertTrue(query1 == query2);
    }

    @Test
    public void testUnderlyingValues() {
        Query query = Query.compile("SELECT * FROM resource WHERE resource.field = 2821c2b9-c485-4550-8dd8-6ec83033fa84 limit 2, 50");
        Assert.assertEquals(query.getLimit().intValue(), 2);
        Assert.assertEquals(query.getUnderlyingLimit().intValue(), 50);

        query = Query.compile("SELECT * FROM resource WHERE resource.field = 2821c2b9-c485-4550-8dd8-6ec83033fa84 limit 2");
        Assert.assertEquals(query.getLimit().intValue(), 2);
        Assert.assertNull(query.getUnderlyingLimit());

        query = Query.compile("SELECT * FROM resource WHERE resource.field = 2821c2b9-c485-4550-8dd8-6ec83033fa84 limit ,50");
        Assert.assertEquals(query.getUnderlyingLimit().intValue(), 50);
        Assert.assertNull(query.getLimit());
    }

}
