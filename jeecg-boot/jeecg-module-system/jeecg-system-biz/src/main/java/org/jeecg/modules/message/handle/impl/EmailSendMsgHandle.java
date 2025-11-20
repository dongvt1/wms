package org.jeecg.modules.message.handle.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.StaticConfig;
import org.jeecg.modules.message.entity.SysMessage;
import org.jeecg.modules.message.handle.ISendMsgHandle;
import org.jeecg.modules.message.mapper.SysMessageMapper;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: Send information via email
 * @author: jeecg-boot
 */
@Slf4j
@Component("emailSendMsgHandle")
public class EmailSendMsgHandle implements ISendMsgHandle {
    static String emailFrom;

    public static void setEmailFrom(String emailFrom) {
        EmailSendMsgHandle.emailFrom = emailFrom;
    }

    @Autowired
    SysUserMapper sysUserMapper;

    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private SysMessageMapper sysMessageMapper;

    /**
     * real name variable
     */
    private static final String  realNameExp = "{REALNAME}";
    /**
     * Thread pool is used to send messages asynchronously
     */
    public static ExecutorService cachedThreadPool = new ThreadPoolExecutor(0, 1024, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());


    @Override
    public void sendMsg(String esReceiver, String esTitle, String esContent) {
        JavaMailSender mailSender = (JavaMailSender) SpringContextUtils.getBean("mailSender");
        MimeMessage message = mailSender.createMimeMessage();
        //update-begin-author：taoyan date:20200811 for:Configuration data acquisition
        if(oConvertUtils.isEmpty(emailFrom)){
            StaticConfig staticConfig = SpringContextUtils.getBean(StaticConfig.class);
            setEmailFrom(staticConfig.getEmailFrom());
        }
        //update-end-author：taoyan date:20200811 for:Configuration data acquisition
        cachedThreadPool.execute(()->{
            try {
                log.info("============> Start sending email，recipient："+esReceiver);
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                // Set sender email address
                helper.setFrom(emailFrom);
                helper.setTo(esReceiver);
                helper.setSubject(esTitle);
                helper.setText(esContent, true);
                mailSender.send(message);
                log.info("============> Email sent successfully，recipient："+esReceiver);
            } catch (MessagingException e) {
                log.error("============> Email sending failed，recipient："+esReceiver, e.getMessage());
            }
        });
    }

    @Override
    public void sendMessage(MessageDTO messageDTO) {
        String content = messageDTO.getContent();
        String title = messageDTO.getTitle();
        //update-begin---author:wangshuai---date:2024-11-20---for:【QQYUN-8523】Send email notification via KnockKou Cloud，unstable---
        boolean timeJobSendEmail = this.isTimeJobSendEmail(messageDTO.getToUser(), title, content);
        if(timeJobSendEmail){
            return;
        }
        //update-end---author:wangshuai---date:2024-11-20---for:【QQYUN-8523】Send email notification via KnockKou Cloud，unstable---
        this.sendEmailMessage(messageDTO);
    }

    /**
     * Send email directly
     * 
     * @param messageDTO
     */
    public void sendEmailMessage(MessageDTO messageDTO) {
        String[] arr = messageDTO.getToUser().split(",");
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>().in(SysUser::getUsername, arr);
        List<SysUser> list = sysUserMapper.selectList(query);
        String content = messageDTO.getContent();
        String title = messageDTO.getTitle();
        for(SysUser user: list){
            String email = user.getEmail();
            if (ObjectUtils.isEmpty(email)) {
                continue;
            }
            content=replaceContent(user,content);
            log.info("Email content："+ content);
            sendMsg(email, title, content);
        }
        
        //update-begin-author:taoyan date:2023-6-20 for: QQYUN-5557【Simple flow】notification node Send email There is an email field on the form，in process，Email sending node，邮件recipient Email cannot be selected
        Set<String> toEmailList = messageDTO.getToEmailList();
        if(toEmailList!=null && toEmailList.size()>0){
            for(String email: toEmailList){
                if (ObjectUtils.isEmpty(email)) {
                    continue;
                }
                log.info("Email content："+ content);
                sendMsg(email, title, content);
            }
        }
        //update-end-author:taoyan date:2023-6-20 for: QQYUN-5557【Simple flow】notification node Send email There is an email field on the form，in process，Email sending node，邮件recipient Email cannot be selected
        
        //Send to CC
        sendMessageToCopyUser(messageDTO);
    }

