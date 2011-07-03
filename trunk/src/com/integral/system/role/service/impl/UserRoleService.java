package com.integral.system.role.service.impl;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.springframework.dao.DataAccessResourceFailureException;

import com.integral.common.dao.IBaseDao;
import com.integral.system.role.dao.IUserRoleDao;
import com.integral.system.role.dao.impl.RoleDao;
import com.integral.system.role.service.IUserRoleService;

public class UserRoleService implements IUserRoleService {
    private IUserRoleDao userRoleDao;
    private RoleDao roleDao;
    private IBaseDao baseDao;
    

    /**
     * <p>Discription:[方法功能描述]</p>
     * @return IBaseDao baseDao.
     */
    public IBaseDao getBaseDao() {
        return baseDao;
    }

    /**
     * <p>Discription:[方法功能描述]</p>
     * @param baseDao The baseDao to set.
     */
    public void setBaseDao(IBaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /**
     * <p>
     * Discription:[方法功能描述]
     * </p>
     * 
     * @return IUserRoleDao userRoleDao.
     */
    public IUserRoleDao getUserRoleDao() {
        return userRoleDao;
    }

    /**
     * <p>
     * Discription:[方法功能描述]
     * </p>
     * 
     * @param userRoleDao
     *            The userRoleDao to set.
     */
    public void setUserRoleDao(IUserRoleDao userRoleDao) {
        this.userRoleDao = userRoleDao;
    }

    /**
     * <p>
     * Discription:[方法功能描述]
     * </p>
     * 
     * @return RoleDao roleDao.
     */
    public RoleDao getRoleDao() {
        return roleDao;
    }

    /**
     * <p>
     * Discription:[方法功能描述]
     * </p>
     * 
     * @param roleDao
     *            The roleDao to set.
     */
    public void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    @Override
    public List findRoleByUserName(String userName) {
        return this.userRoleDao.findRoleByUserIdName(userName);
    }
    
    /**
     * <p>Discription:[通过角色ID删除用户角色信息]</p>
     * @param roles
     * @author: 代超
     * @throws SQLException 
     * @throws IllegalStateException 
     * @throws HibernateException 
     * @throws DataAccessResourceFailureException 
     * @update: 2011-7-3 代超[变更描述]
     */
    public void deleteByRole(String[] roles) throws DataAccessResourceFailureException, HibernateException, IllegalStateException, SQLException{
        if(roles == null || roles.length<1){
            return;
        }
        String sql = "delete from supplier_role where role_id in ( ? ";
        if(roles != null && roles.length>1){
            for(int i=1;i<roles.length;i++){
                sql += " , ? ";
            }
        }
        sql += " )";
        this.baseDao.excuteSQL(sql, roles);
    }
}