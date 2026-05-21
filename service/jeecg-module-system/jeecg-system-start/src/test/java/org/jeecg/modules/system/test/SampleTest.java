package org.jeecg.modules.system.test;

import jakarta.annotation.Resource;
import org.jeecg.JeecgSystemApplication;
import org.jeecg.modules.demo.mock.MockController;
import org.jeecg.modules.demo.test.entity.JeecgDemo;
import org.jeecg.modules.demo.test.mapper.JeecgDemoMapper;
import org.jeecg.modules.demo.test.service.IJeecgDemoService;
import org.jeecg.modules.system.service.ISysDataLogService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = JeecgSystemApplication.class)
public class SampleTest {

	@Resource
	private JeecgDemoMapper jeecgDemoMapper;
	@Resource
	private IJeecgDemoService jeecgDemoService;
	@Resource
	private ISysDataLogService sysDataLogService;
	@Resource
	private MockController mock;

	@Test
	public void testSelect() {
		System.out.println(("----- selectAll method test ------"));
		List<JeecgDemo> userList = jeecgDemoMapper.selectList(null);
		Assert.isTrue(15==userList.size(),"The result is not5strip");
		userList.forEach(System.out::println);
	}

	@Test
	public void testXmlSql() {
		System.out.println(("----- selectAll method test ------"));
		List<JeecgDemo> userList = jeecgDemoMapper.getDemoByName("Sandy12");
		userList.forEach(System.out::println);
	}

	/**
	 * test transaction
	 */
	@Test
	public void testTran() {
		jeecgDemoService.testTran();
	}

	/**
	 * Test data log added
	 */
	@Test
	public void testDataLogSave() {
		System.out.println(("----- datalog test ------"));
		String tableName = "jeecg_demo";
		String dataId = "4028ef81550c1a7901550c1cd6e70001";
		String dataContent = mock.sysDataLogJson();
		sysDataLogService.addDataLog(tableName, dataId, dataContent);
	}
}
