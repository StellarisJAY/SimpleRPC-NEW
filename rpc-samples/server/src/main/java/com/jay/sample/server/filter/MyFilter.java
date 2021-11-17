package com.jay.sample.server.filter;

import com.jay.rpc.entity.RpcRequest;
import com.jay.rpc.transport.handler.Filter;
import com.jay.rpc.transport.handler.filter.exception.FilteredException;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * <p>
 *   自定义过滤器
 *   通过继承Filter并实现doFilter方法来完成自定义请求过滤规则
 *   所有的自定义Filter都交由Spring容器管理
 * </p>
 *
 * @author Jay
 * @date 2021/11/8
 **/
@Component
public class MyFilter extends Filter {

    private Logger logger = LoggerFactory.getLogger(filterName);

    public MyFilter() {
        super("my-filter");
    }

    @Override
    public boolean doFilter(ChannelHandlerContext context, RpcRequest request) throws FilteredException {
        logger.info("执行过滤器：{}", filterName);
        return true;
    }
}
