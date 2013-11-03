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
import com.fasterxml.jackson.core.JsonParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import java.io.IOException;

/**
 * Helper class that can be used for JSON -> XML transformation.
 * <pre>
 *	Transformer transformer = TransformerFactory.newInstance().newTransformer();
 *	InputSource source = new InputSource(...);
 *	Result result = ...;
 *	transformer.transform(new SAXSource(new JsonXmlReader(namespace),source), result);
 * </pre>
 */
public class JsonXmlReader implements XMLReader {

    private ContentHandler contentHandler;
    private final String namespaceUri;
    private final boolean addTypeAttributes;
    private final String artificialRootName;
    private final ElementNameConverter elementNameConverter;


    /**
     * Creates JsonXmlReader
     */
    public JsonXmlReader() {
        this("");
    }

    /**
     * Creates JsonXmlReader
     * @param namespaceUri namespace uri of the resulting XML.
     */
    public JsonXmlReader(String namespaceUri) {
    	this(namespaceUri, false);
    }

    /**
     * Creates JsonXmlReader
     * @param namespaceUri namespace uri of the resulting XML.
     * @param addTypeAttributes if true adds attributes with type info
     */
    public JsonXmlReader(String namespaceUri, boolean addTypeAttributes) {
    	this(namespaceUri, addTypeAttributes, null);
	}

    /**
     * Creates JsonXmlReader
     * @param namespaceUri namespace uri of the resulting XML.
     * @param addTypeAttributes if true adds attributes with type info
     * @param artificialRootName if set, an artificial root is generated so JSON documents with more roots can be handeled.
     */
    public JsonXmlReader(String namespaceUri, boolean addTypeAttributes, String artificialRootName) {
    	this(namespaceUri, addTypeAttributes, artificialRootName, null);
	}

    /**
     * Creates JsonXmlReader
     * @param namespaceUri namespace uri of the resulting XML.
     * @param addTypeAttributes if true adds attributes with type info
     * @param artificialRootName if set, an artificial root is generated so JSON documents with more roots can be handled.
     * @param elementNameConverter converter to convert JSON object names to valid XML element names
     */
    public JsonXmlReader(String namespaceUri, boolean addTypeAttributes, String artificialRootName, ElementNameConverter elementNameConverter) {
    	this.namespaceUri = namespaceUri;
		this.addTypeAttributes = addTypeAttributes;
        this.artificialRootName = artificialRootName;
        this.elementNameConverter = elementNameConverter;
	}


	public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new UnsupportedOperationException();
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {

    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new UnsupportedOperationException();
    }


    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        //ignore
    }

    public void setEntityResolver(EntityResolver resolver) {
    	 //ignore
    }

    public EntityResolver getEntityResolver() {
        throw new UnsupportedOperationException();
    }

    public void setDTDHandler(DTDHandler handler) {
        //ignore
    }

    public DTDHandler getDTDHandler() {
        throw new UnsupportedOperationException();
    }

    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    public void setErrorHandler(ErrorHandler handler) {
    	 //ignore

    }

    public ErrorHandler getErrorHandler() {
        throw new UnsupportedOperationException();
    }


    public void parse(InputSource input) throws IOException, SAXException {
        JsonParser jsonParser = new JsonFactory().createParser(input.getCharacterStream());
        new JsonSaxAdapter(jsonParser, contentHandler, namespaceUri, addTypeAttributes, artificialRootName, elementNameConverter).parse();
    }

    public void parse(String systemId) throws IOException, SAXException {
        throw new UnsupportedOperationException();
    }

    public  String getNamespaceUri() {
        return namespaceUri;
    }
}
