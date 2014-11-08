package com.lakeside.thrift.loadbalancer;


import com.lakeside.core.Closure;
import com.lakeside.thrift.ResourceExhaustedException;

import java.util.Collection;
import java.util.Set;

/**
 * A strategy for balancing request load among backends.
 *
 * Strategies should be externally synchronized, and therefore do not have to worry about reentrant
 * access.
 *
 * @author William Farner
 */
public interface LoadBalancingStrategy<K> {

  /**
   * Offers a set of backends that the load balancer should choose from to distribute load amongst.
   *
   * @param offeredBackends Backends to choose from.
   * @param onBackendsChosen A callback that should be notified when the offered backends have been
   *     (re)chosen from.
   */
  public void offerBackends(Set<K> offeredBackends, Closure<Collection<K>> onBackendsChosen);

  /**
   * Gets the next backend that a request should be sent to.
   *
   * @return Next backend to send a request.
   * @throws com.lakeside.thrift.ResourceExhaustedException If there are no available backends.
   */
  public K nextBackend() throws ResourceExhaustedException;

  /**
   * Offers information about a connection result.
   *
   * @param key Backend key.
   * @param result Connection result.
   * @param connectTimeNanos Time spent waiting for connection to be established.
   */
  public void addConnectResult(K key, RequestTracker.RequestResult result, long connectTimeNanos);

  /**
   * Offers information about a connection that was returned.
   *
   * @param key Backend key.
   */
  public void connectionReturned(K key);
}
