package org.hcjf.layers.scripting;

import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import org.hcjf.bson.BsonDecoder;
import org.hcjf.bson.BsonDocument;
import org.hcjf.bson.BsonEncoder;
import org.hcjf.errors.HCJFServiceTimeoutException;
import org.hcjf.layers.Layer;
import org.hcjf.properties.SystemProperties;
import org.hcjf.service.Service;
import org.hcjf.service.ServiceSession;
import org.hcjf.utils.Strings;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class JavaCodeEvaluator extends Layer implements CodeEvaluator {

    private static final String NAME = "java";

    private static final String CLASS_PATH_PROPERTY = "java.class.path";
    private static final String[] imports = {
            "import java.util.*;",
            "import org.hcjf.utils.*;",
            "import org.hcjf.bson.*;"
    };
    private static final String BSON_RESULT_VAR_NAME = "_bsonResult";
    private static final String CREATE_PARAMETERS_LINE = "Map<String,Object> parameters = new HashMap<>(parameters);";
    private static final String CREATE_RESULT_LINE = "Map<String,Object> result = new HashMap<>(parameters);";
    private static final String CREATE_BSON_RESULT_LINE = "String _bsonResult = \"\";";
    private static final String OVERRIDE_PARAMETERS_LINE = "parameters = BsonDecoder.decode(Strings.hexToBytes(\"%s\")).toMap();";
    private static final String OVERRIDE_RESULT_LINE = "result = new HashMap<>(parameters);";
    private static final String OVERRIDE_BSON_RESULT_LINE = "_bsonResult = Strings.bytesToHex(BsonEncoder.encode(new BsonDocument(result)));";
    private static final String OUT_FIELD = "_out";
    private static final String ERR_FIELD = "_error";
    private static final String WAITING_VM_TIME_FIELD = "_waitingVmTime";
    private static final String EVAL_TIME_FIELD = "_evalTime";
    private static final Integer DEFAULT_CACHE_SIZE = 1;
    private static final Long DEFAULT_EVAL_TIMEOUT = 5000L;

    private final Queue<JShellInstance> cache;

    public JavaCodeEvaluator() {
        this.cache = new LinkedList<>();
        Integer size = SystemProperties.getInteger(SystemProperties.CodeEvaluator.JAVA_CACHE_SIZE, DEFAULT_CACHE_SIZE);
        for (int i = 0; i < size; i++) {
            cache.offer(new JShellInstance());
        }
    }

    @Override
    public String getImplName() {
        return NAME;
    }

    /**
     * Evaluate the script with a set of parameter and store the result into the result object. This particular
     * implementation evaluate java code.
     * @param script     Script to evaluate.
     * @param parameters Parameters object.
     * @return Model as result of the script evaluation.
     */
    @Override
    public Map<String,Object> evaluate(String script, Map<String, Object> parameters) {
        JShellInstance jShellInstance;
        Long waitingVmTime = System.currentTimeMillis();
        synchronized (cache) {
            while (cache.isEmpty()) {
                try {
                    cache.wait();
                } catch (InterruptedException e) {
                    break;
                }
            }
            jShellInstance = cache.remove();
            waitingVmTime = System.currentTimeMillis() - waitingVmTime;
        }

        Boolean killShell = false;
        Long timeout = SystemProperties.getLong(SystemProperties.CodeEvaluator.JAVA_CACHE_TIMEOUT, DEFAULT_EVAL_TIMEOUT);
        try {
            Map<String,Object> result = Service.call(() -> jShellInstance.evaluate(script, parameters),
                    ServiceSession.getCurrentIdentity(), timeout);
            result.put(WAITING_VM_TIME_FIELD, waitingVmTime);
            return result;
        } catch (HCJFServiceTimeoutException ex) {
            killShell = true;
            throw ex;
        } finally {
            synchronized (cache) {
                if(killShell) {
                    jShellInstance.kill();
                    cache.offer(new JShellInstance());
                } else {
                    cache.offer(jShellInstance);
                }
                cache.notifyAll();
            }
        }
    }

    private static class JShellInstance {

        private JShell jShell;
        private ByteArrayOutputStream outStream;
        private ByteArrayOutputStream errorStream;
        private PrintStream out;
        private PrintStream error;

        public JShellInstance() {
            init();
        }

        public void init() {
            outStream = new ByteArrayOutputStream();
            out = new PrintStream(outStream);
            errorStream = new ByteArrayOutputStream();
            error = new PrintStream(errorStream);
            jShell = JShell.builder().out(out).err(error).build();
            jShell.addToClasspath(System.getProperty(CLASS_PATH_PROPERTY));
            for(String i : imports) {
                jShell.eval(i);
            }
            jShell.eval(CREATE_PARAMETERS_LINE);
            jShell.eval(CREATE_RESULT_LINE);
            jShell.eval(CREATE_BSON_RESULT_LINE);
        }

        public Map<String,Object> evaluate(String script, Map<String, Object> parameters) {
            Long time = System.currentTimeMillis();
            String bson = Strings.bytesToHex(BsonEncoder.encode(new BsonDocument(parameters)));
            jShell.eval(String.format(OVERRIDE_PARAMETERS_LINE, bson));
            jShell.eval(OVERRIDE_RESULT_LINE);
            List<SnippetEvent> snippets = jShell.eval(script);
            jShell.eval(OVERRIDE_BSON_RESULT_LINE);
            String bsonResult = jShell.varValue(jShell.variables().filter(V -> V.name().equals(BSON_RESULT_VAR_NAME)).findFirst().get());
            bsonResult = bsonResult.replace("\"", Strings.EMPTY_STRING);
            Map<String,Object> result = BsonDecoder.decode(Strings.hexToBytes(bsonResult)).toMap();
            result.put(OUT_FIELD, outStream.toString());
            result.put(ERR_FIELD, errorStream.toString());
            result.put(EVAL_TIME_FIELD, System.currentTimeMillis() - time);
            snippets.forEach(S -> jShell.drop(S.snippet()));
            outStream.reset();
            errorStream.reset();
            return result;
        }

        public void kill() {
            jShell.close();
            System.out.println();
        }

    }
}
