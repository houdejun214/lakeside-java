package com.lakeside.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import com.lakeside.thrift.pool.ThriftConnection.TServiceValidator;

public class HelloClient extends Hello.Client implements TServiceValidator {
	
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
