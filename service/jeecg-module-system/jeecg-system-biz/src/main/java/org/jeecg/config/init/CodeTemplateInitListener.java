package org.jeecg.config.init;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Automatically initialize code generator templates
 * <p>
 * solveJARPublishing requires manual configuration of code generator templates
 * @author zhang
 */
@Slf4j
@Component
public class CodeTemplateInitListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            long startTime = System.currentTimeMillis(); // Recording start time
            log.info(" Init Code Generate Template [ Check if it isJARstart up，Copytemplate toconfigTable of contents ] ");
            this.initJarConfigCodeGeneratorTemplate();
            long endTime = System.currentTimeMillis(); // Record end time
            log.info(" Init Code Generate Template completed in " + (endTime - startTime) + " ms"); // Calculate and record time taken
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ::Jar包start up模式Down::
     * Initialize the code generator template file
     */
    private void initJarConfigCodeGeneratorTemplate() throws Exception {
        //1.GetjarBelow the same levelconfigpath
        String configPath = System.getProperty("user.dir") + File.separator + "config" + File.separator;
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:jeecg/code-template-online/**/*");
        for (Resource re : resources) {
            URL url = re.getURL();
            String filepath = url.getPath();
            //System.out.println("native url= " + filepath);
            filepath = java.net.URLDecoder.decode(filepath, "utf-8");
            //System.out.println("decode url= " + filepath);

            //2.existconfigDown，createjeecg/code-template-online/*template
            String createFilePath = configPath + filepath.substring(filepath.indexOf("jeecg/code-template-online"));

            // Nojar模式不生成template
            // 不生成Table of contents，只生成具体template文件
            if ((!filepath.contains(".jar!/BOOT-INF/lib/") && !filepath.contains(".jar/!BOOT-INF/lib/")) || !createFilePath.contains(".")) {
                continue;
            }
            if (!FileUtil.exist(createFilePath)) {
                log.info("create file codeTemplate = " + createFilePath);
                FileUtil.writeString(IOUtils.toString(url, StandardCharsets.UTF_8), createFilePath, "UTF-8");
            }
        }
    }
}
