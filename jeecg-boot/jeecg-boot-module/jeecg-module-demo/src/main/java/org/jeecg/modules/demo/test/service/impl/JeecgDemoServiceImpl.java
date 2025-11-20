package org.jeecg.modules.demo.test.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.demo.test.entity.JeecgDemo;
import org.jeecg.modules.demo.test.mapper.JeecgDemoMapper;
import org.jeecg.modules.demo.test.service.IJeecgDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: jeecg testdemo
 * @Author: jeecg-boot
 * @Date:  2018-12-29
 * @Version: V1.0
 */
@Service
public class JeecgDemoServiceImpl extends ServiceImpl<JeecgDemoMapper, JeecgDemo> implements IJeecgDemoService {
	@Autowired
	JeecgDemoMapper jeecgDemoMapper;
	
	/**
	 * transaction control inservicelevel
	 * Add annotations：@Transactional，The declared method is an independent transaction（There is an abnormalityDBRoll back all operations）
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void testTran() {
		JeecgDemo pp = new JeecgDemo();
		pp.setAge(1111);
		pp.setName("test事务  little white rabbit 1");
		jeecgDemoMapper.insert(pp);
		
		JeecgDemo pp2 = new JeecgDemo();
		pp2.setAge(2222);
		pp2.setName("test事务  little white rabbit 2");
		jeecgDemoMapper.insert(pp2);
        //Custom exception
		Integer.parseInt("hello");
		
		JeecgDemo pp3 = new JeecgDemo();
		pp3.setAge(3333);
		pp3.setName("test事务  little white rabbit 3");
		jeecgDemoMapper.insert(pp3);
		return ;
	}


	/**
	 * 缓存注解test： redis
	 */
	@Override
	@Cacheable(cacheNames = CacheConstant.TEST_DEMO_CACHE, key = "#id")
	public JeecgDemo getByIdCacheable(String id) {
		JeecgDemo t = jeecgDemoMapper.selectById(id);
		System.err.println("---unread cache，Read database---");
		System.err.println(t);
		return t;
	}


	/**
	 * @CacheableCustomizeTTL：#60（The unit is seconds，Currently only this format is supported）
	 * via annotation，Specify cache validity period60Second
	 * 
	 * Reference blog：https://www.cnblogs.com/h2285409/p/18324396
	 */
	@Override
	@Cacheable(cacheNames = "ceshi:redis:ttl#60", key = "#id")
	public JeecgDemo getByIdCacheableTTL(String id) {
		JeecgDemo t = jeecgDemoMapper.selectById(id);
		System.err.println("---unread cache，Read database---");
		System.err.println(t);
		return t;
	}


	@Override
	public IPage<JeecgDemo> queryListWithPermission(int pageSize,int pageNo) {
		Page<JeecgDemo> page = new Page<>(pageNo, pageSize);
		//Programmatically，Get the data permission rules of the current requestSQLfragment
		String sql = QueryGenerator.installAuthJdbc(JeecgDemo.class);
		return this.baseMapper.queryListWithPermission(page, sql);
	}

	@Override
	public String getExportFields() {
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		//Example of exporting permission configuration columns
		//1.The configuration prefix is ​​consistent with the column prefix configured in the menu
		List<String> noAuthList = new ArrayList<>();
		List<String> exportFieldsList = new ArrayList<>();
		String permsPrefix = "testdemo:";
		//Query the valid fields of the configuration menu
		List<String> allAuth = this.jeecgDemoMapper.queryAllAuth(permsPrefix);
		//Query authorized fields
		List<String> userAuth = this.jeecgDemoMapper.queryUserAuth(sysUser.getId(),permsPrefix);
		//List unauthorized fields
		for(String perms : allAuth){
			if(!userAuth.contains(perms)){
				noAuthList.add(perms.substring(permsPrefix.length()));
			}
		}
		//Comparison between fields in entity classes and unauthorized fields，List fields to be exported
		Field[] fileds = JeecgDemo.class.getDeclaredFields();
		List<Field> list = new ArrayList(Arrays.asList(fileds));
		for(Field field : list){
			if(!noAuthList.contains(field.getName())){
				exportFieldsList.add(field.getName());
			}
		}
		return exportFieldsList != null && exportFieldsList.size()>0 ? String.join(",", exportFieldsList) : "";
	}

	@Override
	public List<String> getCreateByList() {
		return jeecgDemoMapper.getCreateByList();
	}

}
