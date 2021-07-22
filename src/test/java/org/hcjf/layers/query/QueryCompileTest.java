package org.hcjf.layers.query;

import org.hcjf.layers.query.evaluators.FieldEvaluator;
import org.hcjf.layers.query.model.QueryDynamicResource;
import org.hcjf.utils.Strings;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * @author javaito
 */
public class QueryCompileTest {

    @Test
    public void testLiteral() {
        String queryAsString = "SELECT 'hola' as value, field, 5 as number FROM resource";
        Query query = Query.compile(queryAsString);
        System.out.println();
    }

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
        Query query1 = Query.compile(query.toString());
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

    @Test
    public void testSubqueryIntoSelect() {
        Query query = Query.compile("select (select type,geometry,properties from disjointResultSet) as zona, newMap('features',newArray(zona)) as zona, celularConductor,loginCode,\n" +
                "disjointResultSet.0.numeroClientes as numeroClientes, disjointResultSet.0.centroDistribucion as centroDistribucion, disjointResultSet.0.fletero as fletero, disjointResultSet.0.codCamion as codCamion, disjointResultSet.0.usuarioCamion as usuarioCamion\n" +
                "from (select *\n" +
                "from (select  new('Feature') as type, newMap('') as properties, bboxBuffer as geometry, numeroClientes, centroDistribucion, fletero, codCamion, usuarioCamion\n" +
                "from (select if(equals(latitudMin,latitudMax),geoNew(concat('POINT(',longitudMin,' ',latitudMin,')')),geoNew(concat('POLYGON((',longitudMin,' ',latitudMin,',', longitudMax,' ',latitudMin,',',longitudMax,' ',latitudMax,',',longitudMin,' ',latitudMax,'))'))) as bbox,\n" +
                "geoBuffer(bbox,0.002) as bboxBuffer, numeroClientes, centroDistribucion, fletero, codCamion, usuarioCamion\n" +
                "from (select disjointResultSet,(select latitud from disjointResultSet where latitud!=0 and latitud!=null order by latitud desc) as latitudMax, first(latitudMax) as latitudMax, latitudMax.latitud as latitudMax,\n" +
                "(select latitud from disjointResultSet where latitud!=0 and latitud!=null order by latitud asc) as latitudMin, first(latitudMin) as latitudMin, latitudMin.latitud as latitudMin,\n" +
                "(select longitud from disjointResultSet where longitud!=0 and latitud!=null order by longitud desc) as longitudMax, first(longitudMax) as longitudMax, longitudMax.longitud as longitudMax,\n" +
                "(select longitud from disjointResultSet where longitud!=0 and latitud!=null order by longitud asc) as longitudMin, first(longitudMin) as longitudMin, longitudMin.longitud as longitudMin,\n" +
                "disjointResultSet.0.numeroClientes as numeroClientes, disjointResultSet.0.centroDistribucion as centroDistribucion, disjointResultSet.0.fletero as fletero, disjointResultSet.0.codCamion as codCamion, disjointResultSet.0.usuarioCamion as usuarioCamion\n" +
                "from (select latitud, longitud, numeroClientes, centroDistribucion, fletero, codCamion, usuarioCamion\n" +
                "from (select clientes, put('clientes','numeroClientes',numeroClientes), put('clientes','centroDistribucion',centroDistribucion), put('clientes','fletero',fletero), put('clientes','codCamion',codCamion), put('clientes','usuarioCamion',usuarioCamion)\n" +
                "from store.dynamic.ccu.lastplanilla where planilla=17774481).clientes as data disjoint by a) as data) as dato) as data2) as data3 disjoint by a) as data4\n" +
                "full join (select celularConductor, loginCode from store.dynamic.ccu.planilla where planilla = 17774481 and rollbackDate = null) as data5 on true");

        Query query1 = Query.compile(query.toString());

        System.out.println();
    }

