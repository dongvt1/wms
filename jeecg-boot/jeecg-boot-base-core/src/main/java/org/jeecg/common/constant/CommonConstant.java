package org.jeecg.common.constant;

/**
 * @Description: Universal constant
 * @author: jeecg-boot
 */
public interface CommonConstant {

	/**
	 * normal state
	 */
	Integer STATUS_NORMAL = 0;

	/**
	 * disabled state
	 */
	Integer STATUS_DISABLE = -1;

	/**
	 * delete flag
	 */
	Integer DEL_FLAG_1 = 1;

	/**
	 * not deleted
	 */
	Integer DEL_FLAG_0 = 0;

	/**
	 * System log type： Log in
	 */
	int LOG_TYPE_1 = 1;
	
	/**
	 * System log type： operate
	 */
	int LOG_TYPE_2 = 2;

    /**
     * System log type： 租户operate日志
     */
    int LOG_TYPE_3 = 3;

    /**
     * System log type： abnormal
     */
    int LOG_TYPE_4 = 4;

	/**
	 * operate日志类型： Query
	 */
	int OPERATE_TYPE_1 = 1;
	
	/**
	 * operate日志类型： Add to
	 */
	int OPERATE_TYPE_2 = 2;
	
	/**
	 * operate日志类型： renew
	 */
	int OPERATE_TYPE_3 = 3;
	
	/**
	 * operate日志类型： delete
	 */
	int OPERATE_TYPE_4 = 4;
	
	/**
	 * operate日志类型： Pour
	 */
	int OPERATE_TYPE_5 = 5;
	
	/**
	 * operate日志类型： Export
	 */
	int OPERATE_TYPE_6 = 6;
	
	
	/** {@code 500 Server Error} (HTTP/1.0 - RFC 1945) */
    Integer SC_INTERNAL_SERVER_ERROR_500 = 500;
    /** {@code 404 Not Found} (HTTP/1.0 - RFC 1945) */
    Integer SC_INTERNAL_NOT_FOUND_404 = 404;
    /** {@code 200 OK} (HTTP/1.0 - RFC 1945) */
    Integer SC_OK_200 = 200;
    
    /**Access permission authentication failed 510*/
    Integer SC_JEECG_NO_AUTHZ=510;

    /** Log inuse户Shiropermission cacheKEYprefix */
    public static String PREFIX_USER_SHIRO_CACHE  = "shiro:cache:org.jeecg.config.shiro.ShiroRealm.authorizationCache:";
    /** Log inuse户TokenToken cacheKEYprefix */
    String PREFIX_USER_TOKEN  = "prefix_user_token:";
//    /** TokenCache time：3600Second is one hour */
//    int  TOKEN_EXPIRE_TIME  = 3600;

    /** Log in二维码 */
    String  LOGIN_QRCODE_PRE  = "QRCODELOGIN:";
    String  LOGIN_QRCODE  = "LQ:";
    /** Log in二维码token */
    String  LOGIN_QRCODE_TOKEN  = "LQT:";


    /**
     *  0：First level menu
     */
    Integer MENU_TYPE_0  = 0;
   /**
    *  1：submenu 
    */
    Integer MENU_TYPE_1  = 1;
    /**
     *  2：Button permissions
     */
    Integer MENU_TYPE_2  = 2;

    /**Notification object type（USER:Specify user，ALL:All users）*/
    String MSG_TYPE_UESR  = "USER";
    String MSG_TYPE_ALL  = "ALL";
    
    /**Release status（0Unpublished，1Published，2Revoked）*/
    String NO_SEND  = "0";
    String HAS_SEND  = "1";
    String HAS_CANCLE  = "2";
    
    /**reading status（0unread，1Read）*/
    Integer HAS_READ_FLAG  = 1;
    Integer NO_READ_FLAG  = 0;
    
    /**priority（LLow，Mmiddle，Hhigh）*/
    String PRIORITY_L  = "L";
    String PRIORITY_M  = "M";
    String PRIORITY_H  = "H";
    
    /**
     * SMS template method  0 .Log in模板、1.Registration template、2.Forgot password template
     */
    String SMS_TPL_TYPE_0  = "0";
    String SMS_TPL_TYPE_1  = "1";
    String SMS_TPL_TYPE_2  = "2";
    
    /**
     * state(0invalid1efficient)
     */
    String STATUS_0 = "0";
    String STATUS_1 = "1";
    Integer STATUS_0_INT = 0;
    Integer STATUS_1_INT = 1;

