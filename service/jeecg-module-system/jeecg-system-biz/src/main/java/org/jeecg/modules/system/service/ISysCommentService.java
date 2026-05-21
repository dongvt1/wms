package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysComment;
import org.jeecg.modules.system.vo.SysCommentFileVo;
import org.jeecg.modules.system.vo.SysCommentVO;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Description: System comment reply form
 * @Author: jeecg-boot
 * @Date: 2022-07-19
 * @Version: V1.0
 */
public interface ISysCommentService extends IService<SysComment> {


    /**
     * Save comment Return to commentsID
     *
     * @param sysComment
     */
    String saveOne(SysComment sysComment);

    /**
     * delete
     *
     * @param id
     */
    void deleteOne(String id);

    /**
     * According to table name and dataidQuery form comments and file information
     *
     * @param sysComment
     * @return
     */
    List<SysCommentVO> queryFormCommentInfo(SysComment sysComment);


    /**
     * save file+Comment
     *
     * @param req
     */
    void saveOneFileComment(HttpServletRequest req);


    /**
     * Query the file list of the current form
     *
     * @param tableName
     * @param formDataId
     * @return
     */
    List<SysCommentFileVo> queryFormFileList(String tableName, String formDataId);
    /**
     * append save file+Comment
     *
     * @param request
     */
    void appSaveOneFileComment(HttpServletRequest request);
}
