package f1ybird.ladder.org.dao;

import f1ybird.ladder.common.dao.CommonDao;
import f1ybird.ladder.org.entity.Resource;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResourceDao extends ResourceDaoCustom, CommonDao<Resource,String> {

	@Query("select r from Resource r where r.deleteFlag = 0 and r.parent.id = null ")
	List<Resource> getRootResourceList();
	
	@Query("select r from Resource r where r.deleteFlag = 0 order by r.orderNo")
	List<Resource> getSystemResourceList();
	
}
