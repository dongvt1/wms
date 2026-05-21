package org.jeecg.modules.system.vo;

import lombok.Data;

/**
 * @Description: documentVO
 * @Author: jeecg-boot
 * @Date: 2022-07-21
 * @Version: V1.0
 */
@Data
public class SysCommentFileVo {

    /**
     * sys_files id
     */
    private String fileId;
    /**
     * sys_form_file id
     */
    private String sysFormFileId;
    /**
     * File name
     */
    private String name;

    private Double fileSize;

    /**
     * File address
     */
    private String url;

    /**
     * document类型（folder:folder excel:excel doc:word pp:ppt image:picture  archive:其他document video:video）
     */
    private String type;

    /**
     * File upload type(temp/Local upload(temporary files) manage/knowledge base)
     */
    private String storeType;

}
