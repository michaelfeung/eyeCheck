package frcbRep.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.NoRouteToHostException;
import java.util.HashMap;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frcbRep.common.tasks.DoMain;

public class FtpApcheUtil {
	private static FTPClient ftpClient = new FTPClient();
	private static HashMap<String,Object> map = new HashMap<String,Object>();
	private static String encoding = System.getProperty("file.encoding");
	private static final Logger log= LoggerFactory.getLogger(FtpApcheUtil.class);

	/**
	 * Description: 上传文件至FTP服务器
	 * 
	 * @Version1.0
	 * 
	 * @param ip
	 *            FTP服务器ip
	 * @param port
	 *            FTP服务器端口
	 * @param username
	 *            FTP用户名
	 * @param password
	 *            FTP密码
	 * @param path
	 *            FTP服务器路径
	 * @param filename
	 *            上传至FTP服务器后的文件名
	 * @param input
	 *            本地文件输入流
	 * @return
	 * 			是否成功
	 */
	public static boolean uploadFile(String ip, int port, String username, String password, String path,
			String filename, InputStream input) {
		boolean result = false;

		try {
			//获取ftp服务器状态
			if(!ftpClient.isConnected()){
				if(!loginFtp(ip,port,username,password)){//未登陆ftp则登陆
					return result;
				}
			}
			log.info("ftp在线");
			//转变ftp目录
			boolean change = ftpClient.changeWorkingDirectory(path);
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			if (change) {
				result = ftpClient.storeFile(new String(filename.getBytes(encoding), "iso-8859-1"), input);
				if (result) {
					log.info("上传文件"+filename+"成功！");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


	/**
	 * Description: 查询FTP服务器文件并下载
	 * 
	 * @Version1.0
	 * 
	 * @param url
	 *            FTP服务器hostname
	 * @param port
	 *            FTP服务器端口
	 * @param username
	 *            FTP用户名
	 * @param password
	 *            FTP密码
	 * @param remotePath
	 *            FTP服务器路径
	 * @param fileName
	 *            就绪文件格式
	 * @param localPath
	 *            本地目录
	 * @param dataFile
	 * 			  数据文件格式
	 * @return
	 */
	public static HashMap<String, Object> downFile(String ip,int port,String username,String password,String remotePath,String fileName,String localPath,String dataFile) {
//		String fileName = "gov_20171010.ok";
//		String remotePath = "C:\\TEMP\\"+fileName;
		boolean result = false;
		try {
			//获取ftp服务器状态
			if(!ftpClient.isConnected()){
				if(!loginFtp(ip,port,username,password)){//未登陆ftp则登陆
					map.put("result",result);
					return map;
				}
			}
			log.info("ftp在线");	
			// 转换FTP服务器目录
			ftpClient.changeWorkingDirectory(new String(remotePath.getBytes(encoding), "iso-8859-1"));
			// 获取目录下所有文件名
			FTPFile[] fs = ftpClient.listFiles();
			for (FTPFile ff : fs) {
				String ftpFile = ff.getName();
			    // 找到就绪文件
			    boolean rs = ftpFile.endsWith(fileName);
				if (rs) {
					//获取银行数据文件的名字
					String dataFileInFtp = ftpFile.replaceAll(fileName.substring(fileName.lastIndexOf(".")), dataFile);
					log.info("----------------"+dataFileInFtp);
					//下载数据文件
					File local_Path = new File(localPath);
					if(!local_Path.exists()){
						local_Path.mkdir();
					}
					if(!local_Path.isDirectory()){
						local_Path.mkdir();
					}
					File localFile = new File(localPath + "/" + dataFileInFtp);
					OutputStream is = new FileOutputStream(localFile);
					boolean down_bank = ftpClient.retrieveFile(dataFileInFtp, is);
					if(down_bank){
						log.info("下载银行文件"+dataFileInFtp+"成功！");
					}else{
						log.info("下载银行文件"+dataFileInFtp+"失败！");
					}
					//下载就绪文件
					File localFileOk = new File(localPath + "/" + ftpFile);
					OutputStream is_ok = new FileOutputStream(localFileOk);
					boolean down_bank_ok = ftpClient.retrieveFile(ftpFile, is_ok);
					if(down_bank_ok){
						log.info("下载就绪文件"+ftpFile+"成功！");
					}else{
						log.info("下载就绪文件"+ftpFile+"失败！");
					}
					//数据文件转入历史文件夹
//					boolean rt1 = ftpClient.rename(dataFileInFtp, "/bak/"+dataFileInFtp);
//					//就绪文件转入历史文件夹
//					boolean rt2 = ftpClient.rename(ftpFile, "/bak/"+ftpFile);
					boolean rt = ftpClient.rename(ftpFile, "bak");
					boolean delete_bank_ok = ftpClient.deleteFile(ftpFile);
					boolean delete_bank = ftpClient.deleteFile(dataFileInFtp);
					log.info("----------------"+ftpFile+"重命名结果rt："+rt);
					log.info("----------------就绪文件"+ftpFile+"删除结果："+delete_bank_ok);
					log.info("----------------数据文件"+dataFileInFtp+"删除结果："+delete_bank);
					
//					log.info("----------------重命名结果rt1："+rt1);
//					log.info("----------------重命名结果rt2："+rt2);
					
					//上传数据文件至历史目录
					FileInputStream in_bank = new FileInputStream(localFile);
					uploadFile(ip, port, username, password, remotePath+"bak\\", dataFileInFtp, in_bank);
//					ftpClient.storeFile(new String(dataFileInFtp.getBytes(encoding), "iso-8859-1"), in_bank);
					//上传就绪文件至历史目录
					FileInputStream in_bank_ok = new FileInputStream(localFileOk);
					uploadFile(ip, port, username, password, remotePath+"bak\\", ftpFile, in_bank_ok);
//					ftpClient.storeFile(new String(ftpFile.getBytes(encoding), "iso-8859-1"), in_bank_ok);
					is.close();
					is_ok.close();
					in_bank.close();
					in_bank_ok.close();
					if(down_bank&&down_bank_ok){//就绪文件和数据文件都完成下载
						result = true;
						map.put(DoMain.BANKFILE,dataFileInFtp);
					}
				}
			}
			//ftpClient.logout();
			
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(),e);
		}
		map.put("result",result);
		return map;
	}
	/**
	 * Description: 登陆ftp服务器
	 * 
	 * @Version1.0
	 * 
	 * @param url
	 *            FTP服务器hostname
	 * @param port
	 *            FTP服务器端口
	 * @param username
	 *            FTP用户名
	 * @param password
	 *            FTP密码
	 * @return
	 * 			    是否成功
	 */
	public static boolean loginFtp(String ip,int port,String username,String password) {
		log.info("--------------------登陆ftp---------");
		boolean result = false;
		try {
			int reply;
			ftpClient.setControlEncoding(encoding);
			ftpClient.connect(ip, port);
			//开始登陆FTP服务器
			result = ftpClient.login(username, password);
			if(!result){
				log.info("ftp用户名或密码错误！");
				return result;
			}
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			//获取服务器返回码
			reply = ftpClient.getReplyCode();
			//检查返回码是否正确
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				System.err.println("服务器拒绝链接！");
				return result;
			}
			result = true;
		} catch(NoRouteToHostException e1){
			log.error(e1.getMessage());
			log.info("无法连接远程服务器，请确认已经打开ftp服务器");
		}catch (IOException e) {
			e.printStackTrace();
			log.error(e.getMessage(),e);
		}
		return result;
	}
}
