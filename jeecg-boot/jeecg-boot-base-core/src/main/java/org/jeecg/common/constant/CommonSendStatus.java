package org.jeecg.common.constant;

/**
 * 	System notification - Release status
 * @Author LeeShaoQing
 *
 */
public interface CommonSendStatus {

    /**
     * Unpublished
     */
    public static final String UNPUBLISHED_STATUS_0 = "0";

    /**
     * Published
     */
	public static final String PUBLISHED_STATUS_1 = "1";

    /**
     * Cancel
     */
	public static final String REVOKE_STATUS_2 = "2";

    /**
     * appEnd push session ID suffix
     */
	public static final String  APP_SESSION_SUFFIX = "_app";


	/**-----【Process related notification templatecode】------------------------------------------------------------*/
	/**Process reminder——System notification message template*/
	public static final String TZMB_BPM_CUIBAN = "bpm_cuiban";
	/**process copy——System notification message template*/
	public static final String TZMB_BPM_CC = "bpm_cc";
	/**Process reminder——Email notification message template*/
	public static final String TZMB_BPM_CUIBAN_EMAIL = "bpm_cuiban_email";
	/**Standard template—System message notification*/
	public static final String TZMB_SYS_TS_NOTE = "sys_ts_note";
	/**Process timeout reminder——System notification message template*/
	public static final String TZMB_BPM_CHAOSHI_TIP = "bpm_chaoshi_tip";
	/**-----【Process related notification templatecode】-----------------------------------------------------------*/

	/**
	 * System notification expansion parameters（for example：用于process copy和催办通知，Here additionally passes the routing parameters required for the process jump page.）
	 */
	public static final String MSG_ABSTRACT_JSON = "msg_abstract";
}
