package org.jeecg.common.util;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Zhang Daihao
 * @desc Dynamically called through reflectionget and set method
 */
@Slf4j
public class ReflectHelper {

    private Class cls;

    /**
     * passed object
     */
    private Object obj;

    /**
     * storegetmethod
     */
    private Hashtable<String, Method> getMethods = null;
    /**
     * storesetmethod
     */
    private Hashtable<String, Method> setMethods = null;

    /**
     * 定义构造method -- Generally speaking it is apojo
     *
     * @param o target audience
     */
    public ReflectHelper(Object o) {
        obj = o;
        initMethods();
    }

    /**
     * @desc initialization
     */
    public void initMethods() {
        getMethods = new Hashtable<String, Method>();
        setMethods = new Hashtable<String, Method>();
        cls = obj.getClass();
        Method[] methods = cls.getMethods();
        // Define regular expression，从methodmiddle过滤出getter / setter function.
        String gs = "get(\\w+)";
        Pattern getM = Pattern.compile(gs);
        String ss = "set(\\w+)";
        Pattern setM = Pattern.compile(ss);
        // Bundlemethodmiddleof"set" or "get" remove
        String rapl = "$1";
        String param;
        for (int i = 0; i < methods.length; ++i) {
            Method m = methods[i];
            String methodName = m.getName();
            if (Pattern.matches(gs, methodName)) {
                param = getM.matcher(methodName).replaceAll(rapl).toLowerCase();
                getMethods.put(param, m);
            } else if (Pattern.matches(ss, methodName)) {
                param = setM.matcher(methodName).replaceAll(rapl).toLowerCase();
                setMethods.put(param, m);
            } else {
                // logger.info(methodName + " nogetter,settermethod！");
            }
        }
    }

    /**
     * @desc callsetmethod
     */
    public boolean setMethodValue(String property, Object object) {
        Method m = setMethods.get(property.toLowerCase());
        if (m != null) {
            try {
                // call目标classsetterfunction
                m.invoke(obj, object);
                return true;
            } catch (Exception ex) {
                log.info("invoke getter on " + property + " error: " + ex.toString());
                return false;
            }
        }
        return false;
    }

    /**
     * @desc callsetmethod
     */
    public Object getMethodValue(String property) {
        Object value = null;
        Method m = getMethods.get(property.toLowerCase());
        if (m != null) {
            try {
                /*
                 * callobjclasssetterfunction
                 */
                value = m.invoke(obj, new Object[]{});

            } catch (Exception ex) {
                log.info("invoke getter on " + property + " error: " + ex.toString());
            }
        }
        return value;
    }

    /**
     * BundlemapAll contents in are injected intoobjmiddle
     *
     * @param data
     * @return
     */
    public Object setAll(Map<String, Object> data) {
        if (data == null || data.keySet().size() <= 0) {
            return null;
        }
        for (Entry<String, Object> entry : data.entrySet()) {
            this.setMethodValue(entry.getKey(), entry.getValue());
        }
        return obj;
    }

    /**
     * BundlemapAll contents in are injected intoobjmiddle
     *
     * @param o
     * @param data
     * @return
     */
    public static Object setAll(Object o, Map<String, Object> data) {
        ReflectHelper reflectHelper = new ReflectHelper(o);
        reflectHelper.setAll(data);
        return o;
    }

    /**
     * BundlemapAll contents in are injected into新实例middle
     *
     * @param clazz
     * @param data
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T setAll(Class<T> clazz, Map<String, Object> data) {
        T o = null;
        try {
            o = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            o = null;
            return o;
        }
        return (T) setAll(o, data);
    }

    /**
     * According to the incomingclassWillmapListConvert to entity classlist
     *
     * @param mapist
     * @param clazz
     * @return
     */
    public static <T> List<T> transList2Entrys(List<Map<String, Object>> mapist, Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        if (mapist != null && mapist.size() > 0) {
            for (Map<String, Object> data : mapist) {
                list.add(ReflectHelper.setAll(clazz, data));
            }
        }
        return list;
    }

    /**
     * Get attribute value based on attribute name
     */
    public static Object getFieldValueByName(String fieldName, Object o) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = o.getClass().getMethod(getter, new Class[]{});
            Object value = method.invoke(o, new Object[]{});
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get attribute value
     */
    public static Object getFieldVal(String fieldName, Object o) {
        try {
            // Violent reflection to obtain attributes
            Field filed = o.getClass().getDeclaredField(fieldName);
            // Cancel when setting reflectionJavaaccess check，Brute force access
            filed.setAccessible(true);
            Object val = filed.get(o);
            return val;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get array of attribute names
     */
    public static String[] getFiledName(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            //log.info(fields[i].getType());
            fieldNames[i] = fields[i].getName();
        }
        return fieldNames;
    }

    /**
     * Get attribute type(type)，attribute name(name)，attribute value(value)ofmap组成oflist
     */
    public static List<Map> getFiledsInfo(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        List<Map> list = new ArrayList<Map>();
        Map<String, Object> infoMap = null;
        for (int i = 0; i < fields.length; i++) {
            infoMap = new HashMap<>(5);
            infoMap.put("type", fields[i].getType().toString());
            infoMap.put("name", fields[i].getName());
            infoMap.put("value", getFieldValueByName(fields[i].getName(), o));
            list.add(infoMap);
        }
        return list;
    }

    /**
     * Get对象of所有attribute value，Returns an array of objects
     */
    public static Object[] getFiledValues(Object o) {
        String[] fieldNames = getFiledName(o);
        Object[] value = new Object[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            value[i] = getFieldValueByName(fieldNames[i], o);
        }
        return value;
    }

    /**
     * 判断给定of字段是no类middleof属性
     * @param field Field name
     * @param clazz class object
     * @return
     */
    public static boolean isClassField(String field, Class clazz){
        Field[] fields = clazz.getDeclaredFields();
        for(int i=0;i<fields.length;i++){
            String fieldName = fields[i].getName();
            String tableColumnName = oConvertUtils.camelToUnderline(fieldName);
            if(fieldName.equalsIgnoreCase(field) || tableColumnName.equalsIgnoreCase(field)){
                return true;
            }
        }
        return false;
    }

    /**
     * Getclassof 包括父class
     * @param clazz
     * @return
     */
    public static List<Field> getClassFields(Class<?> clazz) {
        List<Field> list = new ArrayList<Field>();
        Field[] fields;
        do{
            fields = clazz.getDeclaredFields();
            for(int i = 0;i<fields.length;i++){
                list.add(fields[i]);
            }
            clazz = clazz.getSuperclass();
        }while(clazz!= Object.class&&clazz!=null);
        return list;
    }

    /**
     * Get表Field name
     * @param clazz
     * @param name
     * @return
     */
    public static String getTableFieldName(Class<?> clazz, String name) {
        try {
            //If the field is annotated@TableField(exist = false),Not leavingDBQuery
            Field field = null;
            try {
                field = clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                //e.printStackTrace();
            }

            //if empty，Then go to the parent class to find the field
            if (field == null) {
                List<Field> allFields = getClassFields(clazz);
                List<Field> searchFields = allFields.stream().filter(a -> a.getName().equals(name)).collect(Collectors.toList());
                if(searchFields!=null && searchFields.size()>0){
                    field = searchFields.get(0);
                }
            }

            if (field != null) {
                TableField tableField = field.getAnnotation(TableField.class);
                if (tableField != null){
                    if(tableField.exist() == false){
                        //If setTableField false This field does not need to be processed
                        return null;
                    }else{
                        String column = tableField.value();
                        //If setTableField value This field is an entity field
                        if(!"".equals(column)){
                            return column;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

}