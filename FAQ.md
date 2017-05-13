# JMeter WebSocket Samplers FAQ

## When trying to connect to websocket server, i get "Response code: 404 Response message: Http Upgrade failed". What's going on?

You're probably providing an incorrect ws-URL. Check the "Path" field in the "Server URL" details.

Every WebSocket connection starts with an "upgrade request", which is standard HTTP GET call with some special headers. 
The 404 status code in the error message is just the HTTP status that was returned by the server in response to the upgrade request. 
As you probably know, HTTP status code 404 means "NOT FOUND", so the problem is that your sampler is trying to access an URL that does not exist.


## When running the "Basic request-response sample" sample test plan, it fails with a time out error. Shouldn't this sample work?

Of course, the sample should work. If it doesn't and you did not change it, please check the [websocket.org](http://www.websocket.org/echo.html) website and try the echo demo in your browser; sometimes their websocket server is down or otherwise not fully available.
Another reason could be that you are using a proxy; older (0.7.x and before) versions of the plugin does not yet support http proxy.

