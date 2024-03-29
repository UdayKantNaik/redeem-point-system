package com.integral.system.codelist.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.integral.common.dao.IBaseDao;
import com.integral.system.codelist.bean.CodeListData;
import com.integral.system.codelist.dao.ICodeListDao;
import com.integral.system.codelist.dao.ICodeListDataDao;
import com.integral.system.codelist.service.ICodeListDataService;
import com.integral.system.menu.bean.MenuTree;

/** 
 * <p>Description: [描述该类概要功能介绍]</p>
 * @author  <a href="mailto: swpigris81@126.com">代超</a>
 * @version $Revision$ 
 */
public class CodeListDataServiceImpl implements ICodeListDataService {
    private static final Log log = LogFactory.getLog(CodeListDataServiceImpl.class);
    private ICodeListDao codeListDao;
    private ICodeListDataDao codeListDataDao;
    private IBaseDao baseDao;
    /**
     * 查找所有数据标准，并且以codeid,dataid排序
     * 经过优化之后的sql语句，查询速度大大增加, 使用子查询利用表中的索引,更新表结构
     */
    private static String ALLCODELISTDATASQL = "SELECT dataid, codeid, codename, datakey, datavalue, parentdatakey, parentvalue, remark" +
    		" FROM (SELECT child.dataid, child.codeid, ( SELECT codelist.codename FROM point_system_codelist AS codelist" +
    		" WHERE child.codeid = codelist.codeid) codename, child.datakey, child.datavalue, child.parentdatakey, parent.datavalue AS parentvalue, child.remark" +
    		" FROM point_system_codelist_data AS parent JOIN point_system_codelist_data AS child ON child.parentdatakey = parent.datakey UNION" +
    		" SELECT child.dataid, child.codeid, ( SELECT codelist.codename FROM point_system_codelist AS codelist WHERE child.codeid = codelist.codeid) codename," +
    		" child.datakey,child.datavalue, child.parentdatakey, NULL AS parentvalue, child.remark FROM point_system_codelist_data child " +
    		" WHERE child.parentdatakey IS NULL ORDER BY codeid, dataid ) AS codetable where 1=1";
    
