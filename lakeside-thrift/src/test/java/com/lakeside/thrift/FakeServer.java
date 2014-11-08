package com.lakeside.thrift;

public class FakeServer extends ThriftServer {
    private static final String SERVER_NAME = "Test";
    private static final String SERVER_VERSION = "test-version";

    public FakeServer() {
        super(SERVER_NAME, SERVER_VERSION);
    }
}