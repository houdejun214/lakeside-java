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
package com.hajo.config;

/**
 *
 * @author hari
 */
public class HajoConfiguration {
    
    private String databaseDirectory;
    private boolean allowCreate;
    private boolean transactional;
    private boolean sortedDuplicates;
    private String databaseName;
    private int webServerPort = 1122;
    private int dataBaseServerPort = 1120;

    public boolean isAllowCreate() {
        return allowCreate;
    }

    public void setAllowCreate(boolean allowCreate) {
        this.allowCreate = allowCreate;
    }

    public String getDatabaseDirectory() {
        return databaseDirectory;
    }

    public void setDatabaseDirectory(String databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public boolean isSortedDuplicates() {
        return sortedDuplicates;
    }

    public void setSortedDuplicates(boolean sortedDuplicates) {
        this.sortedDuplicates = sortedDuplicates;
    }

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

	public int getWebServerPort() {
		return webServerPort;
	}

	public void setWebServerPort(int webServerPort) {
		this.webServerPort = webServerPort;
	}

	public int getDataBaseServerPort() {
		return dataBaseServerPort;
	}

	public void setDataBaseServerPort(int dataBaseServerPort) {
		this.dataBaseServerPort = dataBaseServerPort;
	}
}
