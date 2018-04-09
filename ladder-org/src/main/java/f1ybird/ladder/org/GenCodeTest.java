package f1ybird.ladder.org;

import f1ybird.ladder.common.util.GenCodeUtil;
import f1ybird.ladder.org.entity.Resource;
import f1ybird.ladder.org.entity.Role;
import f1ybird.ladder.org.entity.User;

import java.io.File;
import java.io.IOException;

public class GenCodeTest {
	
	public static void main(String[] args) throws IOException {
		//基本包目录（不用到entity那一层级）
		String s = "f1ybird.ladder.org";
		//作者
		String writer = "kai zhang";
		//Demo为Entity类（放上自己新增的实体类即可）
		GenCodeTest.autoGenAllCode(Resource.class,s,writer);
	}
	
	/**
	 * 组装所有生成类
	 * @param c
	 * @param commonPackage
	 * @param writer
	 * @throws IOException
	 */
	public static void autoGenAllCode(Class c,String commonPackage,String writer) throws IOException{
		GenCodeUtil.createQueryDTO(c, commonPackage, writer);
//		GenCodeUtil.createFrontQueryDTO(c, commonPackage, writer);
		GenCodeUtil.createDaoCustomInterface(c,commonPackage,writer);
		GenCodeUtil.createDaoInterface(c,commonPackage,writer);
		GenCodeUtil.createDaoClass(c,commonPackage,writer);
		GenCodeUtil.createServiceClass(c,commonPackage,writer);
	}

}