    /**
     * Send email to CC
     * @param messageDTO
     */
    public void sendMessageToCopyUser(MessageDTO messageDTO) {
        String copyToUser = messageDTO.getCopyToUser();
        if(ObjectUtils.isNotEmpty(copyToUser)) {
            LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>().in(SysUser::getUsername, copyToUser.split(","));
            List<SysUser> list = sysUserMapper.selectList(query);
            String content = messageDTO.getContent();
            String title = messageDTO.getTitle();

            for (SysUser user : list) {
                String email = user.getEmail();
                if (ObjectUtils.isEmpty(email)) {
                    continue;
                }
                content=replaceContent(user,content);
                log.info("Email content：" + content);
                
            //update-begin-author:taoyan date:2023-6-20 for: QQYUN-5557【Simple flow】notification node Send email There is an email field on the form，in process，Email sending node，邮件recipient Email cannot be selected
                sendEmail(email, content, title);
            }

            Set<String> ccEmailList = messageDTO.getCcEmailList();
            if(ccEmailList!=null && ccEmailList.size()>0){
                for(String email: ccEmailList){
                    if (ObjectUtils.isEmpty(email)) {
                        continue;
                    }
                    log.info("Email content："+ content);
                    sendEmail(email, content, title);
                }
            }
            
        }
    }

    /**
     * Send email to CC调用
     * @param email
     * @param content
     * @param title
     */
    private void sendEmail(String email, String content, String title){
        JavaMailSender mailSender = (JavaMailSender) SpringContextUtils.getBean("mailSender");
        MimeMessage message = mailSender.createMimeMessage();
        if (oConvertUtils.isEmpty(emailFrom)) {
            StaticConfig staticConfig = SpringContextUtils.getBean(StaticConfig.class);
            setEmailFrom(staticConfig.getEmailFrom());
        }
        cachedThreadPool.execute(()->{
            try {
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                // Set sender email address
                helper.setFrom(emailFrom);
                helper.setTo(email);
                //Set carbon copy person
                helper.setCc(email);
                helper.setSubject(title);
                helper.setText(content, true);
                mailSender.send(message);
                log.info("============> Email sent successfully，recipient："+email);
            } catch (MessagingException e) {
                log.warn("============> Email sending failed，recipient："+email, e.getMessage());
            }
        });
    }
    //update-end-author:taoyan date:2023-6-20 for: QQYUN-5557【Simple flow】notification node Send email There is an email field on the form，in process，Email sending node，邮件recipient Email cannot be selected
    

    /**
     * 替换Email content变量
     * @param user
     * @param content
     * @return
     */
    private String replaceContent(SysUser user,String content){
        if (content.indexOf(realNameExp) > 0) {
            content = content.replace("$"+realNameExp,user.getRealname()).replace(realNameExp, user.getRealname());
        }
        if (content.indexOf(CommonConstant.LOGIN_TOKEN) > 0) {
            String token = getToken(user);
            try {
                content = content.replace(CommonConstant.LOGIN_TOKEN, URLEncoder.encode(token, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.error("Email messagetokenEncoding failed", e.getMessage());
            }
        }
        return content;
    }

    /**
     * Gettoken
     * @param user
     * @return
     */
    private String getToken(SysUser user) {
        // generatetoken
        String token = JwtUtil.sign(user.getUsername(), user.getPassword());
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
        // Set timeout 1hours
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 1 / 1000);
        return token;
    }

    /**
     * 是否定时Send email
     * @param toUser
     * @param title
     * @param content
     * @return
     */
    private boolean isTimeJobSendEmail(String toUser, String title, String content) {
        StaticConfig staticConfig = SpringContextUtils.getBean(StaticConfig.class);
        Boolean timeJobSend = staticConfig.getTimeJobSend();
        if(null != timeJobSend && timeJobSend){
            this.addSysSmsSend(toUser,title,content);
            return true;
        }
        return false;
    }
    
    /**
     * Save to SMS sending table
     */
    private void addSysSmsSend(String toUser, String title, String content) {
        SysMessage sysMessage = new SysMessage();
        sysMessage.setEsTitle(title);
        sysMessage.setEsContent(content);
        sysMessage.setEsReceiver(toUser);
        sysMessage.setEsSendStatus("0");
        sysMessage.setEsSendNum(0);
        sysMessage.setEsType(MessageTypeEnum.YJ.getType());
        sysMessageMapper.insert(sysMessage);
    }
}
