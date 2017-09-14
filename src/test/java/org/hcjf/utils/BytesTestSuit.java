package org.hcjf.utils;

import org.hcjf.io.net.http.HttpHeader;
import org.junit.Test;

import java.util.List;

/**
 * @author javaito
 * @email javaito@gmail.com
 */
public class BytesTestSuit {

    @Test
    public void test() {
        byte[] value = ("------WebKitFormBoundarypNGoPl2J1EhtqPpS\n" +
                "Content-Disposition: form-data; name=\"page\"\n" +
                "\n" +
                "tripSavePage\n" +
                "------WebKitFormBoundarypNGoPl2J1EhtqPpS\n" +
                "Content-Disposition: form-data; name=\"checkpoints\"\n" +
                "\n" +
                "[{\"index\":0,\"layerName\":\"4141 Zonas -Mirtrans Uruguay SA \",\"zoneId\":\"585723\",\"zoneName\":\"PEPSICO TASA \",\"type\":\"Punto de Interés\",\"dateStart\":\"2017-09-05 10:16:26\",\"dateEnd\":\"2017-09-07 10:16:28\",\"notifyContactValue\":true,\"notifyContact\":\"<i class='fa fa-check-square-o'></i>\",\"upload\":\"<label class='btn btn-default'><input name='files_585723' type='file'></label>\",\"action\":\"<a class='btn btn-danger' onclick='removeData(0)'><i class='fa fa-trash-o'></i></a>\"}]\n" +
                "------WebKitFormBoundarypNGoPl2J1EhtqPpS\n" +
                "Content-Disposition: form-data; name=\"route\"\n" +
                "\n" +
                "\n" +
                "------WebKitFormBoundarypNGoPl2J1EhtqPpS\n" +
                "Content-Disposition: form-data; name=\"tripName\"\n" +
                "\n" +
                "dfgdfg\n" +
                "------WebKitFormBoundarypNGoPl2J1EhtqPpS\n" +
                "Content-Disposition: form-data; name=\"vehicleid\"\n" +
                "\n" +
                "123\n" +
                "------WebKitFormBoundarypNGoPl2J1EhtqPpS\n" +
                "Content-Disposition: form-data; name=\"files_585723\"; filename=\"matias.txt\"\n" +
                "Content-Type: text/plain\n" +
                "\n" +
                "hola\n" +
                "\n" +
                "------WebKitFormBoundarypNGoPl2J1EhtqPpS--").getBytes();

        byte[] key = "------WebKitFormBoundarypNGoPl2J1EhtqPpS".getBytes();


        HttpHeader header = new HttpHeader("Content-Disposition: form-data; name=\"files_585723\"; filename=\"matias.txt\"");
        List<byte[]> result = Bytes.split(value, key);
        List<Integer> indexes = Bytes.allIndexOf(value, key, 0, Integer.MAX_VALUE);
    }

}