    /**
     * Sync workflow engine1synchronous0不synchronous
     */
    Integer ACT_SYNC_1 = 1;
    Integer ACT_SYNC_0 = 0;

    /**
     * Message type1:Notices and Announcements2:System messages
     */
    String MSG_CATEGORY_1 = "1";
    String MSG_CATEGORY_2 = "2";
    
    /**
     * Whether to configure the data permissions of the menu 1yes0no
     */
    Integer RULE_FLAG_0 = 0;
    Integer RULE_FLAG_1 = 1;

    /**
     * yesnouse户已被freeze 1normal(thaw) 2freeze 3Resign
     */
    Integer USER_UNFREEZE = 1;
    Integer USER_FREEZE = 2;
    Integer USER_QUIT = 3;
    
    /**Dictionary translation text suffix*/
    String DICT_TEXT_SUFFIX = "_dictText";
    /**Dictionary translation color suffix*/
    String DICT_COLOR_SUFFIX = "_dictColor";

    /**
     * Form Designer Main Table Type
     */
    Integer DESIGN_FORM_TYPE_MAIN = 1;

    /**
     * Form Designer Subform Table Type
     */
    Integer DESIGN_FORM_TYPE_SUB = 2;

    /**
     * form designerURLAuthorization passed
     */
    Integer DESIGN_FORM_URL_STATUS_PASSED = 1;

    /**
     * form designerURLAuthorization failed
     */
    Integer DESIGN_FORM_URL_STATUS_NOT_PASSED = 2;

    /**
     * form designer新增 Flag
     */
    String DESIGN_FORM_URL_TYPE_ADD = "add";
    /**
     * form designer修改 Flag
     */
    String DESIGN_FORM_URL_TYPE_EDIT = "edit";
    /**
     * form designer详情 Flag
     */
    String DESIGN_FORM_URL_TYPE_DETAIL = "detail";
    /**
     * form designer复use数据 Flag
     */
    String DESIGN_FORM_URL_TYPE_REUSE = "reuse";
    /**
     * form designer编辑 Flag （Deprecated）
     */
    String DESIGN_FORM_URL_TYPE_VIEW = "view";

    /**
     * onlineParameter value setting（yes：Y, no：N）
     */
    String ONLINE_PARAM_VAL_IS_TURE = "Y";
    String ONLINE_PARAM_VAL_IS_FALSE = "N";

    /**
     * File upload type（local：local，Minio：minio，Alibaba Cloud：alioss）
     */
    String UPLOAD_TYPE_LOCAL = "local";
    String UPLOAD_TYPE_MINIO = "minio";
    String UPLOAD_TYPE_OSS = "alioss";

    /**
     * Document upload custom bucket name
     */
    String UPLOAD_CUSTOM_BUCKET = "eoafile";
    /**
     * Document upload custom path
     */
    String UPLOAD_CUSTOM_PATH = "eoafile";
    /**
     * document外链接efficient天数
     */
    Integer UPLOAD_EFFECTIVE_DAYS = 1;

    /**
     * Employee status （1:Ordinary employees  2:Superior）
     */
    Integer USER_IDENTITY_1 = 1;
    Integer USER_IDENTITY_2 = 2;

    /** sys_user surface username unique key index */
    String SQL_INDEX_UNIQ_SYS_USER_USERNAME = "uniq_sys_user_username";
    /** sys_user surface work_no unique key index */
    String SQL_INDEX_UNIQ_SYS_USER_WORK_NO = "uniq_sys_user_work_no";
    /** sys_user surface phone unique key index */
    String SQL_INDEX_UNIQ_SYS_USER_PHONE = "uniq_sys_user_phone";
    /** Dameng database upgrade tips。违反surface[SYS_USER]uniqueness constraint */
    String SQL_INDEX_UNIQ_SYS_USER = "uniqueness constraint";

    /** sys_user surface email unique key index */
    String SQL_INDEX_UNIQ_SYS_USER_EMAIL = "uniq_sys_user_email";
    /** sys_quartz_job surface job_class_name unique key index */
    String SQL_INDEX_UNIQ_JOB_CLASS_NAME = "uniq_job_class_name";
    /** sys_position surface code unique key index */
    String SQL_INDEX_UNIQ_CODE = "uniq_code";
    /** sys_role surface code unique key index */
    String SQL_INDEX_UNIQ_SYS_ROLE_CODE = "uniq_sys_role_role_code";
    /** sys_depart surface code unique key index */
    String SQL_INDEX_UNIQ_DEPART_ORG_CODE = "uniq_depart_org_code";
    /** sys_category surface code unique key index */
    String SQL_INDEX_UNIQ_CATEGORY_CODE = "idx_sc_code";
    /**
     * online chat yesno为默认分组
     */
    String IM_DEFAULT_GROUP = "1";
    /**
     * online chat Picture file saving path
     */
    String IM_UPLOAD_CUSTOM_PATH = "biz/user_imgs";
    /**
     * online chat use户state
     */
    String IM_STATUS_ONLINE = "online";

