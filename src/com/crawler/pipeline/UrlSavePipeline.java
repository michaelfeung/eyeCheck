package com.crawler.pipeline;

import com.crawler.industry.config.CrawlerConfig;
import com.crawler.industry.config.FileNameConfig;
import com.crawler.industry.guangdong.GuangDongIndustryFast;
import com.crawler.industry.guangdong.WebmagicValueHelp;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chenshengju on 2017/10/24 0024.
 * 保存详情页url
 */
public class UrlSavePipeline implements Pipeline {
    private  final Logger log= LoggerFactory.getLogger(UrlSavePipeline.class);

    public AtomicInteger getSuccessCount() {
        return successCount;
    }

    public AtomicInteger getNoSearchCount() {
        return noSearchCount;
    }

    public  AtomicInteger successCount = new AtomicInteger(0);
    public  AtomicInteger noSearchCount = new AtomicInteger(0);
    @Override
    public void process(ResultItems resultItems, Task task) {

        //是否有url
        boolean isHaveUrl=resultItems.get("isHaveUrl");
        String name=resultItems.get("name");
        if(isHaveUrl)
        {
            //存入缓存url
            String companyUrl=resultItems.get("companyUrl");
            synchronized (this) {
                if(WebmagicValueHelp.cacheUrlSet.add(name+FileNameConfig.spilt+companyUrl))//为了不将重复的缓存url放入到缓存文件中
                {
                    try {
                        FileUtils.write(new File(CrawlerConfig.filePath+File.separator+FileNameConfig.urlCompany), name + FileNameConfig.spilt+companyUrl+"\r\n", FileNameConfig.urlCompanyEncode, true);
                    } catch (IOException e) {
                        log.info("写入{}异常",FileNameConfig.urlCompany);
                    }
                }
            }
            successCount.incrementAndGet();
        }else
        {
            //存入没有公司的名单
            synchronized (this) {
                try {
                    //如果之前已经存在那么就不放进noUrl名单了
                    if(WebmagicValueHelp.noUrlNameSet.add(name))
                    {
                        FileUtils.write(new File(CrawlerConfig.filePath+File.separator+ FileNameConfig.noUrlCompany), name +"\r\n", FileNameConfig.noUrlCompanyEncode, true);
                    }
                } catch (IOException e) {
                    log.info("写入{}文件异常",FileNameConfig.noUrlCompany);
                }
            }
        }

    }
}
