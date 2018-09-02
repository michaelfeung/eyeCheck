package com.crawler.pipeline;

import com.alibaba.fastjson.JSON;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

/**
 * Created by chenshengju on 2017/10/2 0002.
 */
public class ConsoleJsonPipeline implements Pipeline{
    @Override
    public void process(ResultItems resultItems, Task task) {
        String s = JSON.toJSONString(resultItems.getAll(),true);

        System.out.println(s);
    }
}
