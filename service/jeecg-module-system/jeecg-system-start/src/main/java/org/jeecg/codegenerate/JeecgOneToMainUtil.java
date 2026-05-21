package org.jeecg.codegenerate;

import java.util.ArrayList;
import java.util.List;

import org.jeecgframework.codegenerate.generate.impl.CodeGenerateOneToMany;
import org.jeecgframework.codegenerate.generate.pojo.onetomany.MainTableVo;
import org.jeecgframework.codegenerate.generate.pojo.onetomany.SubTableVo;

/**
 * Code generator entry【one to many】
 * 
 * 【 GUIMode function is weaker，Please give priority toOnlinecode generation 】
 * @Author Zhang Daihao
 * @site www.jeecg.com
 * 
 */
public class JeecgOneToMainUtil {

	/**
	 * one to many(Father and son table)data model，Generate method
	 * @param args
	 */
	public static void main(String[] args) {
		//first step：Set main table configuration
		MainTableVo mainTable = new MainTableVo();
        //table name
		mainTable.setTableName("jeecg_order_main");
        //Entity name
		mainTable.setEntityName("GuiTestOrderMain");
        //package name
		mainTable.setEntityPackage("gui");
        //describe
		mainTable.setFtlDescription("GUIOrder management");
		
		//Step 2：Set subtable collection configuration
		List<SubTableVo> subTables = new ArrayList<SubTableVo>();
		//[1].Sublist one
		SubTableVo po = new SubTableVo();
        //table name
		po.setTableName("jeecg_order_customer");
        //Entity name
		po.setEntityName("GuiTestOrderCustom");
        //package name
		po.setEntityPackage("gui");
        //describe
		po.setFtlDescription("Customer details");
		//Subtable foreign key parameter configuration
		/*illustrate: 
		 * a) Subtable references the primary key of the main tableIDas foreign key，Foreign key fields must end with_IDending;
		 * b) Foreign key field names of main table and subtable，must be the same（except primary keyIDoutside）;
		 * c) 多个outside键字段，separated by commas;
		*/
		po.setForeignKeys(new String[]{"order_id"});
		subTables.add(po);
		//[2].Sublist 2
		SubTableVo po2 = new SubTableVo();
        //table name
		po2.setTableName("jeecg_order_ticket");
        //Entity name
		po2.setEntityName("GuiTestOrderTicket");
        //package name
		po2.setEntityPackage("gui");
        //describe
		po2.setFtlDescription("Product details");
		//Subtable foreign key parameter configuration
		/*illustrate: 
		 * a) Subtable references the primary key of the main tableIDas foreign key，Foreign key fields must end with_IDending;
		 * b) Foreign key field names of main table and subtable，must be the same（except primary keyIDoutside）;
		 * c) 多个outside键字段，separated by commas;
		*/
		po2.setForeignKeys(new String[]{"order_id"});
		subTables.add(po2);
		mainTable.setSubTables(subTables);
		
		//Step 3：one to many(Father and son table)data model,code generation
		try {
			new CodeGenerateOneToMany(mainTable,subTables).generateCodeFile(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
