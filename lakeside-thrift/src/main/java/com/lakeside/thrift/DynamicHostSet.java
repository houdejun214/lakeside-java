package com.lakeside.thrift;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.net.HostAndPort;
import com.lakeside.core.utils.StringUtils;

import java.util.Collection;
import java.util.Set;

/**
 * Created by dejun on 29/07/14.
 */
public class DynamicHostSet implements DynamicSet<HostAndPort> {

    private Set<HostAndPort> all = Sets.newHashSet();
    private Set<HostAndPort> lives = Sets.newHashSet();
    private Set<HostAndPort> deads = Sets.newHashSet();

    private HostChangeMonitor<HostAndPort> monitor;

    /**
     * add a server instance.
     * @param host
     * @param port
     */
    public void addServerInstance(String host, int port) {
        HostAndPort hostAndPort = HostAndPort.fromParts(host, port);
        if (!lives.contains(hostAndPort)) {
            lives.add(hostAndPort);
            all.add(hostAndPort);
            onChange();
        }
    }

    /**
     * add dead instance
     * @param host
     * @param port
     */
    public void addDeadInstance(String host, int port) {
        HostAndPort endpoint = HostAndPort.fromParts(host, port);
        if (all.contains(endpoint)) {
            deads.add(endpoint);
            lives.remove(endpoint);
            onChange();
        }
    }

    /**
     * replace all hosts with new
     * @param hosts
     */
    public void replaceWithList(Collection<HostAndPort> hosts) {
        // remove previous
        all.clear();
        lives.clear();
        deads.clear();

        // replace all.
        all.addAll(hosts);
        lives.addAll(hosts);
        onChange();
    }

    /**
     * remove a service instance
     * @param host
     * @param port
     */
    public void removeServerInstance(String host, int port) {
        HostAndPort hostAndPort = HostAndPort.fromParts(host, port);
        lives.remove(hostAndPort);
        all.remove(hostAndPort);
        deads.remove(hostAndPort);
        onChange();
    }

    @Override
    public void monitor(HostChangeMonitor<HostAndPort> monitor) {
        this.monitor = monitor;
        this.onChange();
    }

    private void onChange() {
        if (monitor != null) {
            monitor.onChange(ImmutableSet.copyOf(lives));
        }
    }

    @Override
    public String toString() {
        return StringUtils.join(lives, ",");
    }

    public Set<HostAndPort> getLives() {
        return lives;
    }

    public Set<HostAndPort> getAll() {
        return all;
    }

    public Set<HostAndPort> getDeads() {
        return deads;
    }
}
