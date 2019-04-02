/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.javacrumbs.json2xml;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Helper class that can be used for XML -> JSON transformation.
 * 
 * It is mostly useful to go back to JSON after using JsonXmlReader and eventually modifying the DOM using XPath. 
 * This class needs the type attribute so it can only convert back XML created from JSON with addTypeAttributes true.
 * 
 * <pre>
 *  Node node = ...
 *  StringWriter writer = new StringWriter();
 *  SAXResult result = new SAXResult(new XmlToJsonHandler(writer));
 *  Transformer xformer = TransformerFactory.newInstance().newTransformer();
 *  xformer.transform(new DOMSource(node), result);
 *  String json = writer.toString();
 * </pre>
 */
public class XmlToJsonHandler implements ContentHandler {

    private static enum TYPE {
        STRING, INT, BOOLEAN, NULL, ARRAY, OBJECT
    }

    private final JsonFactory factory;

    private final JsonGenerator generator;
    private final Stack<TYPE> stack;

    public XmlToJsonHandler(Writer writer) throws IOException {
        this(writer, null);
    }

    public XmlToJsonHandler(Writer writer, ObjectCodec codec) throws IOException {
        factory = new JsonFactory(codec);
        generator = factory.createGenerator(writer);
        stack = new Stack<>();
    }

    private TYPE toTYPE(String type) {
        if (null == type) {
            return TYPE.OBJECT;
        } else {
            return TYPE.valueOf(type.toUpperCase());
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            TYPE type = toTYPE(attributes.getValue("type"));

            if (!stack.isEmpty() && stack.peek() != TYPE.ARRAY) {
                generator.writeFieldName(localName);
            }

            switch (type) {
                case OBJECT:
                    generator.writeStartObject();
                    break;
                case ARRAY:
                    generator.writeStartArray();
                    break;
                case NULL:
                    generator.writeNull();
                    break;
            }
            stack.push(type);

        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            switch (stack.peek()) {
                case INT:
                    generator.writeNumber(new BigDecimal(new String(ch, start, length)));
                    break;
                case STRING:
                    generator.writeString(new String(ch, start, length));
                    break;
                case BOOLEAN:
                    generator.writeBoolean(Boolean.parseBoolean(new String(ch, start, length)));
                    break;
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            switch (stack.pop()) {
                case OBJECT:
                    generator.writeEndObject();
                    break;
                case ARRAY:
                    generator.writeEndArray();
                    break;
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            generator.close();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
    }
}
