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
package com.hajo.berkeleydb;

import java.io.File;
import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hajo.Main;
import com.hajo.config.HajoConfigHolder;
import com.hajo.thrift.HajoException;
import com.hajo.thrift.HajoService.Iface;
import com.hajo.thrift.RecordType;
import com.lakeside.core.utils.ApplicationResourceUtils;
import com.lakeside.core.utils.FileUtils;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseStats;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentStats;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.StatsConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionStats;

/**
 *
 * @author hari
 */
public class MultiThreadedBerkeleyDbHandler implements Iface, IBerkeleyDbHandler {
	private static Logger log = LoggerFactory.getLogger(MultiThreadedBerkeleyDbHandler.class);
    private Database database;
    private Environment env;
    private Transaction txn = null;
    private HajoConfigHolder holder;
    private static final Object lock = new Object();

    public MultiThreadedBerkeleyDbHandler(HajoConfigHolder holder) {
        this.holder = holder;
    }

    public void connect() {
        synchronized (lock) {
            EnvironmentConfig env_config = new EnvironmentConfig();
            env_config.setAllowCreate(holder.config.isAllowCreate());
            env_config.setTransactional(holder.config.isTransactional());
            String databaseDirectory = ApplicationResourceUtils.getResourceUrl(holder.config.getDatabaseDirectory());
            FileUtils.mkDirectory(databaseDirectory);
			env = new Environment(new File(databaseDirectory), env_config);
            if (holder.config.isTransactional()) {
                txn = env.beginTransaction(null, null);
            }
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setTransactional(holder.config.isTransactional());
            dbConfig.setAllowCreate(holder.config.isAllowCreate());
            dbConfig.setSortedDuplicates(holder.config.isSortedDuplicates());
            database = env.openDatabase(txn, holder.config.getDatabaseName(), dbConfig);
            if (holder.config.isTransactional() && txn != null) {
                txn.commit();
                txn = null;
            }
        }
    }

    public void disconnect() {
        synchronized (lock) {
            if (holder.config.isTransactional()) {
                txn = env.beginTransaction(null, null);
            }
            database.close();
            if (holder.config.isTransactional() && txn != null) {
                txn.commit();
                txn = null;
            }
            env.close();
        }
    }

    public void insertRecord(RecordType record) throws HajoException, TException {
        synchronized (lock) {
            if (holder.config.isTransactional()) {
                txn = env.beginTransaction(null, null);
            }
            byte[] keybytes = record.getKey();
			DatabaseEntry key = new DatabaseEntry(keybytes);
            DatabaseEntry value = new DatabaseEntry(record.getValue());
            OperationStatus status = database.put(txn, key, value);
            if (status != OperationStatus.SUCCESS) {
                throw new HajoException("Data insertion got status "
                        + status);
            }
            if (holder.config.isTransactional() && txn != null) {
                txn.commit();
                txn = null;
            }
            //log.info("inserted record:"+new String(keybytes));
        }

    }

    public RecordType getRecord(byte[] key) throws HajoException, TException {
        synchronized (lock) {
            if (holder.config.isTransactional()) {
                txn = env.beginTransaction(null, null);
            }
            DatabaseEntry entry_key = new DatabaseEntry(key);
            DatabaseEntry value = new DatabaseEntry();
            OperationStatus status = database.get(txn, entry_key, value, LockMode.RMW);
            if (status != OperationStatus.SUCCESS) {
                throw new HajoException("Data retrieval got status "
                        + status);
            }
            if (holder.config.isTransactional() && txn != null) {
                txn.commit();
                txn = null;
            }
            RecordType record = new RecordType();
            record.setKey(key);
            record.setValue(value.getData());
            return record;
        }
    }

    public void deleteRecord(byte[] key) throws HajoException, TException {
        synchronized (lock) {
            if (holder.config.isTransactional()) {
                txn = env.beginTransaction(null, null);
            }
            DatabaseEntry entry_key = new DatabaseEntry(key);
            OperationStatus status = database.delete(txn, entry_key);
            if (status != OperationStatus.SUCCESS) {
                throw new HajoException("Data deletion got status "
                        + status);
            }
            if (holder.config.isTransactional() && txn != null) {
                txn.commit();
                txn = null;
            }
        }
    }

    public String getStats() {
        synchronized (lock) {
            DatabaseStats dbStats = database.getStats(StatsConfig.DEFAULT);
            EnvironmentStats envStats = database.getEnvironment().getStats(StatsConfig.DEFAULT);
            TransactionStats txnStats = database.getEnvironment().getTransactionStats(StatsConfig.DEFAULT);
            return "<h2>Database Stats</h2><br>" + dbStats.toString() + "<br><h2>Environment Stats</h2><br>" + envStats + "<br><h2>Transaction Stats</h2><br>" + txnStats;
        }
    }

	public RecordType getRecord(ByteBuffer key) throws HajoException,
			TException {
		return this.getRecord(key.array());
	}

	public void deleteRecord(ByteBuffer key) throws HajoException, TException {
		this.deleteRecord(key.array());
	}
}
