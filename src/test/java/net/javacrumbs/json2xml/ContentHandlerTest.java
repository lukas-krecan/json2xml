/*
 * Copyright 2005-2010 the original author or authors.
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

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;

public class ContentHandlerTest {

	@Test
	@Ignore
	public void testCH() throws ParserConfigurationException
	{
		ContentHandler ch = new DefaultHandler();
		JsonSaxAdapter adapter = new JsonSaxAdapter(JsonSaxAdapterTest.JSON, ch);
		adapter.parse();
	}
}
