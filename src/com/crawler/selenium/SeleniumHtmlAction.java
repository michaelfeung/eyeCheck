package com.crawler.selenium;

import org.openqa.selenium.WebDriver;
import us.codecraft.webmagic.Page;

/**
 * Created by chenshengju on 2017/9/25 0025.
 */
public interface SeleniumHtmlAction {
    void execute(WebDriver driver,Page page) throws Exception;
}
