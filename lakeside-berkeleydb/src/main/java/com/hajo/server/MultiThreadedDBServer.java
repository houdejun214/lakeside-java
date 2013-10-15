/*
 * This file is part of Hajo (http://allamraju.com/hajo) Copyright (C) 2010 Narahari
 * Allamraju (anarahari@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package com.hajo.server;

import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import com.hajo.berkeleydb.IBerkeleyDbHandler;
import com.hajo.berkeleydb.MultiThreadedBerkeleyDbHandler;
import com.hajo.config.HajoConfigHolder;
import com.hajo.thrift.HajoException;
import com.hajo.thrift.HajoService;
import com.hajo.thrift.HajoService.Iface;
import com.hajo.utils.HajoStatsManager;

/**
 *
 * @author hari
 */
public class MultiThreadedDBServer implements IHajoServer {
    private HajoConfigHolder holder;
    private int port;
    private IBerkeleyDbHandler handler;
    private HajoService.Processor<Iface> processor;
    private TServerSocket socket;
    private TThreadPoolServer server;

    public MultiThreadedDBServer(HajoConfigHolder holder) throws HajoException {
        this.holder = holder;
        this.port = holder.config.getDataBaseServerPort();
    }

    private void connectBerkeleyDB() {
        handler = new MultiThreadedBerkeleyDbHandler(holder);
        handler.connect();
        HajoStatsManager.handler=handler;
    }

    public void startThriftServer() throws TTransportException {
        connectBerkeleyDB();
        processor = new HajoService.Processor<Iface>((Iface)handler);
        socket = new TServerSocket(port);
        Args args = new Args(socket).processor(processor);
        server = new TThreadPoolServer(args);
        server.serve();
    }

    public void stopThriftServer(){
        server.stop();
    }

}
