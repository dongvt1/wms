package org.jeecg.common.system.util;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.annotation.EnumDict;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Enumerate dictionary data Resource loading tool class
 *
 * @Author taoYan
 * @Date 2022/7/8 10:40
 **/
@Slf4j
public class ResourceUtil {

    /**
     * Multiple package scan root path
     *
     * The reason why users are allowed to manually configure scan paths，is to avoid unnecessary class loading overhead，Improve startup performance。
     * Be sure to add the package path of all enumeration classes to this configuration。
     */
    private final static String[] BASE_SCAN_PACKAGES = {
            "org.jeecg.common.constant.enums",
            "org.jeecg.modules.message.enums"
    };
    
    /**
     * Enumerate dictionary data
     */
    private final static Map<String, List<DictModel>> enumDictData = new HashMap<>(5);
    /**
     * All enumerationsjavakind
     */

    private final static String CLASS_ENUM_PATTERN="/**/*Enum.class";

    /**
     * Initialization status identifier
     */
    private static volatile boolean initialized = false;

    /**
     * 枚举kind中获取字典数据的方法名
     */
    private final static String METHOD_NAME = "getDictList";

    /**
     * 获取Enumerate dictionary data
     * 获取枚举kind对应的字典数据 SysDictServiceImpl#queryAllDictItems()
     *
     * @return Enumerate dictionary data
     */
    public static Map<String, List<DictModel>> getEnumDictData() {
        if (!initialized) {
            synchronized (ResourceUtil.class) {
                if (!initialized) {
                    long startTime = System.currentTimeMillis();
                    log.info("【Enum dictionary loading】开始初始化Enumerate dictionary data...");

                    initEnumDictData();
                    initialized = true;

                    long endTime = System.currentTimeMillis();
                    log.info("【Enum dictionary loading】Enumerate dictionary data初始化完成，Loaded in total {} dictionary，Total time spent: {}ms", enumDictData.size(), endTime - startTime);
                }
            }
        }
        return enumDictData;
    }

    /**
     * 使用多包路径扫描方式初始化Enumerate dictionary data
     */
    private static void initEnumDictData() {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

        long scanStartTime = System.currentTimeMillis();
        List<Resource> allResources = new ArrayList<>();

        // Scan multiple package paths
        for (String basePackage : BASE_SCAN_PACKAGES) {
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(basePackage) + CLASS_ENUM_PATTERN;

            try {
                Resource[] resources = resourcePatternResolver.getResources(pattern);
                allResources.addAll(Arrays.asList(resources));
                log.debug("【Enum dictionary loading】Scan package {} turn up {} 个枚举kind文件", basePackage, resources.length);
            } catch (Exception e) {
                log.warn("【Enum dictionary loading】Scan package {} Exception occurs when: {}", basePackage, e.getMessage());
            }
        }

        long scanEndTime = System.currentTimeMillis();
        log.info("【Enum dictionary loading】File scan completed，总共turn up {} 个枚举kind文件，Scanning time: {}ms", allResources.size(), scanEndTime - scanStartTime);

        MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        long processStartTime = System.currentTimeMillis();
        int processedCount = 0;

        for (Resource resource : allResources) {
            try {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                String classname = reader.getClassMetadata().getClassName();

                // Check in advance to see if there is@EnumDictannotation，avoid unnecessaryClass.forName
                if (hasEnumDictAnnotation(reader)) {
                    processEnumClass(classname);
                    processedCount++;
                }
            } catch (Exception e) {
                log.debug("Handling resource exceptions: {} - {}", resource.getFilename(), e.getMessage());
            }
        }

        long processEndTime = System.currentTimeMillis();
        log.info("【Enum dictionary loading】Processing completed，actual processing {} 个带annotation的枚举kind，Processing time: {}ms", processedCount, processEndTime - processStartTime);
    }

    /**
     * 检查kind是否有EnumDictannotation（via metadata，避免kind加载）
     */
    private static boolean hasEnumDictAnnotation(MetadataReader reader) {
        try {
            return reader.getAnnotationMetadata().hasAnnotation(EnumDict.class.getName());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 处理单个枚举kind
     */
    private static void processEnumClass(String classname) {
        try {
            Class<?> clazz = Class.forName(classname);
            EnumDict enumDict = clazz.getAnnotation(EnumDict.class);

            if (enumDict != null) {
                String key = enumDict.value();
                if (oConvertUtils.isNotEmpty(key)) {
                    Method method = clazz.getDeclaredMethod(METHOD_NAME);
                    List<DictModel> list = (List<DictModel>) method.invoke(null);
                    enumDictData.put(key, list);
                    log.debug("Successfully loaded enum dictionary: {} -> {}", key, classname);
                }
            }
        } catch (Exception e) {
            log.debug("处理枚举kind异常: {} - {}", classname, e.getMessage());
        }
    }

    /**
     * Used for backend dictionary translation SysDictServiceImpl#queryManyDictByKeys(java.util.List, java.util.List)
     *
     * @param dictCodeList Dictionary encoding list
     * @param keys         key value list
     * @return dictionary data mapping
     */
    public static Map<String, List<DictModel>> queryManyDictByKeys(List<String> dictCodeList, List<String> keys) {
        Map<String, List<DictModel>> enumDict = getEnumDictData();
        Map<String, List<DictModel>> map = new HashMap<>();

        // Use more efficient search methods
        Set<String> dictCodeSet = new HashSet<>(dictCodeList);
        Set<String> keySet = new HashSet<>(keys);

        for (String code : enumDict.keySet()) {
            if (dictCodeSet.contains(code)) {
                List<DictModel> dictItemList = enumDict.get(code);
                for (DictModel dm : dictItemList) {
                    String value = dm.getValue();
                    if (keySet.contains(value)) {
                        List<DictModel> list = new ArrayList<>();
                        list.add(new DictModel(value, dm.getText()));
                        map.put(code, list);
                        break;
                    }
                }
            }
        }
        return map;
    }
    
}