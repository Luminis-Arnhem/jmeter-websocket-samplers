# JMeter WebSocket Samplers

JMeter add-on that defines a number of samplers for load testing WebSocket applications.

## Usage

Download the jar from the [downloads](https://bitbucket.org/pjtr/jmeter-websocket-samplers/downloads/) dir, copy it to <jmeter-home>/lib/ext and start JMeter. That's all.

You can also install the plugin with the jmeter-plugins [Plugins Manager](https://jmeter-plugins.org/install/Install/). 
If you use this installer, select "WebSocket Samplers by Peter Doornbosch".

![Sampler GUI](https://bytebucket.org/pjtr/jmeter-websocket-samplers/raw/master/docs/install_with_plugins_mgr.png)

If you are running JMeter as part of your build pipeline, getting the plugin from a [maven repository](https://search.maven.org/#search%7Cga%7C1%7Cjmeter-websocket-samplers) might be useful.

Make sure you're running JMeter with Java 8. Loading the plugin will fail silently if running with Java 7 (or older).

From version 1.0 onwards, the plugin requires JMeter 3.1 or later. Older versions work with JMeter 3.0 too.

## You can help! Spread the word!

If you like this plugin, if you find it useful, you can help others that might have similar problems or challenges as you had, by spreading the word. Talk, tweet, blog about it; answer questions about how to load-test WebSocket on forums, stackoverflow etc. and let people know this plugin exists. If you think the plugin needs improvement, let the author know (see "feedback" below).

## Features
The WebSocket Samplers plugin provides the following features:

* provides 6 different WebSocket samplers
* samplers do not create additional threads, so large number of JMeter threads can be used,
* support for wss (WebSocket over TLS)
* wss support fully compatible with JMeter's SSLManager, including client certificates
* support for binary WebSocket frames
* assertion for checking binary responses
* view binary results in "View Results Tree"
* integrates with JMeter's Header Manager to set additional HTTP headers on WebScoket upgrade request
* sends cookies defined by JMeter's Cookie Manager with each upgrade request (i.e. the HTTP request that initiates the WebSocket connection)
* proxy support
* provides filters for discarding frames that are not relevant for the test
* many sample JMeter test plans illustrate the various features.

### Samplers

Currently, there are six samplers:

* request-response sampler, for performing a basic request-response exchange,
* ping-pong sampler, for sending a ping and receiving a pong (or just sending an unsolicited pong)
* close connection sampler, for properly closing a websocket connection
* single-read sampler, for receiving one (text or binary) WebSocket frame
* single-write sampler, for sending one (text or binary) WebSocket frame
* open connection sampler, for _explicitly_ setting up a WebSocket connection.

The request-response sampler is the most commonly used one. With this sampler you can test a request-response exchange, much like an ordinary HTTP request/response. As all other samplers in this plugin, it does not create any thread by itself, but instead performs all communication on the JMeter ThreadGroup thread. This implies that it scales very well, comparable with standard JMeter HTTP sampler.

![Sampler GUI](https://bytebucket.org/pjtr/jmeter-websocket-samplers/raw/master/docs/request-response-sample.png)

The request-response sampler, as well as the single-read and single-write samplers, support both text and binary frames. 
For binary frames, enter the payload in hexadecimal format, e.g. `0xca 0xfe` or `ba be`; JMeter variables can be used, but should resolve to hex format at runtime. On linux system, you can use the following command to generate hex format from a binary file: `hexdump -e '16/1 "0x%02x " " "' myFile.wav`.

The payload (request data) can also be loaded from file, in which case it is not interpreted at all, but sent as is. Hence, JMeter variables cannot be used (or at least: will not be resolved) and binary content should be stored in binary files; e.g. not encoded in hex.

Standard JMeter cannot display binary responses in the results viewers, but this plugin adds a binary view to the "View Results Tree" listener element (if the "Response data" tab stays empty, select "Binary" in the types dropdown).

![Binary response](https://bytebucket.org/pjtr/jmeter-websocket-samplers/raw/master/docs/binary-response.png)

The maximum number of bytes displayed is limited to 1 MB, set the JMeter property `view.results.tree.max_binary_size` to increase this value. 
To make the "Binary" render type appear higher in the dropdown, insert the class name `eu.luminis.jmeter.visualizers.RenderAsBinary` in the `view.results.tree.renderers_order` property.

For examples of how to use the request-response sampler as well as the other samplers, see the JMeter .jmx files in the [samples directory](https://bitbucket.org/pjtr/jmeter-websocket-samplers/src/master/samples/?at=master)!

### Connections

Each JMeter (ThreadGroup) thread can have at most one active WebSocket connection. In the sampler, you can indicate whether you want to (re) use the current connection, or create a new one. If you create a new one, the current connection is closed at TCP level, but no WebSocket close frames are sent. If you want to close the connection properly (i.e. send a WebSocket close frame and wait for the close response), use the WebSocket Close sampler. 

There is also a WebSocket Open Connection sampler that only opens the WebSocket connection (i.e. sends an upgrade request) and sends no data once the websocket connection is established.

If you do not close the WebSocket connection yourself, it will stay open at the end of the test. This is usually pretty harmless, also because JMeter will initiate a gc (just) before the next test is run and that will cause all open connections to be closed (at TCP level).
If you want the connections to be closed immediately at the end of the test, you can set the JMeter property `websocket.thread.stop.policy`. As the name indicates, this determines what is done when the JMeter test thread finishes. If you set it to `tcpClose`, the connection will be closed at TCP level, but no WebSocket close will be sent. If you set it to `wsClose`, the WebSocket connection will be properly closed by sending a close frame. 
The property value is case insensative, so you might for example also write `tcpclose`. To set the property, add it to one of the `.properties` files in JMeter's `bin` directory, or use the `-J` option on the command line, e.g. `-J websocket.thread.stop.policy=wsclose`. Setting it to any other value than the ones mentioned will simply lead to the default behaviour, so if it doesn't work like expected, check for typo's.

### WebSockets over TLS

To use the wss (WebSockets over TLS) protocol instead of plain ws, simply select the wss protocol in the Server URL settings. Make sure you also change the port number (e.g. to 443, the default wss port), or you'll get confusing results when trying to set up a TLS connection with a normal HTTP port.

TLS server certificates are accepted without any verification; this is default JMeter behaviour, see for example <http://jmeter.apache.org/usermanual/get-started.html#opt_ssl>.

Using client certificates is also fully supported. It works exactly the same as the default SSL support in JMeter. However, setting it up correctly can be a bit of a challenge; see [jmeter_ssl_with_client_certificates.md](https://bitbucket.org/pjtr/jmeter-websocket-samplers/src/master/jmeter_ssl_with_client_certificates.md) for a step by step guide.


### Binary response assertion

In addition to WebSocket samplers, the plugin also provides an generic JMeter assertion element that can be used for veryfying binary responses. It's usage is pretty straight forward:

![Binary assertion](https://bytebucket.org/pjtr/jmeter-websocket-samplers/raw/master/docs/binary-assertion-sample.png)

This assertion element is of course very usefull when load testing binary websocket calls, but it is not limited to websocket tests in any way. It can be used with any sampler in the JMeter toolbox. For example, you could use it to check that an image result in a HTTP sampler, is a proper PNG file (see sample).

Note that the assertion element does not check the type of the response: it simply takes the binary value of the response and checks it against the match value provided. In that sense, it is completely analogous to the standard JMeter Response Assertion, except that this one provides a convenient way for specifying a binary match value.

### Proxy

The plugin respects standard JMeter proxy support: if you provide the `"-H <proxyHost>"` and `"-P <proxyPort>"` options on the command line, websocket connections are set up using that proxy.
As with standard JMeter, use `"-N <nonProxyHosts>"` to specify which hosts should not be proxied (supports wildcards like `*.apache.org`)
and `"-u <username>"` `"-a <password>"` for proxy authentication.

Tested with Apache HTTPD and Fiddler.

### Filters

To handle situations in which the server sends unsolicited messages and the occurrence of such messages is hard to predict or otherwise hard to take into account in the test plan, filters can be used to discard such messages before they are seen by a sampler.
There are three different kinds of filters: 
 
- Ping/Pong frame filter: discards all ping and pong frames and has an option to automatically respond to pings (with a pong of course)
- Binary frame filter: discards any binary frame, or binary frames that match a given sequence of bytes
- Text frame filter: discards any text frame, or text frames that contain/match a given substring or regular expression.
The text filter also provides a regular expression tester that can be used to quickly check whether the given regular expression matches or does not match, a number of test strings. 

![Text frame filter](https://bytebucket.org/pjtr/jmeter-websocket-samplers/raw/master/docs/text-frame-filter-with-regex-test-dialog-sample.png)

The filters can be found in the (`Edit->Add`) `Config Element` menu. 

Filters operate in the scope they are defined and can be combined in arbitrary ways. Of course, when multiple frames apply, only frames that do not match any of the filters in scope for a given sampler, will reach the sampler.

When filters apply, the plugin treats read timeouts a little different. This is best to explain with an example. 
Suppose you want to test a simple request-response exchange and because your requirement is that response messages should be send within 30 seconds, you set the read timeout to 30 seconds.
Even though setting the read timeout is not exactly the same has having an answer within 30 seconds (the read timeout is applied on the socket, when parts of the message arrive within 30 seconds periods, receiving the message might take much longer),
it will at least avoid the test from waiting too long, because if nothing is received for 30 seconds, the read (and thus the sampler), will time out.
When you add a filter, e.g. a ping/pong filter to ignore pong messages send by the server, the read timeout on the socket might never be reached when the server _never_ replies to the request, but _does_ send pings every 30 seconds, because the read timeout on the filter is never reached!
To avoid the sampler waiting forever in such cases, the read timeout is treated differently: the time the filter has been waiting for an message, is subtracted from the read timeout that is used for the remainder.
This is a simple solution that probably works well in most cases, but you need to realize that in such cases, you should consider the read-timeout more as a "maximum time to wait for a message" 
(which probably is how most people think about the read time out anyway). Future releases of this plugin might change this behaviour or provide a better solution; please check the documentation if it matters to you. 

Filtered frames are visible in the result listeners as subresults, so you can always monitor what is exactly received over the websocket connection. 
However, the filtered frames do not contribute to the received size of the "main" result. As a consequence, the figures for received bytes and throughput etc. do not exactly represent what is received over the line. If that is an issue, set the JMeter property `websocket.result.size_includes_filtered_frames` to true and the size of filtered frames will be added to their "parent" result and thus be included in the total figures for received bytes.

### Fragmentation

WebSocket messages may be fragmented into several frames. In such cases the first frame is an ordinary text or binary frame, but it will have the `final` bit cleared. The succeeding frames will be continuation frames (whether they are text or binary is inferred by the first frame) and the last continuation frame will have the `final` bit set.
The plugin supports continuation frames, but as the plugin is frame-oriented, you'll have to read them yourself. In cases where the number of fragments is known beforehand, this is as easy as adding an extra WebSocketReadSampler for each continuation frame you expect.
If the number of continuation frames is not known, you need to create a loop to read all the continuation frames. For this purpose, the plugin provides a new JMeter variable called `websocket.last_frame_final` that indicates whether the last frame read was final. 
This enables you to write a simple loop with a standard JMeter While Controller; use the expression `${__javaScript(! ${websocket.last_frame_final},)}` as condition. With a JMeter If Controller, the condition can be simplified to `! ${websocket.last_frame_final}` because that controller automatically interprets the condition as JavaScript.
See the sample [Read continuation frames.jmx](https://bitbucket.org/pjtr/jmeter-websocket-samplers/src/master/samples/Read%20continuation%20frames.jmx) test plan for examples of using the While or the If controller to read continuation frames.

If you are unsure whether continuation frames are sent by your server or how much, switch on debug logging: samplers reading a frame will log whether the received frame is a "normal" single frame, a non-final frame (i.e. 1st fragment), a continuation frame or a final continuation frame (last fragment).

### Logging

To enable debug logging with JMeter 3.2 or later, add the following line to `log4j2.xml`:

    <Logger name="eu.luminis" level="debug" />


To enable debug logging with JMeter 3.1 or 3.0, add the following lines to the `jmeter.properties` file:

    log_level.eu.luminis.jmeter=DEBUG
    log_level.eu.luminis.websocket=DEBUG
    

## Building

Gradle is used as build tool, so execute

    gradle assemble

to build. Almost any version of gradle will do (tested with 2.2). If you don't have installed gradle, use the gradle wrapper:

    ./gradlew
    
Gradle can also generate IntelliJ Idea project files for you:

    gradle idea


## Feedback

If you encounter any issues or ambiguities, please report them. Also questions, problems, or other feedback (appreciation ;-)) is always welcome. Please mail the author (peter dot doornbosch) at luminis dot eu, or create an issue at <https://bitbucket.org/pjtr/jmeter-websocket-samplers/issues>.


## Acknowledgements

The following people have contributed to this plugin by providing feedback, filing isssues, etc.: Eric Engels, Siarhei Huzau,
Victor Komlev, Chitta Ranjan, Oswin Nathanial, Andrew, Fedor Pavkovcek, Alexander Barker, Sachin D. Agrawal, Nicholas Naisbitt,
Amol Chavan, Vladimir Melnikov, David Hubbard, Tien Tran, Ray Oei, Georgy O, Rytis Kymantas, Petra Vanickova, Éliás Ádám, Nicolas Casaux,
Andrzej Nosal, Philippe M.


## License

This program is open source and licensed under LGPL (see the LICENSE.txt and LICENSE-LESSER.txt files in the distribution). This means that you can use this program for anything you like, and that you can embed it as a library in other applications, even commercial ones. If you do so, the author would appreciate if you include a reference to the original. As of the LGPL license, all modifications and additions to the source code must be published as (L)GPL as well.
