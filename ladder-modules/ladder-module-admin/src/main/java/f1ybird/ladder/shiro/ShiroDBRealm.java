package f1ybird.ladder.shiro;

import f1ybird.ladder.org.entity.Resource;
import f1ybird.ladder.org.entity.Role;
import f1ybird.ladder.org.entity.User;
import f1ybird.ladder.org.service.ResourceService;
import f1ybird.ladder.org.service.UserService;
import f1ybird.ladder.util.WebHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
  * @Description: shiro安全验证
  *
  * @Author: kevin
  *
  * @Date 22:07 2018/4/9
  */
public class ShiroDBRealm extends AuthorizingRealm{

    @Autowired
    private UserService userService;

    @Autowired
    private ResourceService resourceService;

    /**
     * 用户授权
     * @param principalCollection 被授权人
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        String userName = (String)principalCollection.getPrimaryPrincipal();
        User user = userService.findUserByName(userName);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        Set<Role> roleSet = user.getRoles();
        Set<String> permissionSet = new HashSet<String>();
        for(Role role : roleSet){
            if(StringUtils.isNotBlank(role.getRoleName())){
                info.addRole(role.getRoleName());
                Set<Resource> resources = role.getResources();
                for (Resource r : resources){
                    if(StringUtils.isNotBlank(r.getUrl())){
                        permissionSet.add(r.getUrl());
                    }
                }
            }
        }
        info.addStringPermissions(permissionSet);
        return info;
    }

    /**
     * 验证用户
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken)authenticationToken;
        if(StringUtils.isEmpty(token.getUsername())){
            return null;
        }

        User user = userService.findUserByName(token.getUsername());
        if(null != user){
            if(user.getStatus() == User.STATUS_NO){
                throw new LockedAccountException();
            }

            AuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(user.getUsername(), user.getPassword(), getName());
            setSession(WebHelper.SESSION_LOGIN_USER,user);

            // 初始化菜单权限
            initMenu(user.getId());

            return authenticationInfo;
        }
        return null;
    }

    /**
     * 初始化菜单
     * @param userId 用户ID
     */
    private void initMenu(String userId) {
        List<Resource> resourceList = resourceService.findAllMenu();
        Map<String, Object> resourceMap = userService.findResourceMap(userId);
        List<Resource> hasResources = new ArrayList<>();
        if(null != resourceList && !resourceList.isEmpty()){
            for(Resource resource : resourceList){
                Resource hasResource = hasResource(resource, resourceMap);
                if(null != hasResource){
                    hasResources.add(hasResource);
                }
            }
        }

        setSession(WebHelper.SESSION_MENU_RESOURCE,hasResources);

    }

    /**
     * 级联判断用户的菜单权限
     * @param resource
     * @param resourceMap
     * @return
     */
    private Resource hasResource(Resource resource, Map<String, Object> resourceMap) {
        if(resourceMap.containsKey(resource.getId())){
            List<Resource> resourceChildren = resource.getChildren();
            List<Resource> hasResources = new ArrayList<>();
            if(null != resourceChildren && !resourceChildren.isEmpty()){
                for (Resource r : resourceChildren){
                    Resource hasResource = hasResource(r, resourceMap);
                    if(null != hasResource){
                        hasResources.add(hasResource);
                    }
                }
            }
            resource.setChildren(hasResources);
            return resource;
        }
        return null;
    }

    /**
     * 保存当前登陆人的session
     * @param key
     * @param value
     */
    private void setSession(Object key, Object value) {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        if(null != session){
            session.setAttribute(key,value);
        }
    }
}
