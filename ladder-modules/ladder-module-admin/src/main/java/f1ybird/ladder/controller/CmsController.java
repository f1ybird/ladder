package f1ybird.ladder.controller;

import f1ybird.ladder.cms.dto.ArticleQueryDTO;
import f1ybird.ladder.cms.dto.ColumnInfoDTO;
import f1ybird.ladder.cms.dto.ColumnInfoQueryDTO;
import f1ybird.ladder.cms.entity.Article;
import f1ybird.ladder.cms.entity.ColumnInfo;
import f1ybird.ladder.cms.service.ArticleService;
import f1ybird.ladder.cms.service.ColumnInfoService;
import f1ybird.ladder.common.dto.AjaxResult;
import f1ybird.ladder.common.entity.PageModel;
import f1ybird.ladder.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.util.StringUtil;
import org.json.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Description: 内容管理请求控制类
 * @Author: kevin
 * @Date 15:28 2018/4/14
 */
@RequestMapping("/cms")
@Controller
public class CmsController {

    @Autowired
    ArticleService articleService;

    @Autowired
    ColumnInfoService columnInfoService;

    private static final Logger log = Logger.getLogger(CmsController.class);

    /**
     * 获取栏目列表
     *
     * @param request
     * @return
     */
    @RequestMapping("/column/list")
    public String columnList(HttpServletRequest request, Model model) {
        log.info("获取栏目列表");

        String code = request.getParameter("code");
        String name = request.getParameter("name");
        String rootColumnId = request.getParameter("rootColumnId");

        ColumnInfoQueryDTO cDTO = new ColumnInfoQueryDTO();
        cDTO.setLevel(ColumnInfo.LEVEL_ROOT);
        List<ColumnInfo> rootCoulumnInfoList = this.columnInfoService.queryColumnInfoList(cDTO);
        if (StringUtils.isBlank(rootColumnId) && rootCoulumnInfoList != null && rootCoulumnInfoList.size() > 0) {
            rootColumnId = rootCoulumnInfoList.get(0).getId();
        }
        ColumnInfoQueryDTO columnInfoQueryDTO = new ColumnInfoQueryDTO();
        columnInfoQueryDTO.setRootColumnId(rootColumnId);
        columnInfoQueryDTO.setIsRootColumnLike(false);
        columnInfoQueryDTO.setCode(code);
        columnInfoQueryDTO.setName(name);
        List<ColumnInfo> list = columnInfoService.queryColumnInfoList(columnInfoQueryDTO);

        model.addAttribute("list", list);
        model.addAttribute("columnInfoQueryDTO", columnInfoQueryDTO);
        model.addAttribute(Constants.MENU_NAME, Constants.MENU_COLUMN_LIST);
        model.addAttribute("rootCoulumnInfoList", rootCoulumnInfoList);

        return "/cms/column_default_list";
    }

