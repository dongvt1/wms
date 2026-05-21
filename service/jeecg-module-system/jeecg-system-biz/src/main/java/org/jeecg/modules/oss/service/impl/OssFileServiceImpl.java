package org.jeecg.modules.oss.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.util.oss.OssBootUtil;
import org.jeecg.modules.oss.entity.OssFile;
import org.jeecg.modules.oss.mapper.OssFileMapper;
import org.jeecg.modules.oss.service.IOssFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Description: OSSCloud storage implementation class
 * @author: jeecg-boot
 */
@Service("ossFileService")
public class OssFileServiceImpl extends ServiceImpl<OssFileMapper, OssFile> implements IOssFileService {

	@Override
	public void upload(MultipartFile multipartFile) throws Exception {
		String fileName = multipartFile.getOriginalFilename();
		fileName = CommonUtils.getFileName(fileName);
		OssFile ossFile = new OssFile();
		ossFile.setFileName(fileName);
		String url = OssBootUtil.upload(multipartFile,"upload/test");
		if(oConvertUtils.isEmpty(url)){
			throw new JeecgBootException("File upload failed! ");
		}
		//update-begin--Author:scott  Date:20201227 for：JT-361【File preview】阿里云原生域名可以File preview，Map domain name yourselfkkfileviewPrompt file download failed-------------------
		// Returns the Alibaba Cloud native domain name prefixURL
		ossFile.setUrl(OssBootUtil.getOriginalUrl(url));
		//update-end--Author:scott  Date:20201227 for：JT-361【File preview】阿里云原生域名可以File preview，Map domain name yourselfkkfileviewPrompt file download failed-------------------
		this.save(ossFile);
	}

	@Override
	public boolean delete(OssFile ossFile) {
		try {
			this.removeById(ossFile.getId());
			OssBootUtil.deleteUrl(ossFile.getUrl());
		}
		catch (Exception ex) {
			log.error(ex.getMessage(),ex);
			return false;
		}
		return true;
	}

}