    /**
     * <p>Discription:[方法功能中文描述]</p>
     * @return ICodeListDao codeListDao.
     */
    public ICodeListDao getCodeListDao() {
        return codeListDao;
    }
    /**
     * <p>Discription:[方法功能中文描述]</p>
     * @param codeListDao The codeListDao to set.
     */
    public void setCodeListDao(ICodeListDao codeListDao) {
        this.codeListDao = codeListDao;
    }
    /**
     * <p>Discription:[方法功能中文描述]</p>
     * @return ICodeListDataDao codeListDataDao.
     */
    public ICodeListDataDao getCodeListDataDao() {
        return codeListDataDao;
    }
    /**
     * <p>Discription:[方法功能中文描述]</p>
     * @param codeListDataDao The codeListDataDao to set.
     */
    public void setCodeListDataDao(ICodeListDataDao codeListDataDao) {
        this.codeListDataDao = codeListDataDao;
    }
    /**
     * <p>Discription:[方法功能中文描述]</p>
     * @return IBaseDao baseDao.
     */
    public IBaseDao getBaseDao() {
        return baseDao;
    }
    /**
     * <p>Discription:[方法功能中文描述]</p>
     * @param baseDao The baseDao to set.
     */
    public void setBaseDao(IBaseDao baseDao) {
        this.baseDao = baseDao;
    }
    @Override
    public List<CodeListData> getCodeListDataByPage(int start, int limit, Map<String, Object> paramMap) throws SQLException {
        
        StringBuffer sql = new StringBuffer(ALLCODELISTDATASQL);
        /*
        sql = "SELECT child.dataid AS dataid, child.codeid AS codeid, codelist.codename AS codename," +
        		" child.datakey AS datakey, child.datavalue AS datavalue, child.parentdatakey AS parentdatakey," +
        		" parent.datavalue AS parentvalue,  child.remark AS remark, parent.datakey AS parentdatakey1" +
        		" FROM ( SELECT * FROM point_system_codelist_data) AS child LEFT JOIN " +
        		" ( SELECT datakey, datavalue FROM point_system_codelist_data) AS parent ON child.parentdatakey = parent.datakey" +
        		" INNER JOIN point_system_codelist AS codelist ON child.codeid = codelist.codeid ";
        BaseHibernateJDBCDao jdbcDao = new BaseHibernateJDBCDao();
        */
        //List list = this.codeListDataDao.findCodeListDataByPage(true, sql, start, limit, null);
        
        StringBuffer param = new StringBuffer();
        if(paramMap != null && paramMap.size() > 0 && !paramMap.isEmpty()){
            if(paramMap.get("dataKey") != null && !"".equals(paramMap.get("dataKey").toString().trim())){
                sql.append(" and datakey = ? ");
                param.append(paramMap.get("dataKey").toString()).append(",");
            }
            if(paramMap.get("dataValue") != null && !"".equals(paramMap.get("dataValue").toString().trim())){
                sql.append(" and datavalue = ? ");
                param.append(paramMap.get("dataValue").toString()).append(",");
            }
            if(paramMap.get("codeId") != null && !"".equals(paramMap.get("codeId").toString().trim())){
                sql.append(" and codeid = ? ");
                param.append(paramMap.get("codeId").toString()).append(",");
            }
            if(paramMap.get("parentDataKey") != null && !"".equals(paramMap.get("parentDataKey").toString().trim())){
                sql.append(" and parentdatakey = ? ");
                param.append(paramMap.get("parentDataKey").toString()).append(",");
            }
            if(paramMap.get("remark") != null && !"".equals(paramMap.get("remark").toString().trim())){
                sql.append(" and binary ucase(remark) like concat('%',ucase(?),'%') ");
                param.append(paramMap.get("remark").toString());
            }
        }
        String []params = null;
        if(param != null && param.toString().length() > 0){
            params = param.toString().split(",");
        }
        
        List<CodeListData> codeDataList = new ArrayList<CodeListData>();
        log.info( new Date() + "find codeListdata by page : ");
        List list = this.baseDao.queryListByPageByJDBC(sql.toString(), start, limit, params);
        log.info(new Date() + "find codeListdata by page : " + list);
        if(list != null){
            for(int i=0, j = list.size(); i < j; i++){
                CodeListData codeData = new CodeListData();
                Object[] obj = (Object[]) list.get(i);
                codeData.setDataId(obj[0] == null ? "" : obj[0].toString());
                codeData.setCodeId(obj[1] == null ? "" : obj[1].toString());
                codeData.setCodeName(obj[2] == null ? "" : obj[2].toString());
                codeData.setDataKey(obj[3] == null ? "" : obj[3].toString());
                codeData.setDataValue(obj[4] == null ? "" : obj[4].toString());
                codeData.setParentDataKey(obj[5] == null ? "" : obj[5].toString());
                codeData.setParentDataValue(obj[6] == null ? "" : obj[6].toString());
                codeData.setRemark(obj[7] == null ? "" : obj[7].toString());
                codeDataList.add(codeData);
            }
        }
        return codeDataList;
    }
    @Override
    public long getCodeListDataSize(Map<String, Object> paramMap) {
        long size = 0L;
        StringBuffer sql = new StringBuffer("select count(dataid) codesize from point_system_codelist_data where 1 = 1");
        StringBuffer param = new StringBuffer();
        if(paramMap != null && paramMap.size() > 0 && !paramMap.isEmpty()){
            if(paramMap.get("dataKey") != null && !"".equals(paramMap.get("dataKey").toString().trim())){
                sql.append(" and datakey = ? ");
                param.append(paramMap.get("dataKey").toString()).append(",");
            }
            if(paramMap.get("dataValue") != null && !"".equals(paramMap.get("dataValue").toString().trim())){
                sql.append(" and datavalue = ? ");
                param.append(paramMap.get("dataValue").toString()).append(",");
            }
            if(paramMap.get("codeId") != null && !"".equals(paramMap.get("codeId").toString().trim())){
                sql.append(" and codeid = ? ");
                param.append(paramMap.get("codeId").toString()).append(",");
            }
            if(paramMap.get("parentDataKey") != null && !"".equals(paramMap.get("parentDataKey").toString().trim())){
                sql.append(" and parentdatakey = ? ");
                param.append(paramMap.get("parentDataKey").toString()).append(",");
            }
            if(paramMap.get("remark") != null && !"".equals(paramMap.get("remark").toString().trim())){
                sql.append(" and binary ucase(remark) like concat('%',ucase(?),'%') ");
                param.append(paramMap.get("remark").toString());
            }
        }
        String []params = null;
        if(param != null && param.toString().length() > 0){
            params = param.toString().split(",");
        }
        
        List list = this.baseDao.queryBySQL(sql.toString(), params);
        if(list!=null){
            size = NumberUtils.toLong((String.valueOf(list.get(0))), 0L);
        }
        return size;
    }
    @Override
    public void deleteAll(Collection<CodeListData> entities) {
        this.codeListDataDao.deleteAll(entities);
    }
    @Override
    public void saveOrUpdate(CodeListData entity) {
        this.codeListDataDao.saveOrUpdate(entity);
    }
    @Override
    public void deleteByCodeListId(String[] codeList) throws Exception {
        if(codeList == null || codeList.length <1){
            return;
        }
        StringBuffer sql = new StringBuffer("delete from point_system_codelist_data where codeid in ( ? ");
        if(codeList.length > 1){
            for(int i = 1; i< codeList.length; i++){
                sql.append(" , ? ");
            }
        }
        sql.append(" ) ");
        this.baseDao.excuteSQL(sql.toString(), codeList);
    }
    @Override
    public List findCodeDataListTree(String codeId, String parentDataKey) throws SQLException {
        String sql = "SELECT parent.datakey, parent.datavalue,(SELECT IF(COUNT(child.dataid) > 0, 'false', 'true')" +
        		" FROM point_system_codelist_data AS child WHERE child.parentdatakey = parent.datakey AND child.codeid=parent.codeid) AS isleft" +
        		" FROM point_system_codelist_data AS parent WHERE parent.codeid= ? AND parent.parentdatakey  ";
        String params[] = null;
        if(parentDataKey == null || "".equals(parentDataKey.trim())){
            sql += " IS NULL ";
            params = new String[]{codeId};
        }else{
            sql += " = ? ";
            params = new String[]{codeId, parentDataKey.trim()};
        }
        List list = this.baseDao.queryListByPageByJDBC(sql, 0, 999999999, params);
        List result = new ArrayList();
        if(list != null && list.size() >0){
            for(int i=0,j=list.size();i<j;i++){
                MenuTree tree = new MenuTree();
                Object [] obj = (Object[]) list.get(i);
                tree.setId(obj[0] == null ? "" : obj[0].toString());
                tree.setText(obj[1] == null ? "" : obj[1].toString());
                tree.setQtip(obj[1] == null ? "" : obj[1].toString());
                tree.setSingleClickExpand(true);
                tree.setHref(null);
                tree.setTarget(false);
                String isLeft = obj[2] == null ? "" : obj[2].toString();
                boolean leaf = BooleanUtils.toBoolean(isLeft);
                tree.setLeaf(leaf);
                if(leaf){
                    //子节点
                    tree.setCls("file");
                    tree.setExpandable(false);
                }else{
                    tree.setCls("folder");
                    tree.setExpandable(true);
                }
                result.add(tree);
            }
        }
        return result;
    }
    @Override
    public List findByDataKey(Object dataKey) {
        return this.codeListDataDao.findByDataKey(dataKey);
    }
    @Override
    public List findByExample(CodeListData instance) {
        return this.codeListDataDao.findByExample(instance);
    }
    @Override
    public List findByProperty(String propertyName, Object value) {
        return this.codeListDataDao.findByProperty(propertyName, value);
    }
    @Override
    public List findAllOrderByDataCode() throws SQLException {
        return this.getCodeListDataByPage(0, 999999999, null);
    }
    
