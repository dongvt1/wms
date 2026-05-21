package org.jeecg.modules.system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: User Announcement Reading Mark Sheet
 * @Author: jeecg-boot
 * @Date:  2019-02-21
 * @Version: V1.0
 */
@Data
public class AnnouncementSendModel implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
	private java.lang.String id;
	/**noticeid*/
	private java.lang.String anntId;
	/**userid*/
	private java.lang.String userId;
	/**title*/
	private java.lang.String titile;
	/**content*/
	private java.lang.String msgContent;
	/**Posted by*/
	private java.lang.String sender;
	/**priority（LLow，Mmiddle，Hhigh）*/
	private java.lang.String priority;
	/**reading status*/
	private java.lang.Integer readFlag;
	/**Release time*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date sendTime;
	/**Number of pages*/
	private java.lang.Integer pageNo;
	/**size*/
	private java.lang.Integer pageSize;
    /**
     * Message type1:Notices and Announcements2:System messages
     */
    private java.lang.String msgCategory;
	/**
	 * businessid
	 */
	private java.lang.String busId;
	/**
	 * business类型
	 */
	private java.lang.String busType;
	/**
	 * Open method components：component routing：url
	 */
	private java.lang.String openType;
	/**
	 * components/routing address
	 */
	private java.lang.String openPage;

	/**
	 * business类型查询（0.Nobpmbusiness）
	 */
	private java.lang.String bizSource;

	/**
	 * summary
	 */
	private java.lang.String msgAbstract;

	/**
	 * Release start date
	 */
	private java.lang.String sendTimeBegin;
	
	/**
	 * Release end date
	 */
	private java.lang.String sendTimeEnd;
	/**
	 * appendix
	 */
	private java.lang.String files;
	/**
	 * Visits
	 */
	private java.lang.Integer visitsNum;
	/**
	 * Whether to pin it to the top（0no 1yes）
	 */
	private java.lang.Integer izTop;
}
