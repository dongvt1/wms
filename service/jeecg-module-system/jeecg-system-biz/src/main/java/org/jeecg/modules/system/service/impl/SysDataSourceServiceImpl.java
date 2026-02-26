package org.jeecg.modules.system.service.impl;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.druid.DruidDataSourceCreator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.dynamic.db.DataSourceCachePool;
import org.jeecg.modules.system.entity.SysDataSource;
import org.jeecg.modules.system.mapper.SysDataSourceMapper;
import org.jeecg.modules.system.service.ISysDataSourceService;
import org.jeecg.modules.system.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * @Description: Multiple data source management
 * @Author: jeecg-boot
 * @Date: 2019-12-25
 * @Version: V1.0
 */
@Service
public class SysDataSourceServiceImpl extends ServiceImpl<SysDataSourceMapper, SysDataSource> implements ISysDataSourceService {

    @Autowired
    private DruidDataSourceCreator dataSourceCreator;

    @Autowired
    private DataSource dataSource;

    @Override
    public Result saveDataSource(SysDataSource sysDataSource) {
        try {
            long count = checkDbCode(sysDataSource.getCode());
            if (count > 0) {
                return Result.error("Data source encoding already exists");
            }
            String dbPassword = sysDataSource.getDbPassword();
            if (StringUtils.isNotBlank(dbPassword)) {
                String encrypt = SecurityUtil.jiami(dbPassword);
                sysDataSource.setDbPassword(encrypt);
            }
            boolean result = save(sysDataSource);
            if (result) {
                //Dynamically create data sources
                //addDynamicDataSource(sysDataSource, dbPassword);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.OK("Added successfully！");
    }

    @Override
    public Result editDataSource(SysDataSource sysDataSource) {
        try {
            SysDataSource d = getById(sysDataSource.getId());
            DataSourceCachePool.removeCache(d.getCode());
            String dbPassword = sysDataSource.getDbPassword();
            if (StringUtils.isNotBlank(dbPassword)) {
                String encrypt = SecurityUtil.jiami(dbPassword);
                sysDataSource.setDbPassword(encrypt);
            }
            Boolean result=updateById(sysDataSource);
            if(result){
                //Delete the old data source first
               // removeDynamicDataSource(d.getCode());
                //Add new data source
                //addDynamicDataSource(sysDataSource,dbPassword);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.OK("Edited successfully!");
    }

    @Override
    public Result deleteDataSource(String id) {
        SysDataSource sysDataSource = getById(id);
        DataSourceCachePool.removeCache(sysDataSource.getCode());
        removeById(id);
        return Result.OK("Delete successfully!");
    }

    /**
     * Dynamically add data sources 【registermybatisdynamic data source】
     *
     * @param sysDataSource Add data source data object
     * @param dbPassword    unencrypted password
     */
    private void addDynamicDataSource(SysDataSource sysDataSource, String dbPassword) {
        DataSourceProperty dataSourceProperty = new DataSourceProperty();
        dataSourceProperty.setUrl(sysDataSource.getDbUrl());
        dataSourceProperty.setPassword(dbPassword);
        dataSourceProperty.setDriverClassName(sysDataSource.getDbDriver());
        dataSourceProperty.setUsername(sysDataSource.getDbUsername());
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        DataSource dataSource = dataSourceCreator.createDataSource(dataSourceProperty);
        try {
            ds.addDataSource(sysDataSource.getCode(), dataSource);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete data source
     * @param code
     */
    private void removeDynamicDataSource(String code) {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        ds.removeDataSource(code);
    }

    /**
     * Check if the data source encoding exists
     *
     * @param dbCode
     * @return
     */
    private long checkDbCode(String dbCode) {
        QueryWrapper<SysDataSource> qw = new QueryWrapper();
        qw.lambda().eq(true, SysDataSource::getCode, dbCode);
        return count(qw);
    }

}
