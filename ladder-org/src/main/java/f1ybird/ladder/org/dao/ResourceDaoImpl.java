package f1ybird.ladder.org.dao;

import f1ybird.ladder.common.dao.CustomBaseSqlDaoImpl;
import f1ybird.ladder.org.entity.Resource;

import java.util.List;
import java.util.Map;

public class ResourceDaoImpl extends CustomBaseSqlDaoImpl implements ResourceDaoCustom {

	@SuppressWarnings("unchecked")
	@Override
	public List findMenuResource(Map<String, Object> params) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select r from Resource r where r.type in('module', 'page') ");
		
		Object deleteFlag = params.get("deleteFlag");
		if(deleteFlag != null){
			sb.append(" and r.deleteFlag = :deleteFlag ");
		}

		Object name = params.get("name");
		if(name != null){
			sb.append(" and r.name like :name ");
		}
		
		sb.append(" order by r.orderNo ");
		
		return this.queryByMapParams(sb.toString(), params, null, null);
	}

}