    /**
     * online chat SOCKETMessage type
     */
    String IM_SOCKET_TYPE = "chatMessage";

    /**
     * online chat yesno开启默认Add to好友 1yes 0no
     */
    String IM_DEFAULT_ADD_FRIEND = "1";

    /**
     * online chat use户好友缓存prefix
     */
    String IM_PREFIX_USER_FRIEND_CACHE = "sys:cache:im:im_prefix_user_friend_";
    /**
     * Cache usersidRelationship with username
     */
    String SYS_USER_ID_MAPPING_CACHE = "sys:cache:user:id_mapping";

    /**
     * System role administrator code
     */
    String SYS_ROLE_ADMIN = "admin";

    /**
     * 考勤补卡业务state （1：agree  2：不agree）
     */
    String SIGN_PATCH_BIZ_STATUS_1 = "1";
    String SIGN_PATCH_BIZ_STATUS_2 = "2";

    /**
     * 公文Document upload custom path
     */
    String UPLOAD_CUSTOM_PATH_OFFICIAL = "officialdoc";
     /**
     * Official document download custom path
     */
    String DOWNLOAD_CUSTOM_PATH_OFFICIAL = "officaldown";

    /**
     * WPSStored value category(1 codeDocument number 2 text（WPS模板还yes公文发文模板）)
     */
    String WPS_TYPE_1="1";
    String WPS_TYPE_2="2";


    /**===============================================================================================*/
    /**
     * ::very important::
     * Notice：If these four constant values ​​are modified，Need and jeecg-boot-starter/jeecg-boot-common/org.jeecg.config.FeignConfig 类middle的value保持一致。
     */
    String X_ACCESS_TOKEN = "X-Access-Token";
    String X_SIGN = "X-Sign";
    String X_TIMESTAMP = "X-TIMESTAMP";
    /** Tenant request header renamed：X-Tenant-Id */
    String TENANT_ID = "X-Tenant-Id";
    /** Simplified flow interface request header，Used to exclude unsupported control fields  */
    String X_MiniFlowExclusionFieldMode = "X-Miniflowexclusionfieldmode";
    /**===============================================================================================*/

    String TOKEN_IS_INVALID_MSG = "TokenInvalid，请重新Log in!";
    String X_FORWARDED_SCHEME = "X-Forwarded-Scheme";


    /**
     * Microservice reads configuration file properties Service address
     */
    String CLOUD_SERVER_KEY = "spring.cloud.nacos.discovery.server-addr";

    /**
     * third partyLog in Verify password/Create user 都需要设置一个operate码 Prevent malicious calls
     */
    String THIRD_LOGIN_CODE = "third_login_code";

    /**
     * third partyAPPsynchronous方向：local --> third partyAPP
     */
    String THIRD_SYNC_TO_APP = "SYNC_TO_APP";
    /**
     * third partyAPPsynchronous方向：third partyAPP --> local
     */
    String THIRD_SYNC_TO_LOCAL = "SYNC_TO_LOCAL";

    /** 系统通告消息state：0=Unpublished */
    String ANNOUNCEMENT_SEND_STATUS_0 = "0";
    /** 系统通告消息state：1=Published */
    String ANNOUNCEMENT_SEND_STATUS_1 = "1";
    /** 系统通告消息state：2=Revoked */
    String ANNOUNCEMENT_SEND_STATUS_2 = "2";

    /**ONLINE 报surface权限use fromrequestmiddle获取地址栏后的参数*/
    String ONL_REP_URL_PARAM_STR="onlRepUrlParamStr";

    /**POSTask*/
    String HTTP_POST = "POST";

    /**PUTask*/
    String HTTP_PUT = "PUT";

    /**PATCHask*/
    String HTTP_PATCH = "PATCH";

    /**unknown*/
    String UNKNOWN = "unknown";

    /**stringhttp*/
    String STR_HTTP = "http";

    /**String null value of type*/
    String STRING_NULL = "null";

    /**front endvue3VersionHeaderParameter name*/
    String VERSION="X-Version";

