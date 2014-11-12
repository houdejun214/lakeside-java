package com.lakeside.core.resource;


import com.lakeside.core.utils.ClassUtils;
import com.lakeside.core.utils.StringUtils;

import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;


public class DefaultPropertiesPersister{

	// Determine whether Properties.load(Reader) is available (on JDK 1.6+)
	private static final boolean loadFromReaderAvailable =
			ClassUtils.hasMethod(Properties.class, "load", new Class[]{Reader.class});

	// Determine whether Properties.store(Writer, String) is available (on JDK 1.6+)
	private static final boolean storeToWriterAvailable =
			ClassUtils.hasMethod(Properties.class, "store", new Class[] {Writer.class, String.class});


	public void load(Properties props, InputStream is) throws IOException {
		props.load(is);
	}

	public void load(Properties props, Reader reader) throws IOException {
		if (loadFromReaderAvailable) {
			// On JDK 1.6+
			props.load(reader);
		}
		else {
			// Fall back to manual parsing.
			doLoad(props, reader);
		}
	}

	protected void doLoad(Properties props, Reader reader) throws IOException {
		BufferedReader in = new BufferedReader(reader);
		while (true) {
			String line = in.readLine();
			if (line == null) {
				return;
			}
			line = StringUtils.trimLeadingWhitespace(line);
			if (line.length() > 0) {
				char firstChar = line.charAt(0);
				if (firstChar != '#' && firstChar != '!') {
					while (endsWithContinuationMarker(line)) {
						String nextLine = in.readLine();
						line = line.substring(0, line.length() - 1);
						if (nextLine != null) {
							line += StringUtils.trimLeadingWhitespace(nextLine);
						}
					}
					int separatorIndex = line.indexOf("=");
					if (separatorIndex == -1) {
						separatorIndex = line.indexOf(":");
					}
					String key = (separatorIndex != -1 ? line.substring(0, separatorIndex) : line);
					String value = (separatorIndex != -1) ? line.substring(separatorIndex + 1) : "";
					key = StringUtils.trimTrailingWhitespace(key);
					value = StringUtils.trimLeadingWhitespace(value);
					props.put(unescape(key), unescape(value));
				}
			}
		}
	}

	protected boolean endsWithContinuationMarker(String line) {
		boolean evenSlashCount = true;
		int index = line.length() - 1;
		while (index >= 0 && line.charAt(index) == '\\') {
			evenSlashCount = !evenSlashCount;
			index--;
		}
		return !evenSlashCount;
	}

	protected String unescape(String str) {
		StringBuilder result = new StringBuilder(str.length());
		for (int index = 0; index < str.length();) {
			char c = str.charAt(index++);
			if (c == '\\') {
				c = str.charAt(index++);
				if (c == 't') {
					c = '\t';
				}
				else if (c == 'r') {
					c = '\r';
				}
				else if (c == 'n') {
					c = '\n';
				}
				else if (c == 'f') {
					c = '\f';
				}
			}
			result.append(c);
		}
		return result.toString();
	}


	public void store(Properties props, OutputStream os, String header) throws IOException {
		props.store(os, header);
	}

	public void store(Properties props, Writer writer, String header) throws IOException {
		if (storeToWriterAvailable) {
			// On JDK 1.6+
			props.store(writer, header);
		}
		else {
			// Fall back to manual parsing.
			doStore(props, writer, header);
		}
	}

	protected void doStore(Properties props, Writer writer, String header) throws IOException {
		BufferedWriter out = new BufferedWriter(writer);
		if (header != null) {
			out.write("#" + header);
			out.newLine();
		}
		out.write("#" + new Date());
		out.newLine();
		for (Enumeration keys = props.keys(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			String val = props.getProperty(key);
			out.write(escape(key, true) + "=" + escape(val, false));
			out.newLine();
		}
		out.flush();
	}

	protected String escape(String str, boolean isKey) {
		int len = str.length();
		StringBuilder result = new StringBuilder(len * 2);
		for (int index = 0; index < len; index++) {
			char c = str.charAt(index);
			switch (c) {
				case ' ':
					if (index == 0 || isKey) {
						result.append('\\');
					}
					result.append(' ');
					break;
				case '\\':
					result.append("\\\\");
					break;
				case '\t':
					result.append("\\t");
					break;
				case '\n':
					result.append("\\n");
					break;
				case '\r':
					result.append("\\r");
					break;
				case '\f':
					result.append("\\f");
					break;
				default:
					if ("=: \t\r\n\f#!".indexOf(c) != -1) {
						result.append('\\');
					}
					result.append(c);
			}
		}
		return result.toString();
	}


	public void loadFromXml(Properties props, InputStream is) throws IOException {
		try {
			props.loadFromXML(is);
		}
		catch (NoSuchMethodError err) {
			throw new IOException("Cannot load properties XML file - not running on JDK 1.5+: " + err.getMessage());
		}
	}

	public void storeToXml(Properties props, OutputStream os, String header) throws IOException {
		try {
			props.storeToXML(os, header);
		}
		catch (NoSuchMethodError err) {
			throw new IOException("Cannot store properties XML file - not running on JDK 1.5+: " + err.getMessage());
		}
	}

	public void storeToXml(Properties props, OutputStream os, String header, String encoding) throws IOException {
		try {
			props.storeToXML(os, header, encoding);
		}
		catch (NoSuchMethodError err) {
			throw new IOException("Cannot store properties XML file - not running on JDK 1.5+: " + err.getMessage());
		}
	}

}
