package com.lakeside.thrift.host;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 * An immutable representation of a host and port of a thrift server.
 * 
 * @author zhufb
 * 
 */
public class ThriftHost {

	private static final int NO_PORT = -1;
	private String ip;
	private int port;

	public ThriftHost(String ip, int port) {
		this.ip = ip;
		this.port = port;
		if(!isValidPort(port)){
			throw new IllegalArgumentException("please specify a valid port");
		}
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		return ip+":"+port;
	}

	/**
	 * get a thrift host from specify host and port
	 * @param host
	 * @param port
	 * @return
	 */
	public static ThriftHost from(String host, int port) {
		return new ThriftHost(host, port);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(ip,port);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThriftHost other = (ThriftHost) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (port != other.port)
			return false;
		return true;
	}



	/**
	 * ipv6 match patterns
	 */
	private static final Pattern BRACKET_PATTERN = Pattern.compile("^\\[(.*:.*)\\](?::(\\d*))?$");

	/**
	 * Split a freeform string into a host and port, without strict validation.
	 * 
	 * Note that the host-only formats will leave the port field undefined. You
	 * 
	 * @param hostPortString the input string to parse.
	 * @return if parsing was successful, a populated ThriftHost object.
	 * @throws IllegalArgumentException
	 *             if nothing meaningful could be parsed.
	 */
	public static ThriftHost from(String hostPortString) {
		checkNotNull(hostPortString);
		String host;
		String portString = null;
		if (hostPortString.startsWith("[")) {
			// Parse a bracketed host, typically an IPv6 literal.
			Matcher matcher = BRACKET_PATTERN.matcher(hostPortString);
			checkArgument(matcher.matches(), "Invalid bracketed host/port: " + hostPortString);
			host = matcher.group(1);
			portString = matcher.group(2); // could be null
		} else {
			int colonPos = hostPortString.indexOf(':');
			if (colonPos >= 0 && hostPortString.indexOf(':', colonPos + 1) == -1) {
				// Exactly 1 colon. Split into host:port.
				host = hostPortString.substring(0, colonPos);
				portString = hostPortString.substring(colonPos + 1);
			} else {
				// 0 or 2+ colons. Bare hostname or IPv6 literal.
				host = hostPortString;
			}
		}

		int port = NO_PORT;
		if (!Strings.isNullOrEmpty(portString)) {
			// Try to parse the whole port string as a number.
			// JDK7 accepts leading plus signs. We don't want to.
			checkArgument(!portString.startsWith("+"), "Unparseable port number: %s",
					hostPortString);
			try {
				port = Integer.parseInt(portString);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Unparseable port number: " + hostPortString);
			}
			checkArgument(isValidPort(port), "Port number out of range: %s", hostPortString);
		}

		return new ThriftHost(host, port);
	}

	/** Return true for valid port numbers. */
	private static boolean isValidPort(int port) {
		return port >= 0 && port <= 65535;
	}
}
