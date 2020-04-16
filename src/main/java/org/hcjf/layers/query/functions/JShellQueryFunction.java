package org.hcjf.layers.query.functions;

import jdk.jshell.JShell;
import jdk.jshell.VarSnippet;
import org.apache.commons.collections.map.HashedMap;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JShellQueryFunction extends BaseQueryFunctionLayer {

    private static final class Patterns {
        private static final String ASSIGNATION_PATTERN = "%s = %s;";
        private static final String MAP_PATTERN = "Map<String,Object> %s";
        private static final String MAP_INSTANCE_PATTERN = "new HashMap<>()";
        private static final String LIST_PATTERN = "List<Object> %s";
        private static final String LIST_INSTANCE_PATTERN = "new ArrayList<>()";
        private static final String DATE_PATTERN = "Date %s";
        private static final String DATE_INSTANCE_PATTERN = "new Date(%d)";
        private static final String BIG_DECIMAL_PATTERN = "BigDecimal %s";
        private static final String BIG_DECIMAL_INSTANCE_PATTERN = "BigDecimal.valueOf(%f)";
        private static final String BOOLEAN_PATTERN = "Boolean %s";
        private static final String BYTE_PATTERN = "Byte %s";
        private static final String SHORT_PATTERN = "Short %s";
        private static final String INTEGER_PATTERN = "Integer %s";
        private static final String LONG_PATTERN = "Long %s";
        private static final String FLOAT_PATTERN = "Float %s";
        private static final String DOUBLE_PATTERN = "Double %s";
        private static final String STRING_PATTERN = "String %s";
        private static final String STRING_INSTANCE_PATTERN = "\"%s\"";
    }

    private static final class Functions {
        private static final String SHELL = "jsh";
        private static final String NAMED_VALUE = "$";
    }

    private static final String NAME = "jShell";

    public JShellQueryFunction() {
        super(NAME);
        addFunctionName(Functions.SHELL);
        addFunctionName(Functions.NAMED_VALUE);
    }

    @Override
    public Object evaluate(String functionName, Object... parameters) {
        return null;
    }

    private String createShellVariable(String name, Object object) {
        String result = null;
        if(object instanceof Map) {
            result = String.format(String.format(Patterns.MAP_PATTERN, name),Patterns.MAP_INSTANCE_PATTERN);
        } else if(object instanceof List) {
            result = String.format(String.format(Patterns.LIST_PATTERN, name),Patterns.LIST_INSTANCE_PATTERN);
        } else if(object instanceof Date) {
            result = String.format(
                    String.format(Patterns.DATE_PATTERN, name),
                    String.format(Patterns.DATE_INSTANCE_PATTERN, ((Date)object).getTime())
            );
        } else if(object instanceof BigDecimal) {
            result = String.format(
                    String.format(Patterns.BIG_DECIMAL_PATTERN, name),
                    String.format(Patterns.BIG_DECIMAL_INSTANCE_PATTERN, ((BigDecimal)object).doubleValue())
            );
        } else if(object instanceof Boolean || boolean.class.isAssignableFrom(object.getClass())) {
            result = String.format(String.format(Patterns.BOOLEAN_PATTERN, name), object.toString());
        } else if(object instanceof Byte || byte.class.isAssignableFrom(object.getClass())) {
            result = String.format(String.format(Patterns.BYTE_PATTERN, name), object.toString());
        } else if(object instanceof Short || short.class.isAssignableFrom(object.getClass())) {
            result = String.format(String.format(Patterns.SHORT_PATTERN, name), object.toString());
        } else if(object instanceof Integer || int.class.isAssignableFrom(object.getClass())) {
            result = String.format(String.format(Patterns.INTEGER_PATTERN, name), object.toString());
        } else if(object instanceof Long || long.class.isAssignableFrom(object.getClass())) {
            result = String.format(String.format(Patterns.LONG_PATTERN, name), object.toString());
        } else if(object instanceof Float || float.class.isAssignableFrom(object.getClass())) {
            result = String.format(String.format(Patterns.FLOAT_PATTERN, name), object.toString());
        } else if(object instanceof Double || double.class.isAssignableFrom(object.getClass())) {
            result = String.format(String.format(Patterns.DOUBLE_PATTERN, name), object.toString());
        } else if(object instanceof String) {
            result = String.format(
                    String.format(Patterns.STRING_PATTERN, name),
                    String.format(Patterns.STRING_INSTANCE_PATTERN, ((BigDecimal)object).doubleValue())
            );
        }
        return result;
    }

    private String createMapVariable(String name, Map<String,Object> map) {
        return null;
    }

    private String createListVariable(List<Object> list) {
        return null;
    }

    public static void main(String[] args) {
        Map<String,Object> params = new HashedMap();
        params.put("param1", 10);


        JShell jShell = JShell.create();
        jShell.eval("import java.util.Date;");
        jShell.eval("import java.util.Map;");
        jShell.eval("import java.util.HashMap;");
        jShell.eval("int i = 0;");
        jShell.eval("i = i + 10;");
        jShell.eval("Map<String,Object> map1 = new HashMap<>();");
        jShell.eval("map1.put(\"field\", new Date());");
        jShell.variables().map(x -> x.name()).forEach(System.out::println);
        jShell.variables().map(x -> jShell.varValue(x)).forEach(System.out::println);
    }

}
