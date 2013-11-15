Java Json to XML conversion
============================

Json2Xml project is a simple implementation of JSON to XML conversion. Under the hood it uses [Jackson](http://jackson.codehaus.org/) [pull parser](http://wiki.fasterxml.com/JacksonInFiveMinutes#Streaming_API_Example) and generates
XML SAX events. This way the conversion has low memory consumption and is pretty fast.

There is already [Jettison] (http://jettison.codehaus.org/) project that has similar objective, unfortunately it can not handle JSON arrays properly.

Json2Xml converts the following JSON 

<table>
  <tr>
    <td valign="top">
     <pre>
       {
       "document":{
          "a":1,
          "b":2,
          "c":{
             "d":"text"
          },
          "e":[1, 2, 3],
          "f":[[1, 2, 3], [4, 5,6]],
          "g":null,
          "h":[
             {
                "i":true,
                "j":false
             }
          ],
          "k":[
             [
                {"l":1, "m":2}
             ],
             [
                {"n":3, "o":4},
                {"p":5, "q":6}
             ]
          ]
       }
    }
     </pre>
    </td>
    <td>
     <pre>
    &lt;document&gt;
       &lt;a&gt;1&lt;/a&gt;
       &lt;b&gt;2&lt;/b&gt;
       &lt;c&gt;
          &lt;d&gt;text&lt;/d&gt;
       &lt;/c&gt;
       &lt;e&gt;
          &lt;e&gt;1&lt;/e&gt;
          &lt;e&gt;2&lt;/e&gt;
          &lt;e&gt;3&lt;/e&gt;
       &lt;/e&gt;
       &lt;f&gt;
          &lt;f&gt;
             &lt;f&gt;1&lt;/f&gt;
             &lt;f&gt;2&lt;/f&gt;
             &lt;f&gt;3&lt;/f&gt;
          &lt;/f&gt;
          &lt;f&gt;
             &lt;f&gt;4&lt;/f&gt;
             &lt;f&gt;5&lt;/f&gt;
             &lt;f&gt;6&lt;/f&gt;
          &lt;/f&gt;
       &lt;/f&gt;
       &lt;g /&gt;
       &lt;h&gt;
          &lt;h&gt;
             &lt;i&gt;true&lt;/i&gt;
             &lt;j&gt;false&lt;/j&gt;
          &lt;/h&gt;
       &lt;/h&gt;
       &lt;k&gt;
          &lt;k&gt;
             &lt;k&gt;
                &lt;l&gt;1&lt;/l&gt;
                &lt;m&gt;2&lt;/m&gt;
             &lt;/k&gt;
          &lt;/k&gt;
          &lt;k&gt;
             &lt;k&gt;
                &lt;n&gt;3&lt;/n&gt;
                &lt;o&gt;4&lt;/o&gt;
             &lt;/k&gt;
             &lt;k&gt;
                &lt;p&gt;5&lt;/p&gt;
                &lt;q&gt;6&lt;/q&gt;
             &lt;/k&gt;
          &lt;/k&gt;
       &lt;/k&gt;
    &lt;/document&gt;
     </pre>
    </td>
  </tr>
</table>


	
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
       <h type="array">
          <h>
             <i type="boolean">true</i>
             <j type="boolean">false</j>
          </h>
       </h>
       <k type="array">
          <k type="array">
             <k>
                <l type="int">1</l>
                <m type="int">2</m>
             </k>
          </k>
          <k type="array">
             <k>
                <n type="int">3</n>
                <o type="int">4</o>
             </k>
             <k>
                <p type="int">5</p>
                <q type="int">6</q>
             </k>
          </k>
       </k>
    </document>
	
Artificial root
---------------
XML support only one root element but JSON documents may have multiple roots. To overcome this mismatch,
you can specify `artificialRootName` which will generate artificial XML root with given name.
`new JsonXmlReader(null, false, "artificialRoot")` will transform

    {"a":1, "b":2}
    
to
    
    <artificialRoot>
      <a>1</a>
      <b>2</b>
    </artificialRoot>



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
Version 4.1 handles arrays differently than the previous version. The change is in handling of arrays of JSON objects.
In older version it behaved erratically. Version 4 fixes the behavior, but is backwards-incompatible.

Also note that version 2.0 and higher require Jackson 2.x If you need new features in Jackson 1.x, just file a ticket and
I will backport the changes.

*Please do not use version 4.0. It has broken handling of arrays. Since it has been in the wild for two days only, I do not assume
 anyone is using it.*

Maven
-----
To use with Maven, add this dependency

	<dependency>
		<groupId>net.javacrumbs</groupId>
		<artifactId>json-xml</artifactId>
		<version>4.1</version><!-- for Jackson >= 2.0 -->
		<!--<version>1.3</version>--><!-- older version for Jackson < 2.0 -->
	</dependency>






	
