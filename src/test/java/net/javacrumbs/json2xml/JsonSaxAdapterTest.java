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
import org.junit.Test;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;


public class JsonSaxAdapterTest {

    public static final String JSON = "{\"document\":{\"a\":1,\"b\":2,\"c\":{\"d\":\"text\"},\"e\":[1,2,3],\"f\":[[1,2,3],[4,5,6]], \"g\":null}}";

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
            "</document>";

    @Test
    public void testParse() throws Exception {
        String xml = convertToXml(JSON);
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = XMLUnit.compareXML(XML, xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testParseNamespace() throws Exception {
        String xml = convertToXml(JSON, "http://javacrumbs.net/test");
        System.out.println(xml);
        XMLUnit.setIgnoreWhitespace(true);
        String xmlWithNamespace = XML.replace("<document>", "<document xmlns=\"http://javacrumbs.net/test\">");
        Diff diff = XMLUnit.compareXML(xmlWithNamespace, xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testParseNamespaceWithAttributes() throws Exception {
        String xml = convertToXml(JSON, "http://javacrumbs.net/test", true);
        System.out.println(xml);
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = XMLUnit.compareXML(XML_WITH_TYPES, xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testParseMultipleRootsArtificialRoot() throws Exception {
        String xml = convertToXml("{\"a\":1, \"b\":2}", null, false, "artificialRoot");
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = XMLUnit.compareXML("<artificialRoot><a>1</a><b>2</b></artificialRoot>", xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void testParseMultipleRootsArtificialRootWithNamespace() throws Exception {
        String xml = convertToXml("{\"a\":1, \"b\":2}", "http://javacrumbs.net/test", false, "artificialRoot");
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = XMLUnit.compareXML("<artificialRoot xmlns=\"http://javacrumbs.net/test\"><a>1</a><b>2</b></artificialRoot>", xml);
        assertTrue(diff.toString(), diff.similar());
    }

    @Test(expected = ParserException.class)
    public void testMultipleRoots() throws Exception {
        ContentHandler contentHandler = mock(ContentHandler.class);
        JsonSaxAdapter adapter = new JsonSaxAdapter("{\"a\":1, \"b\":2}", contentHandler);
        adapter.parse();
    }


    public static String convertToXml(final String json) throws Exception {
        return convertToXml(json, "");
    }

    public static String convertToXml(final String json, final String namespace) throws Exception {
        return convertToXml(json, namespace, false);
    }

    public static String convertToXml(final String json, final String namespace, final boolean addTypeAttributes) throws Exception {
        return convertToXml(json, namespace, addTypeAttributes, null);
    }

    public static String convertToXml(final String json, final String namespace, final boolean addTypeAttributes, final String artificalRootName) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        InputSource source = new InputSource(new StringReader(json));
        Result result = new StreamResult(out);
        transformer.transform(new SAXSource(new JsonXmlReader(namespace, addTypeAttributes, artificalRootName), source), result);
        return new String(out.toByteArray());
    }
}
