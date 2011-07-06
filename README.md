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

	