    /**
     * 跳转到栏目编辑或者新增表单
     *
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/column/edit")
    public String columnEdit(HttpServletRequest request, Model model) {
        log.info("跳转到编辑或者新增栏目表单");

        String id = request.getParameter("id");
        String rootColumnId = request.getParameter("rootColumnId");
        String columnLevel = request.getParameter("columnLevel");

        ColumnInfo columnInfo = null;
        ColumnInfoQueryDTO columnInfoQueryDTO = new ColumnInfoQueryDTO();
        columnInfoQueryDTO.setLevel(ColumnInfo.LEVEL_ROOT);
        List<ColumnInfo> rootCoulumnInfoList = columnInfoService.queryColumnInfoList(columnInfoQueryDTO);
        if (StringUtils.isNotBlank(id)) {
            columnInfo = columnInfoService.find(id);
        }

        model.addAttribute("columnInfo", columnInfo);
        model.addAttribute("rootCoulumnInfoList", rootCoulumnInfoList);
        model.addAttribute("rootColumnId", rootColumnId);
        model.addAttribute("columnLevel", columnLevel);

        return "/cms/dialog/column_default_edit";
    }

    /**
     * 栏目保存
     *
     * @param request
     * @return
     */
    @RequestMapping("/column/ajax/save")
    @ResponseBody
    public AjaxResult columnAjaxSave(HttpServletRequest request) {
        log.info("栏目保存");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);
        try {
            String id = request.getParameter("id");
            String columnLevel = request.getParameter("columnLevel");
            String code = request.getParameter("code");
            String name = request.getParameter("name");
            String parentId = request.getParameter("parentId");
            String orderNoStr = request.getParameter("orderNo");

            Integer orderNo = null;
            if (StringUtils.isNotBlank(orderNoStr)) {
                orderNo = Integer.parseInt(orderNoStr);
            }

            ColumnInfo columnInfo = null;
            if (StringUtils.isNotBlank(id)) {
                columnInfo = columnInfoService.find(id);
                columnInfo.setName(name);
                if ("level0".equals(columnLevel)) {
                    columnInfo.setLevel(ColumnInfo.LEVEL_ROOT);
                    columnInfo.setPath(columnInfo.getId());
                } else {
                    columnInfo.setLevel(ColumnInfo.LEVEL_LEAF);
                    if (StringUtils.isNotBlank(parentId)) {
                        ColumnInfo parentColumnInfo = columnInfoService.find(parentId);
                        columnInfo.setParent(parentColumnInfo);
                    }
                }
                columnInfo.setChannel(ColumnInfo.CHANNEL_PC);
                columnInfo.setOrderNo(orderNo);
                columnInfo.setUpdateDate(new Date());
            } else {
                columnInfo = new ColumnInfo();
                columnInfo.setCode(code);
                columnInfo.setName(name);
                if ("level0".equals(columnLevel)) {
                    columnInfo.setLevel(ColumnInfo.LEVEL_ROOT);
                } else {
                    columnInfo.setLevel(ColumnInfo.LEVEL_LEAF);
                    if (StringUtils.isNotBlank(parentId)) {
                        ColumnInfo parentColumnInfo = columnInfoService.find(parentId);
                        columnInfo.setParent(parentColumnInfo);
                    }
                }
                columnInfo.setChannel(ColumnInfo.CHANNEL_PC);
                columnInfo.setOrderNo(orderNo);
                columnInfo.setCreateDate(new Date());
                columnInfo.setDeleteFlag(ColumnInfo.DELETE_FLAG_NORMAL);
            }

            if (StringUtils.isNotBlank(id)) {
                columnInfoService.update(columnInfo);
            } else {
                columnInfoService.save(columnInfo);
                if (null != columnInfo && null != columnInfo.getParent()) {
                    String path = columnInfo.getParent().getId() + "/" + columnInfo.getId();
                    columnInfo.setPath(path);
                } else {
                    columnInfo.setPath(columnInfo.getId());
                }
                columnInfoService.update(columnInfo);
            }

            ajaxResult.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ajaxResult;
    }

    /**
     * 校验栏目编码是否已存在
     *
     * @param request
     * @return
     */
    @RequestMapping("/column/ajax/validator/code")
    @ResponseBody
    public Map<String, Object> columnAjaxValidatorCode(HttpServletRequest request) {
        log.info("校验栏目编码是否存在");

        Map<String, Object> map = new HashMap<>();
        String code = request.getParameter("code");
        ColumnInfoQueryDTO columnInfoQueryDTO = new ColumnInfoQueryDTO();
        columnInfoQueryDTO.setCode(code);
        columnInfoQueryDTO.setIsCodeLike(false);
        List<ColumnInfo> columnInfos = columnInfoService.queryColumnInfoList(columnInfoQueryDTO);
        if (null != columnInfos && !columnInfos.isEmpty()) {
            map.put("error", "栏目编码已存在");
        }
        map.put("ok", "");
        return map;
    }

    /**
     * 删除栏目列表
     *
     * @param request
     * @return
     */
    @RequestMapping("/column/ajax/delete")
    @ResponseBody
    public AjaxResult ajaxUpdStatus(HttpServletRequest request) {
        log.info("删除栏目列表");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);

