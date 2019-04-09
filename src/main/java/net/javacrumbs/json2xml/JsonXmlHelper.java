/**
 * Copyright 2009-2019 Gwenhaël Pasquiers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.json2xml;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Helper class that can be used for JSON -> XML and XML -> JSON transformation.
 *
 * It is mostly useful to go back to JSON after using JsonXmlReader and
 * eventually modifying the DOM using XPath. This class needs the type attribute
 * so it can only convert back XML created from JSON with addTypeAttributes
 * true.
 *
 * <pre>
 *  Node node = convertToDom(myJSONString, "", true, "root");
 *  // ...
 *  String json = convertToJson(node);
 * </pre>
 */
public class JsonXmlHelper {

    static public enum TYPE {
        STRING, INT, FLOAT, BOOLEAN, NULL, ARRAY, OBJECT
    }

    /**
     * Helper method to convert JSON string to XML DOM
     *
     * @param json String containing the json document
     * @param namespace Namespace that will contain the generated dom nodes
     * @param addTypeAttributes Set to true to generate type attributes
     * @param artificialRootName Name of the artificial root element node
     * @return Document DOM node.
     * @throws javax.xml.transform.TransformerConfigurationException
     */
    public static Node convertToDom(final String json, final String namespace, final boolean addTypeAttributes, final String artificialRootName) throws TransformerConfigurationException, TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        InputSource source = new InputSource(new StringReader(json));
        DOMResult result = new DOMResult();
        transformer.transform(new SAXSource(new JsonXmlReader(namespace, addTypeAttributes, artificialRootName), source), result);
        return result.getNode();
    }

    /**
     * Simpler helper method to convert DOM node back to JSON. The node MUST
     * have the "type" attributes (generated with addTypeAttributes flag set as
     * true).
     *
     * @param node The DOM Node
     * @return The JSON string
     * @throws IOException
     */
    public static String convertToJson(Node node) throws IOException {
        try (StringWriter writer = new StringWriter(); JsonGenerator generator = new JsonFactory().createGenerator(writer)) {
            convertToJson(node, generator, name -> name);
            return writer.toString();
        }
    }

    /**
     * More complete helper method to convert DOM node back to JSON.The node
     * MUST have the "type" attributes (generated with addTypeAttributes flag
     * set as true).This method allows to customize the JsonGenerator.
     *
     * @param node The DOM Node
     * @param generator A configured JsonGenerator
     * @param converter Converter to convert elements names from XML to JSON
     * @throws IOException
     */
    public static void convertToJson(Node node, JsonGenerator generator, ElementNameConverter converter) throws IOException {
        Element element;
        if (node instanceof Document) {
            element = ((Document) node).getDocumentElement();
        } else if (node instanceof Element) {
            element = (Element) node;
        } else {
            throw new IllegalArgumentException("Node must be either a Document or an Element");
        }

        TYPE type = toTYPE(element.getAttribute("type"));
        switch (type) {
            case OBJECT:
            case ARRAY:
                convertElement(generator, element, true, converter);
                break;
            default:
                throw new RuntimeException("invalid root type [" + type + "]");
        }
        generator.close();
    }

    /**
     * Convert a DOM element to Json, with special handling for arrays since arrays don't exist in XML.
     * @param generator
     * @param element
     * @param isArrayItem
     * @throws IOException 
     */
    private static void convertElement(JsonGenerator generator, Element element, boolean isArrayItem, ElementNameConverter converter) throws IOException {
        TYPE type = toTYPE(element.getAttribute("type"));
        String name = element.getTagName();

        if (!isArrayItem) {
            generator.writeFieldName(converter.convertName(name));
        }

        switch (type) {
            case OBJECT:
                generator.writeStartObject();
                convertChildren(generator, element, false, converter);
                generator.writeEndObject();
                break;
            case ARRAY:
                generator.writeStartArray();
                convertChildren(generator, element, true, converter);
                generator.writeEndArray();
                break;
            case STRING:
                generator.writeString(element.getTextContent());
                break;
            case INT:
            case FLOAT:
                generator.writeNumber(new BigDecimal(element.getTextContent()));
                break;
            case BOOLEAN:
                generator.writeBoolean(Boolean.parseBoolean(element.getTextContent()));
                break;
            case NULL:
                generator.writeNull();
                break;
        }
    }

    /**
     * Method to recurse within children elements and convert them to JSON too.
     * @param generator
     * @param element
     * @param isArray
     * @throws IOException
     */
    private static void convertChildren(JsonGenerator generator, Element element, boolean isArray, ElementNameConverter converter) throws IOException {
        NodeList list = element.getChildNodes();
        int len = list.getLength();
        for (int i = 0; i < len; i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                convertElement(generator, (Element) node, isArray, converter);
            }
        }
    }

    /**
     * Convert type attribute value to TYPE enum (no attribute = OBJECT)
     *
     * @param type The type as a string
     * @return
     */
    private static TYPE toTYPE(String type) {
        if (null == type || type.trim().isEmpty()) {
            return TYPE.OBJECT;
        } else {
            return TYPE.valueOf(type.toUpperCase());
        }
    }
}
