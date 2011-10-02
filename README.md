Java Json to XML conversion
============================

Json2Xml project is a simple implementetion of JSON to XML conversion. Under the hood it uses [Jackson](http://jackson.codehaus.org/) [pull parser](http://wiki.fasterxml.com/JacksonInFiveMinutes#Streaming_API_Example) and generates
XML SAX events. This way the conversion has low memory consumption and is pretty fast.

There is already [Jettison] (http://jettison.codehaus.org/) project that has similar objective, unfortunately it can not handle JSON arrays properly.

Json2Xml converts the following JSON 

	{"root":{
			"data1":[
				[1,2,3], 				
                [4,5,6]
             ],
     		"data2":null,
     		"data3":"2011-05-30T10:00:00",
     		"data4":
     		{
       			"a":1,
       			"b":2
     		}
  		}
	}
	
to

	<?xml version="1.0" encoding="UTF-8"?>
	<root>
		<data1>
			<data1>
				<data1>1</data1>
				<data1>2</data1>
				<data1>3</data1>
			</data1>
			<data1>
				<data1>4</data1>
				<data1>5</data1>
				<data1>6</data1>
			</data1>
		</data1>
		<data2/>
		<data3>2011-05-30T10:00:00</data3>
		<data4>
			<a>1</a>
			<b>2</b>
		</data4>
	</root>  
	
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
	
Maven
-----
To use it with Maven, just add this dependency

	<dependency>
		<groupId>net.javacrumbs</groupId>
		<artifactId>json-xml</artifactId>
		<version>1.0</version>
	</dependency>

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







	
