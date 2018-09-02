package frcbRep.common.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crawler.industry.guangdong.CrawlerReslut;
import com.crawler.industry.guangdong.GuangDongIndustry;
import com.jcraft.jsch.ChannelSftp;

import frcbRep.common.utils.SFTPUtil;
import frcbRep.common.utils.TimeUtil;

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
	
	private SFTPUtil sftp ;
	private ChannelSftp csftp ;

	private int times = 1;
	private static final Logger log= LoggerFactory.getLogger(SearchBankTask.class);
	@Override
	public void run() {
		
		log.info(TimeUtil.getCurrentTime() + ":		第" + times + "次查询文件");
		if(csftp==null||!csftp.isConnected()){
			csftp = sftp.connect();
		}else{
			log.info("ftp在线");
		}
		
		HashMap<String, Object> map = sftp.downFile(remotePath, localPath, csftp);
		boolean isExistOk = (map.get("result")==null||(boolean)map.get("result")==false)?false:true ;
		log.info(isExistOk ? "成功查询到银行端文件" : "暂未发现银行文件");
		if (isExistOk) {
			GuangDongIndustry guangDongIndustry = new GuangDongIndustry();
			String bankFile = localPath + (String) map.get(DoMain.BANKFILE);
			log.info("------------银行数据文件为：" + bankFile + "，即将开始抓取外网数据！");
			CrawlerReslut startCrawler = guangDongIndustry.startCrawler(bankFile);
			if(startCrawler==null){
				log.error("！！！----------爬虫程序已崩溃------！！！");
				return;
			}
			int successCount = startCrawler.getSuccessCount();
			log.info("------------爬虫程序结束爬取目标网站数据--------------------------");
			if(successCount>0){
				log.info("--------------------------------------本次成功爬取企业数"+successCount+"个");
			}else {
				log.info("--------------------------------------本次爬取成功数据为0");
			}
			//爬虫经过时间可能太久，检测连接是否已关闭
			if(csftp==null||!csftp.isConnected()){
				csftp = sftp.connect();
			}else{
				log.info("ftp还在线");
			}
			File govFile = new File(bankFile.replaceAll("bank_", "gov_"));
			if(!govFile.exists()){
				log.info("gov文件"+bankFile.replaceAll("bank_", "gov_")+"未生成？？？？？");
				return;
			}
			try {
				File okFile = new File(bankFile.replaceAll("bank_", "gov_").replaceAll(dataFile, readyFile));
				if(!okFile.exists()){
					okFile.createNewFile();
				}
				// 上传数据文件
				sftp.upload(remotePath.replaceAll("bank", "gov"),
						localPath+"/"+((String) map.get(DoMain.BANKFILE)).replaceAll("bank_", "gov_"), csftp);
				
				// 上传就绪文件
				sftp.upload(remotePath.replaceAll("bank", "gov"),
						localPath+"/"+((String) map.get(DoMain.BANKFILE)).replaceAll("bank_", "gov_").replaceAll(dataFile,
								readyFile),
						csftp);
				if(isDelGovBank==null||isDelGovBank.trim().equals("")||isDelGovBank.trim().toUpperCase().equals("TRUE")){
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
				log.error(e.toString(),e);
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
		sftp = new SFTPUtil(ip, port, username, password);
		log.info("创建了查询任务实例，远程路径是ftp根目录下的" + remotePath + "文件夹");
	}

}
