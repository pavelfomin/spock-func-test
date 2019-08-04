package com.droidablebee.spock

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import org.apache.commons.io.FileUtils
import spock.lang.Specification

abstract class BaseSpockSpec extends Specification {

    protected static final String X_API_CLIENT = "X-API-CLIENT";
    protected static final String USER_AGENT = "User-Agent";

    protected HTTPBuilder http

    def setup() {

        http = createHTTPBuilder()
    }

    protected HTTPBuilder createHTTPBuilder() {

        HTTPBuilder builder = new HTTPBuilder(getDefaultUri(), ContentType.JSON)

        //add commonly used headers
        builder.headers = getHttpHeaders()

        //ignore SSL errors
        builder.ignoreSSLIssues()

        //override default success handler
        builder.handler.success = { HttpResponseDecorator response, parsedResponseBody ->
            response.responseData = parsedResponseBody
            response
        }

        //override default error handler that throws HttpResponseException
        builder.handler.failure = builder.handler.success

        return builder
    }

    protected String readFromFile(String filepathAndName) throws IOException {

        File jsonFile = FileUtils.toFile(getClass().getResource(filepathAndName));
        return FileUtils.readFileToString(jsonFile);
    }

    protected Map<String, String> getHttpHeaders() {

        return [(X_API_CLIENT): "spock-func-test", (USER_AGENT): "groovy-http-client"];
    }

    protected abstract String getDefaultUri()
}