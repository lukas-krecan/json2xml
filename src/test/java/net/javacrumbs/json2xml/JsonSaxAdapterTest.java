/*
 * Copyright 2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.json2xml;

import net.javacrumbs.json2xml.JsonSaxAdapter.ParserException;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class JsonSaxAdapterTest {

    public static final String JSON = "{\"document\":{\"a\":1,\"b\":2,\"c\":{\"d\":\"text\"},\"e\":[1,2,3],\"f\":[[1,2,3],[4,5,6]], \"g\":null, " +
            "\"h\":[{\"i\":true,\"j\":false}],\"k\":[[{\"l\":1,\"m\":2}],[{\"n\":3,\"o\":4},{\"p\":5,\"q\":6}]]}}";

    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<document>\n" +
            "	<a>1</a>\n" +
            "	<b>2</b>\n" +
            "	<c>\n" +
            "		<d>text</d>\n" +
            "	</c>\n" +
            "	<e>\n" +
            "		<e>1</e>\n" +
            "		<e>2</e>\n" +
            "		<e>3</e>\n" +
            "	</e>\n" +
            "	<f>\n" +
            "		<f>\n" +
            "			<f>1</f>\n" +
            "			<f>2</f>\n" +
            "			<f>3</f>\n" +
            "		</f>\n" +
            "		<f>\n" +
            "			<f>4</f>\n" +
            "			<f>5</f>\n" +
            "			<f>6</f>\n" +
            "		</f>\n" +
            "	</f>\n" +
            "   <g/>\n" +
            "   <h>\n" +
            "       <h>\n" +
            "           <i>true</i>\n" +
            "           <j>false</j>\n" +
            "       </h>\n" +
            "   </h>\n" +
            "   <k>\n" +
            "      <k>\n" +
            "        <k>\n" +
            "            <l>1</l>\n" +
            "            <m>2</m>\n" +
            "        </k>\n" +
            "      </k>\n" +
            "      <k>\n" +
            "        <k>\n" +
            "            <n>3</n>\n" +
            "            <o>4</o>\n" +
            "       </k>\n" +
            "        <k>\n" +
            "            <p>5</p>\n" +
            "            <q>6</q>\n" +
            "       </k>\n" +
            "     </k>\n" +
            "   </k>\n" +
            "</document>\n";

    private static final String XML_WITH_TYPES = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<document xmlns=\"http://javacrumbs.net/test\">\n" +
            "	<a type=\"int\">1</a>\n" +
            "	<b type=\"int\">2</b>\n" +
            "	<c>\n" +
            "		<d type=\"string\">text</d>\n" +
            "	</c>\n" +
            "	<e type=\"array\">\n" +
            "		<e type=\"int\">1</e>\n" +
            "		<e type=\"int\">2</e>\n" +
            "		<e type=\"int\">3</e>\n" +
            "	</e>\n" +
            "	<f type=\"array\">\n" +
            "		<f type=\"array\">\n" +
            "			<f type=\"int\">1</f>\n" +
            "			<f type=\"int\">2</f>\n" +
            "			<f type=\"int\">3</f>\n" +
            "		</f>\n" +
            "		<f type=\"array\">\n" +
            "			<f type=\"int\">4</f>\n" +
            "			<f type=\"int\">5</f>\n" +
            "			<f type=\"int\">6</f>\n" +
            "		</f>\n" +
            "	</f>\n" +
            "	<g type=\"null\" />\n" +
            "   <h type=\"array\">\n" +
            "       <h>\n" +
            "         <i type=\"boolean\">true</i>\n" +
            "         <j type=\"boolean\">false</j>\n" +
            "       </h>\n" +
            "   </h>\n" +
            "   <k type=\"array\">\n" +
            "      <k type=\"array\">\n" +
            "         <k>\n" +
            "            <l type=\"int\">1</l>\n" +
            "            <m type=\"int\">2</m>\n" +
            "        </k>\n" +
            "      </k>\n" +
            "      <k type=\"array\">\n" +
            "        <k>\n" +
            "            <n type=\"int\">3</n>\n" +
            "            <o type=\"int\">4</o>\n" +
            "        </k>\n" +
            "        <k>\n" +
            "            <p type=\"int\">5</p>\n" +
            "            <q type=\"int\">6</q>\n" +
            "       </k>\n" +
            "      </k>\n" +
            "   </k>\n" +
            "</document>";

    @Before
    public void ignoreWhitespace() {
        XMLUnit.setIgnoreWhitespace(true);
    }

    @Test
    public void testParse() throws Exception {
        String xml = convertToXml(JSON);
        Diff diff = XMLUnit.compareXML(XML, xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testParseArray() throws Exception {
        String xml = convertToXml("[{\"name\":\"smith\"},{\"skill\":\"java\"}]", new JsonXmlReader(null, false, "elem"));
        Diff diff = XMLUnit.compareXML("<elem><elem><name>smith</name></elem><elem><skill>java</skill></elem></elem>", xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testParseArrayOfObjects() throws Exception {
        String xml = convertToXml("{\"equipments\":[{\"type\":\"charger\",\"cost\":\"1$\"},{\"type\":\"battery\",\"cost\":\"2$\"}]}", new JsonXmlReader());
        Diff diff = XMLUnit.compareXML("" +
                "    <equipments>\n" +
                "      <equipments>\n" +
                "         <type>charger</type>\n" +
                "         <cost>1$</cost>\n" +
                "      </equipments>\n" +
                "      <equipments>\n" +
                "         <type>battery</type>\n" +
                "         <cost>2$</cost>\n" +
                "      </equipments>\n" +
                "   </equipments>", xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testParseSimple() throws Exception {
        String xml = convertToXml("1", new JsonXmlReader(null, false, "elem"));
        Diff diff = XMLUnit.compareXML("<elem>1</elem>", xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test(expected = ParserException.class)
    public void testParseSimpleNoRoot() throws Exception {
        ContentHandler contentHandler = mock(ContentHandler.class);
        JsonSaxAdapter adapter = new JsonSaxAdapter("1", contentHandler);
        adapter.parse();
    }

    @Test
    public void testParseNamespace() throws Exception {
        String xml = convertToXml(JSON, new JsonXmlReader("http://javacrumbs.net/test"));
        String xmlWithNamespace = XML.replace("<document>", "<document xmlns=\"http://javacrumbs.net/test\">");
        Diff diff = XMLUnit.compareXML(xmlWithNamespace, xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testParseNamespaceWithAttributes() throws Exception {
        String xml = convertToXml(JSON, new JsonXmlReader("http://javacrumbs.net/test", true));
        Diff diff = XMLUnit.compareXML(XML_WITH_TYPES, xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testParseMultipleRootsArtificialRoot() throws Exception {
        String xml = convertToXml("{\"a\":1, \"b\":2}", new JsonXmlReader(null, false, "artificialRoot"));
        Diff diff = XMLUnit.compareXML("<artificialRoot><a>1</a><b>2</b></artificialRoot>", xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testParseMultipleRootsArtificialRootWithNamespace() throws Exception {
        String xml = convertToXml("{\"a\":1, \"b\":2}", new JsonXmlReader("http://javacrumbs.net/test", false, "artificialRoot"));
        Diff diff = XMLUnit.compareXML("<artificialRoot xmlns=\"http://javacrumbs.net/test\"><a>1</a><b>2</b></artificialRoot>", xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test(expected = ParserException.class)
    public void testMultipleRoots() throws Exception {
        ContentHandler contentHandler = mock(ContentHandler.class);
        JsonSaxAdapter adapter = new JsonSaxAdapter("{\"a\":1, \"b\":2}", contentHandler);
        adapter.parse();
    }

    @Test
    public void testParseInvalidName() throws Exception {
        String xml = convertToXml("{\"@bum\":1}", new JsonXmlReader("", false, null, new ElementNameConverter() {
            public String convertName(String name) {
                return name.replaceAll("@","_");
            }
        }));
        Diff diff = XMLUnit.compareXML("<_bum>1</_bum>", xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testArrayOfObjects() throws Exception {
        String xml = convertToXml("{\"root\":[{\"a\":1}, {\"b\":2}]}");
        Diff diff = XMLUnit.compareXML("<root><root><a>1</a></root><root><b>2</b></root></root>", xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testArrayOfArraysOfObjects() throws Exception {
        String xml = convertToXml("{\"root\":[[{\"a\":1, \"e\":true}, {\"b\":2}],[{\"c\":3}, {\"d\":4}]]}");
        Diff diff = XMLUnit.compareXML(
                "<root>\n" +
                "   <root>\n" +
                "      <root>\n" +
                "         <a>1</a>\n" +
                "         <e>true</e>\n" +
                "      </root>\n" +
                "      <root>\n" +
                "         <b>2</b>\n" +
                "      </root>\n" +
                "   </root>\n" +
                "   <root>\n" +
                "      <root>\n" +
                "         <c>3</c>\n" +
                "      </root>\n" +
                "      <root>\n" +
                "         <d>4</d>\n" +
                "      </root>\n" +
                "   </root>\n" +
                "</root>", xml);
        assertTrue(diff.toString(), diff.similar());
    }
    @Test
    public void testArrayOfArraysOfObjectsInRoot() throws Exception {
        String xml = convertToXml("[[{\"a\":1, \"e\":true}, {\"b\":2}],[{\"c\":3}, {\"d\":4}]]", new JsonXmlReader("", false, "root"));
        Diff diff = XMLUnit.compareXML(
                "<root>\n" +
                "   <root>\n" +
                "      <root>\n" +
                "         <a>1</a>\n" +
                "         <e>true</e>\n" +
                "      </root>\n" +
                "      <root>\n" +
                "         <b>2</b>\n" +
                "      </root>\n" +
                "   </root>\n" +
                "   <root>\n" +
                "      <root>\n" +
                "         <c>3</c>\n" +
                "      </root>\n" +
                "      <root>\n" +
                "         <d>4</d>\n" +
                "      </root>\n" +
                "   </root>\n" +
                "</root>", xml);
        assertTrue(diff.toString(), diff.similar());
    }


    @Test
    public void testArrayOfScalars() throws Exception {
        String xml = convertToXml("{\"a\":[1,2,3]}");
        Diff diff = XMLUnit.compareXML("<a><a>1</a><a>2</a><a>3</a></a>", xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testMapOfObjects() throws Exception {
        String xml = convertToXml("{\"root\":{\"a\":1, \"b\":2}}");
        Diff diff = XMLUnit.compareXML("<root><a>1</a><b>2</b></root>", xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testArrayOfComplexObjects() throws Exception {
        String xml = convertToXml("{\"root\":[{\"a\":{\"a1\":1}}, {\"b\":2}]}");
        Diff diff = XMLUnit.compareXML("<root><root><a><a1>1</a1></a></root><root><b>2</b></root></root>", xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testXmlEscaping() throws Exception {
        String xml = convertToXml("{\"root\":\"<a>\"}");
        Diff diff = XMLUnit.compareXML("<root>&lt;a&gt;</root>", xml);
        assertTrue(diff.toString(), diff.similar());
    }

    public static String convertToXml(final String json) throws Exception {
        return convertToXml(json, new JsonXmlReader());
    }


    public static String convertToXml(final String json, final JsonXmlReader reader) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        InputSource source = new InputSource(new StringReader(json));
        Result result = new StreamResult(out);
        transformer.transform(new SAXSource(reader, source), result);
        return new String(out.toByteArray());
    }

    public static Node convertToDom(final String json, final String namespace, final boolean addTypeAttributes, final String artificialRootName) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        InputSource source = new InputSource(new StringReader(json));
        DOMResult result = new DOMResult();
        transformer.transform(new SAXSource(new JsonXmlReader(namespace, addTypeAttributes, artificialRootName), source), result);
        return result.getNode();
    }
}
