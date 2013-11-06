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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NULL;

/**
 * Converts JSON to SAX events. It can be used either directly
 * <pre>
 *  <code>
 * 	ContentHandler ch = ...;
 * 	JsonSaxAdapter adapter = new JsonSaxAdapter(JsonSaxAdapterTest.JSON, ch);
 * 	adapter.parse();
 *  </code>
 *  </pre>
 *
 * or using {@link JsonXmlReader}
 * <pre>
 *  <code>
 * 	Transformer transformer = TransformerFactory.newInstance().newTransformer();
 * 	InputSource source = new InputSource(...);
 * 	Result result = ...;
 * 	transformer.transform(new SAXSource(new JsonXmlReader(),source), result);
 *  </code>
 *  </pre>
 */
public class JsonSaxAdapter {

    private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

    private final JsonParser jsonParser;

    private final ContentHandler contentHandler;

    private final String namespaceUri;

    private final boolean addTypeAttributes;

    private final String artificialRootName;

    private final ElementNameConverter nameConverter;

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    /**
     * Creates JsonSaxAdapter that coverts JSON to SAX events.
     * @param json JSON to parse
     * @param contentHandler target of SAX events
     */
    public JsonSaxAdapter(final String json, final ContentHandler contentHandler) {
        this(parseJson(json), contentHandler);
    }


    /**
     * Creates JsonSaxAdapter that coverts JSON to SAX events.
     * @param jsonParser parsed JSON
     * @param contentHandler target of SAX events
     */
    public JsonSaxAdapter(final JsonParser jsonParser, final ContentHandler contentHandler) {
        this(jsonParser, contentHandler, "");
    }

    /**
     * Creates JsonSaxAdapter that coverts JSON to SAX events.
     * @param jsonParser parsed JSON
     * @param contentHandler target of SAX events
     * @param namespaceUri namespace of the generated XML
     */
    public JsonSaxAdapter(final JsonParser jsonParser, final ContentHandler contentHandler, final String namespaceUri) {
        this(jsonParser, contentHandler, namespaceUri, false);
    }

    /**
     * Creates JsonSaxAdapter that coverts JSON to SAX events.
     * @param jsonParser parsed JSON
     * @param contentHandler target of SAX events
     * @param namespaceUri namespace of the generated XML
     * @param addTypeAttributes adds type information as attributes
     */
    public JsonSaxAdapter(final JsonParser jsonParser, final ContentHandler contentHandler, final String namespaceUri, final boolean addTypeAttributes) {
        this(jsonParser, contentHandler, namespaceUri, addTypeAttributes, null);
    }

    /**
     * Creates JsonSaxAdapter that coverts JSON to SAX events.
     * @param jsonParser parsed JSON
     * @param contentHandler target of SAX events
     * @param namespaceUri namespace of the generated XML
     * @param addTypeAttributes adds type information as attributes
     * @param artificialRootName if set, an artificial root is generated so JSON documents with more roots can be handeled.
     */
    public JsonSaxAdapter(final JsonParser jsonParser, final ContentHandler contentHandler, final String namespaceUri,
                          final boolean addTypeAttributes, final String artificialRootName) {
        this(jsonParser, contentHandler, namespaceUri, addTypeAttributes, artificialRootName, null);
    }

    /**
     * Creates JsonSaxAdapter that coverts JSON to SAX events.
     * @param jsonParser parsed JSON
     * @param contentHandler target of SAX events
     * @param namespaceUri namespace of the generated XML
     * @param addTypeAttributes adds type information as attributes
     * @param artificialRootName if set, an artificial root is generated so JSON documents with more roots can be handeled.
     */
    public JsonSaxAdapter(final JsonParser jsonParser, final ContentHandler contentHandler, final String namespaceUri,
                          final boolean addTypeAttributes, final String artificialRootName, final ElementNameConverter nameConverter) {
        this.jsonParser = jsonParser;
        this.contentHandler = contentHandler;
        this.namespaceUri = namespaceUri;
        this.addTypeAttributes = addTypeAttributes;
        this.artificialRootName = artificialRootName;
        this.nameConverter = nameConverter;
        contentHandler.setDocumentLocator(new DocumentLocator());
    }


    private static JsonParser parseJson(final String json) {
        try {
            return JSON_FACTORY.createParser(json);
        } catch (Exception e) {
            throw new ParserException("Parsing error", e);
        }
    }

