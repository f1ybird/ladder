package f1ybird.ladder.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

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

    @RequestMapping("/do_login")
    public String doLogin(HttpServletRequest request, Model model){
        return "index";
    }

}
