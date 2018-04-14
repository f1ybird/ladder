package f1ybird.ladder.cms.dao;

import f1ybird.ladder.cms.dto.ArticleQueryDTO;
import f1ybird.ladder.cms.dto.CurrentArticleInfoDTO;
import f1ybird.ladder.cms.entity.Article;
import f1ybird.ladder.common.entity.PageModel;

import java.util.List;
import java.util.Map;

/**
 * @author xujianfang
 * @desc ArticleDaoCustom接口 
 * @date 2017-03-16
 */
public interface ArticleDaoCustom {

      PageModel<Article> queryArticlePage(ArticleQueryDTO articleQueryDTO);

      List<Article> queryArticleList(ArticleQueryDTO articleQueryDTO);

      List<Map<String, Object>> queryStatisMapList(ArticleQueryDTO articleQueryDTO);
      
      List<Article> queryNextArticleList(CurrentArticleInfoDTO currentArticleInfoDTO);
      
      List<Article> queryPreArticleList(CurrentArticleInfoDTO currentArticleInfoDTO);

}