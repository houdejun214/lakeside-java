package com.lakeside.thrift.loadbalancer;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.lakeside.core.Closure;
import com.lakeside.thrift.ResourceExhaustedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Deque;
import java.util.Set;

public class MarkDeadStrategy<K> implements LoadBalancingStrategy<K> {

    private static final Logger log = LoggerFactory.getLogger(MarkDeadStrategy.class);
    private static final int WAIT_TIME = 10 * 1000;
    private final LoadBalancingStrategy<K> wrappedStrategy;

    private final Deque<K> deadBackends = Queues.newArrayDeque();
    private Closure<Collection<K>> onBackendsChosen;
    private volatile Set<K> liveBackends;
    private final Thread recover;

    public MarkDeadStrategy(LoadBalancingStrategy<K> wrappedStrategy) {
        this.wrappedStrategy = wrappedStrategy;
        recover = new Thread(new DeadRecover());
        recover.start();
    }

    @Override
    public void offerBackends(Set<K> offeredBackends, Closure<Collection<K>> onBackendsChosen) {
        this.liveBackends = Sets.newHashSet(offeredBackends);
        this.onBackendsChosen = onBackendsChosen;
        // reset the backends;
        this.deadBackends.clear();
        wrappedStrategy.offerBackends(offeredBackends,onBackendsChosen);
    }

    @Override
    public K nextBackend() throws ResourceExhaustedException {
        return wrappedStrategy.nextBackend();
    }

    @Override
    public void addConnectResult(K key, RequestTracker.RequestResult result, long connectTimeNanos) {
        switch (result) {
            case DEAD:
            case TIMEOUT:
                addDeadBackends(key);
                break;
            case SUCCESS:
                break;
            default:
        }
    }

    private void addDeadBackends(final K dead) {
        if (dead != null) {
            deadBackends.push(dead);
            if (liveBackends != null) {
                liveBackends.remove(dead);
                wrappedStrategy.offerBackends(liveBackends, onBackendsChosen);
                log.warn("add [{}] to dead list", dead);
            }
        }
    }

    @Override
    public void connectionReturned(K key) {

    }

    private class DeadRecover implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    if (deadBackends.size() > 0) {
                        while(!deadBackends.isEmpty()){
                            K dead = deadBackends.pop();
                            liveBackends.add(dead);
                            log.warn("try to add dead[{}] to live list", dead);
                        }
                        wrappedStrategy.offerBackends(liveBackends, onBackendsChosen);
                    }
                    Thread.sleep(WAIT_TIME);
                }
            } catch (InterruptedException e) {
                log.warn("fail to recover dead endpoint", e);
            }
        }
    }
}