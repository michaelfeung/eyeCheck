package com.crawler.pipeline;

import com.crawler.industry.config.CrawlerConfig;
import com.crawler.industry.config.FileNameConfig;
import com.crawler.industry.guangdong.AbnormalHtmlGdAction;
import com.crawler.industry.guangdong.BlackGdHtmlAction;
import com.crawler.industry.guangdong.WebmagicValueHelp;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chenshengju on 2017/10/13 0013.
 */
public class FastFosanPipeline implements Pipeline {
    private static final Logger log= LoggerFactory.getLogger(FastFosanPipeline.class);
    public AtomicInteger success=new AtomicInteger(0);

    public AtomicInteger getSuccess() {
        return success;
    }
    private String finalFile=CrawlerConfig.filePath;
    private String fileName;
    //已经下载好的公司名单
    public static Set<String> cacheUrlSet;

    public HttpClientDownloader downloader = new HttpClientDownloader();
    {
        //代理
        if(CrawlerConfig.ipProxyList!=null)
        {
            SimpleProxyProvider simpleProxyProvider = new SimpleProxyProvider(CrawlerConfig.ipProxyList);
            downloader.setProxyProvider(simpleProxyProvider);
        }
        //代理
        downloader.setThread(CrawlerConfig.theadCount+5);
        //给存入的政府文件赋值
        fileName= WebmagicValueHelp.fileName;
        cacheUrlSet=WebmagicValueHelp.cacheUrlSet;

    }
    @Override
    public void process(ResultItems resultItems, Task task) {
        String url=resultItems.getRequest().getUrl();
        //本项目所规定的分割符
        String spilt= CrawlerConfig.spilt;
        //其他文件所定的分割符
        String mySpilt=FileNameConfig.spilt;
        Map<String,String> baseInfo = null;
        String companyName = (String) resultItems.getRequest().getExtra("name");
        baseInfo = resultItems.get("baseInfo");
        String line=baseInfo.get("企业名称").replaceAll(" ","")+spilt+ baseInfo.get("法律诉讼")+spilt+ baseInfo.get("失信人信息")+spilt+ baseInfo.get("被执行人")+"\r\n";
        synchronized (this) {
        	try {
                FileUtils.write(new File(finalFile+ File.separator+fileName),line,CrawlerConfig.encode,true);
            } catch (IOException e) {
                log.info("缓存url写入异常");
            }			
		}
        log.info("成功爬取{}第{}条数据",baseInfo.get("企业名称"),success.incrementAndGet());        
    }

    /**
     * 保存缓存类的行和新的缓存行
     * @param line 写入政府文件的行
     * @param lineHelp 缓存文件的行
     * @param helpNewLine 新的缓存文件的行
     */
    public synchronized  void saveUrl(String line,String lineHelp)
    {
        try {
            FileUtils.write(new File(finalFile+ File.separator+fileName),line,CrawlerConfig.encode,true);
            //如果没
            if(cacheUrlSet.add(lineHelp))
            {
                FileUtils.write(new File(finalFile+ File.separator+ FileNameConfig.urlCompany),lineHelp+"\r\n",FileNameConfig.urlCompanyEncode,true);
            }
        } catch (IOException e) {
            log.info("缓存url写入异常");
        }
    }
}
