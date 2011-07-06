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

import static org.codehaus.jackson.JsonToken.END_ARRAY;
import static org.codehaus.jackson.JsonToken.END_OBJECT;
import static org.codehaus.jackson.JsonToken.FIELD_NAME;
import static org.codehaus.jackson.JsonToken.START_ARRAY;
import static org.codehaus.jackson.JsonToken.START_OBJECT;
import static org.codehaus.jackson.JsonToken.VALUE_NULL;

import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Converts JSON to SAX events. It can be used either directly
 *  <pre>
 *  <code>
 *	ContentHandler ch = ...;
 *	JsonSaxAdapter adapter = new JsonSaxAdapter(JsonSaxAdapterTest.JSON, ch);
 *	adapter.parse();
 *  </code>
 *  </pre>
 *  
 *  or using {@link JsonXmlReader}
 *  <pre>  
 *  <code>
 *	Transformer transformer = TransformerFactory.newInstance().newTransformer();
 *	InputSource source = new InputSource(...);
 *	Result result = ...;
 *	transformer.transform(new SAXSource(new JsonXmlReader(),source), result);
 *  </code>
 *  </pre>
 */
public class JsonSaxAdapter {

    private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

    private final JsonParser jsonParser;

    private final ContentHandler contentHandler;

    private final String namespaceUri;

    private static final JsonFactory JSON_FACTORY = new JsonFactory();


    public JsonSaxAdapter(final String json, final ContentHandler contentHandler) {
        this(parseJson(json), contentHandler);
    }


    public JsonSaxAdapter(final JsonParser jsonParser, final ContentHandler contentHandler) {
        this(jsonParser, contentHandler, "");
    }
    public JsonSaxAdapter(final JsonParser jsonParser, final ContentHandler contentHandler, final String namespaceUri) {
        this.jsonParser = jsonParser;
        this.contentHandler = contentHandler;
        this.namespaceUri = namespaceUri;
        contentHandler.setDocumentLocator(new DocumentLocator());
    }


    private static JsonParser parseJson(final String json) {
        try
        {
            return JSON_FACTORY.createJsonParser(json);
        }
        catch(Exception e)
        {
            throw new ParserException("Parsing error",e);
        }
    }

    /**
     * Method parses JSON and emits SAX events.
     */
    public void parse() throws ParserException
    {
        try {
            jsonParser.nextToken();
            contentHandler.startDocument();
            int elementsWritten = parseObject();
            if (elementsWritten>1)
            {
                throw new ParserException("More than one root element. Can not generate legal XML.");
            }
            contentHandler.endDocument();
        } catch (Exception e) {
            throw new ParserException("Parsing error: "+e.getMessage(),e);
        }
    }

    /**
     * Parses generic object.
     * @return number of elements written
     * @throws IOException
     * @throws JsonParseException
     * @throws Exception
     */
    private int parseObject() throws IOException, JsonParseException, Exception {
        int elementsWritten = 0;
        while (jsonParser.nextToken()!=null && jsonParser.getCurrentToken()!=END_OBJECT)
        {
            if (FIELD_NAME.equals(jsonParser.getCurrentToken()))
            {
                String elementName = jsonParser.getCurrentName();
                //jump to element value
                jsonParser.nextToken();
                parseElement(elementName);
                elementsWritten++;
            }
            else
            {
                throw new ParserException("Error when parsing. Expected field name got "+jsonParser.getCurrentToken());
            }
        }
        return elementsWritten;
    }

    private void parseElement(final String elementName) throws Exception {
        startElement(elementName);
        JsonToken currentToken = jsonParser.getCurrentToken();
        if (START_OBJECT.equals(currentToken))
        {
            parseObject();
        }
        else if (START_ARRAY.equals(currentToken))
        {
            parseArray(elementName);
        }
        else if (currentToken.isScalarValue())
        {
            parseValue();
        }

        endElement(elementName);
    }

    private void parseArray(final String elementName) throws Exception {

        while (jsonParser.nextToken()!=END_ARRAY && jsonParser.getCurrentToken()!=null)
        {
            parseElement(elementName);
        }
    }

    private void parseValue() throws Exception {
        if (VALUE_NULL!=jsonParser.getCurrentToken())
        {
            String text = jsonParser.getText();
            contentHandler.characters(text.toCharArray(), 0, text.length());
        }
    }


    private void startElement(final String elementName) throws SAXException {
        contentHandler.startElement(namespaceUri, elementName, elementName, EMPTY_ATTRIBUTES);
    }

    private void endElement(final String elementName) throws SAXException {
        contentHandler.endElement(namespaceUri, elementName, elementName);
    }

    public static class ParserException extends RuntimeException
    {
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
    private class DocumentLocator implements Locator
    {

        public String getPublicId() {
            Object sourceRef = jsonParser.getCurrentLocation().getSourceRef();
            if (sourceRef!=null)
            {
                return sourceRef.toString();
            }
            else
            {
                return "";
            }
        }
        public String getSystemId() {
            return getPublicId();
        }

        public int getLineNumber() {
            return jsonParser.getCurrentLocation()!=null?jsonParser.getCurrentLocation().getLineNr():-1;
        }

        public int getColumnNumber() {
            return jsonParser.getCurrentLocation()!=null?jsonParser.getCurrentLocation().getColumnNr():-1;
        }
    }
}
