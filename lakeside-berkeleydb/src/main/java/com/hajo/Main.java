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
package com.hajo;

import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hajo.config.HajoConfigHolder;
import com.hajo.server.IHajoServer;
import com.hajo.server.MultiThreadedDBServer;
import com.hajo.server.jetty.SimpleJettyServer;
import com.hajo.thrift.HajoException;
import com.lakeside.core.utils.ApplicationResourceUtils;

/**
 *
 * @author hari
 */
public class Main {
	private static Logger log = LoggerFactory.getLogger(Main.class);
    static IHajoServer hajoServer;
    static SimpleJettyServer jettyServer;
    static ShutdownHook shutdownhook = new ShutdownHook();

    public static void main(String args[]) throws HajoException, TTransportException, Exception {
    	// load configuration file
    	String configFileName = ApplicationResourceUtils.getResourceUrl("conf/base-config.yaml");
    	HajoConfigHolder holder = new HajoConfigHolder(configFileName);
        hajoServer = new MultiThreadedDBServer(holder);
        Thread hajoThread = new Thread(new HajoRunner());
        hajoThread.start();
        log.info("starting hajo server success on port {}",holder.config.getDataBaseServerPort());
        
        jettyServer = new SimpleJettyServer(holder.config.getWebServerPort());
        Thread jettyThread = new Thread(new JettyRunner());
        jettyThread.start();
        Runtime.getRuntime().addShutdownHook(shutdownhook);
    }

    static class ShutdownHook extends Thread {

        public void run() {
            try {
                System.out.println("Shutting down");
                hajoServer.stopThriftServer();
                jettyServer.stop();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    static class HajoRunner implements Runnable {

        public void run() {
            try {
                hajoServer.startThriftServer();
            } catch (TTransportException ex) {
                ex.printStackTrace();
            }
        }
    }

    static class JettyRunner implements Runnable {

        public void run() {
            try {
                jettyServer.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
