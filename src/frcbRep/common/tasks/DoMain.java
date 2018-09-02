package frcbRep.common.tasks;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frcbRep.common.utils.PropertiesFileUtil;
import frcbRep.common.utils.timer.TaskPeriod;

public class DoMain {
	private static Map<String, String> map;
	private static final String filePath = "resources/config.properties";
	public static final String BANKFILE = "file";
	private static final Logger log= LoggerFactory.getLogger(DoMain.class);
	//
	public static void main(String[] args) throws IOException {
		DoMain t = new DoMain();
		t.searchBankTask();
	}
	static{
		map = PropertiesFileUtil.readProperties(filePath);
	}
	//轮询ftp银行端就绪文件并下载至本地
	private boolean searchBankTask(){
		
		String ip = map.get("ip");
		int port = Integer.parseInt(map.get("port"));
		String username = map.get("username");
		String password = map.get("password");
		String bankPath = map.get("bankPath");
		String bankOkFile = map.get("bankOkFile");
		String bankFile = map.get("bankFile");
		String localPath = map.get("filePath");
		String period = map.get("period");
		String isDelGovBank = map.get("isDelGovBank");
		Long searchBankOkFilePeriod = null;
		if(isNumeric(period)){//如果只写了数字，则秒为单位
			searchBankOkFilePeriod = Long.parseLong(period)*1000;
		}else{
			try{
				String []periods = period.split(" ");
				if(periods.length!=2){
					log.info("配置文件错误，轮询周期格式错误！");
					return false;
				}
				Long periodL = Long.parseLong(periods[0]);//获得周期数字
				searchBankOkFilePeriod = TaskPeriod.valueOf(periods[1]).getValue()*periodL;
			}catch(IllegalArgumentException e){
				log.info("配置文件错误，轮询周期单位不存在！");
				e.printStackTrace();
				return false;
			}
			
		}
		Timer timer = new Timer();
		String ftpWay = map.get("ftp-way");
		if(ftpWay!=null&&ftpWay.trim().toUpperCase().equals("FTP")){
			//ftp方式
			timer.schedule(new frcbRep.common.utils.SearchBankTask(ip,port,username,password,bankPath,bankOkFile,localPath,bankFile,isDelGovBank), 1000,searchBankOkFilePeriod);
		}else{
			//sftp方式
			timer.schedule(new SearchBankTask(ip,port,username,password,bankPath,bankOkFile,localPath,bankFile,isDelGovBank), 1000,searchBankOkFilePeriod);
		}
		
		return true;
	}
	
	//判断是否数字
	public static boolean isNumeric(String str){
	    Pattern pattern = Pattern.compile("[0-9]*");
	    return pattern.matcher(str).matches();   
	 }
}
