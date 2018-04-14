package f1ybird.ladder.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import f1ybird.ladder.common.dto.AjaxResult;
import f1ybird.ladder.common.entity.PageModel;
import f1ybird.ladder.common.util.ExcelUtils;
import f1ybird.ladder.common.util.Md5Util;
import f1ybird.ladder.org.dto.RoleQueryDTO;
import f1ybird.ladder.org.dto.UserQueryDTO;
import f1ybird.ladder.org.entity.Resource;
import f1ybird.ladder.org.entity.Role;
import f1ybird.ladder.org.entity.User;
import f1ybird.ladder.org.service.ResourceService;
import f1ybird.ladder.org.service.RoleService;
import f1ybird.ladder.org.service.UserService;
import f1ybird.ladder.util.Constants;
import f1ybird.ladder.util.WebHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
  * @Description: 权限管理请求控制类
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

    private static final Logger log = Logger.getLogger(UserController.class);

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


    /**
     * 恢复或者禁用用户
     * @param request
     * @return
     */
    @RequestMapping("/ajax/upd/status")
    @ResponseBody
    public AjaxResult ajaxUpdStatus(HttpServletRequest request){
        log.info("恢复或者禁用用户");
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


    /**
     * excel导出用户列表
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/export")
    public String exportXlsx(HttpServletRequest request, HttpServletResponse response){
        log.info("用户列表导出");
        String username = request.getParameter("username");
        HashMap<String, Object> paramsMap = new HashMap<>();
        if(StringUtils.isNotBlank(username)){
            paramsMap.put("username",username);
        }
        List<User> users = userService.findUsers(paramsMap);

        Map<String, String> headNameMap = new LinkedHashMap<>();
        headNameMap.put("roleName","角色");
        headNameMap.put("userName","帐号");
        headNameMap.put("realName","姓名");
        headNameMap.put("mobile","电话号码");
        headNameMap.put("createDate","创建时间");
        headNameMap.put("status","状态");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Map<String,Object>> dataList = new ArrayList<>();

        if(null != users && !users.isEmpty()){
            for (User user : users){
                String statusName = "正常";// 帐号状态
                String createDate = "";   // 创建时间
                String roleName = "";     // 角色名称（多个）
                if((User.DELETE_FLAG_DELETED).equals(user.getDeleteFlag()) && StringUtils.isNotBlank(user.getDeleteFlag())){
                    statusName = "删除";
                }

                if(null != user.getCreateDate()){
                    createDate = sdf.format(user.getCreateDate());
                }

                Set<Role> roles = user.getRoles();
                if(null != roles && !roles.isEmpty()){
                    for (Role role : roles){
                        roleName += role.getName() + " ";
                    }
                }

                HashMap<String, Object> map = new HashMap<>();
                map.put("roleName",roleName);
                map.put("userName",user.getUsername());
                map.put("realName",user.getRealName());
                map.put("mobile",user.getMobile());
                map.put("createDate",createDate);
                map.put("status",statusName);
                dataList.add(map);
            }
        }

        ExcelUtils.exportXlsx(response,"用户列表导出",headNameMap,dataList);

        return null;
    }

    /**
     * 获取角色列表
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/role_list")
    public String roleList(HttpServletRequest request, Model model){
        log.info("获取角色列表");
        String name = request.getParameter("name");
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

        RoleQueryDTO roleQueryDTO = new RoleQueryDTO();
        roleQueryDTO.setName(name);
        roleQueryDTO.setCurrentPage(currentPage);
        roleQueryDTO.setPageSize(pageSize);

        PageModel<Role> pageModel = roleService.queryRolePage(roleQueryDTO);

        model.addAttribute("page",pageModel);
        model.addAttribute("roleQueryDTO",roleQueryDTO);
        model.addAttribute(Constants.MENU_NAME,Constants.MENU_ROLE_LIST);

        return "/user/role_list";
    }

    /**
     * 跳转到角色新增或者编辑页面
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/dialog/role_edit")
    public String dialogRoleEdit(HttpServletRequest request,Model model){
        log.info("跳转到角色新增或者编辑页面");
        List<Map<String, Object>> resourceMap = resourceService.getMap();
        String id = request.getParameter("id");
        if(StringUtils.isNotBlank(id)){
            Role role = roleService.find(id);
            model.addAttribute("role", role);

            if(null != role){
                Set<Resource> set = role.getResources();
                if(null != set && !set.isEmpty()){
                    for (int i = 0; i < resourceMap.size(); i++) {
                        Map<String, Object> map = resourceMap.get(i);
                        String rId = map.get("id").toString();
                        for (Resource r : set){
                            if(rId.equals(r.getId())){
                                map.put("checked",true);
                                map.put("open",true);
                                break;
                            }
                        }
                    }
                }
            }
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String resourceJson = objectMapper.writeValueAsString(resourceMap);
            model.addAttribute("resourceJson",resourceJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "/user/dialog/role_edit";
    }

    /**
     * 新增或者编辑角色
     * @param request
     * @return
     */
    @RequestMapping("/ajax/save_role")
    @ResponseBody
    public AjaxResult ajaxSaveRole(HttpServletRequest request){
        log.info("新增或者编辑角色");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);

        try {
            String id = request.getParameter("id");
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String[] rescoureIds = request.getParameterValues("rescoureIds");

            Role role = null;
            if(StringUtils.isNotBlank(id)){
                role = roleService.get(id);
            }else{
                role = new Role();
            }

            role.setName(StringUtils.trim(name));
            role.setDescription(StringUtils.trim(description));

            Set<Resource> resources = new HashSet<Resource>();
            if(rescoureIds != null){
                for(String rId : rescoureIds){
                    Resource resource = resourceService.find(rId);
                    if(resource != null){
                        resources.add(resource);
                    }
                }
            }
            role.setResources(resources);

            if(StringUtils.isNotBlank(role.getId())){
                roleService.update(role);
            }else{
                roleService.save(role);
            }

            ajaxResult.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ajaxResult;
    }

    /**
     * 角色删除
     * @param request
     * @return
     */
    @RequestMapping("/ajax/upd_role/delete_flag")
    @ResponseBody
    public AjaxResult ajaxUpdRoleDeleteFlag(HttpServletRequest request){
        log.info("角色删除");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);

        try {
            String[] ids = request.getParameterValues("ids");
            String deleteFlag = request.getParameter("deleteFlag");

            roleService.updateDeleteFlag(ids, deleteFlag);

            ajaxResult.setSuccess(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ajaxResult;
    }

    /**
     * 获取菜单列表
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/menu_list")
    public String muneList(HttpServletRequest request, Model model){
        log.info("获取菜单列表");
        String name = request.getParameter("name");
        HashMap<String, Object> paramsMap = new HashMap<>();
        if(StringUtils.isNotBlank(name)){
            paramsMap.put("name","%" + StringUtils.trim(name) + "%");

        }

        List<Resource> resources = resourceService.findMenuResource(paramsMap);
        model.addAttribute("resources",resources);
        model.addAttribute(Constants.MENU_NAME,Constants.MENU_NAME_LIST);
        model.addAttribute("name",name);

        return "/user/menu_list";
    }

    /**
     * 跳转到菜单新增或者编辑页面
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/dialog/menu_edit")
    public String dialogMenuEdit(HttpServletRequest request,Model model){
        log.info("跳转到菜单新增或者编辑页面");
        String id = request.getParameter("id");
        if(StringUtils.isNotBlank(id)){
            Resource resource = resourceService.find(id);
            model.addAttribute("resource",resource);
        }

        List<Resource> modelResources = resourceService.getRootResourceList();
        model.addAttribute("modelResources",modelResources);

        return "/user/dialog/menu_edit";
    }

    /**
     * 新增或者编辑菜单
     * @param request
     * @return
     */
    @RequestMapping("/ajax/save_menu")
    @ResponseBody
    public AjaxResult ajaxSaveMenu(HttpServletRequest request){
        log.info("新增或者编辑菜单");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);

        try {
            String id = request.getParameter("id");
            String name = request.getParameter("name");
            String icon = request.getParameter("icon");
            String url = request.getParameter("url");
            String orderNoStr = request.getParameter("orderNo");
            String type = request.getParameter("type");
            String parentId = request.getParameter("parentId");

            Resource resource = null;
            if(StringUtils.isNotBlank(id)){
                resource = resourceService.find(id);
            }else{
                resource = new Resource();
            }

            resource.setName(StringUtils.trim(name));
            resource.setIcon(StringUtils.trim(icon));
            resource.setUrl(StringUtils.trim(url));
            resource.setType(StringUtils.trim(type));

            Integer orderNo = null;
            if(StringUtils.isNotBlank(orderNoStr)){
                orderNo = Integer.parseInt(StringUtils.trim(orderNoStr));
            }
            resource.setOrderNo(orderNo);

            Resource parentResource = null;
            if(StringUtils.isNotBlank(parentId) && "page".equals(type)){
                parentResource = resourceService.find(parentId);
            }
            resource.setParent(parentResource);

            resourceService.saveResource(resource);

            ajaxResult.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ajaxResult;
    }

    /**
     * 菜单删除
     * @param request
     * @return
     */
    @RequestMapping("/ajax/upd_menu/delete_flag")
    @ResponseBody
    public AjaxResult ajaxUpdMennuDeleteFlag(HttpServletRequest request){
        log.info("菜单删除");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);

        try {
            String[] ids = request.getParameterValues("ids");
            String deleteFlag = request.getParameter("deletFlag");

            resourceService.updateDeleteFlag(ids, deleteFlag);

            ajaxResult.setSuccess(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ajaxResult;
    }

    /**
     * 测试管理
     * @return
     */
    @RequestMapping("/test")
    public String test(HttpServletRequest request, Model model){
        log.info("获取测试列表");
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
        model.addAttribute(Constants.MENU_NAME,Constants.MENU_TEST_LIST);

        return "user/test_list";
    }

    /**
     * 换肤管理
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/skin/list")
    public String skinList(HttpServletRequest request, Model model){
        log.info("换肤管理");
        model.addAttribute(Constants.MENU_NAME,Constants.MENU_UPDATE_SKIN);
        return "/user/skin_list";
    }

    /**
     * 保存修改皮肤
     * @param request
     * @return
     */
    @RequestMapping("/ajax/upd/skin")
    public AjaxResult ajaxUpdSkin(HttpServletRequest request){
        log.info("保存修改皮肤");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);

        try{
            String currentSkin = request.getParameter("skin");
            User user = (User)request.getSession().getAttribute(WebHelper.SESSION_LOGIN_USER);
            user.setCurrentSkin(currentSkin);
            userService.update(user);
            request.getSession().setAttribute(WebHelper.SESSION_LOGIN_USER,user);
            ajaxResult.setSuccess(true);
        }catch (Exception e){
            e.printStackTrace();
        }
        return ajaxResult;
    }

    /**
     * 跳转修改密码
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/update_pwd")
    public String updatePwd(HttpServletRequest request,Model model){
        log.info("跳转到修改密码页面");
        model.addAttribute(Constants.MENU_NAME,Constants.MENU_UPDATE_PWD);
        return "/user/update_pwd";
    }

    /**
     * 保存修改密码
     * @param request
     * @return
     */
    @RequestMapping("/ajax/save_pwd")
    @ResponseBody
    public AjaxResult ajaxSavePwd(HttpServletRequest request){
        log.info("保存修改密码");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);
        try{
            String oldPwd = request.getParameter("oldPwd");
            String pwd = request.getParameter("pwd");

            User user = WebHelper.getUser(request);

            user = userService.find(user.getId());
            if(Md5Util.generatePassword(oldPwd).equals(user.getPassword())){
                user.setPassword(Md5Util.generatePassword(pwd));
                userService.update(user);
                ajaxResult.setSuccess(true);
            }else{
                ajaxResult.setMsg("原始密码输入不正确");
            }
        }catch (Exception e){
            e.printStackTrace();
            ajaxResult.setMsg("修改失败");
        }
        return ajaxResult;
    }


}
