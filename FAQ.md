# JMeter WebSocket Samplers FAQ

## When trying to connect to websocket server, i get "Response code: 404 Response message: Http Upgrade failed". What's going on?

You're probably providing an incorrect ws-URL. Check the "Path" field in the "Server URL" details.

Every WebSocket connection starts with an "upgrade request", which is standard HTTP GET call with some special headers. 
The 404 status code in the error message is just the HTTP status that was returned by the server in response to the upgrade request. 
As you probably know, HTTP status code 404 means "NOT FOUND", so the problem is that your sampler is trying to access an URL that does not exist.


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

