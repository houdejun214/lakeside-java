package com.lakeside.data.ssh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.lakeside.core.utils.PathUtils;
import com.lakeside.core.utils.StringUtils;

import java.util.HashSet;


/**
 * upload or download file using sftp
 * 
 * @author houdejun
 *
 */

public class SftpFiles {
	
	private HashSet<String> mkdirs = new HashSet<String>();
	
	public SftpFiles(String host,String userName,String password) {
		try {
			connection = new SshConnection(host,userName,password);
			connection.connect();
			sshSession = connection.getSshSession();
			channel = (ChannelSftp) sshSession.openChannel("sftp");
			channel.connect();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private SshConnection connection = null;
	private Session sshSession = null;
	private ChannelSftp channel = null;
	
    public void download(String remoteFile,String localFile){
    	synchronized (this) {
	    	try {
				channel.get(remoteFile, localFile);
			} catch (SftpException e) {
				throw new RuntimeException(e);
			}
    	}
    }
    
    public void upload(String localFile,String remoteFile){
    	synchronized (this) {
    		try {
    			insureRemotePath(remoteFile);
    			makeDirectory(remoteFile);
    			channel.put(localFile, remoteFile);
    		} catch (SftpException e) {
    			System.out.println(remoteFile);
    			throw new RuntimeException(e);
    		}
		}
    }
    
    private void insureRemotePath(String remoteFile){
    	if(StringUtils.isEmpty(remoteFile)){
    		throw new RuntimeException("remote path is empty");
    	}
    	if(!remoteFile.startsWith("/")){
    		throw new RuntimeException("remote path must be a absolute path");
    	}
    }
    
    private void makeDirectory(String path) throws SftpException{
		path = PathUtils.getParentPath(path);
		if(!mkdirs.contains(path)){
			String dir = "";
			int index = path.indexOf("/",1);
			int len = path.length();
			while(true){
				dir = path.substring(0,index);
				if(!mkdirs.contains(dir)){
					try {
						channel.stat(dir); // the directory is exists if the method success
					} catch (Exception e) {
						channel.mkdir(dir);
					}
					mkdirs.add(dir);
				}
				if(index >=len){
					break;
				}
				index = path.indexOf("/",index+1);
				if(index<0){
					index = len;
				}
			}
		}
    }
    
    public void close(){
    	if(channel!=null){
    		channel.disconnect();
    	}
    	if(sshSession!=null){
    		sshSession.disconnect();
    	}
    	if(connection!=null){
    		connection.disconnect();
    	}
    }
}