package com.hajo.utils;

import com.hajo.berkeleydb.IBerkeleyDbHandler;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hari
 */
public class HajoStatsManager {
    public static IBerkeleyDbHandler handler;

    public static String getStats(){
        return handler.getStats();
    }

}
