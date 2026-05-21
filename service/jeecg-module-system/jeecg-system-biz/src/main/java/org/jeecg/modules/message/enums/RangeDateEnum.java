package org.jeecg.modules.message.enums;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.system.annotation.EnumDict;
import org.jeecg.common.system.vo.DictModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Used for message data query【vue3】
 * New version system notification query conditions
 * @Author taoYan
 * @Date 2022/8/19 20:41
 **/
@Slf4j
@EnumDict("rangeDate")
public enum RangeDateEnum {

    JT("jt", "today"),
    ZT("zt", "yesterday"),
    QT("qt", "the day before yesterday"),
    BZ("bz","this week"),
    SZ("sz", "last week"),
    BY("by", "this month"),
    SY("sy", "last month"),
    SEVENDAYS("7day", "7day"),
    ZDY("zdy", "自定义day期");

    String key;

    String title;

    RangeDateEnum(String key, String title){
        this.key = key;
        this.title = title;
    }

    /**
     * Get dictionary data
     * @return
     */
    public static List<DictModel> getDictList(){
        List<DictModel> list = new ArrayList<>();
        DictModel dictModel = null;
        for(RangeDateEnum e: RangeDateEnum.values()){
            dictModel = new DictModel();
            dictModel.setValue(e.key);
            dictModel.setText(e.title);
            list.add(dictModel);
        }
        return list;
    }

    /**
     * according tokey Get range time value
     * @param key
     * @return
     */
    public static Date[] getRangeArray(String key){
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        Date[] array = new Date[2];
        boolean flag = false;
        if(JT.key.equals(key)){
            //today
        } else if(ZT.key.equals(key)){
            //yesterday
            calendar1.add(Calendar.DAY_OF_YEAR, -1);
            calendar2.add(Calendar.DAY_OF_YEAR, -1);
        } else if(QT.key.equals(key)){
            //the day before yesterday
            calendar1.add(Calendar.DAY_OF_YEAR, -2);
            calendar2.add(Calendar.DAY_OF_YEAR, -2);
        } else if(BZ.key.equals(key)){
            //this week
            calendar1.set(Calendar.DAY_OF_WEEK, 2);

            calendar2.add(Calendar.WEEK_OF_MONTH,1);
            calendar2.add(Calendar.DAY_OF_WEEK,-1);
        } else if(SZ.key.equals(key)){
            //this week一减一周
            calendar1.set(Calendar.DAY_OF_WEEK, 2);
            calendar1.add(Calendar.WEEK_OF_MONTH, -1);

            // this week一减一天
            calendar2.set(Calendar.DAY_OF_WEEK, 2);
            calendar2.add(Calendar.DAY_OF_WEEK,-1);
        } else if(BY.key.equals(key)){
            //this month
            calendar1.set(Calendar.DAY_OF_MONTH, 1);

            calendar2.set(Calendar.DAY_OF_MONTH, 1);
            calendar2.add(Calendar.MONTH, 1);
            calendar2.add(Calendar.DAY_OF_MONTH, -1);
        } else if(SY.key.equals(key)){
            //this month第一天减一月
            calendar1.set(Calendar.DAY_OF_MONTH, 1);
            calendar1.add(Calendar.MONTH, -1);

            //this month第一天减一天
            calendar2.set(Calendar.DAY_OF_MONTH, 1);
            calendar2.add(Calendar.DAY_OF_MONTH, -1);
        } else if (SEVENDAYS.key.equals(key)){
            //七day第一天
            calendar1.setTime(new Date());
            calendar1.add(Calendar.DATE, -7);
        }else{
            flag = true;
        }
        if(flag){
            return null;
        }
        // start time00:00:00 end time23:59:59
        calendar1.set(Calendar.HOUR, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);
        calendar2.set(Calendar.HOUR, 23);
        calendar2.set(Calendar.MINUTE, 59);
        calendar2.set(Calendar.SECOND, 59);
        calendar2.set(Calendar.MILLISECOND, 999);
        array[0] = calendar1.getTime();
        array[1] = calendar2.getTime();
        return array;
    }
    
    public String getKey(){
        return this.key;
    }

}
