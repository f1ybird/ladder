package f1ybird.ladder.org.dao;

import f1ybird.ladder.org.entity.Resource;

import java.util.List;
import java.util.Map;


public interface ResourceDaoCustom {

	public List<Resource> findMenuResource(Map<String, Object> params);
	
}
