package f1ybird.ladder.shiro;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
  * @Description: 用户登录验证
  *
  * @Author: kevin
  *
  * @Date 22:53 2018/4/9
  */
public class RememberAuthenticationFilter extends FormAuthenticationFilter{

    /**
     * 判断是否让用户登陆
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        Subject subject = getSubject(request, response);
        return subject.isAuthenticated() || subject.isRemembered();
    }
}
