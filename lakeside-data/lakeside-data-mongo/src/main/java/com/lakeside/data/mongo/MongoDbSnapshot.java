// Copyright (C) 2010 
//
// This program is free software: you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or (at your 
// option) any later version.  This program is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser 
// General Public License for more details.  You should have received a copy 
// of the GNU Lesser General Public License along with this program.  If not,
// see <http://www.gnu.org/licenses/>.

package com.lakeside.data.mongo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.BSON;

import com.lakeside.core.utils.FileUtils;
import com.lakeside.core.utils.PathUtils;
import com.lakeside.core.utils.time.DateTimeUtils;
import com.lakeside.data.compress.CompressUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.wordnik.system.mongodb.CollectionInfo;
import com.wordnik.system.mongodb.MongoUtil;
import com.wordnik.util.AbstractFileWriter;
import com.wordnik.util.BinaryRotatingFileWriter;
import com.wordnik.util.PrintFormat;
import com.wordnik.util.RotatingFileWriter;


public class MongoDbSnapshot extends MongoUtil {
	protected static int THREAD_COUNT = 3;
	protected static String COLLECTION_STRING;
	protected static boolean WRITE_JSON = false;
	protected static String outputTempDirectory = null;
	protected static String outputDirectory = null;

	protected static String databaseHost = "localhost";
	protected static String databaseName = null;
	protected static String databaseUserName = null;
	protected static String databasePassword = null;
	
	protected static boolean COMPRESS_OUTPUT_FILES = false;
	protected static int UNCOMPRESSED_FILE_SIZE_MB = 100;
	protected static long WRITES = 0;
	protected static long REPORT_INTERVAL = 10000;
	
	protected Map<String, AbstractFileWriter> writers = new HashMap<String, AbstractFileWriter>();

	public MongoDbSnapshot(String output,String host,String database,String collections){
		outputDirectory = output;
		databaseHost = host;
		databaseName = database;
		outputTempDirectory = PathUtils.getPath(output+"/"+database+"/");
		FileUtils.mkDirectory(outputTempDirectory);
		FileUtils.emptyDir(outputTempDirectory);
		COLLECTION_STRING = collections;
	}
	
	public MongoDbSnapshot(String output,String host,String database,String user,String pass,String collections){
		outputTempDirectory = output;
		databaseHost = host;
		databaseName = database;
		databaseUserName = user;
		databasePassword = pass;
		COLLECTION_STRING = collections;
	}

