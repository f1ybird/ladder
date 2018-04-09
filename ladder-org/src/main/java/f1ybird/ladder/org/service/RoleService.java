package f1ybird.ladder.org.service;

import f1ybird.ladder.common.entity.PageModel;
import f1ybird.ladder.common.service.CommonService;
import f1ybird.ladder.org.dao.RoleDao;
import f1ybird.ladder.org.dto.RoleQueryDTO;
import f1ybird.ladder.org.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RoleService extends CommonService<Role,String> {

	@Autowired
	private RoleDao roleDao;

	@Autowired
	public void setRoleDao(RoleDao roleDao) {
		super.setCommonDao(roleDao);
	}
	
	public List<Role> findRoles(Map<String, Object> params) {
		return roleDao.findRoles(params);
	}
	
	public void updateDeleteFlag(String[] ids, String deleteFlag){
		if(ids != null){
			for(String id : ids){
				Role role = this.find(id);
				role.setDeleteFlag(deleteFlag);
				this.update(role);
			}
		}
	}
	
	public Role findByRoleName(String roleName) {
		return roleDao.findByRoleName(roleName);
	}
	
	/**
	 * 根据查询条件查询角色分页信息
	 * @param userQueryDTO
	 * @return
	 */
	public PageModel<Role> queryRolePage(RoleQueryDTO roleQueryDTO){
		return this.roleDao.queryRolePage(roleQueryDTO);
	}
}
