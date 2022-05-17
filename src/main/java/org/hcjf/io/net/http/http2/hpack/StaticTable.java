package org.hcjf.io.net.http.http2.hpack;

import org.hcjf.io.net.http.HttpHeader;
import org.hcjf.utils.Strings;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticTable {

    private static final List<HttpHeader> STATIC_TABLE = Arrays.asList(
            new HttpHeader(":authority", Strings.EMPTY_STRING),
            new HttpHeader(":method", "GET"),
            new HttpHeader(":method", "POST"),
            new HttpHeader(":path", "/"),
            new HttpHeader(":path", "/index.html"),
            new HttpHeader(":scheme", "http"),
            new HttpHeader(":scheme", "https"),
            new HttpHeader(":status", "200"),
            new HttpHeader(":status", "204"),
            new HttpHeader(":status", "206"),
            new HttpHeader(":status", "304"),
            new HttpHeader(":status", "400"),
            new HttpHeader(":status", "404"),
            new HttpHeader(":status", "500"),
            new HttpHeader("accept-charset", Strings.EMPTY_STRING),
            new HttpHeader("accept-encoding", "gzip, deflate"),
            new HttpHeader("accept-language", Strings.EMPTY_STRING),
            new HttpHeader("accept-ranges", Strings.EMPTY_STRING),
            new HttpHeader("accept", Strings.EMPTY_STRING),
            new HttpHeader("access-control-allow-origin", Strings.EMPTY_STRING),
            new HttpHeader("age", Strings.EMPTY_STRING),
            new HttpHeader("allow", Strings.EMPTY_STRING),
            new HttpHeader("authorization", Strings.EMPTY_STRING),
            new HttpHeader("cache-control", Strings.EMPTY_STRING),
            new HttpHeader("content-disposition", Strings.EMPTY_STRING),
            new HttpHeader("content-encoding", Strings.EMPTY_STRING),
            new HttpHeader("content-language", Strings.EMPTY_STRING),
            new HttpHeader("content-length", Strings.EMPTY_STRING),
            new HttpHeader("content-location", Strings.EMPTY_STRING),
            new HttpHeader("content-range", Strings.EMPTY_STRING),
            new HttpHeader("content-type", Strings.EMPTY_STRING),
            new HttpHeader("cookie", Strings.EMPTY_STRING),
            new HttpHeader("date", Strings.EMPTY_STRING),
            new HttpHeader("etag", Strings.EMPTY_STRING),
            new HttpHeader("expect", Strings.EMPTY_STRING),
            new HttpHeader("expires", Strings.EMPTY_STRING),
            new HttpHeader("from", Strings.EMPTY_STRING),
            new HttpHeader("host", Strings.EMPTY_STRING),
            new HttpHeader("if-match", Strings.EMPTY_STRING),
            new HttpHeader("if-modified-since", Strings.EMPTY_STRING),
            new HttpHeader("if-none-match", Strings.EMPTY_STRING),
            new HttpHeader("if-range", Strings.EMPTY_STRING),
            new HttpHeader("if-unmodified-since", Strings.EMPTY_STRING),
            new HttpHeader("last-modified", Strings.EMPTY_STRING),
            new HttpHeader("link", Strings.EMPTY_STRING),
            new HttpHeader("location", Strings.EMPTY_STRING),
            new HttpHeader("max-forwards", Strings.EMPTY_STRING),
            new HttpHeader("proxy-authenticate", Strings.EMPTY_STRING),
            new HttpHeader("proxy-authorization", Strings.EMPTY_STRING),
            new HttpHeader("range", Strings.EMPTY_STRING),
            new HttpHeader("referer", Strings.EMPTY_STRING),
            new HttpHeader("refresh", Strings.EMPTY_STRING),
            new HttpHeader("retry-after", Strings.EMPTY_STRING),
            new HttpHeader("server", Strings.EMPTY_STRING),
            new HttpHeader("set-cookie", Strings.EMPTY_STRING),
            new HttpHeader("strict-transport-security", Strings.EMPTY_STRING),
            new HttpHeader("transfer-encoding", Strings.EMPTY_STRING),
            new HttpHeader("user-agent", Strings.EMPTY_STRING),
            new HttpHeader("vary", Strings.EMPTY_STRING),
            new HttpHeader("via", Strings.EMPTY_STRING),
            new HttpHeader("www-authenticate", Strings.EMPTY_STRING)
    );

    private static final Map<String, Integer> STATIC_INDEX_BY_NAME = createMap();

    /**
     * The number of header fields in the static table.
     */
    static final int length = STATIC_TABLE.size();

    /**
     * Return the header field at the given index value.
     */
    static HttpHeader getEntry(int index) {
        return STATIC_TABLE.get(index - 1);
    }

    /**
     * Returns the lowest index value for the given header field name in the static table.
     * Returns -1 if the header field name is not in the static table.
     */
    static Integer getIndex(byte[] name) {
        String nameString = new String(name, Charset.defaultCharset());
        return STATIC_INDEX_BY_NAME.get(nameString);
    }

    /**
     * Returns the index value for the given header field in the static table.
     * Returns -1 if the header field is not in the static table.
     */
    static Integer getIndex(byte[] name, byte[] value) {
        Integer result = null;
        Integer index = getIndex(name);

        if(index != null) {
            // Note this assumes all entries for a given header field are sequential.
            String nameString = new String(name, Charset.defaultCharset());
            String valueString = new String(value, Charset.defaultCharset());
            while (index <= length) {
                HttpHeader header = getEntry(index);
                if (!header.getHeaderName().equals(nameString)) {
                    break;
                }
                if (header.getHeaderValue().equals(valueString)) {
                    result = index;
                    break;
                }
                index++;
            }
        }

        return result;
    }

    // create a map of header name to index value to allow quick lookup
    private static Map<String, Integer> createMap() {
        Map<String, Integer> result = new HashMap<>();
        // Iterate through the static table in reverse order to
        // save the smallest index for a given name in the map.
        for (int index = length; index > 0; index--) {
            HttpHeader header = getEntry(index);
            result.put(header.getHeaderName(), index);
        }
        return result;
    }
}
