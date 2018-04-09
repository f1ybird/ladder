package f1ybird.ladder.org.dao;


import f1ybird.ladder.common.dao.CommonDao;
import f1ybird.ladder.org.entity.Role;
import org.springframework.data.jpa.repository.Query;

public interface RoleDao extends RoleCustomDao,CommonDao<Role,String> {

	@Query("from Role r where r.roleName = ?1 ")
	public Role findByRoleName(String roleName);
	
}
