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

import java.io.File;
import java.io.FileNotFoundException;

import org.ho.yaml.Yaml;

import com.hajo.thrift.HajoException;

/**
 *
 * @author hari
 */
public class HajoConfigHolder {
    public HajoConfiguration config;

    public HajoConfigHolder(String filename) throws HajoException {
        if(config==null){
            try {
                config = (HajoConfiguration) Yaml.loadType(new File(filename), HajoConfiguration.class);
            } catch (FileNotFoundException ex) {
                throw new HajoException(ex.getMessage());
            }
        }
    }

}
