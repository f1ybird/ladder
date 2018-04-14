package f1ybird.ladder.cms.dao;

import f1ybird.ladder.cms.dto.ColumnInfoQueryDTO;
import f1ybird.ladder.cms.entity.ColumnInfo;
import f1ybird.ladder.common.entity.PageModel;

import java.util.List;

/**
 * @author xujianfang
 * @desc ColumnInfoDaoCustom接口 
 * @date 2017-03-16
 */
public interface ColumnInfoDaoCustom {

      PageModel<ColumnInfo> queryColumnInfoPage(ColumnInfoQueryDTO columnInfoQueryDTO);

      List<ColumnInfo> queryColumnInfoList(ColumnInfoQueryDTO columnInfoQueryDTO);



}