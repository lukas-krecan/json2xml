/**
 * Copyright 2009-2017 Lukáš Křečan
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

import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;

public class JsonTest {

    @Test
    public void testMe() throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        String json = "[{\"studentName\" : \"Foo\", \"subjects2marks\":\"50\", \"subjects1name\":\"English\", \"subjects1marks\":\"40\", \"subjects2name\":\"History\", \"Age\":\"12\"},{\"studentName\":\"Bar\", \"subjects2marks\":\"50\", \"subjects1name\":\"English\", \"subjects1marks\":\"40\", \"subjects3marks\":\"40\", \"subjects2name\":\"History\", \"Age\":\"12\", \"subjects3name\":\"Science\"}, {\"studentName\":\"Baz\", \"Age\":\"12\"}]";
        InputSource source = new InputSource(new StringReader(json));

        DOMResult result = new DOMResult();

        transformer.transform(new SAXSource(new JsonXmlReader("http://www.w3.org/1999/xhtml", true, "details"), source), result);

        System.out.println(result.getNode().getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(0));
    }

}
