package com.lakeside.thrift.loadbalancer;

import com.lakeside.thrift.ResourceExhaustedException;

import java.util.Set;

/**
 * Created by dejun on 06/08/14.
 */
public class SingleBackend<K> implements LoadBalancer<K> {

    private final K backend;

    public SingleBackend(K backend) {
        this.backend = backend;
    }

    @Override
    public void offerBackends(Set<K> offeredBackends) {

    }

    @Override
    public K nextBackend() throws ResourceExhaustedException {
        return this.backend;
    }

    @Override
    public void requestResult(K key, RequestResult result, long requestTimeNanos) {

    }
}
