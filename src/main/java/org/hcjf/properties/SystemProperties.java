package org.hcjf.properties;

import com.google.gson.*;
import org.hcjf.cloud.impl.DefaultCloudServiceImpl;
import org.hcjf.layers.locale.DefaultLocaleLayer;
import org.hcjf.utils.JsonUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class overrides the system properties default implementation adding
 * some default values and properties definitions for the service-oriented platforms
 * works.
 * @author javaito
 */
public final class SystemProperties extends Properties {

    public static final String HCJF_DEFAULT_DATE_FORMAT = "hcjf.default.date.format";
    public static final String HCJF_DEFAULT_DATE_FORMAT_REGEX = "hcjf.default.date.format.regex";
    public static final String HCJF_DEFAULT_LOCAL_DATE_FORMAT_REGEX = "hcjf.default.date.format.regex";
    public static final String HCJF_DEFAULT_NUMBER_FORMAT = "hcjf.default.number.format";
    public static final String HCJF_DEFAULT_SCIENTIFIC_NUMBER_FORMAT = "hcjf.default.scientific.number.format";
    public static final String HCJF_DEFAULT_DECIMAL_SEPARATOR = "hcjf.default.decimal.separator";
    public static final String HCJF_DEFAULT_GROUPING_SEPARATOR = "hcjf.default.grouping.separator";
    public static final String HCJF_DEFAULT_PROPERTIES_FILE_PATH = "hcjf.default.properties.file.path";
    public static final String HCJF_DEFAULT_PROPERTIES_FILE_XML = "hcjf.default.properties.file.xml";
    public static final String HCJF_UUID_REGEX = "hcjf.uuid.regex";
    public static final String HCJF_INTEGER_NUMBER_REGEX = "hcjf.integer.number.regex";
    public static final String HCJF_DECIMAL_NUMBER_REGEX = "hcjf.decimal.number.regex";
    public static final String HCJF_SCIENTIFIC_NUMBER_REGEX = "hcjf.scientific.number.regex";
    public static final String HCJF_MATH_REGULAR_EXPRESSION = "hcjf.math.regular.expression";
    public static final String HCJF_MATH_CONNECTOR_REGULAR_EXPRESSION = "hcjf.math.connector.regular.expression";
    public static final String HCJF_MATH_SPLITTER_REGULAR_EXPRESSION = "hcjf.math.splitter.regular.expression";
    public static final String HCJF_DEFAULT_LRU_MAP_SIZE = "hcjf.default.lru.map.size";
    public static final String HCJF_DEFAULT_EXCEPTION_MESSAGE_TAG = "hcjf.default.exception.message.tag";
    public static final String HCJF_CHECKSUM_ALGORITHM = "hcjf.checksum.algorithm";

    public static final class Locale {
        public static final String LOG_TAG = "hcjf.locale.log.tag";
        public static final String DEFAULT_LOCALE = "hcjf.default.locale";
        public static final String DEFAULT_LOCALE_LAYER_IMPLEMENTATION_CLASS_NAME = "hcjf.default.locale.layer.implementation.class.name";
        public static final String DEFAULT_LOCALE_LAYER_IMPLEMENTATION_NAME = "hcjf.default.locale.layer.implementation.name";
    }

    public static final class Layer {
        public static final String LOG_TAG = "hcjf.layers.log.tag";
        public static final String READABLE_ALL_LAYER_IMPLEMENTATION_NAME = "hcjf.layers.readable.all.layer.implementation.name";
        public static final String READABLE_LAYER_IMPLEMENTATION_NAME =  "hcjf.layers.readable.layer.implementation.name";
        public static final String PLUGIN_THREADING_GRANT = "hcjf.layers.plugin.threading.grant";
        public static final String PLUGIN_FILE_ACCESS_GRANT = "hcjf.layers.plugin.file.access.grant";
        public static final String DISTRIBUTED_LAYER_ENABLED = "hcjf.layers.distributed.layer.enabled";
    }

    public static final class Service {
        public static final String STATIC_THREAD_NAME = "hcjf.service.static.thread.name";
        public static final String STATIC_THREAD_POOL_CORE_SIZE = "hcjf.service.static.thread.pool.core.size";
        public static final String STATIC_THREAD_POOL_MAX_SIZE = "hcjf.service.static.thread.pool.max.size";
        public static final String STATIC_THREAD_POOL_KEEP_ALIVE_TIME = "hcjf.service.static.thread.pool.keep.alive.time";
        public static final String THREAD_POOL_CORE_SIZE = "hcjf.service.thread.pool.core.size";
        public static final String THREAD_POOL_MAX_SIZE = "hcjf.service.thread.pool.max.size";
        public static final String THREAD_POOL_KEEP_ALIVE_TIME = "hcjf.service.thread.pool.keep.alive.time";
        public static final String GUEST_SESSION_NAME = "hcjf.service.guest.session.name";
        public static final String SYSTEM_SESSION_NAME = "hcjf.service.system.session.name";
        public static final String SHUTDOWN_TIME_OUT = "hcjf.service.shutdown.time.out";
        public static final String MAX_ALLOCATED_MEMORY_EXPRESSED_IN_PERCENTAGE = "hcjf.service.max.allocated.memory.expressed.in.percentage";
        public static final String MAX_ALLOCATED_MEMORY_FOR_THREAD = "max.allocated.memory.for.thread";
        public static final String MAX_EXECUTION_TIME_FOR_THREAD = "max.execution.time.for.thread";
        public static final String MAX_ALLOCATED_MEMORY_EXCEEDED_THROWS_EXCEPTION = "max.allocated.memory.exceeded.throws.exception";
    }

    public static final class Event {
        public static final String LOG_TAG = "hcjf.event.log.tag";
        public static final String SERVICE_NAME = "hcjf.event.service.name";
        public static final String SERVICE_PRIORITY = "hcjf.event.service.priority";
    }

    public static final class Collector {
        public static final String SERVICE_NAME = "hcjf.collector.service.name";
        public static final String SERVICE_PRIORITY = "hcjf.collector.service.priority";
        public static final String LOG_TAG = "hcjf.collector.log.tag";
        public static final String FLUSH_PERIOD = "hcjf.collector.flush.period";
        public static final String CLOUD_SAVE_MODE = "hcjf.collector.cloud.save.mode";
        public static final String CLOUD_TIMER_TASK_NAME = "hcjf.collector.cloud.timer.task.name";
    }

    public static final class CodeEvaluator {

        public static final class Java {
            public static final String IMPL_NAME = "hcjf.code.evaluator.java.impl.name";
            public static final String J_SHELL_POOL_SIZE = "hcjf.code.evaluator.java.j.shell.pool.size";
            public static final String J_SHELL_INSTANCE_TIMEOUT = "hcjf.code.evaluator.java.j.shell.instance.timeout";
            public static final String SCRIPT_CACHE_SIZE = "hcjf.code.evaluator.java.script.cache.size";
            public static final String IMPORTS = "hcjf.code.evaluator.java.script.cache.imports";
        }

        public static final class Python {
            public static final String IMPL_NAME = "hcjf.code.evaluator.python.impl.name";
        }

        public static final class Js {
            public static final String IMPL_NAME = "hcjf.code.evaluator.js.impl.name";
        }
    }

    public static final class Cryptography{

        public static final String KEY = "hcjf.cryptography.key";
        public static final String ALGORITHM = "hcjf.cryptography.algorithm";
        public static final String OPERATION_MODE = "hcjf.cryptography.operation.mode";
        public static final String PADDING_SCHEME = "hcjf.cryptography.padding.scheme";
        public static final String AAD = "hcjf.cryptography.aad";

        public static final class Random {
            public static final String IV_SIZE = "hcjf.cryptography.random.iv.size";
        }

        public static final class GCM {
            public static final String TAG_BIT_LENGTH = "hcjf.cryptography.gcm.tag.bit.length";
        }
    }

    public static final class Log {
        public static final String SERVICE_NAME = "hcjf.log.service.name";
        public static final String SERVICE_PRIORITY = "hcjf.log.service.priority";
        public static final String FILE_PREFIX = "hcjf.log.file.prefix";
        public static final String ERROR_FILE = "hcjf.log.error.file";
        public static final String WARNING_FILE = "hcjf.log.warning.file";
        public static final String INFO_FILE = "hcjf.log.info.file";
        public static final String DEBUG_FILE = "hcjf.log.debug.file";
        public static final String LEVEL = "hcjf.log.level";
        public static final String DATE_FORMAT = "hcjf.log.date.format";
        public static final String CONSUMERS = "hcjf.log.consumers";
        public static final String SYSTEM_OUT_ENABLED = "hcjf.log.system.out.enabled";
        public static final String JAVA_STANDARD_LOGGER_ENABLED = "hcjf.log.java.standard.logger.enabled";
        public static final String QUEUE_INITIAL_SIZE = "hcjf.log.queue.initial.size";
        public static final String TRUNCATE_TAG = "hcjf.log.truncate.tag";
        public static final String TRUNCATE_TAG_SIZE = "hcjf.log.truncate.tag.size";
        public static final String LOG_CONSUMERS_SIZE = "hcjf.log.consumers.size";
    }

    public static final class Encoding {
        public static final String SERVICE_NAME = "hcjf.encoding.service.name";
        public static final String SERVICE_PRIORITY = "hcjf.encoding.service.priority";
    }

    public static final class FileSystem {
        public static final String SERVICE_NAME = "hcjf.file.system.service.name";
        public static final String SERVICE_PRIORITY = "hcjf.file.system.service.priority";
        public static final String LOG_TAG = "hcjf.file.system.log.tag";
        public static final String POLLING_WAIT_TIME = "hcjf.file.system.polling.wait.time";
    }

    public static final class Net {
        public static final String SERVICE_NAME = "hcjf.net.service.name";
        public static final String LOG_TAG = "hcjf.net.log.tag";
        public static final String INPUT_BUFFER_SIZE = "hcjf.net.input.buffer.size";
        public static final String OUTPUT_BUFFER_SIZE = "hcjf.net.output.buffer.size";
        public static final String DISCONNECT_AND_REMOVE = "hcjf.net.disconnect.and.remove";
        public static final String CONNECTION_TIMEOUT_AVAILABLE = "hcjf.net.connection.timeout.available";
        public static final String CONNECTION_TIMEOUT = "hcjf.net.connection.timeout";
        public static final String WRITE_TIMEOUT = "hcjf.net.write.timeout";
        public static final String IO_UDP_LRU_SESSIONS_SIZE = "hcjf.net.io.udp.lru.sessions.size";
        public static final String IO_UDP_LRU_ADDRESSES_SIZE = "hcjf.net.io.udp.lru.addresses.size";
        public static final String IO_QUEUE_SIZE = "hcjf.net.io.queue.size";
        public static final String IO_THREAD_POOL_KEEP_ALIVE_TIME = "hcjf.net.io.thread.pool.keep.alive.time";
        public static final String IO_THREAD_POOL_NAME = "hcjf.net.io.thread.pool.name";
        public static final String DEFAULT_INPUT_BUFFER_SIZE = "hcjf.net.default.input.buffer.size";
        public static final String DEFAULT_OUTPUT_BUFFER_SIZE = "hcjf.net.default.output.buffer.size";
        public static final String IO_THREAD_DIRECT_ALLOCATE_MEMORY = "hcjf.net.io.thread.direct.allocate.memory";
        public static final String SSL_MAX_IO_THREAD_POOL_SIZE = "hcjf.net.ssl.max.io.thread.pool.size";
        public static final String PORT_PROVIDER_TIME_WINDOWS_SIZE = "hcjf.net.port.provider.time.windows.size";
        public static final String PORT_PROBE_CONNECTION_TIMEOUT = "hcjf.net.port.probe.connection.timeout";
        public static final String REMOTE_ADDRESS_INTO_NET_PACKAGE = "hcjf.net.remote.address.into.net.package";
        public static final String REMOTE_ADDRESS_INTO_NET_SESSION = "hcjf.net.remote.address.into.net.session";
        public static final String NIO_SELECTOR_HEALTH_CHECKER_RUNNING_TIME = "hcjf.net.nio.selector.health.checker.running.time";
        public static final String NIO_SELECTOR_HEALTH_CHECKER_SAMPLE_TIME = "hcjf.net.nio.selector.health.checker.sample.time";
        public static final String NIO_SELECTOR_HEALTH_CHECKER_DANGEROUS_THRESHOLD = "hcjf.net.nio.selector.health.checker.dangerous.threshold";
        public static final String NIO_SELECTOR_HEALTH_CHECKER_DANGEROUS_REPEATS = "hcjf.net.nio.selector.health.checker.dangerous.repeats";
        public static final String NIO_SELECTOR_HEALTH_CHECKER_DANGEROUS_ACTION = "hcjf.net.nio.selector.health.checker.dangerous.action";

