package f1ybird.ladder.org.dao;

import f1ybird.ladder.org.entity.User;

import java.util.List;


public interface UserRoleRelCustomDao {

	/**
	 * 根据角色ID和用户名查找用户
	 * @param roleId
	 * @param name
	 * @return
	 */
	List<User> findUserListByRoleCondition(String roleId, String name);
}
