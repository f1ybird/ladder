package f1ybird.ladder.org.dao;

import f1ybird.ladder.common.entity.PageModel;
import f1ybird.ladder.org.dto.RoleQueryDTO;
import f1ybird.ladder.org.entity.Role;

import java.util.List;
import java.util.Map;

public interface RoleCustomDao {
	
	public List<Role> findRoles(Map<String, Object> params);
	
	/**
	 * 根据查询条件查询角色分页信息
	 * @param userQueryDTO
	 * @return
	 */
	PageModel<Role> queryRolePage(RoleQueryDTO roleQueryDTO);
	
}
