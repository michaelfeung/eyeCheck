package com.crawler.proxy;

import us.codecraft.webmagic.Page;

/**
 * Created by chenshengju on 2017/10/6 0006.
 */
public interface OfflineStrategy {
    boolean needOfflineProxy(Page page);
    class NotOfflineStrategy implements OfflineStrategy {

        @Override
        public boolean needOfflineProxy(Page page) {
            return false;
        }
    }
}
