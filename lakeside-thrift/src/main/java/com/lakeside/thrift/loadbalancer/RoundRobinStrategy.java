package com.lakeside.thrift.loadbalancer;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.lakeside.thrift.ResourceExhaustedException;

import java.util.*;

/**
 * A load balancer that distributes load by randomizing the list of available backends, and then
 * rotating through them evenly.
 */
public class RoundRobinStrategy<S> extends StaticLoadBalancingStrategy<S> {

  private volatile Iterator<S> iterator = Iterators.emptyIterator();

  @Override
  protected Collection<S> onBackendsOffered(Set<S> targets) {
    List<S> newTargets = Lists.newArrayList(targets);
    Collections.shuffle(newTargets);
    iterator = Iterators.cycle(newTargets);
    return newTargets;
  }

  @Override
  public S nextBackend() throws ResourceExhaustedException {
    if (!iterator.hasNext()) throw new ResourceExhaustedException("No backends available!");
    return iterator.next();
  }
}
