package com.crawler.selenium;

import org.openqa.selenium.WebDriver;
import us.codecraft.webmagic.proxy.Proxy;

/**
 * Created by chenshengju on 2017/9/29 0029.
 */
public interface WebDriverPool {
    WebDriver get() throws InterruptedException;

    void returnToPool(WebDriver webDriver);

    void close(WebDriver webDriver);

    void shutdown();

    void setProxy(WebDriver webDriver,Proxy proxy);
}
