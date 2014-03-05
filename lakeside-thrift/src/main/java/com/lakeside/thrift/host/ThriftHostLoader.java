package com.lakeside.thrift.host;

import java.util.List;

import com.lakeside.thrift.ThriftConfig;

/**
 * @author zhufb
 *
 */
public interface ThriftHostLoader {

	public List<ThriftHost> load(ThriftConfig config);
}
