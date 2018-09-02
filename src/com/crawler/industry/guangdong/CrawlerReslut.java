package com.crawler.industry.guangdong;

/**
 * Created by chenshengju on 2017/11/2 0002.
 */
public class CrawlerReslut {
    //总数
    int allCount;
    //本次查询成功的数量
    int successCount;
    //本次查询失败的数量
    int failCount;
    //搜索不出来的数量
    int noSearch;
    boolean isCanCrawler=true;

    public CrawlerReslut() {
    }

    public CrawlerReslut(int allCount, int successCount, int failCount) {
        this.allCount = allCount;
        this.successCount = successCount;
        this.failCount = failCount;
    }

    public boolean isCanCrawler() {
        return isCanCrawler;
    }

    public void setCanCrawler(boolean canCrawler) {
        isCanCrawler = canCrawler;
    }

    public int getAllCount() {
        return allCount;
    }

    public void setAllCount(int allCount) {
        this.allCount = allCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }


    public int getNoSearch() {
        return noSearch;
    }

    public void setNoSearch(int noSearch) {
        this.noSearch = noSearch;
    }
}