        public static final class Broadcast {
            public static final String SERVICE_NAME = "hcjf.net.broadcast.service.name";
            public static final String LOG_TAG = "hcjf.net.broadcast.log.tag";
            public static final String INTERFACE_NAME = "hcjf.net.broadcast.interface.name";
            public static final String IP_VERSION = "hcjf.net.broadcast.ip.version";
            public static final String SENDER_DELAY = "hcjf.net.broadcast.sender.delay";
            public static final String SIGNATURE_ALGORITHM = "hcjf.net.broadcast.signature.algorithm";
            public static final String RECEIVER_BUFFER_SIZE = "hcjf.net.broadcast.receiver.buffer.size";
        }

        public static final class KubernetesSpy {
            public static final String SERVICE_NAME = "hcjf.net.kubernetes.service.name";
            public static final String LOG_TAG = "hcjf.net.kubernetes.log.tag";
            public static final String CLIENT_CONNECTION_TIMEOUT = "hcjf.net.kubernetes.client.connection.timeout";
            public static final String TASK_SLEEP_TIME = "hcjf.net.kubernetes.task.sleep.time";
            public static final String CURL_COMMAND = "hcjf.net.kubernetes.curl.command";
            public static final String CURL_COMMAND_AUTHENTICATION_HEADER = "hcjf.net.kubernetes.curl.command.authentication.header";
            public static final String CURL_COMMAND_CACERT_PARAMETER = "hcjf.net.kubernetes.curl.command.cacert.parameter";
            public static final String CACERT_FILE_PATH = "hcjf.net.kubernetes.cacert.file.path";
            public static final String TOKEN_FILE_PATH = "hcjf.net.kubernetes.token.file.path";
            public static final String NAMESPACE_FILE_PATH = "hcjf.net.kubernetes.namespace.file.path";
            public static final String MASTER_NODE_HOST = "hcjf.net.kubernetes.master.node.host";
            public static final String MASTER_NODE_PORT = "hcjf.net.kubernetes.master.node.port";
            public static final String AUTHORIZATION_HEADER = "hcjf.net.kubernetes.authorization.header";
            public static final String JSON_DATE_FORMAT = "hcjf.net.kubernetes.json.date.format";

            public static final class EndPoints {
                public static final String LIST_PODS = "hcjf.net.kubernetes.end.points.list.pods";
                public static final String LIST_SERVICES = "hcjf.net.kubernetes.end.points.list.services";
                public static final String LIST_END_POINTS = "hcjf.net.kubernetes.end.points.list.end_points";
            }
        }

        public static final class Ssl {
            public static final String DEFAULT_PROTOCOL = "hcjf.net.ssl.default.protocol";
            public static final String IO_THREAD_NAME = "hcjf.net.ssl.io.thread.name";
            public static final String ENGINE_THREAD_NAME = "hcjf.net.ssl.engine.thread.name";
            public static final String DEFAULT_KEYSTORE_PASSWORD = "hcjf.net.ssl.default.keystore.password";
            public static final String DEFAULT_KEY_PASSWORD = "hcjf.net.ssl.default.key.password";
            public static final String DEFAULT_KEYSTORE_FILE_PATH = "hcjf.net.ssl.default.keystore.file.path";
            public static final String DEFAULT_TRUSTED_CERTS_FILE_PATH = "hcjf.net.ssl.default.trusted.certs.file.path";
            public static final String DEFAULT_KEY_TYPE = "hcjf.net.ssl.default.key.type";

        }

        public static final class Messages {
            public static final String LOG_TAG = "hcjf.net.messages.log.tag";
            public static final String SERVER_DECOUPLED_IO_ACTION = "hcjf.net.messages.server.decoupled.io.action";
            public static final String SERVER_IO_QUEUE_SIZE = "hcjf.net.messages.server.io.queue.size";
            public static final String SERVER_IO_WORKERS = "hcjf.net.messages.server.io.workers";
        }

        public static final class Http {
            public static final String INPUT_LOG_ENABLED = "hcjf.net.http.server.input.log.enabled";
            public static final String OUTPUT_LOG_ENABLED = "hcjf.net.http.server.output.log.enabled";
            public static final String LOG_TAG = "hcjf.net.http.server.log.tag";
            public static final String SERVER_NAME = "hcjf.net.http.server.name";
            public static final String RESPONSE_DATE_HEADER_FORMAT_VALUE = "hcjf.net.http.response.date.header.format.value";
            public static final String INPUT_LOG_BODY_MAX_LENGTH = "hcjf.net.http.input.log.body.max.length";
            public static final String OUTPUT_LOG_BODY_MAX_LENGTH = "hcjf.net.http.output.log.body.max.length";
            public static final String DEFAULT_SERVER_PORT = "hcjf.net.http.default.server.port";
            public static final String DEFAULT_CLIENT_PORT = "hcjf.net.http.default.client.port";
            public static final String STREAMING_LIMIT_FILE_SIZE = "hcjf.net.http.streaming.limit.file.size";
            public static final String DEFAULT_ERROR_FORMAT_SHOW_STACK = "hcjf.net.http.default.error.format.show.stack";
            public static final String DEFAULT_CLIENT_CONNECT_TIMEOUT = "hcjf.net.http.default.client.connect.timeout";
            public static final String DEFAULT_CLIENT_READ_TIMEOUT = "hcjf.net.http.default.client.read.timeout";
            public static final String DEFAULT_CLIENT_WRITE_TIMEOUT = "hcjf.net.http.default.client.write.timeout";
            public static final String DEFAULT_GUEST_SESSION_NAME = "hcjf.net.http.default.guest.session.name";
            public static final String DEFAULT_FILE_CHECKSUM_ALGORITHM = "hcjf.net.http.default.file.checksum.algorithm";
            public static final String ENABLE_AUTOMATIC_RESPONSE_CONTENT_LENGTH = "hcjf.net.http.enable.automatic.response.content.length";
            public static final String AUTOMATIC_CONTENT_LENGTH_SKIP_CODES = "hcjf.net.http.automatic.content.length.skip.codes";
            public static final String MAX_PACKAGE_SIZE = "hcjf.net.http.max.package.size";
            public static final String SERVER_DECOUPLED_IO_ACTION = "hcjf.net.http.server.decoupled.io.action";
            public static final String SERVER_IO_QUEUE_SIZE = "hcjf.net.http.server.io.queue.size";
            public static final String SERVER_IO_WORKERS = "hcjf.net.http.server.io.workers";
            public static final String HOST_ACCESS_CONTROL_REGEX_START_CHAR = "hcjf.net.http.host.access.control.regex.start.char";
            public static final String CLIENT_RESPONSE_HANDLER_QUEUE_SIZE = "hcjf.net.http.client.response.handler.queue.size";

            public static final class Http2 {
                public static final String HEADER_TABLE_SIZE = "hcjf.net.http.http2.header.table.size";
                public static final String ENABLE_PUSH = "hcjf.net.http.http2.enable.push";
                public static final String MAX_CONCURRENT_STREAMS = "hcjf.net.http.http2.max.concurrent.streams";
                public static final String INITIAL_WINDOWS_SIZE = "hcjf.net.http.http2.initial.windows.size";
                public static final String MAX_FRAME_SIZE = "hcjf.net.http.http2.max.frame.size";
                public static final String MAX_HEADER_LIST_SIZE = "hcjf.net.http.http2.max.header.list.size";
                public static final String STREAM_FRAMES_QUEUE_MAX_SIZE = "hcjf.net.http.http2.stream.frames.queue.max.size";
            }

            public static final class Folder {
                public static final String LOG_TAG = "hcjf.net.http.folder.log.tag";
                public static final String FORBIDDEN_CHARACTERS = "hcjf.net.http.folder.forbidden.characters";
                public static final String FILE_EXTENSION_REGEX = "hcjf.net.http.folder.file.extension.regex";
                public static final String DEFAULT_HTML_DOCUMENT = "hcjf.net.http.folder.default.html.document";
                public static final String DEFAULT_HTML_BODY = "hcjf.net.http.folder.default.html.body";
                public static final String DEFAULT_HTML_ROW = "hcjf.net.http.folder.default.html.row";
                public static final String ZIP_CONTAINER = "hcjf.net.http.folder.zip.container";
                public static final String ZIP_TEMP_PREFIX = "hcjf.net.http.folder.zip.temp.prefix";
                public static final String JAR_CONTAINER = "hcjf.net.http.folder.jar.container";
                public static final String JAR_TEMP_PREFIX = "hcjf.net.http.folder.jar.temp.prefix";
            }

            public static final class EndPoint {

                public static final class Json {
                    public static final String DATE_FORMATS = "hcjf.net.http.end.point.json.date.formats";
                }

            }

            public static final class DataSources {
                public static final String SERVICE_NAME = "hcjf.net.http.data.sources.service.name";
                public static final String SERVICE_PRIORITY = "hcjf.net.http.data.sources.service.priority";
                public static final String THREAD_POOL_ENABLED = "hcjf.net.http.data.sources.service.thread.pool.enabled";
                public static final String THREAD_POOL_CORE_SIZE = "hcjf.net.http.data.sources.service.thread.pool.core.size";
                public static final String THREAD_POOL_MAX_SIZE = "hcjf.net.http.data.sources.service.thread.pool.max.size";
                public static final String THREAD_POOL_KEEP_ALIVE_TIME = "hcjf.net.http.data.sources.service.thread.pool.keep.alive.time";
            }
        }

        public static final class Https {
            public static final String DEFAULT_SERVER_PORT = "hcjf.net.https.default.server.port";
            public static final String DEFAULT_CLIENT_PORT = "hcjf.net.https.default.server.port";
        }

        public static final class Rest {
            public static final String DEFAULT_MIME_TYPE = "hcjf.rest.default.mime.type";
            public static final String DEFAULT_ENCODING_IMPL = "hcjf.rest.default.encoding.impl";
            public static final String QUERY_PATH = "hcjf.rest.query.path";
            public static final String QUERY_PARAMETER = "hcjf.rest.query.parameter.path";
            public static final String BODY_FIELD = "hcjf.net.http.rest.body.field";
            public static final String QUERY_FIELD = "hcjf.net.http.rest.query.field";
            public static final String QUERIES_FIELD = "hcjf.net.http.rest.queries.field";
            public static final String DATA_SOURCE_FIELD = "hcjf.net.http.rest.data.source.field";
            public static final String COMMAND_FIELD = "hcjf.net.http.rest.command.field";
            public static final String COMMANDS_FIELD = "hcjf.net.http.rest.commands.field";
        }

    }

    public static final class ProcessDiscovery {
        public static final String LOG_TAG = "hcjf.process.log.tag";
        public static final String SERVICE_NAME = "hcjf.process.discovery.service.name";
        public static final String SERVICE_PRIORITY = "hcjf.process.discovery.service.priority";
        public static final String DELAY = "hcjf.process.delay";
    }

