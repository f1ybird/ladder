package f1ybird.ladder.cms.service;

import f1ybird.ladder.cms.dao.ColumnInfoDao;
import f1ybird.ladder.cms.dto.ColumnInfoQueryDTO;
import f1ybird.ladder.cms.entity.ColumnInfo;
import f1ybird.ladder.common.entity.PageModel;
import f1ybird.ladder.common.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xujianfang
 * @desc ColumnInfoService类 
 * @date 2017-03-16
 */
@Service
public class ColumnInfoService extends CommonService< ColumnInfo,String >  {

    @Autowired
    private ColumnInfoDao columnInfoDao;

    @Autowired
    public void setColumnInfoDao(ColumnInfoDao columnInfoDao){
      super.setCommonDao(columnInfoDao);
    }

    public PageModel<ColumnInfo> queryColumnInfoPage(ColumnInfoQueryDTO columnInfoQueryDTO){
           return this.columnInfoDao.queryColumnInfoPage(columnInfoQueryDTO);
    }

    public List<ColumnInfo> queryColumnInfoList(ColumnInfoQueryDTO columnInfoQueryDTO){
           return this.columnInfoDao.queryColumnInfoList(columnInfoQueryDTO);
    }


}