package com.lakeside.data.berkeleydb;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.lakeside.core.utils.UUIDUtils;

public abstract class DataBinding<E> {
	abstract ByteBuffer getBytes(E obj);
	abstract E getObject(ByteBuffer buffer);
	
	 private static final Map<Class,DataBinding> primitives =
	        new HashMap<Class,DataBinding>();
	    static {
	        addPrimitive(byte[].class,new ByteDataBinding());
	        addPrimitive(float[].class,new FloatArrayDataBinding());
	        addPrimitive(double[].class,new DoubleArrayDataBinding());
	        addPrimitive(String.class,new StringDataBinding());
	        addPrimitive(UUID.class,new UUIDDataBinding());
	    }
    private static void addPrimitive(Class<?> cls1,DataBinding<?> binding) {
		primitives.put(cls1, binding);
    }
    
    public static <T> DataBinding<T> getPrimaryBinding(Class<T> cls1){
    	return primitives.get(cls1);
    }
}

class ByteDataBinding extends DataBinding<byte[]>{

	@Override
	ByteBuffer getBytes(byte[] obj) {
		return ByteBuffer.wrap(obj);
	}

	@Override
	byte[] getObject(ByteBuffer buffer) {
		return buffer.array();
	}
}


class StringDataBinding extends DataBinding<String>{

	@Override
	ByteBuffer getBytes(String obj) {
		return ByteBuffer.wrap(obj.getBytes());
	}

	@Override
	String getObject(ByteBuffer buffer) {
		return new String(buffer.array());
	}
}

class UUIDDataBinding extends DataBinding<UUID>{

	@Override
	ByteBuffer getBytes(UUID obj) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(obj.getMostSignificantBits());
		bb.putLong(obj.getLeastSignificantBits());
		return bb;
	}

	@Override
	UUID getObject(ByteBuffer buffer) {
		return  UUIDUtils.byteToUUID(buffer.array());
	}
}

class FloatArrayDataBinding extends DataBinding<float[]>{

	@Override
	ByteBuffer getBytes(float[] obj) {
		ByteBuffer byteBuf = ByteBuffer.allocate(4 * obj.length);
		FloatBuffer floatBuf = byteBuf.asFloatBuffer();
		floatBuf.put(obj);
		return byteBuf;
	}

	@Override
	float[] getObject(ByteBuffer buffer) {
		FloatBuffer asFloatBuffer = buffer.asFloatBuffer();
		float[] array = new float[asFloatBuffer.capacity()];
		asFloatBuffer.get(array);
		return array;
	}
}
class DoubleArrayDataBinding extends DataBinding<double[]>{
	
	@Override
	ByteBuffer getBytes(double[] obj) {
		ByteBuffer byteBuf = ByteBuffer.allocate(8 * obj.length);
		DoubleBuffer doubleBuf = byteBuf.asDoubleBuffer();
		doubleBuf.put(obj);
		return byteBuf;
	}
	
	@Override
	double[] getObject(ByteBuffer buffer) {
		DoubleBuffer doubleBuf = buffer.asDoubleBuffer();
		double[] array = new double[doubleBuf.capacity()];
		doubleBuf.get(array);
		return array;
	}
}