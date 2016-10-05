# JMeter WebSocket Samplers

JMeter add-on that defines a number of samplers for load testing WebSocket applications.

## Usage

Copy the jar to <jmeter-home>/lib/ext and start JMeter.

## Features

Currently, there is only one sampler: the request-response sampler. With this sampler you can test a request-response exchange, much like an ordinary HTTP request/response. This sampler does not create any thread by itself, but instead performs all communication on the JMeter ThreadGroup thread. This implies that it scales very well, comparable with standard JMeter HTTP sampler.

The request-response sampler supports both text and binary frames. Unfortunately, JMeter cannot display binary responses in the results viewers, e.g. when using a "View Results Tree" listener element, the "Response data" tab stays empty. There are two possible solutions: use a "Save Responses to a file" listener (see sample) or check the jmeter log.

For examples, see the JMeter .jmx files in the [samples directory](https://bitbucket.org/pjtr/jmeter-websocket-samplers/src/master/samples/?at=master)!

## Status

This project is only just started. It's functional, but don't expect it to be very robust. For example, large payloads are not yet handled correctly, and neither are unexpected server responses. If something unexpected happens, check the JMeter log file.

## Building

Gradle is used as build tool, so execute

    gradle assemble

to build. Almost any version of gradle will do (tested with 2.2). Gradle can also generate IntelliJ Idea project files for you:

    gradle idea


## Feedback

Questions, problems, or other feedback? Please mail the author (peter dot doornbosch) at luminis dot eu, or create an issue at <https://bitbucket.org/pjtr/jmeter-websocket-samplers/issues>.
