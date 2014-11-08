package com.lakeside.thrift.loadbalancer;



import com.lakeside.core.Closure;

import java.util.Collection;
import java.util.Set;

abstract class StaticLoadBalancingStrategy<K> implements LoadBalancingStrategy<K> {

  @Override
  public final void offerBackends(Set<K> offeredBackends, Closure<Collection<K>> onBackendsChosen) {
    onBackendsChosen.execute(onBackendsOffered(offeredBackends));
  }

  protected abstract Collection<K> onBackendsOffered(Set<K> offeredBackends);

  @Override
  public void addConnectResult(K backendKey, RequestTracker.RequestResult result, long connectTimeNanos) {
    // No-op.
  }

  @Override
  public void connectionReturned(K backendKey) {
    // No-op.
  }
}
