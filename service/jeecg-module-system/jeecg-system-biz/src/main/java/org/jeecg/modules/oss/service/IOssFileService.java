package org.jeecg.modules.oss.service;

import java.io.IOException;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.oss.entity.OssFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: OOScloud storageserviceinterface
 * @author: jeecg-boot
 */
public interface IOssFileService extends IService<OssFile> {

    /**
     * ossFile upload
     * @param multipartFile
     * @throws IOException
     */
	void upload(MultipartFile multipartFile) throws Exception;

    /**
     * ossFile deletion
     * @param ossFile OSSFileobject
     * @return
     */
	boolean delete(OssFile ossFile);

}