    @Test
    public void testTextResource() {
        Query query = Query.compile("SELECT * FROM 'file://path/file.ext' AS file");
        System.out.println();
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

    @Test
    public void testReplaceValues() {
        Query query = Query.compile("select if(isNull(store.dynamic.ccu.checkin._creationDate),0,periodInMilliseconds(store.dynamic.ccu.checkin._creationDate, ?)/3600000) as tiempo " +
                "from store.dynamic.ccu.checkin where patente = ? and" +
                " parseDate('yyyy-MM-dd HH:mm:ss',dateFormat(store.dynamic.ccu.checkin._creationDate,'yyyy-MM-dd 00:00:00')) = parseDate('yyyy-MM-dd HH:mm:ss',dateFormat(?,'yyyy-MM-dd 00:00:00')) and" +
                " store.dynamic.ccu.checkin._creationDate < ? order by store.dynamic.ccu.checkin._creationDate desc limit ,1");
        System.out.println();
    }

    @Test
    public void testUnprocessedValuesIntoSelect() {
        Query query = Query.compile("SELECT field1, (SELECT * FROM field WHERE id = 1) as something FROM Resource");
        System.out.println();
    }

    @Test
    public void testQueryWithoutFrom() {
        Query query = Query.compile("SELECT * FROM __new__");
        System.out.println();
    }

    @Test
    public void testLongCompilation() {
        Query query = Query.compile("select * from (select data2.checkOutDate as checkOutDate, data2.checkInDate as checkInDate, data2.phoneMatch as phoneMatch, vueltasRealizadas, fecha, new(18) as zoom, lat, lng, data3.vueltasProgramadas as vueltasProgramadas, zonaRiesgo,  CD, baseTransportista, " +
                "concat(vueltasRealizadas,'/',vueltasProgramadas) as vueltas, if(isNull(fecha),true,false) as estado, data2.codCamion as codCamion, distinct(store.dynamic.ccu.checkout.planilla), data2.codCarga as codCarga, " +
                "store.dynamic.ccu.checkout.planilla as planilla, store.dynamic.ccu.checkout.patente as patente, data4.zonaPeligrosa as zonaPeligrosa, " +
                "data2.celularConductor as celularConductor, if(isNotNull(data2.checkOutDate), dateFormat(data2.checkOutDate,'America/Santiago','HH:mm:ss'),'') as salida, " +
                "if(isNull(data4.clientesVisitados),concat('0/',data4.numeroClientes),concat(data4.clientesVisitados,'/',data4.numeroClientes)) as numeroClientes, " +
                "numberFormat('#0.00',periodInMilliseconds(data2.checkOutDate,now())/3600000) as duracion " +
                "from store.dynamic.ccu.checkout " +
                "join (select aggregateContext(first(patente)) as plate,aggregateContext(size(patente)) as vueltasRealizadas from store.dynamic.ccu.checkout where _creationDate > parseDate('yyyy-MM-dd HH:mm:ss',dateFormat(now(),'America/Santiago','yyyy-MM-dd 00:00:00')) and centroDistribucion=8 group by patente) as data1 on store.dynamic.ccu.checkout.patente = data1.plate " +
                "join (select checkOutDate, checkInDate, phoneMatch, codCamion, codCarga, celularConductor, planilla as planillaId from store.dynamic.ccu.planilla where _creationDate > parseDate('yyyy-MM-dd HH:mm:ss',dateFormat(now(),'America/Santiago','yyyy-MM-dd 04:00:00')) and isNull(rollbackDate) and centroDistribucion=8 and isNull(checkInDate)) as data2 on store.dynamic.ccu.checkout.planilla=data2.planillaId " +
                "left join (select aggregateContext(first(codCamion)) as codigoCamion,aggregateContext(size(codCamion)) as vueltasProgramadas from store.dynamic.ccu.lastplanilla where fecFacturacion > parseDate('yyyy-MM-dd HH:mm:ss',dateFormat(toDate(minusDays(now(),2)),'America/Santiago','yyyy-MM-dd 00:00:00')) and centroDistribucion=8 group by codCamion) as data3 on data2.codCamion = data3.codigoCamion " +
                "left join (select numeroClientes, clientesVisitados, numeroClientesZonaPeligrosa, concat(numeroClientesZonaPeligrosa,'-',numberFormat('#0.0',doubleValue(numeroClientesZonaPeligrosa)/doubleValue(numeroClientes)*doubleValue(100)),'%') as zonaPeligrosa, if(isNull(data4.clientesVisitados),concat('0/',data4.numeroClientes),concat(data4.clientesVisitados,'/',data4.numeroClientes)) as zp, planilla from store.dynamic.ccu.lastplanilla where planilla in (select planilla from store.dynamic.ccu.checkout where centroDistribucion=8 and _creationDate>parseDate('yyyy-MM-dd HH:mm:ss',dateFormat(now(),'America/Santiago','yyyy-MM-dd 04:00:00')))) as data4 on store.dynamic.ccu.checkout.planilla=data4.planilla " +
                "left join (select lat, lng, num_plate, dateFormat(time,'America/Santiago','HH:mm:ss') as fecha, if(isNull(nameZonaRiesgo), false,true) as zonaRiesgo, if(isNull(nameCentroDistribucion), false,true) as CD, if(isNull(nameBaseTransportista), false,true) as baseTransportista FROM time.series.dynamic.ccu.report where periodStart = parseDate('yyyy-MM-dd HH:mm:ss',dateFormat(now(),'America/Santiago','yyyy-MM-dd 04:00:00')) and " +
                "periodEnd = parseDate('yyyy-MM-dd HH:mm:ss',dateFormat(toDate(plusDays(now(),1)),'America/Santiago','yyyy-MM-dd 04:00:00')) and periodGroupedBy = 'num_plate' and onlyLastGroup = true) as data5 " +
                "on store.dynamic.ccu.checkout.patente=data5.num_plate " +
                "where store.dynamic.ccu.checkout._creationDate > parseDate('yyyy-MM-dd HH:mm:ss',dateFormat(now(),'America/Santiago','yyyy-MM-dd 04:00:00')) and store.dynamic.ccu.checkout.centroDistribucion=8" +
                "order by store.dynamic.ccu.checkout._creationDate desc) as data");

        System.out.println();

        query = Query.compile("select * from (select replace(message,'dispositivo ','dispositivo:') as mensaje, replace(mensaje,'{\"message\":\"$@{IMPL,EDNA-KRABAPPEL}','::::::::EDNA-KRABAPPEL (notifique a proveedor):') as mensaje,\n" +
                "replace(mensaje,'<',':') as mensaje, replace(mensaje,'>',':') as mensaje, split(mensaje,':') as mensaje, aggregateContext(if(size(mensaje)>3,mensaje.8,mensaje.0)) as mensaje \n" +
                "from store.dynamic.ccu.error.report where _creationDate >= '2020-08-12 04:00:00' and _creationDate < parseDate('yyyy-MM-dd HH:mm:ss',dateFormat(toDate(plusDays('2020-08-12 04:00:00',1)),'yyyy-MM-dd 04:00:00'))) as dat");

        System.out.println();
    }

    @Test
    public void testUnionCompilation() {
        Query query = Query.compile("SELECT * FROM resource UNION SELECT * from resource2");
        System.out.println();

        query = Query.compile("SELECT  cliente.clienteid as clienteid ,  cliente.nombre as nombre ,  cliente.rubroid as rubroid ,  cliente.documentotipoid as documentotipoid ,  cliente.documentonro as documentonro ,\n" +
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
                " WHERE (  cliente.clienteid = 309 ) " +
                " UNION " +
                " SELECT  cliente.clienteid as clienteid ,  cliente.nombre as nombre ,  cliente.rubroid as rubroid ,  cliente.documentotipoid as documentotipoid ,  cliente.documentonro as documentonro ,\n" +
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
    }

    @Test
    public void subParameterizedQueryTest() {
        String sql = "SELECT * FROM (SELECT * FROM character WHERE name like ?) as hc where lastName like ?";
        Query query = Query.compile(sql);
        System.out.println();

        sql = "select if(equals(isDisabled,true),'',patente) as suerte, instanceOf(isDisabled), * from (select distinct(store.dynamic.ccu.patente.patente), store.dynamic.ccu.patente.patente as patente,\n" +
                "if(equals(store.dynamic.ccu.camion.fleteroAsignado,0),store.dynamic.ccu.patente.patente,if(false,if(equals(store.dynamic.ccu.camion.patente,'FJHG20'),store.dynamic.ccu.patente.patente,concat(store.dynamic.ccu.patente.patente,' - Asignada a ',fleteroAsignado)),concat(store.dynamic.ccu.patente.patente,' - Asignada a ',fleteroAsignado))) as patenteStatus,\n" +
                "if(equals(store.dynamic.ccu.camion.fleteroAsignado,0),false,if(false,if(equals(store.dynamic.ccu.camion.patente,'FJHG20'),false,true),if(equals(store.dynamic.ccu.camion.patente,'FJHG20'),true,false))) as isDisabled\n" +
                "from store.dynamic.ccu.patente join store.dynamic.ccu.camion on store.dynamic.ccu.patente.patente=store.dynamic.ccu.camion.patente \n" +
                "where store.dynamic.ccu.patente.fletero=96022 and store.dynamic.ccu.patente.pallets=6 and store.dynamic.ccu.patente.patente like '') as data";
        query = Query.compile(sql);
        System.out.println();
    }

    @Test
    public void conditionalReturnValue() {
        String sql = "SELECT (equals(name,'javaito') and isNull(lastName)) as thisIsJavaito FROM (SELECT * FROM character WHERE name like ?) as hc where lastName like ?";
        Query query = Query.compile(sql);
        System.out.println();
    }

    @Test
    public void unionInSubQueryTest() {
        String sql = "select * from (select planilla, patente, enteredDate, loginCode, _creationDate, _lastUpdate, rollbackDate, rollbackSource, checkOutDate, checkInDate, new('VIRTUAL TOTEM') as source, new('<h6 align=\"center\" style=\"margin-top:10px;font-size:15px\">') as tagInicial, new('</h6>') as tagFinal, _eventTracking.eventTriggerId as eventTriggerId, id as idRegistro from store.dynamic.ccu.planilla where planilla=800743756 union \n" +
                "select planilla, patente, enteredDate, loginCode, _creationDate, _lastUpdate, new('CHECK OUT') as source, new('<h6 align=\"center\" style=\"margin-top:10px;font-size:15px\">') as tagInicial, new('</h6>') as tagFinal, _eventTracking.eventTriggerId as eventTriggerId, id as idRegistro from store.dynamic.ccu.checkout where planilla=800743756 union\n" +
                "select planilla, patente, enteredDate, loginCode, _creationDate, _lastUpdate, new('CHECK IN') as source, new('<h6 align=\"center\" style=\"margin-top:10px;font-size:15px\">') as tagInicial, new('</h6>') as tagFinal, plateChanged, _eventTracking.eventTriggerId as eventTriggerId, id as idRegistro from store.dynamic.ccu.checkin where planilla=800743756) as data";
        Query query = Query.compile(sql);
        System.out.println();
    }

    @Test
    public void jsonResourceQueryTest() {
        String sql = "select * from '[{\"field1\":2}]' as resource";
        Query query = Query.compile(sql);
        System.out.println();
    }

    @Test
    public void environmentQueryTest() {
        String sql = "environment '{\"field\":4}' select * from '[{\"field1\":2}]' as resource";
        Query query = Query.compile(sql);
        System.out.println();
    }

    @Test
    public void testCompileQueryWithEnvironment() {
        String sql = "environment '{\"field\":4}' select * from '[{\"field\":2}, {\"field\":4}]' as resource where field = $field";
        Query query = Query.compile(sql);
        System.out.println();
    }

    @Test
    public void queryWithTwoSubQueries() {
        String sql = "select zona as geometry, new('#ff0000') as fillColor, new('#b80004') as strokeColor, toUpperCase(nombre) as label from store.dynamic.ccu.zona.riesgo where region = (select region from store.dynamic.ccu.centro.distribucion where codigo=(select centroDistribucion from store.dynamic.ccu.lastplanilla where planilla=7799086))";
        Query query = Query.compile(sql);
        System.out.println();
    }
}
