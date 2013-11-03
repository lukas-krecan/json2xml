Java Json to XML conversion
============================

Json2Xml project is a simple implementation of JSON to XML conversion. Under the hood it uses [Jackson](http://jackson.codehaus.org/) [pull parser](http://wiki.fasterxml.com/JacksonInFiveMinutes#Streaming_API_Example) and generates
XML SAX events. This way the conversion has low memory consumption and is pretty fast.

There is already [Jettison] (http://jettison.codehaus.org/) project that has similar objective, unfortunately it can not handle JSON arrays properly.

Json2Xml converts the following JSON 

	{
       "document":{
          "a":1,
          "b":2,
          "c":{
             "d":"text"
          },
          "e":[1, 2, 3],
          "f":[[1, 2, 3],[4, 5, 6]],
          "g":null,
          "h":[{"i":true,"j":false}],
          "k":[[{"l":1,"m":2}],[{"n":3,"o":4}]]
       }
    }
	
to

	<?xml version="1.0" encoding="UTF-8"?>
    <document>
       <a>1</a>
       <b>2</b>
       <c>
          <d>text</d>
       </c>
       <e>
          <e>1</e>
          <e>2</e>
          <e>3</e>
       </e>
       <f>
          <f>
             <f>1</f>
             <f>2</f>
             <f>3</f>
          </f>
          <f>
             <f>4</f>
             <f>5</f>
             <f>6</f>
          </f>
       </f>
       <g />
       <h>
          <i>true</i>
          <j>false</j>
       </h>
       <k>
          <k>
             <l>1</l>
             <m>2</m>
          </k>
          <k>
             <n>3</n>
             <o>4</o>
          </k>
       </k>
    </document>
	
Usage
-------------

If you have SAX content handler, you can use `net.javacrumbs.json2xml.JsonSaxAdapter` class directly.

	ContentHandler ch = ...;
	JsonSaxAdapter adapter = new JsonSaxAdapter(json, ch);
	adapter.parse();
	
Otherwise it's possible to use `net.javacrumbs.json2xml.JsonXmlReader` together with standard Java transformation.

	Transformer transformer = TransformerFactory.newInstance().newTransformer();
	InputSource source = new InputSource(...);
	Result result = ...;
	transformer.transform(new SAXSource(new JsonXmlReader(),source), result);

For example

    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    InputSource source = new InputSource(new StringReader(json));
    DOMResult result = new DOMResult();
    transformer.transform(new SAXSource(new JsonXmlReader(namespace, addTypeAttributes, artificialRootName), source), result);
    result.getNode();
	
Type attributes
---------------
Since XML does not have any mechanism to reflect JSON type information, there is a new feature since json2xml version 1.2. You can switch on the `addTypeAttributes` flag using a 
constructor argument. Then you will get the type information in XML attributes like this:

	<?xml version="1.0" encoding="UTF-8"?>
	<document xmlns="http://javacrumbs.net/test">
		<a type="int">1</a>
		<b type="int">2</b>
		<c>
			<d type="string">text</d>
		</c>
		<e type="array">
			<e type="int">1</e>
			<e type="int">2</e>
			<e type="int">3</e>
		</e>
		<f type="array">
			<f type="array">
				<f type="int">1</f>
				<f type="int">2</f>
				<f type="int">3</f>
			</f>
			<f type="array">
				<f type="int">4</f>
				<f type="int">5</f>
				<f type="int">6</f>
			</f>
		</f>
		<g type="null" />
	</document>
	
Atrificial root
---------------
XML support only one root element but JSON documents may have multiple roots. To overcome this mismatch,
you can specify `artificialRootName` which will generate artificial XML root with given name. So
document

    {"a":1, "b":2}
    
can be transformed to
    
    <artificialRoot>
      <a>1</a>
      <b>2</b>
    </artificialRoot>


Artificial root element name
----------------------------
If you have JSON with more or zero root elements, it is not possible to represent it in XML. You can specify artificial root name
using constructor parameter. For example `new JsonXmlReader(null, false, "elem")` will convert  `[{"name":"smith"},{"skill":"java"}]`
to `<elem><name>smith</name><skill>java</skill></elem>`

Name transformation
-------------------
Other difference between JSON and XML are allowed names. In cases, when your JSON contains names not allowed as XML element names,
an `ElementNameConverter` can be used. For example to convert '@' to '_' create the following reader

    new JsonXmlReader("", false, null, new ElementNameConverter() {
        public String convertName(String name) {
            return name.replaceAll("@","_");
        }
    })

Compatibility notes:
--------------------
Version 4.0 handles arrays differently than the previous version. The change is in handling of arrays of JSON objects.
In older version it behaved erratically. Version 4 fixes the behavior, but is backwards-incompatible.

Also note that version 2.0 and higher require Jackson 2.x If you need new features in Jackson 1.x, just file a ticket and
I will backport the changes.

Maven
-----
To use with Maven, add this dependency

	<dependency>
		<groupId>net.javacrumbs</groupId>
		<artifactId>json-xml</artifactId>
		<version>4.0</version><!-- for Jackson >= 2.0 -->
		<!--<version>1.3</version>--><!-- older version for Jackson < 2.0 -->
	</dependency>






	
