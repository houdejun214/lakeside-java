package com.lakeside.thrift;

/**
 * Created by dejun on 09/08/14.
 */
public interface DynamicSet<K> {

    void monitor(HostChangeMonitor<K> monitor);
}
