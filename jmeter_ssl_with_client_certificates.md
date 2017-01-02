# JMeter SSL with client certificates

JMeter out of the box supports SSL / TLS with client authentication, but setting it up correctly, especially with different client certificates for each connection, can be a bit of a challenge. Follow these step to get it up and running quickly.

## Prerequisite: Java keystore

Before you start, you should ensure that you have one keystore (.jks) file, that contains all client certificates that you want to use in the test. Moreover, the password of the keys in that keystore, must be the same as the password of the keystore itself! If this is not the case, you can change the password of a key with a command like this:

    keytool -keystore keystore.jks -keypasswd -storepass secret -alias entry1 -keypass 12345 -new secret

which changes the password for the key with alias 'entry1' from '12345' to 'secret', which is also the password for the keystore itself ('-storepass').

## Configure keystore

Unfortunately, the keystore configuration (i.e. its path and the keystore password) cannot be part of the JMeter test plan; it can only configured globally. You can either do this via the JMeter GUI, or via the `system.properties` file.

### Configure keystore via GUI

Select "SSL Manager" from the "Options" menu and select the keystore (.jks) file. Note that you could also use a ".p12" (PKCS12) file format. 

JMeter will ask for the keystore password when the test is started. The password nor the keystore location will be remembered when JMeter is restarted, so you'll have to enter this information every time. To avoid this, set the appropriate properties in the system.properties file.

### Configure keystore via system.properties file

In the JMeter <bin> directory, you'll find a file named `system.properties`. Open it, find the following two properties and set the location and password of your keystore:

    javax.net.ssl.keyStore=/path/to/your/keystore.jks
    javax.net.ssl.keyStorePassword=secret

Having these properties set is more convenient thab using the "SSL Manager" GUI Option each time, but if you use the same JMeter for different purposes, you need to remember that you configured it with a specific keystore.

## Consider disabling cached ssl context

When you plan to use different client certificates on the same JMeter thread, e.g. you use loops in the test plan and each iteration is supposed to set up a new TLS connection with a different cleitn certificate, you _must_ set the `https.use.cached.ssl.context property` to false, otherwise, the same certificate will be used for each iteration in the loop.

You will find this property in the `jmeter.properties` file in the JMeter <bin> directory. Initially, it will contain these lines:

    # Control if we allow reuse of cached SSL context between iterations
    # set the value to 'false' to reset the SSL context each iteration
    #https.use.cached.ssl.context=true

Uncomment the last line *and* don't forget to set the value to *false*!

## Test plan

In the test plan, you need to incorporate two components for making JMeter use the right certificate for each SSL connection.

### Define 'alias' property

In the last step below, you'll specify a JMeter property, whose value will be the alias of the client certificate that is going to be used to set up the connection. Of course, you'll need to provide proper values for this property as well. The most common (and easy) way is to use a "CSV Data Set Config". 

Create a simple text file with all key aliasses you want to use, one alias on each line. Next, configure a "CSV Data Set Config" with this file and set the name of the JMeter property you want to have loaded with the values from the file, e.g. "client_cert_alias".

### Keystore configuration

You need to configure the (use of the) keystore in your test plan too. Add a "Keystore Configuration" component to your test plan and provide the following values:

* Preload: true
* Variable name holding certificate alias: the name of the JMeter property you defined in the previous step, e.g. "client_cert_alias"
* Alias Start index (0-based): 0
* Alias End index (0-based): a value that is at least as large as the number of certificates in your keystore (I always set it to 1.000.000 as the real value doesn't seem to matter).

You might wonder what the "start index"/"end index" thing is for. Me too, as loading large number of certificates doesn't take that long (less then a second), so why bother? Maybe this is a remnant from old times, when loading certificates took long.

## Ready

If you did all of the above, you are ready to go. Configure a Sampler that uses TLS and it should use client certificates.
