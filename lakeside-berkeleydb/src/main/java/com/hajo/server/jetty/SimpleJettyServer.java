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
package com.hajo.server.jetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;


/**
 *
 * @author hari
 */
public class SimpleJettyServer {
    Server server;

    public SimpleJettyServer(int port){
        server = new Server();
        Connector connector=new SocketConnector();
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});        
        server.setHandler(new BerkeleyDBStatsHandler());
    }

    public void start() throws Exception{
        server.start();
        server.join();
    }

    public void stop() throws Exception{
        server.stop();
    }

    public void restart() throws Exception{
        server.stop();
        server.start();
        server.join();
    }

}
