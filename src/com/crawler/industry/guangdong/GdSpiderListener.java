package com.crawler.industry.guangdong;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.SpiderListener;

import java.io.File;
import java.io.IOException;

/**
 * Created by chenshengju on 2017/10/10 0010.
 */
public class GdSpiderListener implements SpiderListener {
    private static final Logger log= LoggerFactory.getLogger(GdSpiderListener.class);
    @Override
    public void onSuccess(Request request) {

    }

    @Override
    public void onError(Request request) {
//        Object extra = request.getExtra(Request.CYCLE_TRIED_TIMES);
//        if(extra!=null&&(int)extra>5-2)
//        {
//            try {
//                FileUtils.write(new File("e:\\fosan\\failUrl.txt"),request.getUrl()+"\r\n","utf-8",true);
//            } catch (IOException e) {
//                log.info(e.getMessage());
//            }
////        }
//        log.error(request.getUrl()+"下载失败了");
    }
}
