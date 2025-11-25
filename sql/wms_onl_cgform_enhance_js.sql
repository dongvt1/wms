-- MySQL dump 10.13  Distrib 8.0.44, for macos15 (arm64)
--
-- Host: 127.0.0.1    Database: wms
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `onl_cgform_enhance_js`
--

DROP TABLE IF EXISTS `onl_cgform_enhance_js`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `onl_cgform_enhance_js` (
  `ID` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'primary keyID',
  `CG_JS` longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT 'JSEnhancecontent',
  `CG_JS_TYPE` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'type',
  `CONTENT` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'Remark',
  `CGFORM_HEAD_ID` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'surfaceoneID',
  PRIMARY KEY (`ID`) USING BTREE,
  KEY `idx_ejs_cgform_head_id` (`CGFORM_HEAD_ID`) USING BTREE,
  KEY `idx_ejs_cg_js_type` (`CG_JS_TYPE`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `onl_cgform_enhance_js`
--

LOCK TABLES `onl_cgform_enhance_js` WRITE;
/*!40000 ALTER TABLE `onl_cgform_enhance_js` DISABLE KEYS */;
INSERT INTO `onl_cgform_enhance_js` VALUES ('0b326acbbc8e52c9c9ecdb19342fc3cf','show(){\n   console.log(\'form\',that)\n   //this.form.setFieldsValue({\"name\":\"namevalue\"})  \n  that.$nextTick(() => {\n           //ageyescorrespondsurfaceofField名\n            that.form.setFieldsValue({\"age\":\"999\"})\n          });\n}\n\nonlChange(){\n   return {\n     \n     sex(){\n        let value = event.value\n        //alert(\'trigger control\',value)\n        \n         //genderchange动，age归零\n        if(value!=null && value!=\"\"){   \n          let values = {\'age\':0}\n          that.triggleChangeValues(values)\n        }\n      }\n     \n    }\n }\n','form',NULL,'05a3a30dada7411c9109306aa4117068'),('274b5d741a0262d3411958f0c465c5f0','genereate_person_config(row){\nconsole.log(\'choose\',row)\nalert(row.name + \'，indivualpeople积pointConfigurationgenerate成功！\');\n}','list',NULL,'e2faf977fdaf4b25a524f58c2441a51c'),('2cbaf25f1edb620bea2d8de07f8233a1','air_china_post_materiel_item_onlChange(){\n    return {\n        wl_name(){\n           \n            let id = event.row.id\n            let cnum = event.row.num\n            let value = event.value\n            let targrt = event.target\n            let columnKey = event.column.key\n           let nval = 200*cnum\n           console.log(\'row\',event.row);\n           console.log(\'cnum\',cnum);\n           let otherValues = {\'jifen\': nval}\n              \n                that.triggleChangeValues(targrt,id,otherValues)\n\n        }\n    }\n}','form',NULL,'e67d26b610dd414c884c4dbb24e71ce3'),('32e7b7373abe0fb9c4dd608b4517f814','','form',NULL,'53a3e82b54b946c2b904f605875a275c'),('35d4ef464e5e8c87c9aa82ea89215fc1','','list',NULL,'e67d26b610dd414c884c4dbb24e71ce3'),('44cad4eec436328ed3cc134855f8d1d5',' onlChange(){\n   return {\n    name(that, event) {\n      that.executeMainFillRule()\n    }\n  }\n }','form',NULL,'4adec929a6594108bef5b35ee9966e9f'),('4569bc0e6126d2b8a0e0c69b9a47e8db','','list',NULL,'56efb74326e74064b60933f6f8af30ea'),('5e9ccc1e2b977bdd5a873a6bd6311290','jihuo_hook(){\n  console.log(\'whenforward选middleOKofid\', this);\n  console.log(\'this.abc\', this.abc);\n   import {useMessage} from \"@/hooks/useMessage\"\n        const {createMessage} = useMessage()\n        function sayHi () {\n            createMessage.success(\"这yescode里of提示：hello world!\")\n        }\n        sayHi();\n}\n\n\n\ntest1(){\n  this.openCustomModal({\n    title: \'test自definition弹框1\',\n    width: 800,\n     hide: [\'cc\']\n  });\n}\n\nsetup(){\n    console.log(\"Enterform: \",this)\n    import { defineComponent, computed, CSSProperties, unref, ref, watchEffect, watch, PropType } from \'vue\';\n  \n  watch(name,(newValue,oldValue)=>{\n    console.log(\"新valueyes\"+newValue, \"旧址yes\"+oldValue);\n  })\n  \n}','list',NULL,'9ab817fd4c2e4e7ba6652c4fa46af389'),('6dd82d8eac166627302230a809233481','ces_order_goods_onlChange(){\n    return {\n        num(){\n           \n            let id = event.row.id\n            let num = event.row.num\n            let price = event.row.price\n\n            let targrt = event.target\n            \n            let nval = price*num\n            console.log(\'row\',event.row);\n            console.log(\'num\',num);\n            console.log(\'that\',that);\n            let otherValues = {\'zong_price\': nval}\n              \n            that.triggleChangeValues(otherValues,id,targrt)\n   \n\n        }\n    }\n}','form',NULL,'56efb74326e74064b60933f6f8af30ea'),('73ff4666e0cf5c2070263345e1e11835','one(){\n  console.log(\'whenforward选middleOKofid\', this.selectedRowKeys);\n}\n\nbeforeDelete(row){\n  return new Promise(resolve=>{\n    console.log(\'deletedata之forward看看data\', row);\n    resolve();\n  });\n}','list',NULL,'553a4172fde446419cb602dc70f9ee67'),('85e7acd772c8ec322b97a1fd548007e0','','form',NULL,'09fd28e4b7184c1a9668496a5c496450'),('8b76f282ddc81ce99a129e90fdd977ce','','form',NULL,'86bf17839a904636b7ed96201b2fa6ea'),('90394fbc3d48978cc0937bc56f2d5370','','list',NULL,'deea5a8ec619460c9245ba85dbc59e80'),('a0ca1d842f138ba2cda00bc44e95edd9','loaded(){\n  this.$nextTick(()=>{\n    let text = \'testjsEnhanceset updefaultvalue\';\n    if(this.isUpdate.value === true){\n      text = \'testjsEnhance修changesurfaceonevalue\';\n    }\n    this.setFieldsValue({\n      name: text\n    })\n  })\n}\n\n onlChange(){\n   return {\n    name(){\n      let value = event.value\n      let values = {\'dhwb\': \'我ofnameyes：\'+ value }\n      this.triggleChangeValues(values)\n    }\n  }\n }\n\nbeforeSubmit(row){\n	return new Promise((resolve, reject)=>{\n    //此处模拟wait待hour间，可能need发起ask\n    setTimeout(()=>{\n      if(row.name == \'test\'){\n        // whencertainindivualField不Full足Requireofhour候可byreject \n        reject(\'不能提交testdata\');\n      }else{\n        resolve();\n      }\n    },3000)\n  })\n}','form',NULL,'553a4172fde446419cb602dc70f9ee67'),('ae9cf52fbe13cc718de2de6e1b3d6792','','list',NULL,'18f064d1ef424c93ba7a16148851664f'),('beec235f0b2d633ff3a6c395affdf59d','','list',NULL,'4adec929a6594108bef5b35ee9966e9f'),('c5ac9a2b2fd92ef68274f630b8aec78a','tjbpm(row){\nconst { createMessage, notification ,createConfirm, createConfirmSync, createSuccessModal, createErrorModal, createInfoModal, createWarningModal } = useMessage();\n  alert(\'提交process\')\n  createMessage.warn(\'Click提交process\');\n  console.log(\'row\',row)\n}\n\nbt1(){\n   console.log(\'that.table.selectionRows\',this.selectedRowKeys)\n   console.log(\'that.table.selectedRowKeys\',this.selectedRows)\n   alert(\'activationalldata\')\n}','list',NULL,'05a3a30dada7411c9109306aa4117068'),('d7ddb7aa407f6deed75aac11f0a25f0e','222','list',NULL,'09fd28e4b7184c1a9668496a5c496450'),('de79fe5530e19ccb71b750900892a3a4','setup(){\n    console.log(\"Enterform: \",this)\n    import { defineComponent, computed, CSSProperties, unref, ref, watchEffect, watch, PropType } from \'vue\';\n  \n  watch(name,(newValue,oldValue)=>{\n    console.log(\"新valueyes\"+newValue, \"旧址yes\"+oldValue);\n  })\n  \n}','form',NULL,'9ab817fd4c2e4e7ba6652c4fa46af389'),('f6f8f230566d09d4b66338955ffb5691','','form',NULL,'18f064d1ef424c93ba7a16148851664f'),('fd711738f58d5481ca0ce9bc3a415223','','list',NULL,'86bf17839a904636b7ed96201b2fa6ea');
/*!40000 ALTER TABLE `onl_cgform_enhance_js` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-25 17:35:33
