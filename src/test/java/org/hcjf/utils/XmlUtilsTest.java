package org.hcjf.utils;

import org.hcjf.io.net.http.HttpRequest;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class XmlUtilsTest {

    @Test
    public void simpleTest() {
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<data/>" +
                "<bookstore>\n" +
                "  <book category=\"cooking\">\n" +
                "    <title lang=\"en\">Everyday Italian</title>\n" +
                "    <author>Giada De Laurentiis</author>\n" +
                "    <year>2005</year>\n" +
                "    <price>30.00</price>\n" +
                "  </book>\n" +
                "  <book category=\"children\">\n" +
                "    <title lang=\"en\">Harry Potter</title>\n" +
                "    <author>J K. Rowling</author>\n" +
                "    <year>2005</year>\n" +
                "    <price>29.99</price>\n" +
                "  </book>\n" +
                "  <book category=\"web\">\n" +
                "    <title lang=\"en\">Learning XML</title>\n" +
                "    <author>Erik T. Ray</author>\n" +
                "    <year>2003</year>\n" +
                "    <price>39.95</price>\n" +
                "  </book>\n" +
                "</bookstore>";

        Map<String,Object> obj = XmlUtils.parse(xml);
        System.out.println(JsonUtils.toJsonTree(obj).toString());
    }

    @Test
    public void wsdlTest() {
        String xml2 = "<definitions name = \"HelloService\"\n" +
        "   targetNamespace = \"http://www.examples.com/wsdl/HelloService.wsdl\"\n" +
        "   xmlns = \"http://schemas.xmlsoap.org/wsdl/\"\n" +
        "   xmlns:soap = \"http://schemas.xmlsoap.org/wsdl/soap/\"\n" +
        "   xmlns:tns = \"http://www.examples.com/wsdl/HelloService.wsdl\"\n" +
        "   xmlns:xsd = \"http://www.w3.org/2001/XMLSchema\">\n" +
        " \n" +
        "   <message name = \"SayHelloRequest\">\n" +
        "      <part name = \"firstName\" type = \"xsd:string\"/>\n" +
        "   </message>\n" +
        "\t\n" +
        "   <message name = \"SayHelloResponse\">\n" +
        "      <part name = \"greeting\" type = \"xsd:string\"/>\n" +
        "   </message>\n" +
        "\n" +
        "   <portType name = \"Hello_PortType\">\n" +
        "      <operation name = \"sayHello\">\n" +
        "         <input message = \"tns:SayHelloRequest\"/>\n" +
        "         <output message = \"tns:SayHelloResponse\"/>\n" +
        "      </operation>\n" +
        "   </portType>\n" +
        "\n" +
        "   <binding name = \"Hello_Binding\" type = \"tns:Hello_PortType\">\n" +
        "      <soap:binding style = \"rpc\"\n" +
        "         transport = \"http://schemas.xmlsoap.org/soap/http\"/>\n" +
        "      <operation name = \"sayHello\">\n" +
        "         <soap:operation soapAction = \"sayHello\"/>\n" +
        "         <input>\n" +
        "            <soap:body\n" +
        "               encodingStyle = \"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
        "               namespace = \"urn:examples:helloservice\"\n" +
        "               use = \"encoded\"/>\n" +
        "         </input>\n" +
        "\t\t\n" +
        "         <output>\n" +
        "            <soap:body\n" +
        "               encodingStyle = \"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
        "               namespace = \"urn:examples:helloservice\"\n" +
        "               use = \"encoded\"/>\n" +
        "         </output>\n" +
        "      </operation>\n" +
        "   </binding>\n" +
        "\n" +
        "   <service name = \"Hello_Service\">\n" +
        "      <documentation>WSDL File for HelloService</documentation>\n" +
        "      <port binding = \"tns:Hello_Binding\" name = \"Hello_Port\">\n" +
        "         <soap:address\n" +
        "            location = \"http://www.examples.com/SayHello/\" />\n" +
        "      </port>\n" +
        "   </service>\n" +
        "</definitions>";

        System.out.println(JsonUtils.toJsonTree(XmlUtils.parse(xml2)).toString());
    }

    @Test
    public void cdataTest() {
        String xml4 = "<sometext>\n" +
                "<![CDATA[ They're saying \"x < y\" & that \"z > y\" so I guess that means that z > x ]]>\n" +
                "</sometext>";
        System.out.println(JsonUtils.toJsonTree(XmlUtils.parse(xml4)).toString());
    }

    @Test
    public void commentTest() {
        String xml3 = "<description>An example of escaped CENDs</description>\n" +
                "<!-- This text contains a CEND ]] -->\n" +
                "<!-- In this first case we put the ]] at the end of the first CDATA block\n" +
                "     and the  in the second CDATA block -->";

        System.out.println(JsonUtils.toJsonTree(XmlUtils.parse(xml3)).toString());
    }
}
