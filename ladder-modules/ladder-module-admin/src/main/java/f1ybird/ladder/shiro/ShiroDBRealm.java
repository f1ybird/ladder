package f1ybird.ladder.shiro;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

public class ShiroDBRealm extends AuthorizingRealm{

    @Autowired
    private UserService userService;

    @Autowired
    private ResourceService resourceService;

    /**
     * 授权
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
            if(StringUtils.isNotBlank(role.getRoleName))){
                info.addRole(role.getRoleName);
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

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        return null;
    }
}