    String VERSION_V3 = "v3";

    /**存储在线程变量里的动态surface名*/
    String DYNAMIC_TABLE_NAME="DYNAMIC_TABLE_NAME";
    /**
     * http:// httpprotocol
     */
    String HTTP_PROTOCOL = "http://";

    /**
     * https:// httpsprotocol
     */
    String HTTPS_PROTOCOL = "https://";
    
    /** 部门surface唯一key，id */
    String DEPART_KEY_ID = "id";
    /** 部门surface唯一key，orgCode */
    String DEPART_KEY_ORG_CODE = "orgCode";

    /**======【Message push related】==============================================================================*/
    /**
     * send message will send some information tomap
     */
    String NOTICE_MSG_SUMMARY = "NOTICE_MSG_SUMMARY";

    /**
     * send message will deliver a businessIDarrivemap
     */
    String NOTICE_MSG_BUS_ID = "NOTICE_MSG_BUS_ID";

   /**
    * send message Message business type
    */
   String NOTICE_MSG_BUS_TYPE = "NOTICE_MSG_BUS_TYPE";

    /**
     * notification type，used to distinguish sources file Knowledge flow process plan schedule system System messages 
     */
    String NOTICE_TYPE = "noticeType";

    /**
     * 邮箱消息middle地址Log in时地址后携带的token,Need to be replaced with realtokenvalue
     */
    String LOGIN_TOKEN = "{LOGIN_TOKEN}";

    /**
     * 模板消息middle The corresponding jump addresskey
     */
    String MSG_HREF_URL = "url";

    /**
     * sys_data_logsurface的类型 Log data used to distinguish comment areas
     */
    String DATA_LOG_TYPE_COMMENT = "comment";

    /**
     * sys_data_logsurface的类型 Compare old data Types are set tojson
     */
    String DATA_LOG_TYPE_JSON = "json";

    /** Message template：markdown */
    String MSG_TEMPLATE_TYPE_MD = "5";
    /**========【Message push related】==========================================================================*/

    /**
     * SMS verification coderedis-key的prefix
     */
    String PHONE_REDIS_KEY_PRE = "phone_msg";

    /**
     * yesdocument夹
     */
    String IT_IS_FOLDER = "1";

    /**
     * File owner
     */
    String FILE_OWNER = "owner";

    /**
     * file manager
     */
    String FILE_ADMIN = "admin";

    /**
     * Only edit allowed
     */
    String FILE_EDITABLE = "editable";

    /**
     * document read only
     */
    String FILE_READONLY = "readonly";

    /**
     * Log in失败，Used to record the number of failureskey
     */
    String LOGIN_FAIL = "LOGIN_FAIL_";

    /**
     * Onboarding events
     */
    Integer BPM_USER_EVENT_ADD = 1;

   /**
    * Resign事件
    */
    Integer BPM_USER_EVENT_LEVEL = 2;

   /**
    * use户租户state(normal/Has been reviewed)
    */
   String USER_TENANT_NORMAL = "1";

   /**
    * use户租户state(Resign)
    */
   String USER_TENANT_QUIT = "2";

   /**
    * use户租户state(审核middle)
    */
   String USER_TENANT_UNDER_REVIEW = "3";
   
   /**
    * use户租户state(reject)
    */
   String USER_TENANT_REFUSE = "4";

   /**
    * use户租户state(invite)
    */
   String USER_TENANT_INVITE = "5";
   
   /**
    * 不yes叶子节点
    */
   Integer NOT_LEAF = 0;

   /**
    * yes叶子节点
    */
   Integer IS_LEAF = 1;

   /**
    * DingTalk
    */
   String DINGTALK = "DINGTALK";

   /**
    * Enterprise WeChat
    */
   String WECHAT_ENTERPRISE = "WECHAT_ENTERPRISE";

  /**
   * System default tenantid 0
   */
  Integer TENANT_ID_DEFAULT_VALUE = 0;

 /**
  * 【low-appuse】 应use级别的复制
  */
 String COPY_LEVEL_APP = "app";

 /**
  * 【low-appuse】 Menu level duplication
  */
 String COPY_LEVEL_MENU = "menu";


 /**
  * 【low-appuse】 应use备份
  */
 String COPY_LEVEL_BAK = "backup";

 /**
  * 【low-appuse】 from备份还原
  */
 String COPY_LEVEL_COVER = "cover";

 /** 【QQYUN-6034】关联字段变更历史value，Cache for half an hour */
 String CACHE_REL_FIELD_OLD_VAL = "sys:cache:desform:relFieldOldVal:";

