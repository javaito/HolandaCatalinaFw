package org.hcjf.io.net.http;

/**
 * Created by javaito on 25/4/2016.
 */
public interface HttpResponseCode {

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.1.1</link>
     */
    Integer CONTINUE = 100;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.1.2</link>
     */
    Integer SWITCHING_PROTOCOLS = 101;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.2.1</link>
     */
    Integer OK = 200;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.2.2</link>
     */
    Integer CREATED = 201;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.2.3</link>
     */
    Integer ACCEPTED = 202;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.2.4</link>
     */
    Integer NON_AUTHORITATIVE_INFOTMATION = 203;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.2.5</link>
     */
    Integer NO_CONTENT = 204;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.2.6</link>
     */
    Integer RESET_CONTENT = 205;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.2.7</link>
     */
    Integer PARTIAL_CONTENT = 206;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.3.1</link>
     */
    Integer MULTIPLE_CHOICES = 300;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.3.2</link>
     */
    Integer MOVED_PERMANENTLY = 301;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.3.3</link>
     */
    Integer FOUND = 302;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.3.4</link>
     */
    Integer SEE_OTHER = 303;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.3.5</link>
     */
    Integer NOT_MODIFIED = 304;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.3.6</link>
     */
    Integer USE_PROXY = 305;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.3.8</link>
     */
    Integer TEMPORARY_REDIRECT = 307;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.1</link>
     */
    Integer BAD_REQUEST = 400;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.2</link>
     */
    Integer UNAUTHORIZED = 401;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.3</link>
     */
    Integer PAYMENT_REQUIRED = 402;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.4</link>
     */
    Integer FORBIDDEN = 403;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.5</link>
     */
    Integer NOT_FOUND = 404;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.6</link>
     */
    Integer METHOD_NOT_ALLOWED = 405;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.7</link>
     */
    Integer NOT_ACCEPTABLE = 406;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.9</link>
     */
    Integer REQUEST_TIMEOUT = 408;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.10</link>
     */
    Integer CONFLICT = 409;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.11</link>
     */
    Integer GONE = 410;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.12</link>
     */
    Integer LENGTH_REQUIRED = 411;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.13</link>
     */
    Integer PRECONDITION_FAILED = 412;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.14</link>
     */
    Integer REQUEST_ENTITY_TOO_LARGE = 413;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.15</link>
     */
    Integer REQUEST_URI_TOO_LARGE = 414;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.16</link>
     */
    Integer UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.17</link>
     */
    Integer REQUESTED_RANGE_NOT_SATISFIABLE = 416;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.4.18</link>
     */
    Integer EXPECTATION_FAILED = 417;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.5.1</link>
     */
    Integer INTERNAL_SERVER_ERROR = 500;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.5.2</link>
     */
    Integer NOT_IMPLEMENTED = 501;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.5.3</link>
     */
    Integer BAD_GATEWAY = 502;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.5.4</link>
     */
    Integer SERVICE_UNAVAILABLE = 503;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.5.5</link>
     */
    Integer GATEWAY_TIMEOUT = 504;

    /**
     * <link href=https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1>10.5.6</link>
     */
    Integer HTTP_VERSION_NOT_SUPPORTED = 505;
}