        try {
            String[] ids = request.getParameterValues("ids");
            String deleteFlag = request.getParameter("deleteFlag");

            for (int i = 0; i < ids.length; i++) {
                if (StringUtils.isNotBlank(ids[i])) {
                    // 删除子节点
                    ColumnInfoQueryDTO columnInfoQueryDTO = new ColumnInfoQueryDTO();
                    columnInfoQueryDTO.setRootColumnId(ids[i]);
                    List<ColumnInfo> columnInfos = columnInfoService.queryColumnInfoList(columnInfoQueryDTO);
                    if (null != columnInfos && !columnInfos.isEmpty()) {
                        for (ColumnInfo c : columnInfos) {
                            c.setDeleteFlag(ColumnInfo.DELETE_FLAG_DELETED);
                            columnInfoService.update(c);
                        }
                    }
                    // 删除父节点
                    ColumnInfo columnInfo = columnInfoService.find(ids[i]);
                    columnInfo.setDeleteFlag(ColumnInfo.DELETE_FLAG_DELETED);
                    columnInfoService.update(columnInfo);
                }
            }
            ajaxResult.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ajaxResult;
    }


    /**
     * 获取文章列表
     *
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/article/list")
    public String articleList(HttpServletRequest request, Model model) {
        log.info("获取文章列表");
        String type = request.getParameter("type");
        String rootColumnId = request.getParameter("rootColumnId");
        String columnId = request.getParameter("columnId");
        String title = request.getParameter("title");
        String publisher = request.getParameter("publisher");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String createDateSortCss = request.getParameter("createDateSortCss");
        String currentPageStr = request.getParameter("currentPage");
        String pageSizeStr = request.getParameter("pageSize");
        int currentPage = 1;
        int pageSize = 10;
        if (StringUtils.isNotBlank(currentPageStr)) {
            currentPage = Integer.parseInt(currentPageStr);
        }
        if (StringUtils.isNotBlank(pageSizeStr)) {
            pageSize = Integer.parseInt(pageSizeStr);
        }

        ColumnInfoQueryDTO columnInfoQueryDTO = new ColumnInfoQueryDTO();
        columnInfoQueryDTO.setLevel(ColumnInfo.LEVEL_ROOT);
        List<ColumnInfo> rootCoulumnInfoList = this.columnInfoService.queryColumnInfoList(columnInfoQueryDTO);
        List<ColumnInfoDTO> columnInfoDTOList = new ArrayList<ColumnInfoDTO>();


        if (rootCoulumnInfoList != null && rootCoulumnInfoList.size() > 0) {
            if (StringUtils.isBlank(rootColumnId)) {
                rootColumnId = rootCoulumnInfoList.get(0).getId();
            }
            for (ColumnInfo c : rootCoulumnInfoList) {
                ColumnInfoDTO columnInfoDTO = new ColumnInfoDTO();
                columnInfoDTO.setId(c.getId());
                columnInfoDTO.setName(c.getName());

                ColumnInfoQueryDTO childColumnInfoQueryDTO = new ColumnInfoQueryDTO();
                childColumnInfoQueryDTO.setRootColumnId(c.getId());
                childColumnInfoQueryDTO.setIsRootColumnLike(false);
                List<ColumnInfo> childColumnInfoList = this.columnInfoService.queryColumnInfoList(childColumnInfoQueryDTO);
                if (childColumnInfoList != null && childColumnInfoList.size() > 0) {
                    List<ColumnInfoDTO> rColumnInfoDTOList = new ArrayList<ColumnInfoDTO>();
                    for (ColumnInfo rc : childColumnInfoList) {
                        ColumnInfoDTO rColumnInfoDTO = new ColumnInfoDTO();
                        rColumnInfoDTO.setId(rc.getId());
                        rColumnInfoDTO.setName(rc.getName());
                        rColumnInfoDTOList.add(rColumnInfoDTO);
                    }
                    columnInfoDTO.setChildColumnInfoList(rColumnInfoDTOList);
                }
                columnInfoDTOList.add(columnInfoDTO);
            }
        }

        ArticleQueryDTO articleQueryDTO = new ArticleQueryDTO();
        articleQueryDTO.setColumnId(columnId);
        articleQueryDTO.setType(type);
        articleQueryDTO.setIsFront(false);
        articleQueryDTO.setRootColumnId(rootColumnId);
        articleQueryDTO.setTitle(title);
        articleQueryDTO.setPublisher(publisher);
        articleQueryDTO.setStartDate(startDate);
        articleQueryDTO.setEndDate(endDate);
        articleQueryDTO.setCreateDateSortCss(createDateSortCss);
        articleQueryDTO.setCurrentPage(currentPage);
        articleQueryDTO.setPageSize(pageSize);

        PageModel<Article> page = this.articleService.queryArticlePage(articleQueryDTO);
        List<Map<String, Object>> statisMapList = this.articleService.queryStatisMapList(articleQueryDTO);
        Map<String, Object> statisMap = null;
        if (statisMapList != null && statisMapList.size() > 0) {
            statisMap = statisMapList.get(0);
        }
        model.addAttribute("page", page);
        model.addAttribute("type", type);
        model.addAttribute("statisMap", statisMap);
        model.addAttribute("articleQueryDTO", articleQueryDTO);
        model.addAttribute("rootCoulumnInfoList", rootCoulumnInfoList);
        model.addAttribute("columnInfoDTOList", columnInfoDTOList);
        model.addAttribute(Constants.MENU_NAME, Constants.MENU_ARTICLE_LIST);

        return "/cms/article_default_list";
    }

    /**
     * 跳转到文章编辑或者新增文章
     *
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/article/edit")
    public String articleEdit(HttpServletRequest request, Model model) {
        log.info("跳转到文章编辑或者新增文章");

        String id = request.getParameter("id");
        String rootColumnId = request.getParameter("rootColumnId");
        String columnId = request.getParameter("columnId");

        Article article = null;
        if (StringUtils.isNotBlank(id)) {
            article = articleService.find(id);
            if (null != article.getRootColumnInfo()) {
                columnId = article.getRootColumnInfo().getId();

            }
        }

        ColumnInfoQueryDTO columnInfoQueryDTO = new ColumnInfoQueryDTO();
        columnInfoQueryDTO.setLevel(ColumnInfo.LEVEL_ROOT);
        List<ColumnInfo> rootCoulumnInfoList = columnInfoService.queryColumnInfoList(columnInfoQueryDTO);

        model.addAttribute("article", article);
        model.addAttribute("rootCoulumnInfoList", rootCoulumnInfoList);
        model.addAttribute("rootColumnId", rootColumnId);
        model.addAttribute("columnId", columnId);
        model.addAttribute(Constants.MENU_NAME, Constants.MENU_ARTICLE_LIST);

        return "/cms/dialog/article_edit";
    }


    /**
     * 文章保存
     *
     * @param request
     * @return
     */
    @RequestMapping("/article/ajax/save")
    @ResponseBody
    public AjaxResult articleAjaxSave(HttpServletRequest request) {
        log.info("文章保存");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);

