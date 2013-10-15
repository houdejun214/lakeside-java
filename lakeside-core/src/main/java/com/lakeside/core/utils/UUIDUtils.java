/*
 * (./) StringUtils.java
 * 
 * (cc) copyright@2010-2011
 * 
 * 
 * this library is all rights reserved , but you can used it for free.
 * if you want more support or functions, please contact with us!
 */
package com.lakeside.core.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * UUID
 * 
 * @author zhufb
 * 
 */
public class UUIDUtils {


	/**
	 * get the md5 value, and convert the md5 value to number code.
	 * 
	 * this method is used for get a unique code from a string
	 * 
	 * @param plain
	 * @return
	 */
	public static UUID getMd5UUID(String plain) {
		byte[] bytes = plain.getBytes();
		return UUID.nameUUIDFromBytes(bytes);
	}

	/**
	 * convert the byte array to UUID object
	 * 
	 * @param data
	 * @return
	 */
	public static UUID byteToUUID(byte[] data) {
		long msb = 0;
		long lsb = 0;
		assert data.length == 16;
		for (int i = 0; i < 8; i++)
			msb = (msb << 8) | (data[i] & 0xff);
		for (int i = 8; i < 16; i++)
			lsb = (lsb << 8) | (data[i] & 0xff);
		UUID uuid = new UUID(msb, lsb);
		return uuid;
	}

	/**
	 * convert UUID object to byte array
	 * 
	 * @param uuid
	 * @return
	 */
	public static byte[] uuidToByte(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		byte[] array = bb.array();
		return array;
	}
	
	/**
	 * covert UUID to base64 encode 
	 * 
	 * @param uuid
	 * @return
	 */
	public static String encode(UUID uuid){
		return new BASE64Encoder().encode(uuidToByte(uuid));
	}
	
	/**
	 * convert UUID to mongodb BinData String.
	 * 
	 * please check the reference web page 
	 * 
	 * https://github.com/mongodb/mongo-csharp-driver/blob/master/uuidhelpers.js
	 * 
	 * BinData type is 3
	 * @param uuid
	 * @return
	 */
	public static String encodeMongoBinData(UUID uuid){
		String uuidStr = uuid.toString();
		String hex = uuidStr.replaceAll("[{}-]", "");
		String msb = hex.substring(0, 16);
		String lsb = hex.substring(16, 32);
		StringBuilder _msb = new StringBuilder();
		StringBuilder _lsb = new StringBuilder();
		_msb.append(msb,14, 16).append( msb,12, 14).append( msb,10, 12).append( msb,8, 10).append( msb,6, 8).append( msb,4, 6).append( msb,2, 4).append( msb,0, 2);
		_msb.append(lsb,14, 16).append(lsb,12, 14).append(lsb,10, 12).append(lsb,8, 10).append(lsb,6, 8).append(lsb,4, 6).append(lsb,2, 4).append(lsb,0, 2);
	    hex = _msb.append(_lsb).toString();
		String base64 = StringUtils.hexToBase64(hex);
		return base64;
	}
	
	/**
	 * covert base64 encode to UUID  
	 * 
	 * @param base64
	 * @return
	 * @throws IOException 
	 */
	public static UUID decode(String base64){
		try {
			return byteToUUID(new BASE64Decoder().decodeBuffer(base64));
		} catch (Exception e) {
			return null;
		}
	}
	
}
