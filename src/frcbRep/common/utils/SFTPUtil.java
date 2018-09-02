package frcbRep.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import frcbRep.common.tasks.DoMain;

/**
 * @author zzbcome
 */
public class SFTPUtil {

	private String host;// 服务器连接ip
	private String username;// 用户名
	private String password;// 密码
	private int port ;// 端口号

	private static HashMap<String,Object> map = new HashMap<String,Object>();
	private static final Logger log = LoggerFactory.getLogger(SFTPUtil.class);

	public SFTPUtil(String host, int port, String username, String password) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.port = port;
	}

	/**
	 * 连接sftp服务器
	 * 
	 * @param host
	 *            主机
	 * @param port
	 *            端口
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @return
	 */
	public ChannelSftp connect() {
		ChannelSftp sftp = null;
		try {
			JSch jsch = new JSch();
			log.info("准备连接ftp服务器...");
			jsch.getSession(username, host, port);
			Session sshSession = jsch.getSession(username, host, port);
			log.info("Session created.");
			sshSession.setPassword(password);
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			sshSession.setConfig(sshConfig);
			sshSession.connect();
			log.info("Session connected.");
			log.info("Opening Channel.");
			Channel channel = sshSession.openChannel("sftp");
			channel.connect();
			sftp = (ChannelSftp) channel;
			log.info("Connected to " + host + ".");
		} catch (Exception e) {
			e.printStackTrace();
			//log.error(e.toString(), e);
		}
		return sftp;
	}

	/**
	 * 上传文件
	 * 
	 * @param directory
	 *            上传的目录
	 * @param uploadFile
	 *            要上传的文件
	 * @param sftp
	 */
	public void upload(String directory, String uploadFile, ChannelSftp sftp) {
		try {
			sftp.cd(sftp.getHome()+"/"+directory);
			File file = new File(uploadFile);
			sftp.put(new FileInputStream(file), file.getName());
			log.info("上传文件"+uploadFile+"成功！");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 下载文件
	 * 
	 * @param directory
	 *            下载目录
	 * @param downloadFile
	 *            下载的文件
	 * @param localPath
	 *            存在本地的路径
	 * @param sftp
	 */
	public HashMap<String, Object> downFile(String directory, String localPath, ChannelSftp sftp) {
		boolean result = false;
		try {
			String home = sftp.getHome();
			//System.out.println("ftp服务器根目录："+home);
			sftp.cd(home);
			@SuppressWarnings("unchecked")
			Vector<ChannelSftp.LsEntry> files = sftp.ls(directory);//列出指定目录下的文件，包括文件夹
			for(ChannelSftp.LsEntry entry : files){
				String filename = entry.getFilename();
				if(filename.endsWith(".ok")){
					String okFile = filename;
					//获取银行数据文件的名字
					String dataFileInFtp = okFile.replaceAll(".ok", ".txt");
					log.info("找到就绪文件："+okFile);
					//判断是否存在对应的数据文件
					boolean exist = false;
					for(ChannelSftp.LsEntry e : files){
						String datafile = e.getFilename();
						if(datafile.equals(dataFileInFtp)){
							exist = true;
						}
					}
					if(!exist){
						log.info("不存在对应的数据文件："+dataFileInFtp);
						continue;
					}
					log.info("找到对应的数据文件："+dataFileInFtp);
					//下载数据文件
					File local_Path = new File(localPath);
					if(!local_Path.exists()){
						local_Path.mkdir();
					}
					if(!local_Path.isDirectory()){
						local_Path.mkdir();
					}
					File localFile = new File(localPath + "/" + dataFileInFtp);
					sftp.get(home+"/"+directory+"/"+dataFileInFtp, new FileOutputStream(localFile));
					log.info("下载银行文件"+dataFileInFtp+"成功！");
					//下载就绪文件
					File localFileOk = new File(localPath + "/" + okFile);
					sftp.get(home+"/"+directory+"/"+okFile, new FileOutputStream(localFileOk));
					log.info("下载就绪文件"+okFile+"成功！");
					
					sftp.rename(home+"/"+directory+"/"+okFile, directory+"/bak/"+okFile);
					log.info("就绪文件"+okFile+"移入历史文件夹成功！");
					
					sftp.rename(home+"/"+directory+"/"+dataFileInFtp, directory+"/bak/"+dataFileInFtp);
					log.info("银行文件"+dataFileInFtp+"移入历史文件夹成功！");
					//sftp.rm(dataFileInFtp);
					//sftp.rm(okFile);
					
					result = true;
					map.put(DoMain.BANKFILE,dataFileInFtp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(),e);
		}
		map.put("result",result);
		return map;
	}
	  /** 
     * Description：	断开FTP连接 
	 * @throws JSchException 
     */ 
    public void disconnect(ChannelSftp sftp) throws JSchException 
    { 
        if (null != sftp) 
        { 
            sftp.disconnect(); 

            if (null != sftp.getSession()) 
            { 
                sftp.getSession().disconnect(); 
            } 
        } 
    } 
	public static void main(String[] args) throws Exception {
		SFTPUtil sf = new SFTPUtil("192.168.169.18", 22, "xiaojing", "xiaojing");
		ChannelSftp chanel = sf.connect();
		//sf.downFile("bank", "/bank/", chanel);
		sf.upload("gov", "resources/bank_20171012.txt", chanel);
	}
}
