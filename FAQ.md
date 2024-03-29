# JMeter WebSocket Samplers FAQ

## When trying to connect to websocket server, I get "Response code: 403" with Response message: "Got unexpected status 403 with statusLine:HTTP/1.1 403 Forbidden"

This means the server is denying access, probably because you did not provide a cookie or HTTP-header with a token or session. 
What the server is missing exactly is hard to tell, as this is implementation dependent.

The best way to find out what you should add to the request, is to capture the communication between your server and a regular client. 
Often this can be done in the developer tools of your browser, or you can capture the session with WireShark. Then, in the HTTP request
that starts the WebSocket connection (look for "Upgrade: websocket" header), try to locate the header that carries the authentication
data and copy that in your test plan - you can use the standard JMeter "HTTP Header Manager" to add an additional header to the HTTP 
Upgrade request that starts the WebSocket connection.

Another infamous cookie that might cause such behaviour is the CSRF token. As the CSRF token will be unpredictable, you must 
extract it from a previous response from your server. Use the standard JMeter "HTTP Header Manager" to add the CSRF header to the 
HTTP Upgrade request that starts the WebSocket connection.


## When trying to connect to websocket server, I get "Response code: 404 Response message: Http Upgrade failed". What's going on?

You're probably providing an incorrect ws-URL. Check the "Path" field in the "Server URL" details.

Every WebSocket connection starts with an "upgrade request", which is standard HTTP GET call with some special headers. 
The 404 status code in the error message is just the HTTP status that was returned by the server in response to the upgrade request. 
As you probably know, HTTP status code 404 means "NOT FOUND", so the problem is that your sampler is trying to access an URL that does not exist.


## When trying to connect to websocket server, I get "Response code: 200 Response message: Got unexpected status 200 with statusLine:HTTP/1.1 200 OK". 

You are probably connecting to a server or server URL that does not support the WebSocket protocol. 

Every WebSocket connection starts with an "upgrade request", which is standard HTTP GET call with some special headers. 
The server _must_ respond with http status 101 ("Switching Protocols") and a set of defined headers. 
If you get status 200 ("OK") instead, the server did not upgrade, but interpreted the upgrade request as a normal GET.


## When running the "Basic request-response sample" sample test plan, it fails with a time out error. Shouldn't this sample work?

Of course, the sample should work. If it doesn't and you did not change it, please check the [websocket.org](http://www.websocket.org/echo.html) website and try the echo demo in your browser; sometimes their websocket server is down or otherwise not fully available.
Another reason could be that you are using a proxy; older (0.7.x and before) versions of the plugin do not yet support http proxy.


## Can I use the websocket plugin for Protobuf, SignalR, Stomp, ...?

Yes, you can, but you have to assemble the Protobuf, SignalR, Stomp, ..., messages yourself in your JMeter test plan.
The websocket plugin implements the WebSocket protocol and (obviously) facilitates sending and receiving WebSocket messages.
Lots of applications use another protocol _on top_ of the WebSocket protocol, e.g. SignalR, and in order to test such a
protocol with the plugin, you will need to adapt your test plan to send these higher level protocol messages. 
In case the higher level protocol is text or JSON based (e.g. SignalR) or text oriented (STOMP), you can simply enter the text or JSON string in the request data field.
For binary protocols (e.g. Protocol Buffers), assembling the message payload can be more challenging, but it is feasible (the websocket plugin can send binary data frames, 
select "Binary" in the data type dropdown).


## How to define websocket subprotocol?

Even though you cannot set the WebSocket Subprotocol in the WebSocket Sampler GUI, you can define it by adding the standard JMeter Header Manager.
Add the Header Manager somewhere in your testplan (e.g. just before the first websocket Sampler) and add an entry with name "Sec-WebSocket-Protocol" 
and as value the identifier of the subprotocol you want to use, e.g. "ocpp1.6" or "v12.stomp".


## I want to share a WebSocket connection between different Thread Groups

You can't. This is by design, because one of the main esign principles of this plugin is to keep it easy to use and sharing 
WebSocket connections between JMeter threads would lead to trouble that is not easy to solve; to give a simple example, 
consider what the plugin would be supposed to do with a received message, to which thread it should be returned.

The solution for cases where you think you might need this feature, is to create a serial test plan. Common cases as unpredictable
ping frames can be solved by adding a "Frame Filter" that discards the ping before your Sampler can read it.