        try {
            String rootColumnId = request.getParameter("rootColumnId");
            String columnInfoId = request.getParameter("leafColumnId");
            String articleTypeStr = request.getParameter("articleType");
            String title = request.getParameter("title");
            String href = request.getParameter("href");
            String content = request.getParameter("content");
            String publisher = request.getParameter("publisher");
            String orderNoStr = request.getParameter("orderNo");
            String id = request.getParameter("id");
            String coverImageUrl = request.getParameter("coverImageUrl");
            String summary = request.getParameter("summary");

            Article article = null;
            ColumnInfo rootColumnInfo = null;
            ColumnInfo columnInfo = null;
            Integer orderNo = null;
            Integer articleType = null;

            if (StringUtils.isNotBlank(rootColumnId)) {
                rootColumnInfo = columnInfoService.find(rootColumnId);
            }
            if (StringUtils.isNotBlank(columnInfoId)) {
                columnInfo = columnInfoService.find(columnInfoId);
            }
            if (StringUtils.isNotBlank(orderNoStr)) {
                orderNo = Integer.parseInt(orderNoStr);
            }
            if (StringUtils.isNotBlank(articleTypeStr)) {
                if ("contentType".equals(articleTypeStr)) {
                    articleType = 0;
                } else if ("hrefType".equals(articleType)) {
                    articleType = 1;
                } else if ("adType".equals(articleType)) {
                    articleType = 2;
                }
            }

            if (StringUtils.isNotBlank(id)) {
                article = articleService.find(id);
                article.setTitle(StringUtils.trim(title));
                article.setPublisher(StringUtils.trim(publisher));
                article.setContent(content);
                article.setType(articleType);
                article.setRootColumnInfo(rootColumnInfo);
                article.setColumnInfo(columnInfo);
                article.setSummary(StringUtils.trim(summary));
                article.setUpdateDate(new Date());
                article.setOrderNo(orderNo);
                article.setHref(StringUtils.trim(href));
                if (StringUtils.isNotBlank(coverImageUrl)) {
                    article.setCoverImageUrl(StringUtils.trim(coverImageUrl));
                }
            } else {
                article = new Article();
                article.setTitle(StringUtils.trim(title));
                article.setPublisher(StringUtils.trim(publisher));
                article.setContent(content);
                article.setType(articleType);
                article.setRootColumnInfo(rootColumnInfo);
                article.setColumnInfo(columnInfo);
                article.setSummary(StringUtils.trim(summary));
                article.setUpdateDate(new Date());
                article.setOrderNo(orderNo);
                article.setViewCount(0);
                article.setDeleteFlag(ColumnInfo.DELETE_FLAG_NORMAL);
                article.setHref(StringUtils.trim(href));
                if (StringUtils.isNotBlank(coverImageUrl)) {
                    article.setCoverImageUrl(StringUtils.trim(coverImageUrl));
                }
            }

            if (StringUtils.isNotBlank(id)) {
                articleService.update(article);
            } else {
                articleService.save(article);
            }

            ajaxResult.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ajaxResult;
    }


