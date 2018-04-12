package f1ybird.ladder.controller;

import f1ybird.ladder.common.util.Md5Util;
import f1ybird.ladder.org.entity.Resource;
import f1ybird.ladder.util.WebHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
  * @Description: 登录请求控制类
  *
  * @Author: kevin
  *
  * @Date 22:55 2018/4/9
  */
@Controller
public class LoginController {

    /**
     * 访问登录
     * @return
     */
    @RequestMapping("/login")
    public String login(){
        return "login";
    }

    /**
     * 验证用户登录信息
     * @param request
     * @param model
     * @return 用户列表页
     */
    @RequestMapping("/do_login")
    public String doLogin(HttpServletRequest request, Model model){
        String username = request.getParameter("username");
        String pwd = request.getParameter("pwd");
        String md5Pwd = Md5Util.generatePassword(pwd);
        boolean rememerMe = false;
        try{
            UsernamePasswordToken token = new UsernamePasswordToken(username, md5Pwd, rememerMe);
            Subject subject = SecurityUtils.getSubject();
            subject.login(token);
            List<Resource>  resourceList = (List<Resource>)request.getSession().getAttribute(WebHelper.SESSION_MENU_RESOURCE);
            if(null != resourceList && !resourceList.isEmpty()){
                for (Resource r : resourceList){
                    List<Resource> rChildren = r.getChildren();
                    if(StringUtils.isNotBlank(r.getUrl()) && (rChildren == null || rChildren.isEmpty())){
                        return "redirect:" + r.getUrl();
                    }
                    if(null != rChildren && !rChildren.isEmpty()){
                        for (Resource resource : rChildren){
                            if(StringUtils.isNotBlank(resource.getUrl())){
                                return "redirect:" + resource.getUrl();
                            }
                        }
                    }
                }
            }
            return "redirect:/user/list";
        } catch (LockedAccountException lae) {
            model.addAttribute("msg", "账号已被禁用");
        } catch (AuthenticationException ae) {
            model.addAttribute("msg", "账号或密码错误");
        } catch (Exception e) {
            model.addAttribute("msg", "登录异常");
        }
        return "login";
    }

    /**
     * 登出
     * @param request
     * @return
     */
    @RequestMapping("/login_out")
    public String logOut(){
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "redirect:/login";
    }

}
