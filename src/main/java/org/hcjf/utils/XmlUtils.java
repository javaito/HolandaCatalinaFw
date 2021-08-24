package org.hcjf.utils;

import org.hcjf.encoding.MimeType;
import org.hcjf.errors.HCJFRuntimeException;
import org.hcjf.io.net.http.*;
import org.hcjf.layers.query.Query;
import org.hcjf.layers.query.Queryable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XmlUtils {

    private static final class Fields {
        private static final String VERSION = "_version";
        private static final String VALUE = "_value";
        private static final String HAS_CHILDREN = "__¿HAS_CHILDREN¿__";
        private static final String CLOSED = "__¿CLOSED¿__";
        private static final String TOKEN_FORMAT = "¿%d·";
        private static final String COMMENT_START = "<!--";
        private static final String COMMENT_END = "-->";
        private static final String CDATA_START = "<![CDATA[";
        private static final String CDATA_END = "]]>";
        private static final String CDATA = "__¿CDATA__%d¿__";
    }

    private static final class Patterns {
        private static final String OPEN_TAG = "<%s%s>";
        private static final String CLOSE_TAG = "</%s>";
        private static final String ATTRIBUTE = " %s=\"%s\"";
        private static final String CDATA = "<![CDATA[%s]]>";
        private static final String ATTRIBUTE_GROUP_NAME = "attribute";
        private static final Pattern TAG_ATTRIBUTES_PATTERN = Pattern.compile("(?<attribute>[a-zA-Z0-9_\\-]+[ ]*=[ ]*\"[a-zA-Z0-9_\\-.:/+*#$%& ]+\"[ ]*)");
    }

    public static void main(String[] args) throws Exception {
//        String regex = "(?<attribute>[a-zA-Z0-9_\\-]{1,}[ ]{0,}\\=[ ]{0,}\\\"[a-zA-Z0-9_\\- ]{1,}\\\"[ ]{0,})";
//        String value1 = "field=\"valu  AAe\"      field=\"valu  BBe\"     field=\"valu  CCe\"";
//
//        Pattern pattern = Pattern.compile("(?<attribute>[a-zA-Z0-9_\\-]+[ ]*=[ ]*\"[a-zA-Z0-9_\\- ]+\"[ ]*)");
//        Matcher matcher = pattern.matcher(value1);
//        String[] s = pattern.split(value1);
////        boolean match = matcher.matches();
//        boolean find = matcher.find();
//        int start = 0;
//        while(matcher.find(start)) {
//            System.out.println(matcher.group("attribute"));
//            start = matcher.end();
//        }
//        System.out.println();


        String xmlFile = Files.readString(Path.of("/", "home", "javaito", "Descargas", "mza_01.osm"));
//        String xmlFile = Files.readString(Path.of("/", "home", "javaito", "Descargas", "zapata.osm"));
        Map<String,Object> value = parse(xmlFile);
        Files.write(Path.of("/", "home", "javaito", "Descargas", "mza_01.json"), JsonUtils.toJsonTree(value).toString().getBytes(StandardCharsets.UTF_8));
//        System.out.println(JsonUtils.toJsonTree(value).toString());

        Queryable.DataSource<Map<String,Object>> ds = new Queryable.DataSource<Map<String, Object>>() {

            private Collection<Map<String,Object>> nodes = Introspection.resolve(value, "osm.node");
            private Collection<Map<String,Object>> ways = Introspection.resolve(value, "osm.way");
            private Collection<Map<String,Object>> relation = Introspection.resolve(value, "osm.relation");

            @Override
            public Collection<Map<String, Object>> getResourceData(Queryable queryable) {
                Collection<Map<String, Object>> result;
                String resourceName = queryable.getResourceName();
                if(resourceName.equals("node")) {
                    result = queryable.evaluate(nodes);
                } else if(resourceName.equals("way")) {
                    result = queryable.evaluate(ways);
                } else if(resourceName.equals("relation")) {
                    result = queryable.evaluate(relation);
                } else {
                    throw new HCJFRuntimeException("Resource not found: %s", resourceName);
                }
                return result;
            }
        };

        HttpServer server = new HttpServer(9132);
        server.addContext(new Context(".*") {
            @Override
            public HttpResponse onContext(HttpRequest request) {
                String query = Introspection.resolve(request.getParameters(), "q");
                Queryable queryable = Query.compile(query);
                Collection<Map<String,Object>> result = queryable.evaluate(ds);
                byte[] body = JsonUtils.toJsonTree(result).toString().getBytes(StandardCharsets.UTF_8);
                HttpResponse response = new HttpResponse();
                response.setResponseCode(HttpResponseCode.OK);
                response.addHeader(new HttpHeader(HttpHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON.toString()));
                response.addHeader(new HttpHeader(HttpHeader.CONTENT_LENGTH, Integer.toString(body.length)));
                response.setBody(body);
                return response;
            }
        });
        server.start();
    }

    /**
     * Parse the xml file and creates a map representation with all the information.
     * @param xml XML file to parse.
     * @return Map instance with all the information of the xml file.
     */
    public static Map<String,Object> parse(String xml) {
        //Removes all the comments into the xml file
        List<String> commentGroups = Strings.replaceableGroup(xml, Fields.COMMENT_START, Fields.COMMENT_END);
        String uncommentedXml = commentGroups.get(commentGroups.size() - 1);
        for (int i = 0; i < commentGroups.size() - 1; i++) {
            uncommentedXml = uncommentedXml.replace(String.format(Fields.TOKEN_FORMAT, i), Strings.EMPTY_STRING);
        }

        Map<String,String> cdataValues = new HashMap<>();
        List<String> cdataGroups = Strings.replaceableGroup(uncommentedXml, Fields.CDATA_START, Fields.CDATA_END);
        String cleanXml = cdataGroups.get(cdataGroups.size() - 1);
        for (int i = 0; i < cdataGroups.size() - 1; i++) {
            String cdataKey = String.format(Fields.CDATA, i);
            cdataValues.put(cdataKey, cdataGroups.get(i));
            cleanXml = cleanXml.replace(String.format(Fields.TOKEN_FORMAT, i), cdataKey);
        }

        List<String> groups = Strings.group(cleanXml, Strings.START_TAG, Strings.END_TAG, false, false);
        AtomicInteger index = new AtomicInteger(groups.size() - 1);
        Map<String, Object> result = new HashMap<>();
        do {
            result.putAll(parse(new HashMap<>(), groups, index, new AtomicInteger(0), cdataValues));
            index.decrementAndGet();
        } while (index.get() >= 0);
        result.remove(Fields.HAS_CHILDREN);
        return result;
    }

    private static Map<String,Object> parse(Map<String,Object> currentObject, List<String> groups, AtomicInteger index, AtomicInteger lastIndexOf, Map<String,String> cdataValues) {
        String currentTag = groups.get(index.get());
        if(currentTag.startsWith(Strings.SLASH)) {
            //In this case the we found the closing tag
            if(!currentObject.containsKey(Fields.HAS_CHILDREN)) {
                Integer currentIndex = index.get();
                currentObject.put(Fields.VALUE, getValue(groups,currentIndex + 1, currentIndex, lastIndexOf, cdataValues));
            } else {
                currentObject.remove(Fields.HAS_CHILDREN);
            }
            currentObject.put(Fields.CLOSED, true);
        } else if(currentTag.endsWith(Strings.SLASH)) {
            // In this case we found the inline-tag
            currentTag = currentTag.replace(Strings.SLASH, Strings.EMPTY_STRING);
            String tagName = getName(currentTag);
            Map<String, Object> child = getAttributes(currentTag);
            currentObject = addChild(currentObject, tagName, child);
        } else if(currentTag.startsWith(Strings.QUESTION)) {
            index.decrementAndGet();
            currentObject = parse(currentObject, groups, index, lastIndexOf, cdataValues);
        } else {
            // In this case we found the opening tag
            String tagName = getName(currentTag);
            Map<String, Object> child = getAttributes(currentTag);
            do {
                index.decrementAndGet();
                child = parse(child, groups, index, lastIndexOf, cdataValues);
            } while (!child.containsKey(Fields.CLOSED));
            child.remove(Fields.CLOSED);

            currentObject = addChild(currentObject, tagName, child);
            currentObject.put(Fields.HAS_CHILDREN, true);

        }
        return currentObject;
    }

    private static Map<String,Object> addChild(Map<String,Object> currentObject, String tagName, Map<String,Object> child) {
        Object childToAdd = child;
        if(child.size() == 1 && child.containsKey(Fields.VALUE)) {
            childToAdd = child.get(Fields.VALUE);
        }
        if(currentObject.containsKey(tagName)) {
            Object value = currentObject.get(tagName);
            if(value instanceof List) {
                ((List)value).add(childToAdd);
            } else {
                List<Object> list = new ArrayList<>();
                list.add(value);
                list.add(childToAdd);
                currentObject.put(tagName, list);
            }
        } else {
            currentObject.put(tagName, childToAdd);
        }
        return currentObject;
    }

    private static String getName(String tag) {
        String[] tagParts = tag.split(Strings.WHITE_SPACE);
        return tagParts[0].trim();
    }

    private static Map<String,Object> getAttributes(String tag) {
        Matcher matcher = Patterns.TAG_ATTRIBUTES_PATTERN.matcher(tag);
        Map<String,Object> result = new HashMap<>();
        int start = 0;
        while(matcher.find(start)) {
            String attribute = matcher.group(Patterns.ATTRIBUTE_GROUP_NAME).trim();
            String[] keyValue = attribute.split(Strings.ASSIGNATION);
            if(keyValue.length == 1) {
                result.put(keyValue[0], null);
            } else {
                result.put(keyValue[0], Strings.deductInstance(keyValue[1].substring(1, keyValue[1].length() - 1)));
            }
            start = matcher.end();
        }
        return result;
    }

    private static Object getValue(List<String> groups, int startIndex, int endIndex, AtomicInteger lastIndexOf, Map<String,String> cdataValues) {
//        String startToken = String.format(Fields.TOKEN_FORMAT, startIndex);
//        String endToken = String.format(Fields.TOKEN_FORMAT, endIndex);
//        String lastGroup = groups.get(groups.size()-1);
//        String result = lastGroup.substring(lastGroup.indexOf(startToken, lastIndexOf.get()) + startToken.length(), lastGroup.indexOf(endToken, lastIndexOf.get())).trim();
//        lastIndexOf.set(lastGroup.indexOf(endToken, lastIndexOf.get()) + endToken.length());
//        if(cdataValues.containsKey(result)) {
//            result = cdataValues.get(result);
//        }
//        return Strings.deductInstance(result.trim());
        return "";
    }

    public static String toXml(Map<String,Object> root) {
        StringBuilder result = new StringBuilder();
        for(String key : root.keySet()) {
            Object value = root.get(key);
            if(value instanceof Map) {

            } else if(value instanceof Collection) {

            } else {

            }
        }
        return result.toString();
    }

    private static String toXml(StringBuilder builder, Map<String,Object> tag) {
        for(String key : tag.keySet()) {
            Object value = tag.get(key);
            if(tag.size() == 1) {
                if (value instanceof Map) {

                } else if (value instanceof Collection) {

                } else {
                    if (value != null) {
                        builder.append(String.format(Patterns.OPEN_TAG, key, Strings.EMPTY_STRING));
                        builder.append(value);
                        builder.append(String.format(Patterns.CLOSE_TAG, key));
                    } else {
                        builder.append(String.format(Patterns.OPEN_TAG, key, Strings.SLASH));
                    }
                }
            } else {
                if (value instanceof Map) {

                } else if (value instanceof Collection) {

                } else {

                }
            }
        }
        return null;
    }
}
