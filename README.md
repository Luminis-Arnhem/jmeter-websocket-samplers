# JMeter WebSocket Samplers

JMeter add-on that defines a number of samplers for load testing WebSocket applications.

## Usage

Download the jar from the [downloads](https://bitbucket.org/pjtr/jmeter-websocket-samplers/downloads) dir, copy it to <jmeter-home>/lib/ext and start JMeter. That's all.

## Features

Currently, there is only one sampler: the request-response sampler. With this sampler you can test a request-response exchange, much like an ordinary HTTP request/response. This sampler does not create any thread by itself, but instead performs all communication on the JMeter ThreadGroup thread. This implies that it scales very well, comparable with standard JMeter HTTP sampler.

![Sampler GUI](https://bytebucket.org/pjtr/jmeter-websocket-samplers/raw/master/docs/request-response-sample.png)

The request-response sampler supports both text and binary frames. Unfortunately, JMeter cannot display binary responses in the results viewers, e.g. when using a "View Results Tree" listener element, the "Response data" tab stays empty. There is a work around however: use a "Save Responses to a file" listener (see sample).

For examples of how to use the sampler, see the JMeter .jmx files in the [samples directory](https://bitbucket.org/pjtr/jmeter-websocket-samplers/src/master/samples/?at=master)!

### Connections

Each JMeter (ThreadGroup) thread can have at most one active WebSocket connection. In the sampler, you can indicate whether you want to (re) use the current connection, or create a new one. If you create a new one, the current connection is closed at TCP level, but no WebSocket close frames are sent. If you want to close the connection properly (i.e. send a WebSocket close frame and wait for the close response), use the WebSocket Close sampler. 

## Status

Even though the project is fairly new, the add-on is fully functional. If you encounter any issues or ambiguities, please report them, see below for contact details.

## Building

Gradle is used as build tool, so execute

    gradle assemble

to build. Almost any version of gradle will do (tested with 2.2). Gradle can also generate IntelliJ Idea project files for you:

    gradle idea


## Feedback

Questions, problems, or other feedback? Please mail the author (peter dot doornbosch) at luminis dot eu, or create an issue at <https://bitbucket.org/pjtr/jmeter-websocket-samplers/issues>.
