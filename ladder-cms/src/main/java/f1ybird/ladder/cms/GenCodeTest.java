package f1ybird.ladder.cms;

import f1ybird.ladder.cms.entity.Article;
import f1ybird.ladder.cms.entity.ColumnInfo;
import f1ybird.ladder.common.util.GenCodeUtil;

import java.io.IOException;

public class GenCodeTest {
	
	public static void main(String[] args) throws IOException{
		//基本包目录（不用到entity那一层级）
		String s = "f1ybird.ladder.cms";
		//作者
		String writer = "kai zhang";
		//Demo为Entity类（放上自己新增的实体类即可）
		GenCodeTest.autoGenAllCode(ColumnInfo.class,s,writer);
		GenCodeTest.autoGenAllCode(Article.class,s,writer);
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
