package com.lakeside.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;

/**
 * A Java method to execute and wait for a shell command to complete
 * 
 * @author houdejun
 *
 */
public class ShellCommand {

	/**
	 * execute and wait for the shell command
	 * 
	 * @param command
	 * @param args
	 * @return
	 */
	public static String execute(String command,Object ... args){
		try {
			String shellCommand = MessageFormat.format(command, args);
			Runtime runtime = Runtime.getRuntime() ;
			Process shellProcess = runtime.exec(shellCommand) ;
			StringBuilder result = new StringBuilder();
			//Only wait for if you need the external app to complete
			shellProcess.waitFor() ;
			//You can read the contents of the application's information it is writing to the console
			BufferedReader shellCommandReader = new BufferedReader( new InputStreamReader(shellProcess.getInputStream() ) ) ;
	 
			String currentLine = null;
			while ( (currentLine = shellCommandReader.readLine() ) != null )
			{
				result.append(currentLine).append("\n");
			}
			int retValue = shellProcess.exitValue();
			if(retValue>0){
				return null;
			}
			return result.toString();
		} catch (IOException e) {
			return null;
		} catch (InterruptedException e) {
			return null;
		}
	}
}