    /**
     * <p>Discription:[增加或修改数据标准值]</p>
     * @param entity
     * @author:[代超]
     * @update:[日期YYYY-MM-DD] [更改人姓名][变更描述]
     */
    public void saveOrUpdateAll(Collection<CodeListData> entities){
        this.codeListDataDao.saveOrUpdateAll(entities);
    }
    @Override
    public List findByDataValue(Object dataValue) {
        return this.codeListDataDao.findByDataValue(dataValue);
    }
    
    public List findCodeDataListCombo(String codeId, String codeName, String parentCodeId){
        Object [] param = null;
        if(codeId != null && !"".equals(codeId.trim())){
            String sql = "FROM CodeListData model WHERE model.codeId = ?";
            if(parentCodeId != null && !"".equals(parentCodeId.trim())){
                sql += " and model.parentDataKey = ?";
                param = new Object[]{codeId, parentCodeId};
            }else{
                sql += " and model.parentDataKey is null";
                param = new Object[]{codeId};
            }
            return this.codeListDataDao.findCodeListDataByPage(false, sql, -1, -1, param);
        }else if(codeName != null && !"".equals(codeName.trim())){
            String sql = "FROM CodeListData model WHERE model.codeId = (SELECT codeId FROM CodeList m WHERE m.codeName = ?)";
            if(parentCodeId != null && !"".equals(parentCodeId.trim())){
                sql += " and model.parentDataKey = ?";
                param = new Object[]{codeId, parentCodeId};
            }else{
                sql += " and model.parentDataKey is null";
                param = new Object[]{codeName};
            }
            return this.codeListDataDao.findCodeListDataByPage(false, sql, -1, -1, param);
        }else{
            return null;
        }
    }
}
