package com.crawler.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.proxy.*;
import us.codecraft.webmagic.proxy.Proxy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by chenshengju on 2017/9/20 0020.
 */
public class SeleniumDownloader  extends AbstractDownloader {
    private static final Logger log=LoggerFactory.getLogger(SeleniumDownloader.class);
    private int sleepTime=3000;//3s
    private SeleniumAction action=null;
    private WebDriverPool webDriverPool=new WebPhantomJsDriverPool();
    private  HttpClientDownloader httpClientDownloader=new HttpClientDownloader();
    private ProxyProvider proxyProvider;

    public SeleniumDownloader(){
    }
    public SeleniumDownloader(WebDriverPool pool){
        this(null,pool,null);
    }
    public SeleniumDownloader(SeleniumAction action){
        this(null,null,action);
    }
    public SeleniumDownloader (Integer sleepTime) {
        this(sleepTime,null,null);
    }
    public SeleniumDownloader(Integer sleepTime, WebDriverPool pool, SeleniumAction action){
        if(sleepTime!=null)
        {
            this.sleepTime=sleepTime;
        }
        if(action!=null)
        {
            this.action=action;
        }
        if(pool!=null){
            webDriverPool=pool;
        }
    }
    public void setProxyProvider(ProxyProvider proxyProvider) {
        this.httpClientDownloader.setProxyProvider(proxyProvider);
        this.proxyProvider = proxyProvider;
    }

    @Override
    public Page download(Request request, Task task) {
       Proxy proxy = proxyProvider != null ? proxyProvider.getProxy(task) : null;
        Site site = task.getSite();
        WebDriverPool webDriverPoolNow=webDriverPool;     
        if( request.getExtra(SeleniumDownloaderService.FAST_DOWNLOAD)!=null)
        {
            return httpClientDownloader.download(request,task);
        }
        if(request.getExtra(SeleniumDownloaderService.OTHER_WEB_DRIVER)!=null)
        {
            webDriverPoolNow = (WebDriverPool) request.getExtra(SeleniumDownloaderService.OTHER_WEB_DRIVER);

        }

        WebDriver webDriver;
        try {
            webDriver = webDriverPoolNow.get();
            if(site.isDisableCookieManagement())
            {
                webDriver.manage().deleteAllCookies();
            }
            if(proxyProvider!=null)
            {
                webDriverPoolNow.setProxy(webDriver,proxy);
            }
        } catch (InterruptedException e) {
            log.warn("interrupted", e);
            return null;
        }
        log.info("downloading page " + request.getUrl());
        //内容
        String content="";
        Page page = new Page();
        WebDriver.Options manage = webDriver.manage();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
    	String date = "2020-08-30 10:00:00";
        if (site.getCookies() != null) {
            for (Map.Entry<String, String> cookieEntry : site.getCookies()
                    .entrySet()) {
                Cookie cookie;
				try {
					cookie = new Cookie(cookieEntry.getKey(),
					        cookieEntry.getValue(),".tianyancha.com","/",sdf.parse(date));
					manage.addCookie(cookie);
				} catch (ParseException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}              
            }
        }
        try {
            webDriver.get(request.getUrl());
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            webDriverPoolNow.close(webDriver);
            onError(request);
//            page.setSkip(true);
            return Page.fail();
        }
        WebElement webElement = webDriver.findElement(By.xpath("/html"));
        content=webElement.getAttribute("outerHTML");
        page.setRawText(content);
        page.setRequest(request);
//        WindowUtil.changeWindow(webDriver);

//        manage.window().maximize();
        if(action!=null){
            try {
                action.execute(webDriver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        SeleniumAction reqAction=(SeleniumAction) request.getExtra(SeleniumDownloaderService.ACTION);
        if(reqAction!=null){
            try {
               reqAction.execute(webDriver);
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                webDriverPoolNow.close(webDriver);
                log.error("浏览器发生异常"+e.getMessage());
                e.printStackTrace();
            }
        }
        Object obj = request.getExtra(SeleniumDownloaderService.ACTION_HTMLS);
        if(obj !=null)
        {
            if(obj instanceof SeleniumHtmlAction)
            {
                try {
                    ((SeleniumHtmlAction)obj).execute(webDriver,page);
                    WebElement webElement2 = webDriver.findElement(By.xpath("/html"));
                    content=webElement2.getAttribute("outerHTML");
                    page.setRawText(content);
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    webDriverPoolNow.close(webDriver);
                    onError(request);
                    log.error("浏览器发生异常"+e.getMessage());
                    e.printStackTrace();
                    return Page.fail();
                }
            }else if(obj instanceof SeleniumHtmlAction[])
            {

                SeleniumHtmlAction[] htmlActionList=(SeleniumHtmlAction[]) obj;
                try {
                    for (SeleniumHtmlAction seleniumAction : htmlActionList) {
                        seleniumAction.execute(webDriver,page);
                    }
                } catch (Exception e) {
                    webDriverPoolNow.close(webDriver);
                    onError(request);
                    log.error("浏览器发生异常"+e.getMessage());
                    e.printStackTrace();
                    return Page.fail();                }
            }else{
                throw new RuntimeException(SeleniumDownloaderService.ACTION_HTMLS+"只能为SeleniumHtmlAction的实现类或数组");
            }

        }

//        page.setHtml(new Html(UrlUtils.fixAllRelativeHrefs(content,
//                webDriver.getCurrentUrl())));
//        page.setUrl(new PlainText(webDriver.getCurrentUrl()));
        page.setRequest(request);
        webDriverPoolNow.returnToPool(webDriver);
        if (proxyProvider != null && proxy != null) {
            proxyProvider.returnProxy(proxy, page, task);
        }
        return page;
    }

    @Override
    public void setThread(int thread) {
        this.httpClientDownloader.setThread(thread);
        this.httpClientDownloader.setProxyProvider(this.proxyProvider);
    }

}