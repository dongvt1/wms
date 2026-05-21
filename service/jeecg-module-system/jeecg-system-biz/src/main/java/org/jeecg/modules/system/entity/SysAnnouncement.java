package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * @Description: System notification table
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
@Data
@TableName("sys_announcement")
public class SysAnnouncement implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private java.lang.String id;
    /**
     * title
     */
    @Excel(name = "title", width = 15)
    private java.lang.String titile;
    /**
     * content
     */
    @Excel(name = "content", width = 30)
    private java.lang.String msgContent;
    /**
     * start time
     */
    @Excel(name = "start time", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date startTime;
    /**
     * end time
     */
    @Excel(name = "end time", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date endTime;
    /**
     * Posted by
     */
    @Excel(name = "Posted by", width = 15)
    private java.lang.String sender;
    /**
     * priority（LLow，Mmiddle，Hhigh）
     */
    @Excel(name = "priority", width = 15, dicCode = "priority")
    @Dict(dicCode = "priority")
    private java.lang.String priority;
    
    /**
     * Message type1:Notices and Announcements2:System messages
     */
    @Excel(name = "Message type", width = 15, dicCode = "msg_category")
    @Dict(dicCode = "msg_category")
    private java.lang.String msgCategory;
    /**
     * Notification object type（USER:Specify user，ALL:All users）
     */
    @Excel(name = "Notification object type", width = 15, dicCode = "msg_type")
    @Dict(dicCode = "msg_type")
    private java.lang.String msgType;
    /**
     * Release status（0Unpublished，1Published，2Revoked）
     */
    @Excel(name = "Release status", width = 15, dicCode = "send_status")
    @Dict(dicCode = "send_status")
    private java.lang.String sendStatus;
    /**
     * Release time
     */
    @Excel(name = "Release time", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date sendTime;
    /**
     * Undo time
     */
    @Excel(name = "Undo time", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date cancelTime;
    /**
     * delete status（0，normal，1Deleted）
     */
    private java.lang.String delFlag;
    /**
     * Creator
     */
    private java.lang.String createBy;
    /**
     * creation time
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;
    /**
     * Updater
     */
    private java.lang.String updateBy;
    /**
     * Update time
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;
    /**
     * Specify user
     **/
    private java.lang.String userIds;
    /**
     * Business type(email:mail bpm:process tenant_invite:Tenant invitation)
     */
    private java.lang.String busType;
    /**
     * businessid
     */
    private java.lang.String busId;
    /**
     * Open method components：component routing：url
     */
    private java.lang.String openType;
    /**
     * components/routing address
     */
    private java.lang.String openPage;
    /**
     * summary/扩展business参数
     * 
     * Example：
     * 1 summary值
     *  Holiday arrangements
     * 2 跳转process的参数值
     * {"taskDetail":true,"procInsId":"1706547306004377602","taskId":"task630958764530507776"}
     */
    private java.lang.String msgAbstract;
    /**
     * DingTalktask_id，Used to withdraw messages
     */
    private String dtTaskId;

    /**
     * reading status 1Indicates that it has been read
     */
    private transient String readFlag;

    /**
     * star status 1Indicates the star
     */
    private transient String starFlag;

    /**
     * Send recordID
     */
    private transient String sendId;

    /**tenantID*/
    private java.lang.Integer tenantId;
    
    /** 
     * enumerate：org.jeecg.common.constant.enums.NoticeTypeEnum
     * notification type(system:System messages、file:knowledge base、flow:process、plan:schedule、meeting:Meeting)
     */
    private String noticeType;
    /**Attachment field*/
    private java.lang.String files;
    /**Visits*/
    private java.lang.Integer visitsNum;
    /**Whether to pin it to the top（0no 1yes）*/
    private java.lang.Integer izTop;
    /**yesno审批（0no 1yes）*/
    private java.lang.String izApproval;
    /**process状态*/
    private java.lang.String bpmStatus;
    /**Message classification*/
    private java.lang.String msgClassify;
}
