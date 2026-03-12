package com.example.spring_boot_flash_sale.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {
    private final StringRedisTemplate redisTemplate;
    private static final int MAX_REQUESTS = 10;
    private static final int WINDOW_SECONDS = 60;

    @Override
    // HttpServletRequest request Chứa toàn bộ thông tin khách hàng gửi lên (như
    // Token, Username, Password, IP...).
    // HttpServletResponse response Dùng để trả về kết quả cho khách hàng (như JSON,
    // lỗi 401, 403...).
    // FilterChain chain Dùng để chuyển yêu cầu sang Filter tiếp theo hoặc
    // Controller.
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException,
            IOException {

         // kiểm URL người dùng gửi lên có phải là API đặt vé hay không, nếu không phải thì bỏ qua Filter này
        //contains() là phương thức của String để kiểm tra xem chuỗi có chứa một chuỗi con nào đó hay không
        if (!request.getRequestURI().contains("/api/bookings/events")){
            // Nếu không phải API đặt vé, bỏ qua Filter này
            chain.doFilter(request, response);
            return;
        }
        String userId = request.getHeader("User-Id");
        if (userId == null){
            chain.doFilter(request, response);
            return;
        }
        String key = "rate:user:" + userId + ":booking";
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/rate_limit.lua")));
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script,List.of(key),
        String.valueOf(MAX_REQUESTS),String.valueOf(WINDOW_SECONDS));
        if (result == null || result == 0){
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Quá nhiều yêu cầu, vui lòng thử lại sau.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

}
