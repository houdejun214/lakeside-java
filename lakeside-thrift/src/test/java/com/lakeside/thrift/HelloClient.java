package com.lakeside.thrift;

import com.lakeside.thrift.pool.ThriftConnection;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

public class HelloClient extends Hello.Client implements ThriftConnection.TServiceValidator {

    public HelloClient() {
       super(null);
    }

	public HelloClient(TProtocol prot) {
		super(prot);
	}

	public boolean validate(){
		try {
			this.hi();
		} catch (TException e) {
			return false;
		}
		return true;
	}
}