    /**
     * Method parses JSON and emits SAX events.
     */
    public void parse() throws ParserException {
        try {
            jsonParser.nextToken();
            contentHandler.startDocument();
            if (shouldAddArtificialRoot()) {
                startElement(artificialRootName);
                parseElement(artificialRootName, false);
                endElement(artificialRootName);
            } else if (START_OBJECT.equals(jsonParser.getCurrentToken())) {
                int elementsWritten = parseObject();
                if (elementsWritten>1) {
                    throw new ParserException("More than one root element. Can not generate legal XML. You can set artificialRootName to generate an artificial root.");
                }
            } else {
                throw new ParserException("Unsupported root element. Can not generate legal XML. You can set artificialRootName to generate an artificial root.");
            }
            contentHandler.endDocument();
        } catch (Exception e) {
            throw new ParserException("Parsing error: " + e.getMessage(), e);
        }
    }

    private boolean shouldAddArtificialRoot() {
        return artificialRootName != null && artificialRootName.length() > 0;
    }

    /**
     * Parses generic object.
     *
     * @return number of elements written
     * @throws IOException
     * @throws JsonParseException
     * @throws Exception
     */
    private int parseObject() throws Exception {
        int elementsWritten = 0;
        while (jsonParser.nextToken() != null && jsonParser.getCurrentToken() != END_OBJECT) {
            if (FIELD_NAME.equals(jsonParser.getCurrentToken())) {
                String elementName = convertName(jsonParser.getCurrentName());
                //jump to element value
                jsonParser.nextToken();
                startElement(elementName);
                parseElement(elementName, false);
                endElement(elementName);
                elementsWritten++;
            } else {
                throw new ParserException("Error when parsing. Expected field name got " + jsonParser.getCurrentToken());
            }
        }
        return elementsWritten;
    }

    private String convertName(String name) {
        if (nameConverter != null) {
            return nameConverter.convertName(name);
        } else {
            return name;
        }
    }

    /**
     * Pares JSON element.
     * @param elementName
     * @param inArray if the element is in an array
     * @throws Exception
     */
    private void parseElement(final String elementName, final boolean inArray) throws Exception {
        JsonToken currentToken = jsonParser.getCurrentToken();
        if (inArray) {
            startElement(elementName);
        }
        if (START_OBJECT.equals(currentToken)) {
            parseObject();
        } else if (START_ARRAY.equals(currentToken)) {
            parseArray(elementName);
        } else if (currentToken.isScalarValue()) {
            parseValue();
        }
        if (inArray) {
            endElement(elementName);
        }
    }

    private void parseArray(final String elementName) throws Exception {
        while (jsonParser.nextToken() != END_ARRAY && jsonParser.getCurrentToken() != null) {
            parseElement(elementName, true);
        }
    }

    private void parseValue() throws Exception {
        if (VALUE_NULL != jsonParser.getCurrentToken()) {
            String text = jsonParser.getText();
            contentHandler.characters(text.toCharArray(), 0, text.length());
        }
    }


    private void startElement(final String elementName) throws SAXException {
        contentHandler.startElement(namespaceUri, elementName, elementName, getTypeAttributes());
    }


    protected Attributes getTypeAttributes() {
        if (addTypeAttributes) {
            String currentTokenType = getCurrentTokenType();
            if (currentTokenType != null) {
                AttributesImpl attributes = new AttributesImpl();
                attributes.addAttribute("", "type", "type", "string", currentTokenType);
                return attributes;
            } else {
                return EMPTY_ATTRIBUTES;
            }
        } else {
            return EMPTY_ATTRIBUTES;
        }
    }


    protected String getCurrentTokenType() {
        switch (jsonParser.getCurrentToken()) {
            case VALUE_NUMBER_INT:
                return "int";
            case VALUE_NUMBER_FLOAT:
                return "float";
            case VALUE_FALSE:
                return "boolean";
            case VALUE_TRUE:
                return "boolean";
            case VALUE_STRING:
                return "string";
            case VALUE_NULL:
                return "null";
            case START_ARRAY:
                return "array";
            default:
                return null;
        }
    }


    private void endElement(final String elementName) throws SAXException {
        contentHandler.endElement(namespaceUri, elementName, elementName);
    }

    public static class ParserException extends RuntimeException {
        private static final long serialVersionUID = 2194022343599245018L;

        public ParserException(final String message, final Throwable cause) {
            super(message, cause);
        }

        public ParserException(final String message) {
            super(message);
        }

        public ParserException(final Throwable cause) {
            super(cause);
        }

    }

    private class DocumentLocator implements Locator {

        public String getPublicId() {
            Object sourceRef = jsonParser.getCurrentLocation().getSourceRef();
            if (sourceRef != null) {
                return sourceRef.toString();
            } else {
                return "";
            }
        }

        public String getSystemId() {
            return getPublicId();
        }

        public int getLineNumber() {
            return jsonParser.getCurrentLocation() != null ? jsonParser.getCurrentLocation().getLineNr() : -1;
        }

        public int getColumnNumber() {
            return jsonParser.getCurrentLocation() != null ? jsonParser.getCurrentLocation().getColumnNr() : -1;
        }
    }
}