    public static final class Query {
        public static final String SINGLE_PATTERN = "hcjf.query.single.pattern";
        public static final String LOG_TAG = "hcjf.query.log.tag";
        public static final String DEFAULT_LIMIT = "hcjf.query.default.limit";
        public static final String DEFAULT_DESC_ORDER = "hcjf.query.default.desc.order";
        public static final String SELECT_REGULAR_EXPRESSION = "hcjf.query.select.regular.expression";
        public static final String CONDITIONAL_REGULAR_EXPRESSION = "hcjf.query.conditional.regular.expression";
        public static final String EVALUATOR_COLLECTION_REGULAR_EXPRESSION = "hcjf.query.evaluator.collection.regular.expression";
        public static final String OPERATION_REGULAR_EXPRESSION = "hcjf.query.operation.regular.expression";
        public static final String JOIN_REGULAR_EXPRESSION = "hcjf.query.join.regular.expression";
        public static final String JOIN_RESOURCE_VALUE_INDEX = "hcjf.query.join.resource.value";
        public static final String JOIN_DYNAMIC_RESOURCE_INDEX = "hcjf.query.join.dynamic.resource.index";
        public static final String JOIN_DYNAMIC_RESOURCE_ALIAS_INDEX = "hcjf.query.join.dynamic.resource.alias.index";
        public static final String JOIN_CONDITIONAL_BODY_INDEX = "hcjf.query.join.conditional.body.index";
        public static final String UNION_REGULAR_EXPRESSION = "hcjf.query.union.regular.expression";
        public static final String SOURCE_REGULAR_EXPRESSION = "hcjf.query.source.regular.expression";
        public static final String AS_REGULAR_EXPRESSION = "hcjf.query.as.regular.expression";
        public static final String DESC_REGULAR_EXPRESSION = "hcjf.query.desc.regular.expression";
        public static final String ENVIRONMENT_GROUP_INDEX = "hcjf.query.environment.group.index";
        public static final String SELECT_GROUP_INDEX = "hcjf.query.select.group.index";
        public static final String FROM_GROUP_INDEX = "hcjf.query.from.group.index";
        public static final String CONDITIONAL_GROUP_INDEX = "hcjf.query.conditional.group.index";
        public static final String RESOURCE_VALUE_INDEX = "hcjf.query.resource.value.index";
        public static final String DYNAMIC_RESOURCE_INDEX = "hcjf.query.dynamic.resource.group.index";
        public static final String DYNAMIC_RESOURCE_ALIAS_INDEX = "hcjf.query.dynamic.resource.alias.group.index";
        public static final String JOIN_RESOURCE_NAME_INDEX = "hcjf.query.join.resource.name.index";
        public static final String JOIN_EVALUATORS_INDEX = "hcjf.query.join.evaluators.index";
        public static final String DATE_FORMAT = "hcjf.query.date.format";
        public static final String DECIMAL_SEPARATOR = "hcjf.query.decimal.separator";
        public static final String DECIMAL_FORMAT = "hcjf.query.decimal.format";
        public static final String SCIENTIFIC_NOTATION = "hcjf.query.scientific.notation";
        public static final String SCIENTIFIC_NOTATION_FORMAT = "hcjf.query.scientific.notation.format";
        public static final String EVALUATORS_CACHE_NAME = "hcjf.query.evaluators.cache";
        public static final String EVALUATOR_LEFT_VALUES_CACHE_NAME = "hcjf.query.evaluator.left.values.cache";
        public static final String EVALUATOR_RIGHT_VALUES_CACHE_NAME = "hcjf.query.evaluator.right.values.cache";
        public static final String COMPILER_CACHE_SIZE = "hcjf.query.compiler.cache.size";
        public static final String DEFAULT_COMPILER = "hcjf.query.default.compiler";
        public static final String DEFAULT_SERIALIZER = "hcjf.query.default.serializer";

        public static final class ReservedWord {
            public static final String ENVIRONMENT = "hcjf.query.environment.reserved.word";
            public static final String SELECT = "hcjf.query.select.reserved.word";
            public static final String FROM = "hcjf.query.from.reserved.word";
            public static final String JOIN = "hcjf.query.join.reserved.word";
            public static final String UNION = "hcjf.query.union.reserved.word";
            public static final String FULL = "hcjf.query.full.reserved.word";
            public static final String INNER = "hcjf.query.inner.join.reserved.word";
            public static final String LEFT = "hcjf.query.left.join.reserved.word";
            public static final String RIGHT = "hcjf.query.right.join.reserved.word";
            public static final String HASH = "hcjf.query.hash.join.reserved.word";
            public static final String ON = "hcjf.query.on.reserved.word";
            public static final String WHERE = "hcjf.query.where.reserved.word";
            public static final String ORDER_BY = "hcjf.query.order.by.reserved.word";
            public static final String DESC = "hcjf.query.desc.reserved.word";
            public static final String LIMIT = "hcjf.query.limit.reserved.word";
            public static final String START = "hcjf.query.start.reserved.word";
            public static final String RETURN_ALL = "hcjf.query.return.all.reserved.word";
            public static final String ARGUMENT_SEPARATOR = "hcjf.query.argument.separator";
            public static final String EQUALS = "hcjf.query.equals.reserved.word";
            public static final String DISTINCT = "hcjf.query.distinct.reserved.word";
            public static final String DISTINCT_2 = "hcjf.query.distinct.2.reserved.word";
            public static final String GREATER_THAN = "hcjf.query.greater.than.reserved.word";
            public static final String GREATER_THAN_OR_EQUALS = "hcjf.query.greater.than.or.equals.reserved.word";
            public static final String SMALLER_THAN = "hcjf.query.smaller.than.reserved.word";
            public static final String SMALLER_THAN_OR_EQUALS = "hcjf.query.smaller.than.or.equals.reserved.word";
            public static final String IN = "hcjf.query.in.reserved.word";
            public static final String NOT_IN = "hcjf.query.not.in.reserved.word";
            public static final String NOT = "hcjf.query.not.reserved.word";
            public static final String NOT_2 = "hcjf.query.not.2.reserved.word";
            public static final String LIKE = "hcjf.query.like.reserved.word";
            public static final String LIKE_WILDCARD = "hcjf.query.like.wildcard.reserved.word";
            public static final String AND = "hcjf.query.and.reserved.word";
            public static final String OR = "hcjf.query.or.reserved.word";
            public static final String STATEMENT_END = "hcjf.query.statement.end.reserved.word";
            public static final String REPLACEABLE_VALUE = "hcjf.query.replaceable.value.reserved.word";
            public static final String STRING_DELIMITER = "hcjf.query.string.delimiter.reserved.word";
            public static final String NULL = "hcjf.query.null.reserved.word";
            public static final String TRUE = "hcjf.query.true.reserved.word";
            public static final String FALSE = "hcjf.query.false.reserved.word";
            public static final String AS = "hcjf.query.as.reserved.word";
            public static final String GROUP_BY = "hcjf.query.group.by.reserved.word";
            public static final String DISJOINT_BY = "hcjf.query.disjoint.by.reserved.word";
            public static final String UNDERLYING = "hcjf.query.underlying.reserved.word";
            public static final String SRC = "hcjf.query.src.reserved.word";
        }

        public static class Function {

            public static final String NAME_PREFIX = "hcjf.query.function.name.prefix";
            public static final String MATH_EVAL_EXPRESSION_NAME = "hcjf.query.function.math.eval.expression.name";
            public static final String MATH_FUNCTION_NAME = "hcjf.query.function.math.name";
            public static final String STRING_FUNCTION_NAME = "hcjf.query.function.string.name";
            public static final String DATE_FUNCTION_NAME = "hcjf.query.function.date.name";
            public static final String MATH_ADDITION = "hcjf.query.function.math.addition";
            public static final String MATH_SUBTRACTION = "hcjf.query.function.math.subtraction";
            public static final String MATH_MULTIPLICATION = "hcjf.query.function.math.multiplication";
            public static final String MATH_DIVISION = "hcjf.query.function.math.division";
            public static final String MATH_MODULUS = "hcjf.query.function.math.modulus";
            public static final String MATH_EQUALS = "hcjf.query.function.maht.equals";
            public static final String MATH_DISTINCT = "hcjf.query.function.math.distinct";
            public static final String MATH_DISTINCT_2 = "hcjf.query.function.math.distinct.2";
            public static final String MATH_GREATER_THAN = "hcjf.query.function.math.grater.than";
            public static final String MATH_GREATER_THAN_OR_EQUALS = "hcjf.query.function.math.grater.than.or.equals";
            public static final String MATH_LESS_THAN =  "hcjf.query.function.math.less.than";
            public static final String MATH_LESS_THAN_OR_EQUALS =  "hcjf.query.function.math.less.than.or.equals";
            public static final String REFERENCE_FUNCTION_NAME = "hcjf.query.function.reference.name";
            public static final String BSON_FUNCTION_NAME = "hcjf.query.function.bson.name";
            public static final String COLLECTION_FUNCTION_NAME = "hcjf.query.function.collection.name";
            public static final String OBJECT_FUNCTION_NAME = "hcjf.query.function.object.name";
            public static final String BIG_DECIMAL_DIVIDE_SCALE = "hcjf.query.function.big.decimal.divide.scale";
            public static final String MATH_OPERATION_RESULT_ROUND = "hcjf.query.function.math.operation.result.round";
            public static final String MATH_OPERATION_RESULT_ROUND_CONTEXT = "hcjf.query.function.math.operation.result.round.context";
        }
    }

    public static class Cloud {
        public static final String SERVICE_NAME = "hcjf.cloud.name";
        public static final String SERVICE_PRIORITY = "hcjf.cloud.priority";
        public static final String IMPL = "hcjf.cloud.impl";
        public static final String LOG_TAG = "hcjf.cloud.log.tag";

        public static class Orchestrator {
            public static final String SERVICE_NAME = "hcjf.cloud.orchestrator.name";
            public static final String SERVICE_PRIORITY = "hcjf.cloud.orchestrator.service.priority";
            public static final String AVAILABLE = "hcjf.cloud.orchestrator.available";
            public static final String SERVER_LISTENER_PORT = "hcjf.cloud.orchestrator.server.listener.port";
            public static final String CONNECTION_LOOP_WAIT_TIME = "hcjf.cloud.orchestrator.connection.loop.wait.time";
            public static final String NODE_LOST_TIMEOUT = "hcjf.cloud.orchestrator.node.lost.timeout";
            public static final String ACK_TIMEOUT = "hcjf.cloud.orchestrator.ack.timeout";
            public static final String CLUSTER_NAME = "hcjf.cloud.orchestrator.cluster.name";
            public static final String WAGON_TIMEOUT = "hcjf.cloud.orchestrator.wagon.timeout";
            public static final String REORGANIZATION_TIMEOUT = "hcjf.cloud.orchestrator.reorganization.timeout";
            public static final String REORGANIZATION_WARNING_TIME_LIMIT = "hcjf.cloud.orchestrator.reorganization.warning.time.limit";
            public static final String INVOKE_TIMEOUT = "hcjf.cloud.orchestrator.invokeNode.timeout";
            public static final String TEST_NODE_TIMEOUT = "hcjf.cloud.orchestrator.test.node.timeout";
            public static final String REPLICATION_FACTOR = "hcjf.cloud.orchestrator.replication.factor";
            public static final String NODES = "hcjf.cloud.orchestrator.nodes";
            public static final String SERVICE_END_POINTS = "hcjf.cloud.orchestrator.service.end.points";
            public static final String SERVICE_PUBLICATION_REPLICAS_BROADCASTING_ENABLED = "hcjf.cloud.orchestrator.service.publication.broadcasting.enabled";
            public static final String SERVICE_PUBLICATION_REPLICAS_BROADCASTING_TIMEOUT = "hcjf.cloud.orchestrator.service.publication.broadcasting.timeout";
            public static final String NETWORKING_HANDSHAKE_DETAILS_AVAILABLE = "hcjf.cloud.orchestrator.networking.handshake.details.available";

