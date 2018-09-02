package com.crawler.selenium;

/**
 * Created by chenshengju on 2017/9/20 0020.
 */
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.proxy.Proxy;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author taojw
 */
public class WebChromeDriverPool implements WebDriverPool{
    private Logger logger = LoggerFactory.getLogger(getClass());

    private int CAPACITY = 5;
    private AtomicInteger refCount = new AtomicInteger(0);
    /**
     * store webDrivers available
     */
    private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>(
            CAPACITY);

    private static String CHROME_PATH;
    private static DesiredCapabilities caps = DesiredCapabilities.chrome();
    static {

        CHROME_PATH = WebChromeDriverPool.class.getResource("/chromedriver.exe").getFile();
        System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY,CHROME_PATH);
        caps.setJavascriptEnabled(true);
        ChromeOptions chromeOptions = new ChromeOptions() ;
//        Data5uProxyHelper data5uProxyHelper = new Data5uProxyHelper();
//        ProxyExpire proxyExpire = data5uProxyHelper.getProxy(1).get(0);
//        chromeOptions.addArguments("--proxy-server=http://"+proxyExpire.getHost()+":"+proxyExpire.getPort());
        caps.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
    }

    public WebChromeDriverPool() {
    }

    public WebChromeDriverPool(int poolsize) {
        this.CAPACITY = poolsize;
        innerQueue = new LinkedBlockingDeque<WebDriver>(poolsize);
    }

    public WebDriver get() throws InterruptedException {
        WebDriver poll = innerQueue.poll();
        if (poll != null) {
//            poll.manage().deleteAllCookies();
            return poll;
        }
        if (refCount.get() < CAPACITY) {
            synchronized (innerQueue) {
                if (refCount.get() < CAPACITY) {

                    WebDriver mDriver = new ChromeDriver(caps);
//                    poll.manage().deleteAllCookies();
                    // 尝试性解决：https://github.com/ariya/phantomjs/issues/11526问题
                    mDriver.manage().timeouts().implicitlyWait(5,TimeUnit.SECONDS);
                    mDriver.manage().timeouts().pageLoadTimeout(60,TimeUnit.SECONDS);
                    mDriver.manage().timeouts().setScriptTimeout(10,TimeUnit.SECONDS);
                    // mDriver.manage().window().setSize(new Dimension(1366,
                    // 768));
                    innerQueue.add(mDriver);
                    refCount.incrementAndGet();
                }
            }
        }
        return innerQueue.take();
    }

    public void returnToPool(WebDriver webDriver) {
        // webDriver.quit();
        // webDriver=null;
        innerQueue.add(webDriver);
    }

    public void close(WebDriver webDriver) {
        refCount.decrementAndGet();
        webDriver.close();
        webDriver.quit();
        webDriver = null;
    }

    public void shutdown() {
        try {
            for (WebDriver driver : innerQueue) {
                close(driver);
            }
            innerQueue.clear();
        } catch (Exception e) {
//            e.printStackTrace();
            logger.warn("webdriverpool关闭失败",e);
        }
    }

    @Override
    public void setProxy(WebDriver webDriver,Proxy proxy) {

    }


}