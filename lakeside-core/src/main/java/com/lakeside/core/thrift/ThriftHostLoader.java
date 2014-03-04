package com.lakeside.core.thrift;

import java.util.List;

/**
 * @author zhufb
 *
 */
public interface ThriftHostLoader {

	public List<ThriftHost> load(ThriftConfig config);
}