            public static final class Events {
                public static final String LOG_TAG = "hcjf.cloud.orchestrator.events.log.tag";
                public static final String TIMEOUT = "hcjf.cloud.orchestrator.events.timeout";
                public static final String ATTEMPTS = "hcjf.cloud.orchestrator.events.attempts";
                public static final String SLEEP_PERIOD_BETWEEN_ATTEMPTS = "hcjf.cloud.orchestrator.events.sleep.period.between.attempts";
                public static final String STORE_STRATEGY = "hcjf.cloud.orchestrator.events.store.strategy";
            }

            public static final class Kubernetes {
                public static final String ENABLED = "hcjf.cloud.orchestrator.kubernetes.enabled";
                public static final String POD_LABELS = "hcjf.cloud.orchestrator.kubernetes.pod.labels";
                public static final String NAMESPACE = "hcjf.cloud.orchestrator.kubernetes.namespace";
                public static final String SERVICE_NAME = "hcjf.cloud.orchestrator.kubernetes.service.name";
                public static final String SERVICE_LABELS = "hcjf.cloud.orchestrator.kubernetes.service.labels";
                public static final String SERVICE_PORT_NAME = "hcjf.cloud.orchestrator.kubernetes.service.port.name";
                public static final String ALLOW_PHASES = "hcjf.cloud.orchestrator.kubernetes.allow.phases";
            }

            public static final class ThisNode {
                public static final String READABLE_LAYER_IMPLEMENTATION_NAME = "hcjf.cloud.orchestrator.this.node.readable.layer.implementation.name";
                public static final String ID = "hcjf.cloud.orchestrator.this.node.id";
                public static final String NAME = "hcjf.cloud.orchestrator.this.node.name";
                public static final String VERSION = "hcjf.cloud.orchestrator.this.node.version";
                public static final String CLUSTER_NAME = "hcjf.cloud.orchestrator.this.node.cluster.name";
                public static final String DATA_CENTER_NAME = "hcjf.cloud.orchestrator.this.node.data.center.name";
                public static final String LAN_ADDRESS = "hcjf.cloud.orchestrator.this.node.lan.address";
                public static final String LAN_PORT = "hcjf.cloud.orchestrator.this.node.lan.port";
                public static final String WAN_ADDRESS = "hcjf.cloud.orchestrator.this.node.wan.address";
                public static final String WAN_PORT = "hcjf.cloud.orchestrator.this.node.wan.port";
            }

            public static final class ThisServiceEndPoint {
                public static final String READABLE_LAYER_IMPLEMENTATION_NAME = "hcjf.cloud.orchestrator.this.service.end.point.readable.layer.implementation.name";
                public static final String ID = "hcjf.cloud.orchestrator.this.service.end.point.id";
                public static final String NAME = "hcjf.cloud.orchestrator.this.service.end.point.name";
                public static final String GATEWAY_ADDRESS = "hcjf.cloud.orchestrator.this.service.end.point.gateway.address";
                public static final String GATEWAY_PORT = "hcjf.cloud.orchestrator.this.service.end.point.gateway.port";
                public static final String PUBLICATION_TIMEOUT = "hcjf.cloud.orchestrator.this.service.end.point.publication.timeout";
                public static final String DISTRIBUTED_EVENT_LISTENER = "hcjf.cloud.orchestrator.this.service.end.point.distributed.event.listener";
            }

            public static final class Broadcast {
                public static final String ENABLED = "hcjf.cloud.orchestrator.broadcast.enabled";
                public static final String TASK_NAME = "hcjf.cloud.orchestrator.broadcast.task.name";
                public static final String IP_VERSION = "hcjf.cloud.orchestrator.broadcast.ip.version";
                public static final String INTERFACE_NAME = "hcjf.cloud.orchestrator.broadcast.interface.name";
                public static final String PORT = "hcjf.cloud.orchestrator.broadcast.port";
            }
        }

        public static class TimerTask {
            public static final String MIN_VALUE_OF_DELAY = "hcjf.cloud.timer.task.min.value.of.delay";
            public static final String MAP_NAME = "hcjf.cloud.timer.task.map.name";
            public static final String MAP_SUFFIX_NAME = "hcjf.cloud.timer.task.map.suffix.name";
            public static final String LOCK_SUFFIX_NAME = "hcjf.cloud.timer.task.lock.suffix.name";
            public static final String CONDITION_SUFFIX_NAME = "hcjf.cloud.timer.task.condition.suffix.name";
        }

        public static class Cache {
            public static final String MAP_SUFFIX_NAME = "hcjf.cloud.cache.map.suffix.name";
            public static final String LOCK_SUFFIX_NAME = "hcjf.cloud.cache.lock.suffix.name";
            public static final String CONDITION_SUFFIX_NAME = "hcjf.cloud.cache.condition.suffix.name";
            public static final String SIZE_STRATEGY_MAP_SUFFIX_NAME = "hcjf.cloud.cache.size.strategy.map.suffix.name";
        }

        public static class Queue {
            public static final String LOCK_NAME_TEMPLATE = "hcjf.cloud.queue.lock.name.template";
            public static final String CONDITION_NAME_TEMPLATE = "hcjf.cloud.queue.condition.name.template";
            public static final String DEFAULT_SIZE = "hcjf.cloud.queue.default.size";
        }
    }

    public static class Cache {
        public static final String SERVICE_NAME = "hcjf.cache.service.name";
        public static final String SERVICE_PRIORITY = "hcjf.cache.service.priority";
        public static final String INVALIDATOR_TIME_OUT = "hcjf.cache.invalidator.time.out";
    }

    //Java property names
    public static final String FILE_ENCODING = "file.encoding";

    private static final SystemProperties instance;

    static {
        instance = new SystemProperties();
    }

    private final Map<String, Object> instancesCache;
    private final Gson gson;