    /**
     * sort type：Ascending order
     */
    String ORDER_TYPE_ASC = "ASC";
    /**
     * sort type：descending order
     */
    String ORDER_TYPE_DESC = "DESC";


   //update-begin---author:scott ---date:2023-09-10  for：积木报surface常量----
   /**
    * 报surface允许设计开发的角色
    */
   public static String[] allowDevRoles = new String[]{"lowdeveloper", "admin"};
   /**
    * 【对应积木报surface的常量】
    * Data isolation mode： Isolate by creator
    */
   public static final String SAAS_MODE_CREATED = "created";
   /**
    * 【对应积木报surface的常量】
    * Data isolation mode： Isolate by tenant
    */
   public static final String SAAS_MODE_TENANT = "tenant";
   //update-end---author:scott ---date::2023-09-10  for：积木报surface常量----
 
   //update-begin---author:wangshuai---date:2024-04-07---for:Modify mobile phone number constant---
   /**
    * Modify mobile phone numberSMS verification coderedis-key的prefix
    */
   String CHANGE_PHONE_REDIS_KEY_PRE = "sys:cache:phone:change_phone_msg:";

  /**
   * 手机号SMS verification coderedis-key的prefix
   */
   String LOG_OFF_PHONE_REDIS_KEY_PRE = "sys:cache:phone:qqy_log_off_user_msg:";

    /**
     * Cache users最后一次收arrive消息通知的时间 KEY
     */
   String CACHE_KEY_USER_LAST_ANNOUNT_TIME_1HOUR = "sys:cache:userinfo:user_last_annount_time::%s";
   
   /**
    * Verify original mobile phone number
    */
   String VERIFY_ORIGINAL_PHONE = "verifyOriginalPhone";

   /**
    * Modify mobile phone number
    */
   String UPDATE_PHONE = "updatePhone";
   //update-end---author:wangshuai---date:2024-04-07---for:Modify mobile phone number constant---
    
   /**
    * Modify mobile phone number验证码ask次数超出
    */
   Integer PHONE_SMS_FAIL_CODE = 40002;


    /**
     * Customized home page association relationship(ROLE:surface示角色 USER:surface示use户 DEFAULT:Default homepage)
     *
     */
    String HOME_RELATION_ROLE = "ROLE";
    String HOME_RELATION_USER = "USER";
    String HOME_RELATION_DEFAULT = "DEFAULT";

    /**
     * yesno置顶(0no 1yes)
     */
    Integer IZ_TOP_1 = 1;
    Integer IZ_TOP_0 = 0;
    
    
    //关注process缓存prefix
    String FLOW_FOCUS_NOTICE_PREFIX = "flow:runtimeData:focus:notice:";
    //任务缓办时间缓存prefix
    String FLOW_TASK_DELAY_PREFIX = "flow:runtimeData:task:delay:";
    /**
     * use户acting类型：Resign：quit acting：agent
     */
    String USER_AGENT_TYPE_QUIT = "quit";
    String USER_AGENT_TYPE_AGENT = "agent";
    /**
     * 督办process首节点任务taskKey
     */
    String SUPERVISE_FIRST_TASK_KEY = "Task_1bhxpt0";

    /**
     * wps模板预览数据缓存prefix
     */
    String EOA_WPS_TEMPLATE_VIEW_DATA ="eoa:wps:templateViewData:";

    /**
     * wps模板预览Version号缓存prefix
     */
    String EOA_WPS_TEMPLATE_VIEW_VERSION ="eoa:wps:templateViewVersion:";
    /**
     * form designeroaAdd new field
     * x_oa_timeout_date:Overdue time
     * x_oa_archive_status:归档state
     */
    String X_OA_TIMEOUT_DATE ="x_oa_timeout_date";
    String X_OA_ARCHIVE_STATUS ="x_oa_archive_status";
    /**
     * processstate
     * To be submitted: 1
     * 处理middle: 2
     * Completed: 3
     * voided: 4
     * Pending: 5
     */
    String BPM_STATUS_1 ="1";
    String BPM_STATUS_2 ="2";
    String BPM_STATUS_3 ="3";
    String BPM_STATUS_4 ="4";
    String BPM_STATUS_5 ="5";

    /**
     * Default tenant product package
     */
    String TENANT_PACK_DEFAULT = "default";

    /**
     * Department nameredisKey(full path)
     */
    String DEPART_NAME_REDIS_KEY_PRE = "sys:cache:departPathName:";
}
