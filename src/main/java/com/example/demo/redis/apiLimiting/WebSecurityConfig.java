package com.example.demo.redis.apiLimiting;

import com.example.demo.redis.template.RedisUtil;
import com.example.demo.utils.WebServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Jashin
 */
@Slf4j
public class WebSecurityConfig implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //如果请求输入方法
        if (handler instanceof HandlerMethod){
            HandlerMethod hm = (HandlerMethod) handler;
            //获取方法中的注解,看是否有该注解
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null){
                return true;
            }
            long seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            //关于key的生成规则可以自己定义 本项目需求是对每个方法都加上限流功能，如果你只是针对ip地址限流，那么key只需要只用ip就好
            String key = WebServletUtil.getClientIpAddr(request)+hm.getMethod().getName();
            try {
                long q = RedisUtil.incr(key,seconds);
                if (q > maxCount){
                    render(response,"请求过于频繁，请稍候再试");
                    return false;
                }
            } catch (RedisConnectionFailureException e) {
                log.error("redis错误"+ e.getMessage());
                return true;
            }
        }
        return true;
    }

    private void render(HttpServletResponse response, String cm) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        out.write(cm.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }
}
