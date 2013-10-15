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

import com.hajo.thrift.HajoException;
import com.hajo.thrift.RecordType;
import org.apache.thrift.TException;

/**
 *
 * @author hari
 */
public interface IBerkeleyDbHandler {

    void connect();

    void deleteRecord(byte[] key) throws HajoException, TException;

    void disconnect();

    RecordType getRecord(byte[] key) throws HajoException, TException;

    String getStats();

    void insertRecord(RecordType record) throws HajoException, TException;

}