    private SystemProperties() {
        super(new Properties());
        instancesCache = new HashMap<>();
        gson = new Gson();

        defaults.put(HCJF_DEFAULT_DATE_FORMAT, "yyyy-MM-dd HH:mm:ss");
        defaults.put(HCJF_DEFAULT_DATE_FORMAT_REGEX, "(19|20)\\d\\d([- /.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01]) ([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]");
        defaults.put(HCJF_DEFAULT_LOCAL_DATE_FORMAT_REGEX, "");
        defaults.put(HCJF_DEFAULT_NUMBER_FORMAT, "0.000");
        defaults.put(HCJF_DEFAULT_SCIENTIFIC_NUMBER_FORMAT, "0.00E00");
        defaults.put(HCJF_DEFAULT_DECIMAL_SEPARATOR, ".");
        defaults.put(HCJF_DEFAULT_GROUPING_SEPARATOR, ",");
        defaults.put(HCJF_DEFAULT_PROPERTIES_FILE_XML, "false");
        defaults.put(HCJF_UUID_REGEX, "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        defaults.put(HCJF_INTEGER_NUMBER_REGEX, "^[-]?[0-9]{1,}$");
        defaults.put(HCJF_DECIMAL_NUMBER_REGEX, "^[-]?[0-9,\\.]{0,}[0-9]{1,}$");
        defaults.put(HCJF_SCIENTIFIC_NUMBER_REGEX, "^[-]?[0-9,\\.]{0,}[0-9]{1,}E[-]?[0-9]{1,}$");
        defaults.put(HCJF_MATH_REGULAR_EXPRESSION, "[-]?(((?<subExpression>¿[\\d]+·)|(?<variable>[a-z A-Z()$_]+)|(?<decimal>[\\d]+\\.[\\d]+)|(?<integer>[\\d]+))(?<operator>[\\-+/*^%=<>! ]?))+");
        defaults.put(HCJF_MATH_CONNECTOR_REGULAR_EXPRESSION, ".*[+\\-*/%=<>!].*");
        defaults.put(HCJF_MATH_SPLITTER_REGULAR_EXPRESSION, "(?<=(\\+|\\-|\\*|/|%|=|>|<|<>|!=|>=|<=))|(?=(\\+|\\-|\\*|/|%|=|>|<|<>|!=|>=|<=))");
        defaults.put(HCJF_DEFAULT_LRU_MAP_SIZE, "1000");
        defaults.put(HCJF_DEFAULT_EXCEPTION_MESSAGE_TAG, "IMPL");
        defaults.put(HCJF_CHECKSUM_ALGORITHM, "MD5");

        defaults.put(Locale.DEFAULT_LOCALE, java.util.Locale.getDefault().toLanguageTag());
        defaults.put(Locale.DEFAULT_LOCALE_LAYER_IMPLEMENTATION_NAME, DefaultLocaleLayer.class.getName());
        defaults.put(Locale.DEFAULT_LOCALE_LAYER_IMPLEMENTATION_CLASS_NAME, DefaultLocaleLayer.class.getName());
        defaults.put(Locale.LOG_TAG, "LOCALE");

        defaults.put(Layer.LOG_TAG, "LAYER");
        defaults.put(Layer.READABLE_ALL_LAYER_IMPLEMENTATION_NAME, "system_layer");
        defaults.put(Layer.READABLE_LAYER_IMPLEMENTATION_NAME, "system_readable_layer");
        defaults.put(Layer.DISTRIBUTED_LAYER_ENABLED, "false");

        defaults.put(Service.STATIC_THREAD_NAME, "StaticServiceThread");
        defaults.put(Service.STATIC_THREAD_POOL_CORE_SIZE, "2");
        defaults.put(Service.STATIC_THREAD_POOL_MAX_SIZE, "200");
        defaults.put(Service.STATIC_THREAD_POOL_KEEP_ALIVE_TIME, "10");
        defaults.put(Service.THREAD_POOL_CORE_SIZE, "10");
        defaults.put(Service.THREAD_POOL_MAX_SIZE, "100");
        defaults.put(Service.THREAD_POOL_KEEP_ALIVE_TIME, "10");
        defaults.put(Service.GUEST_SESSION_NAME, "Guest");
        defaults.put(Service.SYSTEM_SESSION_NAME, "System");
        defaults.put(Service.SHUTDOWN_TIME_OUT, "1000");
        defaults.put(Service.MAX_ALLOCATED_MEMORY_EXPRESSED_IN_PERCENTAGE, "true");
        defaults.put(Service.MAX_ALLOCATED_MEMORY_EXCEEDED_THROWS_EXCEPTION, "false");
        defaults.put(Service.MAX_ALLOCATED_MEMORY_FOR_THREAD, "15");
        defaults.put(Service.MAX_EXECUTION_TIME_FOR_THREAD, Long.toString(10*1000*1000*1000));

        defaults.put(Event.LOG_TAG, "EVENTS");
        defaults.put(Event.SERVICE_NAME, "Events");
        defaults.put(Event.SERVICE_PRIORITY, "0");

        defaults.put(Collector.SERVICE_NAME, "Collectors");
        defaults.put(Collector.SERVICE_PRIORITY, "0");
        defaults.put(Collector.LOG_TAG, "COLLECTOR_SERVICE");
        defaults.put(Collector.FLUSH_PERIOD, Long.toString(5 * 60 * 1000));
        defaults.put(Collector.CLOUD_SAVE_MODE, "false");
        defaults.put(Collector.CLOUD_TIMER_TASK_NAME, "CollectorsFlushCycle");

        defaults.put(CodeEvaluator.Java.IMPL_NAME, "java");
        defaults.put(CodeEvaluator.Java.J_SHELL_POOL_SIZE, "5");
        defaults.put(CodeEvaluator.Java.J_SHELL_INSTANCE_TIMEOUT, "5000");
        defaults.put(CodeEvaluator.Java.SCRIPT_CACHE_SIZE, "10");
        defaults.put(CodeEvaluator.Java.IMPORTS, "[]");

        defaults.put(CodeEvaluator.Js.IMPL_NAME, "js");

        defaults.put(Cryptography.KEY,"71324dccdb58966a04507b0fe2008632940b87c6dc5cea5f4bdf0d0089524c8e");
        defaults.put(Cryptography.ALGORITHM,"AES");
        defaults.put(Cryptography.OPERATION_MODE,"GCM");
        defaults.put(Cryptography.PADDING_SCHEME,"PKCS5Padding");
        defaults.put(Cryptography.Random.IV_SIZE,"96");
        defaults.put(Cryptography.GCM.TAG_BIT_LENGTH,"128");
        defaults.put(Cryptography.AAD,"HolandaCatalinaCrypt");

        defaults.put(Encoding.SERVICE_NAME, "EncodingService");
        defaults.put(Encoding.SERVICE_PRIORITY, "1");

        defaults.put(FileSystem.SERVICE_NAME, "FileSystemWatcherService");
        defaults.put(FileSystem.SERVICE_PRIORITY, "1");
        defaults.put(FileSystem.LOG_TAG, "FILE_SYSTEM_WATCHER_SERVICE");
        defaults.put(FileSystem.POLLING_WAIT_TIME, "5000");

        defaults.put(Log.SERVICE_NAME, "LogService");
        defaults.put(Log.SERVICE_PRIORITY, "0");
        defaults.put(Log.FILE_PREFIX, "hcjf");
        defaults.put(Log.ERROR_FILE, "false");
        defaults.put(Log.WARNING_FILE, "false");
        defaults.put(Log.INFO_FILE, "false");
        defaults.put(Log.DEBUG_FILE, "false");
        defaults.put(Log.LEVEL, "1");
        defaults.put(Log.DATE_FORMAT, "yyyy-MM-dd HH:mm:ss,SSS");
        defaults.put(Log.CONSUMERS, "[]");
        defaults.put(Log.SYSTEM_OUT_ENABLED, "false");
        defaults.put(Log.JAVA_STANDARD_LOGGER_ENABLED, "false");
        defaults.put(Log.QUEUE_INITIAL_SIZE, "10000");
        defaults.put(Log.TRUNCATE_TAG, "false");
        defaults.put(Log.TRUNCATE_TAG_SIZE, "35");
        defaults.put(Log.LOG_CONSUMERS_SIZE, "50");

        defaults.put(Net.SERVICE_NAME, "Net service");
        defaults.put(Net.LOG_TAG, "NET_SERVICE");
        defaults.put(Net.INPUT_BUFFER_SIZE, "102400");
        defaults.put(Net.OUTPUT_BUFFER_SIZE, "102400");
        defaults.put(Net.CONNECTION_TIMEOUT_AVAILABLE, "true");
        defaults.put(Net.CONNECTION_TIMEOUT, "30000");
        defaults.put(Net.DISCONNECT_AND_REMOVE, "true");
        defaults.put(Net.WRITE_TIMEOUT, "100");
        defaults.put(Net.IO_UDP_LRU_ADDRESSES_SIZE, "1000");
        defaults.put(Net.IO_UDP_LRU_SESSIONS_SIZE, "1000");
        defaults.put(Net.IO_QUEUE_SIZE, "1000000");
        defaults.put(Net.IO_THREAD_POOL_KEEP_ALIVE_TIME, "120");
        defaults.put(Net.IO_THREAD_POOL_NAME, "IoThreadPool");
        defaults.put(Net.DEFAULT_INPUT_BUFFER_SIZE, "102400");
        defaults.put(Net.DEFAULT_OUTPUT_BUFFER_SIZE, "102400");
        defaults.put(Net.IO_THREAD_DIRECT_ALLOCATE_MEMORY, "false");
        defaults.put(Net.SSL_MAX_IO_THREAD_POOL_SIZE, "2");
        defaults.put(Net.PORT_PROVIDER_TIME_WINDOWS_SIZE, "15000");
        defaults.put(Net.PORT_PROBE_CONNECTION_TIMEOUT, "1000");
        defaults.put(Net.REMOTE_ADDRESS_INTO_NET_PACKAGE, "false");
        defaults.put(Net.REMOTE_ADDRESS_INTO_NET_SESSION, "false");
        defaults.put(Net.NIO_SELECTOR_HEALTH_CHECKER_RUNNING_TIME, "1000");
        defaults.put(Net.NIO_SELECTOR_HEALTH_CHECKER_SAMPLE_TIME, "2000");
        defaults.put(Net.NIO_SELECTOR_HEALTH_CHECKER_DANGEROUS_THRESHOLD, "60");
        defaults.put(Net.NIO_SELECTOR_HEALTH_CHECKER_DANGEROUS_REPEATS, "5");
        defaults.put(Net.NIO_SELECTOR_HEALTH_CHECKER_DANGEROUS_ACTION, "RECREATE_SELECTOR"); //Valid values [RECREATE_SELECTOR, SHUTDOWN, VOID]
        defaults.put(Net.Http.HOST_ACCESS_CONTROL_REGEX_START_CHAR,"^");
        defaults.put(Net.Http.CLIENT_RESPONSE_HANDLER_QUEUE_SIZE, "1000");

        defaults.put(Net.Broadcast.SERVICE_NAME, "Broadcast service");
        defaults.put(Net.Broadcast.LOG_TAG, "BROADCAST");
        defaults.put(Net.Broadcast.INTERFACE_NAME, "eth0");
        defaults.put(Net.Broadcast.IP_VERSION, "4");
        defaults.put(Net.Broadcast.SENDER_DELAY, "30000");
        defaults.put(Net.Broadcast.SIGNATURE_ALGORITHM, "SHA-1");
        defaults.put(Net.Broadcast.RECEIVER_BUFFER_SIZE, "1024");

        defaults.put(Net.KubernetesSpy.SERVICE_NAME, "Kubernetes Spy Service");
        defaults.put(Net.KubernetesSpy.LOG_TAG, "KUBERNETES_SPY");
        defaults.put(Net.KubernetesSpy.CLIENT_CONNECTION_TIMEOUT, "10000");
        defaults.put(Net.KubernetesSpy.TASK_SLEEP_TIME, "5000");
        defaults.put(Net.KubernetesSpy.CURL_COMMAND, "curl");
        defaults.put(Net.KubernetesSpy.CURL_COMMAND_AUTHENTICATION_HEADER, "-H 'Authorization: Bearer %s'");
        defaults.put(Net.KubernetesSpy.CURL_COMMAND_CACERT_PARAMETER, "--cacert %s");
        defaults.put(Net.KubernetesSpy.CACERT_FILE_PATH, "/var/run/secrets/kubernetes.io/serviceaccount/ca.crt");
        defaults.put(Net.KubernetesSpy.TOKEN_FILE_PATH, "/var/run/secrets/kubernetes.io/serviceaccount/token");
        defaults.put(Net.KubernetesSpy.NAMESPACE_FILE_PATH, "/var/run/secrets/kubernetes.io/serviceaccount/namespace");
        defaults.put(Net.KubernetesSpy.MASTER_NODE_HOST, "KUBERNETES_PORT_443_TCP_ADDR");
        defaults.put(Net.KubernetesSpy.MASTER_NODE_PORT, "KUBERNETES_SERVICE_PORT");
        defaults.put(Net.KubernetesSpy.EndPoints.LIST_PODS, "https://%s:%s/api/v1/namespaces/%s/pods");
        defaults.put(Net.KubernetesSpy.EndPoints.LIST_SERVICES, "https://%s:%s/api/v1/namespaces/%s/services");
        defaults.put(Net.KubernetesSpy.EndPoints.LIST_END_POINTS, "https://%s:%s/api/v1/namespaces/%s/endpoints");
        defaults.put(Net.KubernetesSpy.AUTHORIZATION_HEADER, "Bearer %s");
        defaults.put(Net.KubernetesSpy.JSON_DATE_FORMAT, "yyyy-MM-dd'T'HH:mm:ss'Z'");

        defaults.put(Net.Ssl.DEFAULT_KEY_PASSWORD, "hcjfkeypassword");
        defaults.put(Net.Ssl.DEFAULT_KEY_TYPE, "JKS");
        defaults.put(Net.Ssl.DEFAULT_KEYSTORE_PASSWORD, "hcjfkeystorepassword");
        defaults.put(Net.Ssl.DEFAULT_KEYSTORE_FILE_PATH, "/home/javaito/Git/HolandaCatalinaFw/src/main/resources/org/hcjf/io/net/https/keystore.jks");
        defaults.put(Net.Ssl.DEFAULT_TRUSTED_CERTS_FILE_PATH, "/home/javaito/Git/HolandaCatalinaFw/src/main/resources/org/hcjf/io/net/https/cacerts.jks");
        defaults.put(Net.Ssl.DEFAULT_PROTOCOL, "TLSv1.2");
        defaults.put(Net.Ssl.IO_THREAD_NAME, "SslIoThread");
        defaults.put(Net.Ssl.ENGINE_THREAD_NAME, "SslEngineThread");

        defaults.put(Net.Messages.LOG_TAG, "MESSAGES");
        defaults.put(Net.Messages.SERVER_DECOUPLED_IO_ACTION, "true");
        defaults.put(Net.Messages.SERVER_IO_QUEUE_SIZE, "100000");
        defaults.put(Net.Messages.SERVER_IO_WORKERS, "5");

        defaults.put(Net.Http.INPUT_LOG_ENABLED, "false");
        defaults.put(Net.Http.OUTPUT_LOG_ENABLED, "false");
        defaults.put(Net.Http.LOG_TAG, "HTTP_SERVER");
        defaults.put(Net.Http.SERVER_NAME, "HCJF Web Server");
        defaults.put(Net.Http.RESPONSE_DATE_HEADER_FORMAT_VALUE, "EEE, dd MMM yyyy HH:mm:ss z");
        defaults.put(Net.Http.INPUT_LOG_BODY_MAX_LENGTH, "128");
        defaults.put(Net.Http.OUTPUT_LOG_BODY_MAX_LENGTH, "128");
        defaults.put(Net.Http.DEFAULT_SERVER_PORT, "80");
        defaults.put(Net.Http.DEFAULT_CLIENT_PORT, "80");
        defaults.put(Net.Http.STREAMING_LIMIT_FILE_SIZE, "10240");
        defaults.put(Net.Http.DEFAULT_ERROR_FORMAT_SHOW_STACK, "true");
        defaults.put(Net.Http.DEFAULT_CLIENT_CONNECT_TIMEOUT, "10000");
        defaults.put(Net.Http.DEFAULT_CLIENT_READ_TIMEOUT, "10000");
        defaults.put(Net.Http.DEFAULT_CLIENT_WRITE_TIMEOUT, "10000");
        defaults.put(Net.Http.DEFAULT_GUEST_SESSION_NAME, "Http guest session");
        defaults.put(Net.Http.DEFAULT_FILE_CHECKSUM_ALGORITHM, "MD5");
        defaults.put(Net.Http.ENABLE_AUTOMATIC_RESPONSE_CONTENT_LENGTH, "true");
        defaults.put(Net.Http.AUTOMATIC_CONTENT_LENGTH_SKIP_CODES, "[304]");
        defaults.put(Net.Http.MAX_PACKAGE_SIZE, Integer.toString(20 * 1024 * 1024));
        defaults.put(Net.Http.SERVER_DECOUPLED_IO_ACTION, "true");
        defaults.put(Net.Http.SERVER_IO_QUEUE_SIZE, "100000");
        defaults.put(Net.Http.SERVER_IO_WORKERS, "5");

        defaults.put(Net.Https.DEFAULT_SERVER_PORT, "443");
        defaults.put(Net.Https.DEFAULT_CLIENT_PORT, "443");

        defaults.put(Net.Http.Http2.HEADER_TABLE_SIZE, "4096");
        defaults.put(Net.Http.Http2.ENABLE_PUSH, "true");
        defaults.put(Net.Http.Http2.MAX_CONCURRENT_STREAMS, "-1");
        defaults.put(Net.Http.Http2.INITIAL_WINDOWS_SIZE, "65535");
        defaults.put(Net.Http.Http2.MAX_FRAME_SIZE, "16384");
        defaults.put(Net.Http.Http2.MAX_HEADER_LIST_SIZE, "-1");
        defaults.put(Net.Http.Http2.STREAM_FRAMES_QUEUE_MAX_SIZE, "10");

        defaults.put(Net.Http.Folder.LOG_TAG, "FOLDER_CONTEXT");
        defaults.put(Net.Http.Folder.FORBIDDEN_CHARACTERS, "[]");
        defaults.put(Net.Http.Folder.FILE_EXTENSION_REGEX, "\\.(?=[^\\.]+$)");
        defaults.put(Net.Http.Folder.DEFAULT_HTML_DOCUMENT, "<!DOCTYPE html><html><head><title>%s</title><body>%s</body></html></head>");
        defaults.put(Net.Http.Folder.DEFAULT_HTML_BODY, "<table>%s</table>");
        defaults.put(Net.Http.Folder.DEFAULT_HTML_ROW, "<tr><th><a href=\"%s\">%s</a></th></tr>");
        defaults.put(Net.Http.Folder.ZIP_CONTAINER, System.getProperty("user.home"));
        defaults.put(Net.Http.Folder.ZIP_TEMP_PREFIX, "hcjf_zip_temp");
        defaults.put(Net.Http.Folder.JAR_CONTAINER, System.getProperty("user.home"));
        defaults.put(Net.Http.Folder.JAR_TEMP_PREFIX, "hcjf_jar_temp");

        defaults.put(Net.Http.EndPoint.Json.DATE_FORMATS, " [dd/M/yyyy,dd/MM/yyyy]");

        defaults.put(Net.Http.DataSources.SERVICE_NAME, "DataSourcesService");
        defaults.put(Net.Http.DataSources.SERVICE_PRIORITY, "0");
        defaults.put(Net.Http.DataSources.THREAD_POOL_ENABLED, "true");
        defaults.put(Net.Http.DataSources.THREAD_POOL_CORE_SIZE, "200");
        defaults.put(Net.Http.DataSources.THREAD_POOL_MAX_SIZE, "500");
        defaults.put(Net.Http.DataSources.THREAD_POOL_KEEP_ALIVE_TIME, "60");

        defaults.put(Net.Rest.DEFAULT_MIME_TYPE, "application/json");
        defaults.put(Net.Rest.DEFAULT_ENCODING_IMPL, "hcjf");
        defaults.put(Net.Rest.QUERY_PATH, "query");
        defaults.put(Net.Rest.QUERY_PARAMETER, "q");
        defaults.put(Net.Rest.BODY_FIELD, "_body");
        defaults.put(Net.Rest.QUERY_FIELD, "_query");
        defaults.put(Net.Rest.QUERIES_FIELD, "_queries");
        defaults.put(Net.Rest.DATA_SOURCE_FIELD, "_dataSource");
        defaults.put(Net.Rest.COMMAND_FIELD, "_command");
        defaults.put(Net.Rest.COMMANDS_FIELD, "_commands");

        defaults.put(ProcessDiscovery.LOG_TAG, "PROCESS_DISCOVERY");
        defaults.put(ProcessDiscovery.SERVICE_NAME, "Process Discovery Service");
        defaults.put(ProcessDiscovery.SERVICE_PRIORITY, "1");
        defaults.put(ProcessDiscovery.DELAY, "3000");

        defaults.put(Query.SINGLE_PATTERN, "SELECT * FROM %s");
        defaults.put(Query.LOG_TAG, "QUERY");
        defaults.put(Query.DEFAULT_LIMIT, "1000");
        defaults.put(Query.DEFAULT_DESC_ORDER, "false");
        defaults.put(Query.SELECT_REGULAR_EXPRESSION, "(?i)^(?<environment>environment[ ]{1,}'¡[0-9]{1,}·'[ ]{1,}){0,1}(?<select>select[ ]{1,}[a-zA-Z_0-9'=<>!,.~+-/*\\|%\\$&¡¿·@ ]{1,})(?<from>[  ]?from[  ](?<resourceValue>[a-zA-Z_0-9$¡¿·'.]{1,})(?<dynamicResource> as (?<dynamicResourceAlias>[a-zA-Z_0-9$¡¿·.]{1,}[ ]?)|[ ]?))(?<conditionalBody>[a-zA-Z_0-9'=,.~+-/\\|* ?%\\$&¡¿·@<>!\\:\\-()\\[\\]]{1,})?[$;]?");
        defaults.put(Query.CONDITIONAL_REGULAR_EXPRESSION, "(?i)((?<=(^((hash )?(inner |left |right |full )?join )|^where |^limit |^start |^order by |^group by |^disjoint by |^underlying |(( (hash )?(inner |left |right |full )?join ))| where | limit | start | order by | group by | disjoint by | underlying )))|(?=(^((hash )?(inner |left |right |full )?join )|^where |^limit |^start |^order by |^group by |^disjoint by |^underlying |(( (hash )?(inner |left |right |full )?join ))| where | limit | start | order by | group by | disjoint by | underlying ))");
        defaults.put(Query.EVALUATOR_COLLECTION_REGULAR_EXPRESSION, "(?i)((?<=( and | or ))|(?=( and | or )))");
        defaults.put(Query.OPERATION_REGULAR_EXPRESSION, "(?i)(?<=(=|<>|!=|>|<|>=|<=| in | not in | like ))|(?=(=|<>|!=|>|<|>=|<=| in | not in | like ))");
        defaults.put(Query.JOIN_REGULAR_EXPRESSION, "(?i)(((?<resourceValue>[a-zA-Z_0-9$¡¿·.]{1,})(?<dynamicResource>[ ]as[ ](?<dynamicResourceAlias>[a-zA-Z_0-9.]{1,})|[ ]?)) on (?<conditionalBody>[a-zA-Z_0-9'=,.~+-\\/* ?%\\$&¡¿·@<>!\\:\\-()\\[\\]]{1,}))");
        defaults.put(Query.JOIN_RESOURCE_VALUE_INDEX, "resourceValue");
        defaults.put(Query.JOIN_DYNAMIC_RESOURCE_INDEX, "dynamicResource");
        defaults.put(Query.JOIN_DYNAMIC_RESOURCE_ALIAS_INDEX, "dynamicResourceAlias");
        defaults.put(Query.JOIN_CONDITIONAL_BODY_INDEX, "conditionalBody");
        defaults.put(Query.UNION_REGULAR_EXPRESSION, "(?i)((?<=( union ))|(?=( union )))");
        defaults.put(Query.SOURCE_REGULAR_EXPRESSION, "(?i)((?<=( src ))|(?=( src )))");
        defaults.put(Query.AS_REGULAR_EXPRESSION, "(?i)((?<=( as ))|(?=( as )))");
        defaults.put(Query.DESC_REGULAR_EXPRESSION, "(?i)((?<=( desc| asc))|(?=( desc| asc)))");
        defaults.put(Query.ENVIRONMENT_GROUP_INDEX, "environment");
        defaults.put(Query.SELECT_GROUP_INDEX, "select");
        defaults.put(Query.FROM_GROUP_INDEX, "from");
        defaults.put(Query.CONDITIONAL_GROUP_INDEX, "conditionalBody");
        defaults.put(Query.RESOURCE_VALUE_INDEX, "resourceValue");
        defaults.put(Query.DYNAMIC_RESOURCE_INDEX, "dynamicResource");
        defaults.put(Query.DYNAMIC_RESOURCE_ALIAS_INDEX, "dynamicResourceAlias");
        defaults.put(Query.JOIN_RESOURCE_NAME_INDEX, "0");
        defaults.put(Query.JOIN_EVALUATORS_INDEX, "1");
        defaults.put(Query.DATE_FORMAT, "yyyy-MM-dd HH:mm:ss");
        defaults.put(Query.DECIMAL_SEPARATOR, ".");
        defaults.put(Query.DECIMAL_FORMAT, "0.000");
        defaults.put(Query.SCIENTIFIC_NOTATION, "E");
        defaults.put(Query.SCIENTIFIC_NOTATION_FORMAT, "0.0E0");
        defaults.put(Query.EVALUATORS_CACHE_NAME, "__evaluators__cache__");
        defaults.put(Query.EVALUATOR_LEFT_VALUES_CACHE_NAME, "__evaluator__left__values__cache__");
        defaults.put(Query.EVALUATOR_RIGHT_VALUES_CACHE_NAME, "__evaluator__right__values__cache__");
        defaults.put(Query.COMPILER_CACHE_SIZE, "1000");
        defaults.put(Query.DEFAULT_COMPILER, "SQL");
        defaults.put(Query.DEFAULT_SERIALIZER, "SQL");
        defaults.put(Query.ReservedWord.ENVIRONMENT, "ENVIRONMENT");
        defaults.put(Query.ReservedWord.SELECT, "SELECT");
        defaults.put(Query.ReservedWord.FROM, "FROM");
        defaults.put(Query.ReservedWord.JOIN, "JOIN");
        defaults.put(Query.ReservedWord.UNION, "UNION");
        defaults.put(Query.ReservedWord.FULL, "FULL");
        defaults.put(Query.ReservedWord.INNER, "INNER");
        defaults.put(Query.ReservedWord.LEFT, "LEFT");
        defaults.put(Query.ReservedWord.RIGHT, "RIGHT");
        defaults.put(Query.ReservedWord.HASH, "HASH");
        defaults.put(Query.ReservedWord.ON, "ON");
        defaults.put(Query.ReservedWord.WHERE, "WHERE");
        defaults.put(Query.ReservedWord.ORDER_BY, "ORDER BY");
        defaults.put(Query.ReservedWord.DESC, "DESC");
        defaults.put(Query.ReservedWord.LIMIT, "LIMIT");
        defaults.put(Query.ReservedWord.START, "START");
        defaults.put(Query.ReservedWord.RETURN_ALL, "*");
        defaults.put(Query.ReservedWord.ARGUMENT_SEPARATOR, ",");
        defaults.put(Query.ReservedWord.EQUALS, "=");
        defaults.put(Query.ReservedWord.DISTINCT, "<>");
        defaults.put(Query.ReservedWord.DISTINCT_2, "!=");
        defaults.put(Query.ReservedWord.GREATER_THAN, ">");
        defaults.put(Query.ReservedWord.GREATER_THAN_OR_EQUALS, ">=");
        defaults.put(Query.ReservedWord.SMALLER_THAN, "<");
        defaults.put(Query.ReservedWord.SMALLER_THAN_OR_EQUALS, "<=");
        defaults.put(Query.ReservedWord.IN, "IN");
        defaults.put(Query.ReservedWord.NOT_IN, "NOT IN");
        defaults.put(Query.ReservedWord.NOT, "NOT");
        defaults.put(Query.ReservedWord.NOT_2, "!");
        defaults.put(Query.ReservedWord.LIKE, "LIKE");
        defaults.put(Query.ReservedWord.LIKE_WILDCARD, "%");
        defaults.put(Query.ReservedWord.AND, "AND");
        defaults.put(Query.ReservedWord.OR, "OR");
        defaults.put(Query.ReservedWord.STATEMENT_END, ";");
        defaults.put(Query.ReservedWord.REPLACEABLE_VALUE, "?");
        defaults.put(Query.ReservedWord.STRING_DELIMITER, "'");
        defaults.put(Query.ReservedWord.NULL, "NULL");
        defaults.put(Query.ReservedWord.TRUE, "TRUE");
        defaults.put(Query.ReservedWord.FALSE, "FALSE");
        defaults.put(Query.ReservedWord.AS, "AS");
        defaults.put(Query.ReservedWord.GROUP_BY, "GROUP BY");
        defaults.put(Query.ReservedWord.DISJOINT_BY, "DISJOINT BY");
        defaults.put(Query.ReservedWord.UNDERLYING, "UNDERLYING");
        defaults.put(Query.ReservedWord.SRC, "SRC");
        defaults.put(Query.Function.NAME_PREFIX, "query.");
        defaults.put(Query.Function.MATH_FUNCTION_NAME, "math");
        defaults.put(Query.Function.STRING_FUNCTION_NAME, "string");
        defaults.put(Query.Function.DATE_FUNCTION_NAME, "date");
        defaults.put(Query.Function.MATH_EVAL_EXPRESSION_NAME, "evalExpression");
        defaults.put(Query.Function.MATH_ADDITION, "+");
        defaults.put(Query.Function.MATH_SUBTRACTION, "-");
        defaults.put(Query.Function.MATH_MULTIPLICATION, "*");
        defaults.put(Query.Function.MATH_DIVISION, "/");
        defaults.put(Query.Function.MATH_MODULUS, "%");
        defaults.put(Query.Function.MATH_EQUALS, "=");
        defaults.put(Query.Function.MATH_DISTINCT, "!=");
        defaults.put(Query.Function.MATH_DISTINCT_2, "<>");
        defaults.put(Query.Function.MATH_GREATER_THAN, ">");
        defaults.put(Query.Function.MATH_GREATER_THAN_OR_EQUALS, ">=");
        defaults.put(Query.Function.MATH_LESS_THAN,"<");
        defaults.put(Query.Function.MATH_LESS_THAN_OR_EQUALS, "<=");
        defaults.put(Query.Function.REFERENCE_FUNCTION_NAME, "reference");
        defaults.put(Query.Function.BSON_FUNCTION_NAME, "bson");
        defaults.put(Query.Function.COLLECTION_FUNCTION_NAME, "collection");
        defaults.put(Query.Function.OBJECT_FUNCTION_NAME, "object");
        defaults.put(Query.Function.BIG_DECIMAL_DIVIDE_SCALE, "8");
        defaults.put(Query.Function.MATH_OPERATION_RESULT_ROUND, "true");
        defaults.put(Query.Function.MATH_OPERATION_RESULT_ROUND_CONTEXT, "128");

        defaults.put(Cloud.SERVICE_NAME, "CloudService");
        defaults.put(Cloud.SERVICE_PRIORITY, "0");
        defaults.put(Cloud.IMPL, DefaultCloudServiceImpl.class.getName());
        defaults.put(Cloud.LOG_TAG, "CLOUD");
        defaults.put(Cloud.Orchestrator.SERVICE_NAME, "CloudDefaultImplService");
        defaults.put(Cloud.Orchestrator.AVAILABLE, "false");
        defaults.put(Cloud.Orchestrator.SERVICE_PRIORITY, "0");
        defaults.put(Cloud.Orchestrator.SERVER_LISTENER_PORT, "18080");
        defaults.put(Cloud.Orchestrator.CONNECTION_LOOP_WAIT_TIME, "5000");
        defaults.put(Cloud.Orchestrator.NODE_LOST_TIMEOUT, "1800000");
        defaults.put(Cloud.Orchestrator.ACK_TIMEOUT, "2000");
        defaults.put(Cloud.Orchestrator.REORGANIZATION_TIMEOUT, "2000");
        defaults.put(Cloud.Orchestrator.REORGANIZATION_WARNING_TIME_LIMIT, "1500");
        defaults.put(Cloud.Orchestrator.WAGON_TIMEOUT, "10000");
        defaults.put(Cloud.Orchestrator.INVOKE_TIMEOUT, "120000");
        defaults.put(Cloud.Orchestrator.TEST_NODE_TIMEOUT, "2000");
        defaults.put(Cloud.Orchestrator.REPLICATION_FACTOR, "2");
        defaults.put(Cloud.Orchestrator.NODES, "[]");
        defaults.put(Cloud.Orchestrator.SERVICE_END_POINTS, "[]");
        defaults.put(Cloud.Orchestrator.SERVICE_PUBLICATION_REPLICAS_BROADCASTING_ENABLED, "true");
        defaults.put(Cloud.Orchestrator.SERVICE_PUBLICATION_REPLICAS_BROADCASTING_TIMEOUT, "2000");
        defaults.put(Cloud.Orchestrator.NETWORKING_HANDSHAKE_DETAILS_AVAILABLE, "false");
        defaults.put(Cloud.Orchestrator.CLUSTER_NAME, "hcjf");
        defaults.put(Cloud.Orchestrator.ThisNode.READABLE_LAYER_IMPLEMENTATION_NAME, "system_cloud_node");
        defaults.put(Cloud.Orchestrator.ThisNode.NAME, "hcjf-node");
        defaults.put(Cloud.Orchestrator.ThisNode.VERSION, "0");
        defaults.put(Cloud.Orchestrator.ThisNode.LAN_ADDRESS, "127.0.0.1");
        defaults.put(Cloud.Orchestrator.ThisNode.LAN_PORT, "18080");
        defaults.put(Cloud.Orchestrator.ThisServiceEndPoint.READABLE_LAYER_IMPLEMENTATION_NAME, "system_cloud_service");
        defaults.put(Cloud.Orchestrator.ThisServiceEndPoint.PUBLICATION_TIMEOUT, "3600000");
        defaults.put(Cloud.Orchestrator.ThisServiceEndPoint.DISTRIBUTED_EVENT_LISTENER, "false");
        defaults.put(Cloud.Orchestrator.Broadcast.ENABLED, "false");
        defaults.put(Cloud.Orchestrator.Broadcast.TASK_NAME, "Cloud discovery");
        defaults.put(Cloud.Orchestrator.Broadcast.IP_VERSION, "4");
        defaults.put(Cloud.Orchestrator.Broadcast.INTERFACE_NAME, "eth0");
        defaults.put(Cloud.Orchestrator.Broadcast.PORT, "16000");
        defaults.put(Cloud.Orchestrator.Kubernetes.ENABLED, "false");
        defaults.put(Cloud.Orchestrator.Kubernetes.POD_LABELS, "[]");
        defaults.put(Cloud.Orchestrator.Kubernetes.SERVICE_LABELS, "[]");
        defaults.put(Cloud.Orchestrator.Kubernetes.SERVICE_PORT_NAME, "hcjf-k8s-port");
        defaults.put(Cloud.Orchestrator.Kubernetes.ALLOW_PHASES, "[Running]");
        defaults.put(Cloud.Orchestrator.Events.LOG_TAG, "DISTRIBUTED_EVENT");
        defaults.put(Cloud.Orchestrator.Events.TIMEOUT, "3000");
        defaults.put(Cloud.Orchestrator.Events.ATTEMPTS, "5");
        defaults.put(Cloud.Orchestrator.Events.SLEEP_PERIOD_BETWEEN_ATTEMPTS, "3000");
        defaults.put(Cloud.Orchestrator.Events.STORE_STRATEGY, "default");

        defaults.put(Cloud.TimerTask.MIN_VALUE_OF_DELAY, "10000");
        defaults.put(Cloud.TimerTask.MAP_NAME, "hcjf.cloud.timer.task.map");
        defaults.put(Cloud.TimerTask.MAP_SUFFIX_NAME, "hcjf.cloud.timer.task.map.");
        defaults.put(Cloud.TimerTask.LOCK_SUFFIX_NAME, "hcjf.cloud.timer.task.lock.");
        defaults.put(Cloud.TimerTask.CONDITION_SUFFIX_NAME, "hcjf.cloud.timer.task.condition.");
        defaults.put(Cloud.Cache.MAP_SUFFIX_NAME, "hcjf.cloud.cache.map.");
        defaults.put(Cloud.Cache.LOCK_SUFFIX_NAME, "hcjf.cloud.cache.lock.");
        defaults.put(Cloud.Cache.CONDITION_SUFFIX_NAME, "hcjf.cloud.cache.condition.");
        defaults.put(Cloud.Cache.SIZE_STRATEGY_MAP_SUFFIX_NAME, "hcjf.cloud.cache.size.strategy.map.");
        defaults.put(Cloud.Queue.LOCK_NAME_TEMPLATE, "hcjf.cloud.queue.lock.name.%s");
        defaults.put(Cloud.Queue.CONDITION_NAME_TEMPLATE, "hcjf.cloud.queue.condition.name.%s");
        defaults.put(Cloud.Queue.DEFAULT_SIZE, "100000");

        defaults.put(Cache.SERVICE_NAME, "Cache");
        defaults.put(Cache.SERVICE_PRIORITY, "0");
        defaults.put(Cache.INVALIDATOR_TIME_OUT, "30000");

        Properties system = System.getProperties();
        putAll(system);
        System.setProperties(this);
    }

    /**
     * Put the default value for a property.
     * @param propertyName Property name.
     * @param defaultValue Property default value.
     * @throws NullPointerException Throw a {@link NullPointerException} when the
     * property name or default value are null.
     */
    public static void putDefaultValue(String propertyName, String defaultValue) {
        if(propertyName == null) {
            throw new NullPointerException("Invalid property name null");
        }

        if(defaultValue == null) {
            throw new NullPointerException("Invalid default value null");
        }

        instance.defaults.put(propertyName, defaultValue);
    }

    /**
     * Calls the Hashtable method {@code put}. Provided for
     * parallelism with the getProperty method. Enforces use of
     * strings for property keys and values. The value returned is the
     * result of the Hashtable call to {@code put}.
     *
     * @param key   the key to be placed into this property list.
     * @param value the value corresponding to key.
     * @return the previous value of the specified key in this property
     * list, or {@code null} if it did not have one.
     * @see #getProperty
     * @since 1.2
     */
    @Override
    public synchronized Object setProperty(String key, String value) {
        Object result = super.setProperty(key, value);

        synchronized (instancesCache) {
            instancesCache.remove(key);
        }

        try {
            //TODO: Create listeners
        } catch (Exception ex){}

        return result;
    }

    /**
     * This method return the string value of the system property
     * named like the parameter.
     * @param propertyName Name of the find property.
     * @param validator Property validator.
     * @return Return the value of the property or null if the property is no defined.
     */
    public static String get(String propertyName, PropertyValueValidator<String> validator) {
        String result = System.getProperty(propertyName);

        if(result == null) {
            org.hcjf.log.Log.w("PROPERTIES", "Property not found: %s",  propertyName);
        }

        if(validator != null) {
            if(!validator.validate(result)){
                throw new IllegalPropertyValueException(propertyName + "=" + result);
            }
        }

        return result;
    }

    /**
     * This method return the string value of the system property
     * named like the parameter.
     * @param propertyName Name of the find property.
     * @return Return the value of the property or null if the property is no defined.
     */
    public static String get(String propertyName) {
        return get(propertyName, null);
    }

    /**
     * This method return the value of the system property as boolean.
     * @param propertyName Name of the find property.
     * @return Value of the system property as boolean, or null if the property is not found.
     */
    public static Boolean getBoolean(String propertyName) {
        return getBoolean(propertyName, null);
    }

    /**
     * This method return the value of the system property as boolean.
     * @param propertyName Name of the find property.
     * @param defaultValue If the property value is null then the method returns a default value.
     * @return Value of the system property as boolean, or null if the property is not found.
     */
    public static Boolean getBoolean(String propertyName, Boolean defaultValue) {
        Boolean result = null;

        synchronized (instance.instancesCache) {
            result = (Boolean) instance.instancesCache.get(propertyName);
            if (result == null) {
                String propertyValue = get(propertyName);
                try {
                    if (propertyValue != null) {
                        result = Boolean.valueOf(propertyValue);
                        instance.instancesCache.put(propertyName, result);
                    }
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a boolean valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        if(result == null) {
            result = defaultValue;
        }

        return result;
    }

    /**
     * This method return the value of the system property as integer.
     * @param propertyName Name of the find property.
     * @return Value of the system property as integer, or null if the property is not found.
     */
    public static Integer getInteger(String propertyName) {
        return getInteger(propertyName, null);
    }

    /**
     * This method return the value of the system property as integer.
     * @param propertyName Name of the find property.
     * @param defaultValue If the property value is null then the method returns a default value.
     * @return Value of the system property as integer, or null if the property is not found.
     */
    public static Integer getInteger(String propertyName, Integer defaultValue) {
        Integer result = null;

        synchronized (instance.instancesCache) {
            result = (Integer) instance.instancesCache.get(propertyName);
            if (result == null) {
                String propertyValue = get(propertyName);
                try {
                    if (propertyValue != null) {
                        result = Integer.decode(propertyValue);
                        instance.instancesCache.put(propertyName, result);
                    }
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a integer valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        if(result == null) {
            result = defaultValue;
        }

        return result;
    }

    /**
     * This method return the value of the system property as long.
     * @param propertyName Name of the find property.
     * @return Value of the system property as long, or null if the property is not found.
     */
    public static Long getLong(String propertyName) {
        return getLong(propertyName, null);
    }

    /**
     * This method return the value of the system property as long.
     * @param propertyName Name of the find property.
     * @param defaultValue If the property value is null then the method returns a default value.
     * @return Value of the system property as long, or null if the property is not found.
     */
    public static Long getLong(String propertyName, Long defaultValue) {
        Long result = null;

        synchronized (instance.instancesCache) {
            result = (Long) instance.instancesCache.get(propertyName);
            if (result == null) {
                String propertyValue = get(propertyName);
                try {
                    if (propertyValue != null) {
                        result = Long.decode(propertyValue);
                        instance.instancesCache.put(propertyName, result);
                    }
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a long valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        if(result == null) {
            result = defaultValue;
        }

        return result;
    }

    /**
     * This method return the value of the system property as double.
     * @param propertyName Name of the find property.
     * @return Value of the system property as double, or null if the property is not found.
     */
    public static Double getDouble(String propertyName) {
        return getDouble(propertyName, null);
    }

    /**
     * This method return the value of the system property as double.
     * @param propertyName Name of the find property.
     * @param defaultValue If the property value is null then the method returns a default value.
     * @return Value of the system property as double, or null if the property is not found.
     */
    public static Double getDouble(String propertyName, Double defaultValue) {
        Double result = null;

        synchronized (instance.instancesCache) {
            result = (Double) instance.instancesCache.get(propertyName);
            if(result == null) {
                String propertyValue = get(propertyName);
                try {
                    if (propertyValue != null) {
                        result = Double.valueOf(propertyValue);
                        instance.instancesCache.put(propertyName, result);
                    }
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a double valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        if(result == null) {
            result = defaultValue;
        }

        return result;
    }

    /**
     * This method return the value of the system property as {@link UUID} instance.
     * @param propertyName Name of the find property.
     * @return Value of the system property as {@link UUID} instance, or null if the property is not found.
     */
    public static UUID getUUID(String propertyName) {
        UUID result = null;

        synchronized (instance.instancesCache) {
            result = (UUID) instance.instancesCache.get(propertyName);
            if(result == null) {
                String propertyValue = get(propertyName);
                try {
                    if (propertyValue != null) {
                        result = UUID.fromString(propertyValue);
                        instance.instancesCache.put(propertyName, result);
                    }
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a UUID valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }

        return result;
    }

    /**
     * This method return the value of the system property as {@link Path} instance.
     * @param propertyName Name of the find property.
     * @return Value of the system property as {@link Path} instance, or null if the property is not found.
     */
    public static Path getPath(String propertyName) {
        Path result = null;

        synchronized (instance.instancesCache) {
            result = (Path) instance.instancesCache.get(propertyName);
            if(result == null) {
                String propertyValue = get(propertyName);
                try {
                    if (propertyValue != null) {
                        result = Paths.get(propertyValue);
                        instance.instancesCache.put(propertyName, result);
                    }
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a path valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }

        return result;
    }

    /**
     * This method return the value of the system property as class.
     * @param propertyName Name of the find property.
     * @param <O> Type of the class instance expected.
     * @return Class instance.
     */
    public static <O extends Object> Class<O> getClass(String propertyName) {
        Class<O> result;

        synchronized (instance.instancesCache) {
            result = (Class<O>) instance.instancesCache.get(propertyName);
            if(result == null) {
                String propertyValue = get(propertyName);
                try {
                    if(propertyValue != null) {
                        result = (Class<O>) Class.forName(propertyValue);
                        instance.instancesCache.put(propertyName, result);
                    }
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a class name valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }

        return result;
    }

    /**
     * Return the default charset of the JVM instance.
     * @return Default charset.
     */
    public static String getDefaultCharset() {
        return System.getProperty(FILE_ENCODING);
    }

    /**
     * This method return the value of the property as Locale instance.
     * The instance returned will be stored on near cache and will be removed when the
     * value of the property has been updated.
     * @param propertyName Name of the property that contains locale representation.
     * @return Locale instance.
     */
    public static java.util.Locale getLocale(String propertyName) {
        java.util.Locale result;
        synchronized (instance.instancesCache) {
            result = (java.util.Locale) instance.instancesCache.get(propertyName);
            if(result == null) {
                String propertyValue = get(propertyName);
                try {
                    result = java.util.Locale.forLanguageTag(propertyValue);
                    instance.instancesCache.put(propertyName, result);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a locale tag valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        return result;
    }

    /**
     * This method return the valuo of the property called 'hcjf.default.locale' as a locale instance.
     * The instance returned will be stored on near cache and will be removed when the
     * value of the property has been updated.
     * @return Locale instance.
     */
    public static java.util.Locale getLocale() {
        return getLocale(Locale.DEFAULT_LOCALE);
    }

    /**
     * This method return the value of the property as a DecimalFormat instnace.
     * The instance returned will be stored on near cache and will be removed when the
     * value of the property has been updated.
     * @param propertyName Name of the property that contains decimal pattern.
     * @return DecimalFormat instance.
     */
    public static DecimalFormat getDecimalFormat(String propertyName) {
        DecimalFormat result;
        synchronized (instance.instancesCache) {
            result = (DecimalFormat) instance.instancesCache.get(propertyName);
            if(result == null) {
                String propertyValue = get(propertyName);
                try {
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                    symbols.setDecimalSeparator(get(HCJF_DEFAULT_DECIMAL_SEPARATOR).charAt(0));
                    symbols.setGroupingSeparator(get(HCJF_DEFAULT_GROUPING_SEPARATOR).charAt(0));
                    result = new DecimalFormat(propertyValue, symbols);
                    instance.instancesCache.put(propertyName, result);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a decimal pattern valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        return result;
    }

    /**
     * This method return the value of the property as a SimpleDateFormat instance.
     * The instance returned will be stored on near cache and will be removed when the
     * value of the property has been updated.
     * @param propertyName Name of the property that contains date representation.
     * @return Simple date format instance.
     */
    public static SimpleDateFormat getDateFormat(String propertyName) {
        SimpleDateFormat result;
        synchronized (instance.instancesCache) {
            result = (SimpleDateFormat) instance.instancesCache.get(propertyName);
            if(result == null) {
                String propertyValue = get(propertyName);
                try {
                    result = new SimpleDateFormat(get(propertyName));
                    instance.instancesCache.put(propertyName, result);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a date pattern valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        return result;
    }

    /**
     * Returns the implementation object expected for the specific object type.
     * @param propertyName Name of the property.
     * @param objectType Object type.
     * @param <O> Expected object instance.
     * @return Object instance.
     */
    public static <O extends Object> O getObject(String propertyName, Class<O> objectType) {
        try {
            return  instance.gson.fromJson(get(propertyName), objectType);
        } catch (Exception ex) {
            throw new IllegalArgumentException("The property value has not a json object valid to create the instance: '"
                    + propertyName + ":" + objectType + "'", ex);
        }
    }

    /**
     * Returns a list of expected object instances for the specific object type.
     * @param propertyName Name of the property.
     * @param objectType Object type.
     * @param <O> Expected object instance.
     * @return Object instances.
     */
    public static <O extends Object> List<O> getObjects(String propertyName, Class<O> objectType) {
        List<O> result = new ArrayList<>();
        try {
            JsonArray array = JsonParser.parseString(get(propertyName)).getAsJsonArray();
            Iterator<JsonElement> iterator = array.iterator();
            while(iterator.hasNext()) {
                result.add(instance.gson.fromJson(iterator.next(), objectType));
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("The property value has not a json object valid to create the instance: '"
                    + propertyName + ":" + objectType + "'", ex);
        }
        return result;
    }

    /**
     * This method return the value of the property as instance of list.
     * @param propertyName Name of the property that contains the json array representation.
     * @return List instance.
     */
    public static List<String> getList(String propertyName) {
        String propertyValue = get(propertyName);
        List<String> result = new ArrayList<>();
        synchronized (instance.instancesCache) {
            if (instance.instancesCache.containsKey(propertyName)) {
                result.addAll((List<? extends String>) instance.instancesCache.get(propertyName));
            } else {
                try {
                    result.addAll((Collection<? extends String>) JsonUtils.createObject(propertyValue));
                    List<String> cachedResult = new ArrayList<>();
                    cachedResult.addAll(result);
                    instance.instancesCache.put(propertyName, cachedResult);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a json array valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        return result;
    }

    /**
     * This method return the value of the property as instance of set.
     * @param propertyName Name of the property that contains the json array representation.
     * @return Set instance.
     */
    public static Set<String> getSet(String propertyName) {
        String propertyValue = get(propertyName);
        Set<String> result = new TreeSet<>();
        synchronized (instance.instancesCache) {
            if (instance.instancesCache.containsKey(propertyName)) {
                result.addAll((List<? extends String>) instance.instancesCache.get(propertyName));
            } else {
                try {
                    result.addAll((Collection<? extends String>) JsonUtils.createObject(propertyValue));
                    List<String> cachedResult = new ArrayList<>();
                    cachedResult.addAll(result);
                    instance.instancesCache.put(propertyName, cachedResult);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a json array valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        return result;
    }

    /**
     * This method return the value of the property as instance of map.
     * @param propertyName The name of the property that contains the json object representation.
     * @return Map instance.
     */
    public static Map<String, String> getMap(String propertyName) {
        String propertyValue = get(propertyName);
        Map<String, String> result = new HashMap<>();
        synchronized (instance.instancesCache) {
            if (instance.instancesCache.containsKey(propertyName)) {
                result.putAll((Map<String, String>) instance.instancesCache.get(propertyName));
            } else {
                try {
                    result.putAll((Map<? extends String, ? extends String>) JsonUtils.createObject(propertyValue));
                    Map<String, String> cachedResult = new HashMap<>();
                    cachedResult.putAll(result);
                    instance.instancesCache.put(propertyName, cachedResult);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a json object valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        return result;
    }

    public static Pattern getPattern(String propertyName) {
        return getPattern(propertyName, 0);
    }

    /**
     * Return the compiled pattern from the property value.
     * @param propertyName Name of the property.
     * @param flags Regex flags.
     * @return Compiled pattern.
     */
    public static Pattern getPattern(String propertyName, int flags) {
        String propertyValue = get(propertyName);
        Pattern result;
        synchronized (instance.instancesCache) {
            if(instance.instancesCache.containsKey(propertyName)) {
                result = (Pattern) instance.instancesCache.get(propertyName);
            } else {
                try {
                    result = Pattern.compile(propertyValue, flags);
                    instance.instancesCache.put(propertyName, result);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("The property value has not a regex valid format: '"
                            + propertyName + ":" + propertyValue + "'", ex);
                }
            }
        }
        return result;
    }

}
