package com.integral.system.codelist.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.springframework.dao.DataAccessResourceFailureException;

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
     */
    private static String ALLCODELISTDATASQL = "SELECT child.dataid AS dataid, child.codeid AS codeid, codelist.codename AS codename, child.datakey AS datakey," +
    " child.datavalue AS datavalue, child.parentdatakey AS parentdatakey, parent.datavalue AS parentvalue, child.remark AS remark " +
    " FROM point_system_codelist_data AS child Left Join point_system_codelist_data AS parent ON child.parentdatakey = parent.datakey" +
    " Inner Join point_system_codelist AS codelist on child.codeid = codelist.codeid ORDER BY child.codeid, child.dataid ";
    
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
    public List<CodeListData> getCodeListDataByPage(int start, int limit) throws SQLException {
        
        String sql = ALLCODELISTDATASQL;
        /*
        sql = "SELECT child.dataid AS dataid, child.codeid AS codeid, codelist.codename AS codename," +
        		" child.datakey AS datakey, child.datavalue AS datavalue, child.parentdatakey AS parentdatakey," +
        		" parent.datavalue AS parentvalue,  child.remark AS remark, parent.datakey AS parentdatakey1" +
        		" FROM ( SELECT * FROM point_system_codelist_data) AS child LEFT JOIN " +
        		" ( SELECT datakey, datavalue FROM point_system_codelist_data) AS parent ON child.parentdatakey = parent.datakey" +
        		" INNER JOIN point_system_codelist AS codelist ON child.codeid = codelist.codeid ";
        BaseHibernateJDBCDao jdbcDao = new BaseHibernateJDBCDao();
        */
        List list = this.codeListDataDao.findCodeListDataByPage(true, sql, start, limit, null);
        List<CodeListData> codeDataList = new ArrayList<CodeListData>();
        log.info("find codeListdata by page : " + list);
        list = this.baseDao.queryListByPageByJDBC(sql, start, limit, null);
        log.info("find codeListdata by page : " + list);
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
    public long getCodeListDataSize() {
        long size = 0L;
        String sql = "select count(dataid) codesize from point_system_codelist_data";
        List list = this.baseDao.queryBySQL(sql, null);
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
        return this.getCodeListDataByPage(0, 999999999);
    }
}
