package f1ybird.ladder.controller;

import f1ybird.ladder.common.dto.AjaxResult;
import f1ybird.ladder.common.entity.PageModel;
import f1ybird.ladder.common.util.Md5Util;
import f1ybird.ladder.org.dto.UserQueryDTO;
import f1ybird.ladder.org.entity.Role;
import f1ybird.ladder.org.entity.User;
import f1ybird.ladder.org.service.ResourceService;
import f1ybird.ladder.org.service.RoleService;
import f1ybird.ladder.org.service.UserService;
import f1ybird.ladder.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
  * @Description: 用户请求控制类
  *
  * @Author: kevin
  *
  * @Date 21:41 2018/4/10
  */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    /**
     * 获取用户列表
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/list")
    public String list(HttpServletRequest request, Model model){
        log.info("获取用户列表");
        String userName = request.getParameter("userName");
        String currentPageStr = request.getParameter("currentPage");
        String pageSizeStr = request.getParameter("pageSize");
        int currentPage = 1;
        int pageSize = 10;
        if(StringUtils.isNotBlank(currentPageStr)){
            currentPage = Integer.parseInt(currentPageStr);
        }
        if(StringUtils.isNotBlank(pageSizeStr)){
            pageSize = Integer.parseInt(pageSizeStr);
        }

        UserQueryDTO userQueryDTO = new UserQueryDTO();
        userQueryDTO.setUserName(userName);
        userQueryDTO.setCurrentPage(currentPage);
        userQueryDTO.setPageSize(pageSize);

        PageModel<User> pageModel = userService.queryUserPage(userQueryDTO);

        model.addAttribute("page",pageModel);
        model.addAttribute("userQueryDTO",userQueryDTO);
        model.addAttribute(Constants.MENU_NAME,Constants.MENU_USER_LIST);

        return "user/user_list";
    }

    /**
     * 跳转到编辑页面
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/dialog/edit")
    public String dialogEdit(HttpServletRequest request,Model model){
        log.info("跳转编辑/新增页面");
        String id = request.getParameter("id");
        if(StringUtils.isNotBlank(id)){
            User user = userService.find(id);
            model.addAttribute("user",user);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("deleteFlag","0");
        List<Role> roles = roleService.findRoles(params);
        model.addAttribute("roles",roles);

        return "/user/dialog/user_edit";
    }


    /**
     * 新增或编辑用户
     * @param request
     * @return
     */
    @RequestMapping("/ajax/save")
    @ResponseBody
    public AjaxResult ajaxSave(HttpServletRequest request){
        log.info("编辑/新增保存成功");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);

        try {
            String id = request.getParameter("id");
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String realName = request.getParameter("realName");
            String mobile = request.getParameter("mobile");
            String[] roleIds = request.getParameterValues("roleId");

            User user = null;
            if (StringUtils.isNotBlank(id)) {
                user = userService.find(id);
            } else {
                user = new User();
                user.setUsername(StringUtils.trim(username));
                user.setStatus(User.STATUS_YES);
            }

            if (StringUtils.isNotBlank(password)) {
                user.setPassword(Md5Util.generatePassword(password));
            }
            user.setRealName(StringUtils.trim(realName));
            user.setMobile(StringUtils.trim(mobile));

            Set<Role> roles = new HashSet<>();

            for (String roleId : roleIds) {
                if (StringUtils.isNotBlank(roleId)) {
                    Role role = roleService.find(roleId);
                    if (null != role) {
                        roles.add(role);
                    }
                }
            }

            user.setRoles(roles);

            if (StringUtils.isNotBlank(id)) {
                userService.update(user);
            } else {
                user.setType(0);
                userService.save(user);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        ajaxResult.setSuccess(true);

        return ajaxResult;
    }

    /**
     * 校验用户名是否已经存在
     * @param request
     * @return
     */
    @RequestMapping("/ajax/validator/username")
    @ResponseBody
    public Map<String, Object> ajaxValidatorUsername(HttpServletRequest request){
        log.info("校验用户名是否已存在");
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        String username = request.getParameter("username");

        if(StringUtils.isNotBlank(username)){
            params.put("username",StringUtils.trim(username));
        }

        List<User> users = userService.findUsers(params);
        if(null != users && !users.isEmpty()){
            map.put("error","帐号已存在");
        }else{
            map.put("ok","");
        }
        return map;
    }

    @RequestMapping("/ajax/upd/status")
    @ResponseBody
    public AjaxResult ajaxUpdStatus(HttpServletRequest request){
        log.info("恢复或者删除数据");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);

        try{
            String[] ids = request.getParameterValues("ids");
            String status = request.getParameter("status");

            userService.updateStatus(ids, Integer.parseInt(status));

            ajaxResult.setSuccess(true);
        }catch (Exception e){
            e.printStackTrace();
        }

        return ajaxResult;
    }


}