	public void start(){
		//	collections to snapshot
		List<CollectionInfo> collections = getCollections();

		//	spawn a thread for each job
		List<SnapshotThread> threads = new ArrayList<SnapshotThread>();
		int threadCounter = 0;
		for(CollectionInfo collection : collections){
			if(threads.size() < (threadCounter+1)){
				SnapshotThread thread = new SnapshotThread(threadCounter);
				thread.setName("backup_thread_" + collection.getName());
				threads.add(thread);
			}
			threads.get(threadCounter).add(collection);
			if(++threadCounter >= THREAD_COUNT){
				threadCounter = 0;
			}
		}
		for(SnapshotThread thread : threads){
			thread.start();
		}

		//	monitor & report
		while(true){
			try{
				Thread.sleep(5000);
				boolean isDone = true;
				StringBuilder b = new StringBuilder();
				for(SnapshotThread thread : threads){
					if(!thread.isDone){
						isDone = false;
						double mbRate = thread.getRate();
						double complete = thread.getPercentComplete();
						b.append(thread.currentCollection.getName()).append(": ").append(PrintFormat.PERCENT_FORMAT.format(complete)).append(" (").append(PrintFormat.NUMBER_FORMAT.format(mbRate)).append("mb/s)   ");
					}
				}
				System.out.println(b.toString());
				if(isDone){
					break;
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		// compress output file to archived file
		try {
			String date = DateTimeUtils.format(new Date(), "yyyyMMddhhmm");
			String tarFile = PathUtils.getPath(outputDirectory+"/"+databaseName+"."+date+".tar.gz");
			System.out.println("create arichive file:"+tarFile);
			CompressUtil.createTarGzOfDirectory(outputTempDirectory, tarFile);
			FileUtils.emptyDir(outputTempDirectory);
			System.out.println("create arichive file finished");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<CollectionInfo> getCollections() {
		List<CollectionInfo> collections = new ArrayList<CollectionInfo>();
		try{
			DB db = MongoDBConnectionManager.getConnection(databaseHost, databaseName, databaseUserName, databasePassword);
			Collection<String> collectionsInDb = db.getCollectionNames();

			List<String> collectionsToAdd = new ArrayList<String>();
			List<String> collectionsToSkip = new ArrayList<String>();
			selectCollections(COLLECTION_STRING, collectionsToAdd, collectionsToSkip);

			boolean exclusionsOnly = collectionsToAdd.contains("*");
			if(exclusionsOnly){
				for(String collectionName : collectionsInDb){
					if(!collectionsToSkip.contains(collectionName)){
						collectionsToAdd.add(collectionName);
					}
				}
			}
			else{
				if(collectionsToAdd.size() == 0){
					//	add everything
					collectionsToAdd.addAll(collectionsInDb);
				}
			}

			for(String collectionName : collectionsToAdd){
				if(!"system.indexes".equals(collectionName)){
					long count = db.getCollection(collectionName).count();
					if(count > 0){
						CollectionInfo info = new CollectionInfo(collectionName, count);
						info.setCollectionExists(true);
						collections.add(info);

						String indexName = new StringBuilder().append(databaseName).append(".").append(collectionName).toString(); 
						DBCursor cur = db.getCollection("system.indexes").find(new BasicDBObject("ns", indexName));
						if(cur != null){
							while(cur.hasNext()){
								info.addIndex((BasicDBObject)cur.next());
							}
						}
						cur.close();
					}
				}
			}
			
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
		return collections;
	}

	class SnapshotThread extends Thread {
		long startTime = 0;
		long lastOutput = 0;
		int threadId;
		long writes = 0;
		boolean isDone = false;
		CollectionInfo currentCollection = null;

		public SnapshotThread(int threadId){
			this.threadId = threadId;
		}

		List<CollectionInfo> collections = new ArrayList<CollectionInfo>();
		public void run() {
			for(CollectionInfo info : collections){
				isDone = false;
				writes = 0;
				currentCollection = info;
				try{
					removeSummaryFile(currentCollection.getName());
					writeConnectivityDetailString(currentCollection.getName());
					if(currentCollection.getIndexes().size() > 0){
						writeToSummaryFile(currentCollection.getName(), "indexes");
					}
					for(BasicDBObject index : currentCollection.getIndexes()){
						writeIndexInfoToSummaryFile(currentCollection.getName(), index);
					}
					lastOutput = System.currentTimeMillis();
					startTime = System.currentTimeMillis();
					DB db = MongoDBConnectionManager.getConnection(databaseHost, databaseName, databaseUserName, databasePassword);

					DBCollection collection = db.getCollection(currentCollection.getName());
		            DBCursor cursor = null;
		            cursor = collection.find();
		            cursor.sort(new BasicDBObject("_id", 1));
		            cursor.batchSize(1000);
		            while (cursor.hasNext() ){
		                BasicDBObject x = (BasicDBObject) cursor.next();
		        		++writes;
		            	processRecord(currentCollection.getName(), x);
		            }
		            close(currentCollection.getName());
				}
				catch(Exception e){
					e.printStackTrace();
				}
				finally{
					isDone = true;
				}
			}
		}

		public void processRecord(String collectionName, BasicDBObject dbo) throws Exception {
			if(WRITE_JSON){
				RotatingFileWriter writer = (RotatingFileWriter)writers.get(collectionName);
				if(writer == null){
					writer = new RotatingFileWriter(collectionName, outputTempDirectory, "json", UNCOMPRESSED_FILE_SIZE_MB * 1048576L, COMPRESS_OUTPUT_FILES);
					writers.put(collectionName, writer);
				}
				writer.write(dbo.toString());
			}
			else{
				BinaryRotatingFileWriter writer = (BinaryRotatingFileWriter)writers.get(collectionName);
				if(writer == null){
					writer = new BinaryRotatingFileWriter(collectionName, outputTempDirectory, "bson", UNCOMPRESSED_FILE_SIZE_MB * 1048576L, COMPRESS_OUTPUT_FILES);
					writers.put(collectionName, writer);
				}
				writer.write(BSON.encode(dbo));
			}
		}

		public void close(String collectionName) throws IOException {
			AbstractFileWriter writer;
			if(WRITE_JSON){
				writer = (RotatingFileWriter)writers.get(collectionName);
			}
			else{
				writer = writers.get(collectionName);
			}
			if(writer != null){
				writer.close();
			}
		}
		
		public double getPercentComplete(){
			return (double) writes / (double)currentCollection.getCount();
		}

		public double getRate(){
        	AbstractFileWriter writer = writers.get(currentCollection.getName());
			if(writer != null){
				long bytesWritten = writer.getTotalBytesWritten();
				double brate = (double)bytesWritten / ((System.currentTimeMillis() - startTime) / 1000.0) / ((double)1048576L);
				lastOutput = System.currentTimeMillis();
				return brate;
			}
        	return 0;
		}
		
		public void add(CollectionInfo info){
			this.collections.add(info);
		}
		
		protected void removeSummaryFile(String name) {
			if(outputTempDirectory != null){
				name = outputTempDirectory + File.separator + name;
			}
			File file = new File(name + ".txt");
			if(file.exists() && !file.delete()){
				throw new RuntimeException("unable to remove summary file");
			}
		}

		protected void writeConnectivityDetailString(String collectionName) throws IOException {
			writeToSummaryFile(collectionName, "##########################################");
			writeToSummaryFile(collectionName, "##\texport created on " + new java.util.Date());
			writeToSummaryFile(collectionName, "##\thost: " + databaseHost);
			writeToSummaryFile(collectionName, "##\tdatabase: " + databaseName);
			writeToSummaryFile(collectionName, "##\tcollection: " + collectionName);
			writeToSummaryFile(collectionName, "##########################################");
		}

		protected void writeObjectToSummaryFile(String collectionName, BasicDBObject comment) throws IOException {
			writeToSummaryFile(collectionName, comment.toString());
		}

		protected void writeToSummaryFile(String collectionName, String comment) throws IOException {
			if(outputTempDirectory != null){
				collectionName = outputTempDirectory + File.separator + collectionName;
			}
			String filename = collectionName + ".txt";
			Writer writer = new OutputStreamWriter(new FileOutputStream(new File(filename), true));
			writer.write(comment.toString());
			writer.write("\n");
			writer.close();
		}

		protected void writeIndexInfoToSummaryFile(String collectionName, BasicDBObject index) throws IOException {
			BasicDBObject i = (BasicDBObject)index.get("key");

			//	don't write the _id index
			if(!i.containsField("_id")){
				writeToSummaryFile(collectionName, i.toString());
			}
		}
	}

	public static boolean parseArgs(String...args){
		for (int i = 0; i < args.length; i++) {
			switch (args[i].charAt(1)) {
			case 't':
				THREAD_COUNT = Integer.parseInt(args[++i]);
				break;
			case 'c':
				COLLECTION_STRING = args[++i];
				break;
			case 'o':
				outputTempDirectory = args[++i];
				validateDirectory(outputTempDirectory);
				break;
			case 's':
				UNCOMPRESSED_FILE_SIZE_MB = Integer.parseInt(args[++i]);
				break;
			case 'Z':
				COMPRESS_OUTPUT_FILES = true;
				break;
			case 'J':
				WRITE_JSON = true;
				break;
			case 'd':
				databaseName = args[++i];
				break;
			case 'u':
				databaseUserName = args[++i];
				break;
			case 'p':
				databasePassword = args[++i];
				break;
			case 'h':
				databaseHost = args[++i];
				break;
			default:
				return false;
			}
		}
		return true;
	}

	public static void usage(){
		System.out.println("usage: SnapshotUtil");
		System.out.println(" -d : database name");
		System.out.println(" -o : output directory");
		System.out.println(" [-c : CSV collection string (prefix with ! to exclude)]");
		System.out.println(" [-h : database host[:port]]");
		System.out.println(" [-t : threads to run (default 3)]");
		System.out.println(" [-u : database username]");
		System.out.println(" [-p : database password]");
		System.out.println(" [-s : max file size in MB]");
		System.out.println(" [-J : output in JSON (default is BSON)]");
		System.out.println(" [-Z : compress files]");
	}
}