package org.jeecg.common.util;

import cn.hutool.core.date.DateUtil;
import org.jeecg.common.constant.enums.DateRangeEnum;

import java.util.Calendar;
import java.util.Date;

/**
 * Date range tool class
 *
 * @author scott
 * @date 20230801
 */
public class DateRangeUtils {

    /**
     * Get date range based on date range enumeration
     *
     * @param rangeEnum
     * @return Date[]
     */
    public static Date[] getDateRangeByEnum(DateRangeEnum rangeEnum) {
        if (rangeEnum == null) {
            return null;
        }
        Date[] ranges = new Date[2];
        switch (rangeEnum) {
            case TODAY:
                ranges[0] = getTodayStartTime();
                ranges[1] = getTodayEndTime();
                break;
            case YESTERDAY:
                ranges[0] = getYesterdayStartTime();
                ranges[1] = getYesterdayEndTime();
                break;
            case TOMORROW:
                ranges[0] = getTomorrowStartTime();
                ranges[1] = getTomorrowEndTime();
                break;
            case THIS_WEEK:
                ranges[0] = getThisWeekStartDay();
                ranges[1] = getThisWeekEndDay();
                break;
            case LAST_WEEK:
                ranges[0] = getLastWeekStartDay();
                ranges[1] = getLastWeekEndDay();
                break;
            case NEXT_WEEK:
                ranges[0] = getNextWeekStartDay();
                ranges[1] = getNextWeekEndDay();
                break;
            case LAST_7_DAYS:
                ranges[0] = getLast7DaysStartTime();
                ranges[1] = getLast7DaysEndTime();
                break;
            case THIS_MONTH:
                ranges[0] = getThisMonthStartDay();
                ranges[1] = getThisMonthEndDay();
                break;
            case LAST_MONTH:
                ranges[0] = getLastMonthStartDay();
                ranges[1] = getLastMonthEndDay();
                break;
            case NEXT_MONTH:
                ranges[0] = getNextMonthStartDay();
                ranges[1] = getNextMonthEndDay();
                break;
            default:
                return null;
        }
        return ranges;
    }

    /**
     * Get the first day of next month Sunday 00:00:00
     */
    public static Date getNextMonthStartDay() {
        return DateUtil.beginOfMonth(DateUtil.nextMonth());
    }

    /**
     * Get the last day of next month 23:59:59
     */
    public static Date getNextMonthEndDay() {
        return DateUtil.endOfMonth(DateUtil.nextMonth());
    }

    /**
     * Get the first day of the month Sunday 00:00:00
     */
    public static Date getThisMonthStartDay() {
        return DateUtil.beginOfMonth(DateUtil.date());
    }

    /**
     * Get the last day of the month 23:59:59
     */
    public static Date getThisMonthEndDay() {
        return DateUtil.endOfMonth(DateUtil.date());
    }

    /**
     * Get the first day of last month Sunday 00:00:00
     */
    public static Date getLastMonthStartDay() {
        return DateUtil.beginOfMonth(DateUtil.lastMonth());
    }

    /**
     * Get the last day of the previous month 23:59:59
     */
    public static Date getLastMonthEndDay() {
        return DateUtil.endOfMonth(DateUtil.lastMonth());
    }

    /**
     * Get the first day of last week on Monday 00:00:00
     */
    public static Date getLastWeekStartDay() {
        return DateUtil.beginOfWeek(DateUtil.lastWeek());
    }

    /**
     * Get last day of last week Sunday 23:59:59
     */
    public static Date getLastWeekEndDay() {
        return DateUtil.endOfWeek(DateUtil.lastWeek());
    }

    /**
     * Get the first day of the week on Monday 00:00:00
     */
    public static Date getThisWeekStartDay() {
        Date today = new Date();
        return DateUtil.beginOfWeek(today);
    }

    /**
     * Get the last day of the week Sunday 23:59:59
     */
    public static Date getThisWeekEndDay() {
        Date today = new Date();
        return DateUtil.endOfWeek(today);
    }

    /**
     * Get the first day of next week on Monday 00:00:00
     */
    public static Date getNextWeekStartDay() {
        return DateUtil.beginOfWeek(DateUtil.nextWeek());
    }

    /**
     * Get the last day of next week Sunday 23:59:59
     */
    public static Date getNextWeekEndDay() {
        return DateUtil.endOfWeek(DateUtil.nextWeek());
    }

    /**
     * Start time in the past seven days（Excluding today）
     *
     * @return
     */
    public static Date getLast7DaysStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -7);
        return DateUtil.beginOfDay(calendar.getTime());
    }

    /**
     * End time of past seven days（Excluding today）
     *
     * @return
     */
    public static Date getLast7DaysEndTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getLast7DaysStartTime());
        calendar.add(Calendar.DATE, 6);
        return DateUtil.endOfDay(calendar.getTime());
    }

    /**
     * Yesterday's start time
     *
     * @return
     */
    public static Date getYesterdayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -1);
        return DateUtil.beginOfDay(calendar.getTime());
    }

    /**
     * Yesterday's end time
     *
     * @return
     */
    public static Date getYesterdayEndTime() {
        return DateUtil.endOfDay(getYesterdayStartTime());
    }

    /**
     * tomorrow start time
     *
     * @return
     */
    public static Date getTomorrowStartTime() {
        return DateUtil.beginOfDay(DateUtil.tomorrow());
    }

    /**
     * end time tomorrow
     *
     * @return
     */
    public static Date getTomorrowEndTime() {
        return DateUtil.endOfDay(DateUtil.tomorrow());
    }

    /**
     * Start time today
     *
     * @return
     */
    public static Date getTodayStartTime() {
        return DateUtil.beginOfDay(new Date());
    }

    /**
     * end time today
     *
     * @return
     */
    public static Date getTodayEndTime() {
        return DateUtil.endOfDay(new Date());
    }

}
