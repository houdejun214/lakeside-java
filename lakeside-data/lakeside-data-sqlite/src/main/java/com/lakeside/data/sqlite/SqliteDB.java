package com.lakeside.data.sqlite;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.lakeside.core.utils.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SqliteDB implements Closeable {

	protected final int SUCESS = 1;
	protected final int FAILD = 0;

	protected SQLiteConnection db;

	public SqliteDB() throws SQLiteException {
		db = null;
	}
	
	public SqliteDB(String dbpath) throws SQLiteException {
		FileUtils.insureFileDirectory(dbpath);
		db = new SQLiteConnection(new File(dbpath));
		try {
			db.open(true);
		} catch (SQLiteException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public SQLiteStatement query(String sql, List<Object> parameters) throws SQLiteException {
		SQLiteStatement st = null;
		st = db.prepare(sql);
		bindParameters(st, parameters);
		return st;
	}
	
	public String queryOneString(String sql, List<Object> parameters){
		SQLiteStatement st = null;
		try {
			st = db.prepare(sql);
			bindParameters(st, parameters);
			if (st.step()) {
				return st.columnString(0);
			}
			return null;
		} catch (SQLiteException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (st != null) {
				st.dispose();
			}
		}
	}

	public boolean exists(String sql, List<Object> parameters) {
		SQLiteStatement st = null;
		try {
			st = db.prepare(sql);
			bindParameters(st, parameters);
			if (st.step()) {
				return true;
			}
			return false;
		} catch (SQLiteException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (st != null) {
				st.dispose();
			}
		}
	}
	
	public int execute(String sql, List<Object> parameters) {
		SQLiteStatement st = null;
		try {
			st = db.prepare(sql);
			bindParameters(st, parameters);
			st.stepThrough();
			return SUCESS;
		} catch (SQLiteException e) {
			e.printStackTrace();
			return FAILD;
		} finally {
			if (st != null) {
				st.dispose();
			}
		}
	}

	private void bindParameters(SQLiteStatement st, List<Object> parameters) throws SQLiteException {
		if (parameters != null) {
			int size = parameters.size();
			for (int i = 1; i <= size; i++) {
				int key = i;
				Object val = parameters.get(i);
				Class<? extends Object> valType = val.getClass();
				if (Integer.class.equals(valType)) {
					st.bind(key, (Integer) val);
				} else if (Double.class.equals(valType)) {
					st.bind(key, (Double) val);
				} else if (String.class.equals(valType)) {
					st.bind(key, (String) val);
				} else if (Long.class.equals(valType)) {
					st.bind(key, (Long) val);
				}
			}
		}
	}

	public void close() throws IOException {
		if (db != null) {
			db.dispose();
		}
	}

}
