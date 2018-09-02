package frcbRep.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawler.industry.guangdong.GuangDongIndustry;

import frcbRep.common.tasks.DoMain;

public class SearchBankTask extends TimerTask {
	private String ip;
	private int port;
	private String username;
	private String password;
	private String remotePath;
	private String readyFile;
	private String localPath;
	private String dataFile;
	private String isDelGovBank;

	private int times = 1;
	private static final Logger log= LoggerFactory.getLogger(SearchBankTask.class);
	@Override
	public void run() {
		// TODO 自动生成的方法存根
		log.info(TimeUtil.getCurrentTime() + ":		第" + times + "次查询文件");
		HashMap<String, Object> map = FtpApcheUtil.downFile(ip, port, username, password, remotePath, readyFile,
				localPath, dataFile);
		boolean isExistOk = (map.get("result")==null||(boolean)map.get("result")==false)?false:true ;
		log.info(isExistOk ? "成功查询到银行端文件" : "暂未发现银行文件");
		if (isExistOk) {
			GuangDongIndustry guangDongIndustry = new GuangDongIndustry();
			String bankFile = localPath + (String) map.get(DoMain.BANKFILE);
			log.info("------------银行数据文件为：" + bankFile + "，即将开始抓取外网数据！");
			guangDongIndustry.startCrawler(bankFile);
			log.info("------------爬虫程序结束爬取目标网站数据--------------------------");
			File govFile = new File(bankFile.replaceAll("bank_", "gov_"));
			if(!govFile.exists()){
				return;
			}
			FileInputStream fis = null;
			FileInputStream fis_ok = null;
			try {
				fis = new FileInputStream(bankFile.replaceAll("bank_", "gov_"));
				File okFile = new File(bankFile.replaceAll("bank_", "gov_").replaceAll(dataFile, readyFile));
				if(!okFile.exists()){
					okFile.createNewFile();
				}
				fis_ok = new FileInputStream(okFile);
				// 上传数据文件
				FtpApcheUtil.uploadFile(ip, port, username, password, remotePath.replaceAll("bank", "gov"),
						((String) map.get(DoMain.BANKFILE)).replaceAll("bank_", "gov_"), fis);
				// 上传就绪文件
				FtpApcheUtil.uploadFile(ip, port, username, password, remotePath.replaceAll("bank", "gov"),
						((String) map.get(DoMain.BANKFILE)).replaceAll("bank_", "gov_").replaceAll(dataFile,
								readyFile),
						fis_ok);
				// 关闭文件流
				fis.close();
				fis_ok.close();
				if(isDelGovBank==null||isDelGovBank.trim().equals("")||isDelGovBank.trim().toUpperCase().equals("true")){
					//如果isDelGovBank是空的/不存在/true，则删除本次抓取的bank/gov文件
					//删除gov数据文件
					if(govFile.exists()){
						govFile.delete();
					}
					//删除gov就绪文件
					if(okFile.exists()){
						okFile.delete();
					}
					//删除bank数据文件
					File bankFile_f = new File(bankFile);
					if(bankFile_f.exists()){
						bankFile_f.delete();
					}
					//删除bank就绪文件
					File bankFile_gok = new File(bankFile.replaceAll(dataFile, readyFile));
					if(bankFile_gok.exists()){
						bankFile_gok.delete();
					}
					
				}
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		times++;
	}

	public SearchBankTask(String ip, int port, String username, String password, String remotePath, String readyFile,
			String localPath, String dataFile,String isDelGovBank) {
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
		this.remotePath = remotePath;
		this.readyFile = readyFile;
		this.localPath = localPath;
		this.dataFile = dataFile;
		this.isDelGovBank = isDelGovBank;
		log.info("创建了查询任务实例:文件是ftp根目录下的" + remotePath + readyFile + "文件");
	}

}
