package f1ybird.ladder.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
  * @Description: 后台首页
  *
  * @Author: kevin
  *
  * @Date 21:27 2018/4/10
  */
@Controller
public class IndexController {

    @RequestMapping("/index")
    public String index(){
        return "index";
    }
}
