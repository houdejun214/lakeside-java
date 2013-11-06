package com.lakeside.data.berkeleydb;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

import com.hajo.thrift.HajoService;
import com.hajo.thrift.RecordType;

public class RemoteBerkeleyDB<PK,T> implements Closeable {
	
	//private static final Logger log = LoggerFactory.getLogger("RemoteBerkeleyDB");
	private TSocket transport;
	private HajoService.Client client;
	private DataBinding<PK> keyBinding;
	private DataBinding<T> valueBinding;
	
	public RemoteBerkeleyDB(String host,int port,Class<PK> pkClass, Class<T> valueClass){
		transport = new TSocket(host, port);
        try {
			transport.open();
		} catch (TTransportException e) {
			if(transport!=null){
				transport.close();
			}
			throw new RuntimeException("open connection exception");
		}
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        client = new HajoService.Client(protocol);
        keyBinding = DataBinding.getPrimaryBinding(pkClass);
        valueBinding = DataBinding.getPrimaryBinding(valueClass);
	}
	
	public void save(PK key,T value){
		RecordType record = new RecordType();
		record.setKey(keyBinding.getBytes(key));
		record.setValue(valueBinding.getBytes(value));
		try {
			client.insertRecord(record);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	public T get(PK key){
		try {
			RecordType record = client.getRecord(keyBinding.getBytes(key));
			ByteBuffer value = record.value;
			return valueBinding.getObject(value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void close() throws IOException {
		if(transport!=null){
			transport.close();
		}
	}
}
