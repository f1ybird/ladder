package f1ybird.ladder.org.dao;

import f1ybird.ladder.common.entity.PageModel;
import f1ybird.ladder.org.dto.UserQueryDTO;
import f1ybird.ladder.org.entity.User;

import java.util.List;
import java.util.Map;

public interface SystemUserDao {
	
	public List<User> findUsers(Map<String, Object> params);
	
	/**
	 * 根据用户信息查询分页信息
	 * @param userQueryDTO
	 * @return
	 */
	PageModel<User> queryUserPage(UserQueryDTO userQueryDTO);
	
}
