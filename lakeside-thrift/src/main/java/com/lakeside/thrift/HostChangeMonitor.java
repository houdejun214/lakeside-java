package com.lakeside.thrift;

import com.google.common.collect.ImmutableSet;

/**
 * Created by dejun on 06/08/14.
 */
public interface HostChangeMonitor<T> {
    void onChange(ImmutableSet<T> hostAndPorts);
}