    /**
     * 根据根节点查找子节点
     *
     * @param request
     * @return
     */
    @RequestMapping("/column/ajax/getLeafColumn")
    @ResponseBody
    public AjaxResult colunAjaxGetLeafColumn(HttpServletRequest request) {
        log.info("根据根节点查找子节点");
        String rootColumnInfoId = request.getParameter("rootColumnInfoId");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);
        try {
            if (StringUtils.isNotBlank(rootColumnInfoId)) {
                ColumnInfoQueryDTO columnInfoQueryDTO = new ColumnInfoQueryDTO();
                columnInfoQueryDTO.setRootColumnId(rootColumnInfoId);
                columnInfoQueryDTO.setIsRootColumnLike(false);
                List<ColumnInfo> columnInfoList = columnInfoService.queryColumnInfoList(columnInfoQueryDTO);
                ajaxResult.setData(columnInfoList);
            }
            ajaxResult.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ajaxResult;
    }

    /**
     * 文章删除
     * @param request
     * @return
     */
    @RequestMapping("/article/ajax/delete")
    @ResponseBody
    public AjaxResult ajaxArticleDelete(HttpServletRequest request){
        log.info("文章删除");

        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);

        try {
            String[] ids = request.getParameterValues("ids");
            String deleteFlag = request.getParameter("deleteFlag");

            if(ids != null && ids.length > 0){
                for(String id:ids){
                    Article article = this.articleService.find(id);
                    article.setDeleteFlag(deleteFlag);
                    this.articleService.update(article);
                }
            }
            ajaxResult.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ajaxResult;
    }

    /**
     * 文章审核
     * @param request
     * @return
     */
    @RequestMapping("/article/ajax/audit")
    @ResponseBody
    public AjaxResult ajaxArticleAudit(HttpServletRequest request){
        log.info("文章审核");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);

        try {
            Boolean isAudit = false;
            String[] ids = request.getParameterValues("ids");
            String auditFlag = request.getParameter("auditFlag");
            if(StringUtils.isNotBlank(auditFlag) && auditFlag.equals("1")){
                isAudit = true;
            }
            if(ids != null && ids.length > 0){
                for(String id:ids){
                    Article article = this.articleService.find(id);
                    article.setIsAudit(isAudit);
                    this.articleService.update(article);
                }
            }
            ajaxResult.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ajaxResult;
    }

    /**
     * 文章置顶
     * @param request
     * @return
     */
    @RequestMapping("/article/ajax/top")
    @ResponseBody
    public AjaxResult ajaxArticleTop(HttpServletRequest request){
        log.info("文章置顶");
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccess(false);

        try {
            Boolean isTop = false;
            String[] ids = request.getParameterValues("ids");
            String topFlag = request.getParameter("topFlag");
            if(StringUtils.isNotBlank(topFlag) && topFlag.equals("1")){
                isTop = true;
            }
            if(ids != null && ids.length > 0){
                for(String id:ids){
                    Article article = this.articleService.find(id);
                    article.setIsTop(isTop);
                    this.articleService.update(article);
                }
            }
            ajaxResult.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ajaxResult;
    }

    /**
     * 跳转到flash控件开启页面
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/article/flashView")
    public String articleFlashView(HttpServletRequest request, Model model){
        log.info("跳转到flash控件开启页面");

        return "/cms/article_flash_view";
    }
}
