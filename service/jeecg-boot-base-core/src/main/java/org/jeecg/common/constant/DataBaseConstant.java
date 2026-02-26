package org.jeecg.common.constant;
/**
 * Database context constants
 * @author: jeecg-boot
 */
public interface DataBaseConstant {

	/**
	 * Built-in system variable key list
	 */
	public static final String[] SYSTEM_KEYS = {
			DataBaseConstant.SYS_ORG_CODE, DataBaseConstant.SYS_ORG_CODE_TABLE, DataBaseConstant.SYS_MULTI_ORG_CODE,
			DataBaseConstant.SYS_MULTI_ORG_CODE_TABLE, DataBaseConstant.SYS_ORG_ID, DataBaseConstant.SYS_ORG_ID_TABLE,
			DataBaseConstant.SYS_ROLE_CODE, DataBaseConstant.SYS_ROLE_CODE_TABLE, DataBaseConstant.SYS_USER_CODE,
			DataBaseConstant.SYS_USER_CODE_TABLE, DataBaseConstant.SYS_USER_ID, DataBaseConstant.SYS_USER_ID_TABLE,
			DataBaseConstant.SYS_USER_NAME, DataBaseConstant.SYS_USER_NAME_TABLE, DataBaseConstant.SYS_DATE,
			DataBaseConstant.SYS_DATE_TABLE, DataBaseConstant.SYS_TIME, DataBaseConstant.SYS_TIME_TABLE,
			DataBaseConstant.SYS_BASE_PATH
	};
	
	//*********Database type****************************************

    /**MYSQLdatabase*/
	public static final String DB_TYPE_MYSQL = "MYSQL";

    /** ORACLE*/
	public static final String DB_TYPE_ORACLE = "ORACLE";

    /**达梦database*/
	public static final String DB_TYPE_DM = "DM";

    /**postgreSQL达梦database*/
	public static final String DB_TYPE_POSTGRESQL = "POSTGRESQL";
	
    /**人大金仓database*/
	public static final String DB_TYPE_KINGBASEES = "KINGBASEES";

    /**sqlserverdatabase*/
	public static final String DB_TYPE_SQLSERVER = "SQLSERVER";

    /**mariadb database*/
	public static final String DB_TYPE_MARIADB = "MARIADB";

    /**DB2 database*/
	public static final String DB_TYPE_DB2 = "DB2";

    /**HSQL database*/
	public static final String DB_TYPE_HSQL = "HSQL";

//	// Database type，correspond database_type dictionary
//	public static final String DB_TYPE_MYSQL_NUM = "1";
//	public static final String DB_TYPE_MYSQL7_NUM = "6";
//	public static final String DB_TYPE_ORACLE_NUM = "2";
//	public static final String DB_TYPE_SQLSERVER_NUM = "3";
//	public static final String DB_TYPE_POSTGRESQL_NUM = "4";
//	public static final String DB_TYPE_MARIADB_NUM = "5";

	//*********system context variables****************************************
	/**
	 * data-Affiliation code
	 */
	public static final String SYS_ORG_CODE = "sysOrgCode";
	/**
	 * data-Affiliation code
	 */
	public static final String SYS_ORG_CODE_TABLE = "sys_org_code";
	/**
	 * data-Affiliation code
	 */
	public static final String SYS_MULTI_ORG_CODE = "sysMultiOrgCode";
	/**
	 * data-Affiliation code
	 */
	public static final String SYS_MULTI_ORG_CODE_TABLE = "sys_multi_org_code";
	/**
	 * data-Affiliated institutionID
	 */
	public static final String SYS_ORG_ID = "sysOrgId";
	/**
	 * data-Affiliated institutionID
	 */
	public static final String SYS_ORG_ID_TABLE = "sys_org_id";
	/**
	 * data-Rolecode（Multiple commas separated）
	 */
	public static final String SYS_ROLE_CODE = "sysRoleCode";
	/**
	 * data-Rolecode（Multiple commas separated）
	 */
	public static final String SYS_ROLE_CODE_TABLE = "sys_role_code";
	/**
	 * data-System user code（correspondLogin user账号）
	 */
	public static final String SYS_USER_CODE = "sysUserCode";
	/**
	 * data-System user code（correspondLogin user账号）
	 */
	public static final String SYS_USER_CODE_TABLE = "sys_user_code";
	/**
	 * Login userID
	 */
	public static final String SYS_USER_ID = "sysUserId";
	/**
	 * Login userID
	 */
	public static final String SYS_USER_ID_TABLE = "sys_user_id";
	/**
	 * Login user真实姓名
	 */
	public static final String SYS_USER_NAME = "sysUserName";
	/**
	 * Login user真实姓名
	 */
	public static final String SYS_USER_NAME_TABLE = "sys_user_name";
	/**
	 * System date"yyyy-MM-dd"
	 */
	public static final String SYS_DATE = "sysDate";
	/**
	 * System date"yyyy-MM-dd"
	 */
	public static final String SYS_DATE_TABLE = "sys_date";
	/**
	 * system time"yyyy-MM-dd HH:mm"
	 */
	public static final String SYS_TIME = "sysTime";
	/**
	 * system time"yyyy-MM-dd HH:mm"
	 */
	public static final String SYS_TIME_TABLE = "sys_time";
	/**
	 * data-Affiliation code
	 */
	public static final String SYS_BASE_PATH = "sys_base_path";
	//*********system context variables****************************************
	
	
	//*********System table creation standard fields****************************************
	/**
	 * Creator login name
	 */
	public static final String CREATE_BY_TABLE = "create_by";
	/**
	 * Creator login name
	 */
	public static final String CREATE_BY = "createBy";
	/**
	 * Creation date and time
	 */
	public static final String CREATE_TIME_TABLE = "create_time";
	/**
	 * Creation date and time
	 */
	public static final String CREATE_TIME = "createTime";
	/**
	 * Update user login name
	 */
	public static final String UPDATE_BY_TABLE = "update_by";
	/**
	 * Update user login name
	 */
	public static final String UPDATE_BY = "updateBy";
	/**
	 * Update date time
	 */
	public static final String UPDATE_TIME = "updateTime";
	/**
	 * Update date time
	 */
	public static final String UPDATE_TIME_TABLE = "update_time";
	
	/**
	 * business process status
	 */
	public static final String BPM_STATUS = "bpmStatus";
	/**
	 * business process status
	 */
	public static final String BPM_STATUS_TABLE = "bpm_status";
	//*********System table creation standard fields****************************************

    /**
     * sqlstatement where
     */
    String SQL_WHERE = "where";

    /**
     * sqlstatement asc
     */
    String SQL_ASC = "asc";

    /**
     * sqlserverdatabase,There is a space in the middle
     */
    String DB_TYPE_SQL_SERVER_BLANK = "sql server";
}
