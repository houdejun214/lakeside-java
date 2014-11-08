package com.lakeside.thrift.loadbalancer;

import com.lakeside.thrift.ResourceExhaustedException;

import java.util.Set;

public interface LoadBalancer<K> extends RequestTracker<K> {

  void offerBackends(Set<K> offeredBackends);

  K nextBackend() throws ResourceExhaustedException;
}
