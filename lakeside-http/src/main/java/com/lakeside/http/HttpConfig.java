
package com.lakeside.http;


/**
 *
 */
public class HttpConfig {

	private String userAgentString = "crawler4j";
	
	private int retryTimes = 3;

	/**
	 * Politeness delay in milliseconds (delay between sending two requests to
	 * the same host).
	 */
	private int politenessDelay = 200;

	/**
	 * Should we also crawl https pages?
	 */
	private boolean includeHttpsPages = true;

	/**
	 * Should we fetch binary content such as images, audio, ...?
	 */
	private boolean includeBinaryContentInCrawling = false;

	/**
	 * Maximum Connections per host
	 */
	private int maxConnectionsPerHost = 100;

	/**
	 * Maximum total connections
	 */
	private int maxTotalConnections = 100;

	/**
	 * Socket timeout in milliseconds
	 */
	private int socketTimeout = 30000;

	/**
	 * Connection timeout in milliseconds
	 */
	private int connectionTimeout = 60000;

	/**
	 * Max number of outgoing links which are processed from a page
	 */
	private int maxOutgoingLinksToFollow = 5000;

	/**
	 * Max allowed size of a page. Pages larger than this size will not be
	 * fetched.
	 */
	private int maxDownloadSize = 1048576;

	/**
	 * Should we follow redirects?
	 */
	private boolean followRedirects = true;

	public HttpConfig() {
	}

	public String getUserAgentString() {
		return userAgentString;
	}

	/**
	 * user-agent string that is used for representing your crawler to web
	 * servers. See http://en.wikipedia.org/wiki/User_agent for more details
	 */
	public void setUserAgentString(String userAgentString) {
		this.userAgentString = userAgentString;
	}

	public int getPolitenessDelay() {
		return politenessDelay;
	}

	/**
	 * Politeness delay in milliseconds (delay between sending two requests to
	 * the same host).
	 * 
	 * @param politenessDelay
	 *            the delay in milliseconds.
	 */
	public void setPolitenessDelay(int politenessDelay) {
		this.politenessDelay = politenessDelay;
	}

	public boolean isIncludeHttpsPages() {
		return includeHttpsPages;
	}

	/**
	 * Should we also crawl https pages?
	 */
	public void setIncludeHttpsPages(boolean includeHttpsPages) {
		this.includeHttpsPages = includeHttpsPages;
	}

	public boolean isIncludeBinaryContentInCrawling() {
		return includeBinaryContentInCrawling;
	}

	/**
	 * Should we fetch binary content such as images, audio, ...?
	 */
	public void setIncludeBinaryContentInCrawling(boolean includeBinaryContentInCrawling) {
		this.includeBinaryContentInCrawling = includeBinaryContentInCrawling;
	}

	public int getMaxConnectionsPerHost() {
		return maxConnectionsPerHost;
	}

	/**
	 * Maximum Connections per host
	 */
	public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
		this.maxConnectionsPerHost = maxConnectionsPerHost;
	}

	public int getMaxTotalConnections() {
		return maxTotalConnections;
	}

	/**
	 * Maximum total connections
	 */
	public void setMaxTotalConnections(int maxTotalConnections) {
		this.maxTotalConnections = maxTotalConnections;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	/**
	 * Socket timeout in milliseconds
	 */
	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * Connection timeout in milliseconds
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getMaxOutgoingLinksToFollow() {
		return maxOutgoingLinksToFollow;
	}

	/**
	 * Max number of outgoing links which are processed from a page
	 */
	public void setMaxOutgoingLinksToFollow(int maxOutgoingLinksToFollow) {
		this.maxOutgoingLinksToFollow = maxOutgoingLinksToFollow;
	}

	public int getMaxDownloadSize() {
		return maxDownloadSize;
	}

	/**
	 * Max allowed size of a page. Pages larger than this size will not be
	 * fetched.
	 */
	public void setMaxDownloadSize(int maxDownloadSize) {
		this.maxDownloadSize = maxDownloadSize;
	}

	public boolean isFollowRedirects() {
		return followRedirects;
	}

	/**
	 * Should we follow redirects?
	 */
	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}


	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}
	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("User agent string: " + getUserAgentString() + "\n");
		sb.append("Include https pages: " + isIncludeHttpsPages() + "\n");
		sb.append("Include binary content: " + isIncludeBinaryContentInCrawling() + "\n");
		sb.append("Max connections per host: " + getMaxConnectionsPerHost() + "\n");
		sb.append("Max total connections: " + getMaxTotalConnections() + "\n");
		sb.append("Socket timeout: " + getSocketTimeout() + "\n");
		sb.append("Max total connections: " + getMaxTotalConnections() + "\n");
		sb.append("Max outgoing links to follow: " + getMaxOutgoingLinksToFollow() + "\n");
		sb.append("Max get size: " + getMaxDownloadSize() + "\n");
		sb.append("Should follow redirects?: " + isFollowRedirects() + "\n");
		sb.append("retry times : " + getRetryTimes() + "\n");
		return sb.toString();
	}
}
