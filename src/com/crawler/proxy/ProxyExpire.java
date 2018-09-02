package com.crawler.proxy;

import us.codecraft.webmagic.proxy.Proxy;

/**
 * Created by chenshengju on 2017/10/6 0006.
 */
public class ProxyExpire extends Proxy{
    private long expireTime;
    public Boolean isExpire()
    {
        return  System.currentTimeMillis()-expireTime>0?true:false;
    }
    public ProxyExpire(String host, int port, String username, String password,long expireTime) {
        super(host, port, username, password);
        this.expireTime=expireTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
}
