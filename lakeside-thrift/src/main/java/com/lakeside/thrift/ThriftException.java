package com.lakeside.thrift;

/**
 * @author zhufb
 *
 */
public class ThriftException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 346027109391114652L;

	public ThriftException(String message){
		super(message);
	}

	public ThriftException(Throwable cause){
		super(cause);
	}
	
	public ThriftException(String message, Throwable cause) {
        super(message, cause);
    }
}
