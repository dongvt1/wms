package org.jeecg.common.util;

import org.jeecg.common.constant.SymbolConstant;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Class description：Time operation definition class
 *
 * @Author: Zhang Daihao
 * @Date:2012-12-8 12:15:03
 * @Version 1.0
 */
public class DateUtils extends PropertyEditorSupport {

    public static ThreadLocal<SimpleDateFormat> date_sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    public static ThreadLocal<SimpleDateFormat> yyyyMMdd = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd");
        }
    };
    public static ThreadLocal<SimpleDateFormat> date_sdf_wz = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyYearMMmoonddday");
        }
    };
    public static ThreadLocal<SimpleDateFormat> time_sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }
    };
    public static ThreadLocal<SimpleDateFormat> yyyymmddhhmmss = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };
    public static ThreadLocal<SimpleDateFormat> short_time_sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm");
        }
    };
    public static ThreadLocal<SimpleDateFormat> datetimeFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    /**
     * time in milliseconds
     */
    private static final long DAY_IN_MILLIS = 24 * 3600 * 1000;
    private static final long HOUR_IN_MILLIS = 3600 * 1000;
    private static final long MINUTE_IN_MILLIS = 60 * 1000;
    private static final long SECOND_IN_MILLIS = 1000;

    /**
     * Specifies the time format of the pattern
     * @param pattern
     * @return
     */
    private static SimpleDateFormat getSdFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    /**
     * 当前day历，Here it is expressed in Chinese time
     *
     * @return 以当地hour区表示的系统当前day历
     */
    public static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    /**
     * 指定milliseconds表示的day历
     *
     * @param millis milliseconds
     * @return 指定milliseconds表示的day历
     */
    public static Calendar getCalendar(long millis) {
        Calendar cal = Calendar.getInstance();
        // --------------------cal.setTimeInMillis(millis);
        cal.setTime(new Date(millis));
        return cal;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // getDate
    // Obtained in various waysDate
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 当前day期
     *
     * @return System current time
     */
    public static Date getDate() {
        return new Date();
    }
    
    
    /**
     * 当前day期
     *
     * @return 系统当前day期（Without hours, minutes and seconds）
     */
    public static LocalDate getLocalDate() {
        LocalDate today = LocalDate.now();
        return today;
    }

    /**
     * 指定milliseconds表示的day期
     *
     * @param millis milliseconds
     * @return 指定milliseconds表示的day期
     */
    public static Date getDate(long millis) {
        return new Date(millis);
    }

    /**
     * Convert timestamp to string
     *
     * @param time
     * @return
     */
    public static String timestamptoStr(Timestamp time) {
        Date date = null;
        if (null != time) {
            date = new Date(time.getTime());
        }
        return date2Str(date_sdf.get());
    }

    /**
     * String conversion timestamp
     *
     * @param str
     * @return
     */
    public static Timestamp str2Timestamp(String str) {
        Date date = str2Date(str, date_sdf.get());
        return new Timestamp(date.getTime());
    }

    /**
     * string转换成day期
     *
     * @param str
     * @param sdf
     * @return
     */
    public static Date str2Date(String str, SimpleDateFormat sdf) {
        if (null == str || "".equals(str)) {
            return null;
        }
        Date date = null;
        try {
            date = sdf.parse(str);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * day期Convert tostring
     *
     * @param dateSdf day期Format
     * @return string
     */
    public static String date2Str(SimpleDateFormat dateSdf) {
        synchronized (dateSdf) {
            Date date = getDate();
            if (null == date) {
                return null;
            }
            return dateSdf.format(date);
        }
    }

    /**
     * Format time
     *
     * @param date
     * @param format
     * @return
     */
    public static String dateformat(String date, String format) {
        SimpleDateFormat sformat = new SimpleDateFormat(format);
        Date nowDate = null;
        try {
            nowDate = sformat.parse(date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sformat.format(nowDate);
    }

    /**
     * day期Convert tostring
     *
     * @param date     day期
     * @param dateSdf day期Format
     * @return string
     */
    public static String date2Str(Date date, SimpleDateFormat dateSdf) {
        synchronized (dateSdf) {
            if (null == date) {
                return null;
            }
            return dateSdf.format(date);
        }
    }

    /**
     * day期Convert tostring
     *
     * @param format day期Format
     * @return string
     */
    public static String getDate(String format) {
        Date date = new Date();
        if (null == date) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 指定milliseconds的hour间戳
     *
     * @param millis milliseconds
     * @return 指定milliseconds的hour间戳
     */
    public static Timestamp getTimestamp(long millis) {
        return new Timestamp(millis);
    }

    /**
     * Timestamp as a character
     *
     * @param time milliseconds
     * @return Timestamp as a character
     */
    public static Timestamp getTimestamp(String time) {
        return new Timestamp(Long.parseLong(time));
    }

    /**
     * The current timestamp of the system
     *
     * @return The current timestamp of the system
     */
    public static Timestamp getTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * current time，Format yyyy-MM-dd HH:mm:ss
     *
     * @return current time的标准形式string
     */
    public static String now() {
        return datetimeFormat.get().format(getCalendar().getTime());
    }

    /**
     * 指定day期的hour间戳
     *
     * @param date 指定day期
     * @return 指定day期的hour间戳
     */
    public static Timestamp getTimestamp(Date date) {
        return new Timestamp(date.getTime());
    }

    /**
     * 指定day历的hour间戳
     *
     * @param cal 指定day历
     * @return 指定day历的hour间戳
     */
    public static Timestamp getCalendarTimestamp(Calendar cal) {
        // ---------------------return new Timestamp(cal.getTimeInMillis());
        return new Timestamp(cal.getTime().getTime());
    }

    public static Timestamp gettimestamp() {
        Date dt = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = df.format(dt);
        java.sql.Timestamp buydate = java.sql.Timestamp.valueOf(nowTime);
        return buydate;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // getMillis
    // Obtained in various waysMillis
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 系统hour间的milliseconds
     *
     * @return 系统hour间的milliseconds
     */
    public static long getMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 指定day历的milliseconds
     *
     * @param cal 指定day历
     * @return 指定day历的milliseconds
     */
    public static long getMillis(Calendar cal) {
        // --------------------return cal.getTimeInMillis();
        return cal.getTime().getTime();
    }

    /**
     * 指定day期的milliseconds
     *
     * @param date 指定day期
     * @return 指定day期的milliseconds
     */
    public static long getMillis(Date date) {
        return date.getTime();
    }

    /**
     * Specify timestamp的milliseconds
     *
     * @param ts Specify timestamp
     * @return Specify timestamp的milliseconds
     */
    public static long getMillis(Timestamp ts) {
        return ts.getTime();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // formatDate
    // Willday期按照一定的Format转化为string
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 默认方式表示的系统当前day期，具体Format：Year-moon-day
     *
     * @return 默认day期按“Year-moon-day“Format显示
     */
    public static String formatDate() {
        return date_sdf.get().format(getCalendar().getTime());
    }

    /**
     * 默认方式表示的系统当前day期，具体Format：yyyy-MM-dd HH:mm:ss
     *
     * @return 默认day期按“yyyy-MM-dd HH:mm:ss“Format显示
     */
    public static String formatDateTime() {
        return datetimeFormat.get().format(getCalendar().getTime());
    }

    /**
     * 获取hour间string
     */
    public static String getDataString(SimpleDateFormat formatstr) {
        synchronized (formatstr) {
            return formatstr.format(getCalendar().getTime());
        }
    }

    /**
     * 指定day期的默认显示，具体Format：Year-moon-day
     *
     * @param cal 指定的day期
     * @return 指定day期按“Year-moon-day“Format显示
     */
    public static String formatDate(Calendar cal) {
        return date_sdf.get().format(cal.getTime());
    }

    /**
     * 指定day期的默认显示，具体Format：Year-moon-day
     *
     * @param date 指定的day期
     * @return 指定day期按“Year-moon-day“Format显示
     */
    public static String formatDate(Date date) {
        return date_sdf.get().format(date);
    }

    /**
     * 指定milliseconds表示day期的默认显示，具体Format：Year-moon-day
     *
     * @param millis 指定的milliseconds
     * @return 指定milliseconds表示day期按“Year-moon-day“Format显示
     */
    public static String formatDate(long millis) {
        return date_sdf.get().format(new Date(millis));
    }

    /**
     * 默认day期按指定Format显示
     *
     * @param pattern 指定的Format
     * @return 默认day期按指定Format显示
     */
    public static String formatDate(String pattern) {
        return getSdFormat(pattern).format(getCalendar().getTime());
    }

    /**
     * 指定day期按指定Format显示
     *
     * @param cal     指定的day期
     * @param pattern 指定的Format
     * @return 指定day期按指定Format显示
     */
    public static String formatDate(Calendar cal, String pattern) {
        return getSdFormat(pattern).format(cal.getTime());
    }

    /**
     * 指定day期按指定Format显示
     *
     * @param date    指定的day期
     * @param pattern 指定的Format
     * @return 指定day期按指定Format显示
     */
    public static String formatDate(Date date, String pattern) {
        return getSdFormat(pattern).format(date);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // formatTime
    // Willday期按照一定的Format转化为string
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 默认方式表示的系统当前day期，具体Format：Year-moon-day hour：point
     *
     * @return 默认day期按“Year-moon-day hour：point“Format显示
     */
    public static String formatTime() {
        return time_sdf.get().format(getCalendar().getTime());
    }

    /**
     * 指定milliseconds表示day期的默认显示，具体Format：Year-moon-day hour：point
     *
     * @param millis 指定的milliseconds
     * @return 指定milliseconds表示day期按“Year-moon-day hour：point“Format显示
     */
    public static String formatTime(long millis) {
        return time_sdf.get().format(new Date(millis));
    }

    /**
     * 指定day期的默认显示，具体Format：Year-moon-day hour：point
     *
     * @param cal 指定的day期
     * @return 指定day期按“Year-moon-day hour：point“Format显示
     */
    public static String formatTime(Calendar cal) {
        return time_sdf.get().format(cal.getTime());
    }

    /**
     * 指定day期的默认显示，具体Format：Year-moon-day hour：point
     *
     * @param date 指定的day期
     * @return 指定day期按“Year-moon-day hour：point“Format显示
     */
    public static String formatTime(Date date) {
        return time_sdf.get().format(date);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // formatShortTime
    // Willday期按照一定的Format转化为string
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 默认方式表示的系统当前day期，具体Format：hour：point
     *
     * @return 默认day期按“hour：point“Format显示
     */
    public static String formatShortTime() {
        return short_time_sdf.get().format(getCalendar().getTime());
    }

    /**
     * 指定milliseconds表示day期的默认显示，具体Format：hour：point
     *
     * @param millis 指定的milliseconds
     * @return 指定milliseconds表示day期按“hour：point“Format显示
     */
    public static String formatShortTime(long millis) {
        return short_time_sdf.get().format(new Date(millis));
    }

    /**
     * 指定day期的默认显示，具体Format：hour：point
     *
     * @param cal 指定的day期
     * @return 指定day期按“hour：point“Format显示
     */
    public static String formatShortTime(Calendar cal) {
        return short_time_sdf.get().format(cal.getTime());
    }

    /**
     * 指定day期的默认显示，具体Format：hour：point
     *
     * @param date 指定的day期
     * @return 指定day期按“hour：point“Format显示
     */
    public static String formatShortTime(Date date) {
        return short_time_sdf.get().format(date);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // parseDate
    // parseCalendar
    // parseTimestamp
    // Willstring按照一定的Format转化为day期或hour间
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 根据指定的FormatWillstring转换成Date Such as input：2003-11-19 11:20:20Will按照这个转成hour间
     *
     * @param src     The original characters to be converted will be
     * @param pattern 转换的匹配Format
     * @return 如果转换成功则返回转换后的day期
     * @throws ParseException
     */
    public static Date parseDate(String src, String pattern) throws ParseException {
        return getSdFormat(pattern).parse(src);

    }

    /**
     * 根据指定的FormatWillstring转换成Date Such as input：2003-11-19 11:20:20Will按照这个转成hour间
     *
     * @param src     The original characters to be converted will be
     * @param pattern 转换的匹配Format
     * @return 如果转换成功则返回转换后的day期
     * @throws ParseException
     */
    public static Calendar parseCalendar(String src, String pattern) throws ParseException {

        Date date = parseDate(src, pattern);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static String formatAddDate(String src, String pattern, int amount) throws ParseException {
        Calendar cal;
        cal = parseCalendar(src, pattern);
        cal.add(Calendar.DATE, amount);
        return formatDate(cal);
    }

    /**
     * 根据指定的FormatWillstring转换成Date Such as input：2003-11-19 11:20:20Will按照这个转成hour间
     *
     * @param src     The original characters to be converted will be
     * @param pattern 转换的匹配Format
     * @return 如果转换成功则返回转换后的hour间戳
     * @throws ParseException
     */
    public static Timestamp parseTimestamp(String src, String pattern) throws ParseException {
        Date date = parseDate(src, pattern);
        return new Timestamp(date.getTime());
    }

    // ////////////////////////////////////////////////////////////////////////////
    // dateDiff
    // 计算两个day期之间的差值
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * 计算两个hour间之间的差值，Varies depending on the logo
     *
     * @param flag   Compute flag，表示按照Year/moon/day/hour/point/Seconds and other calculations
     * @param calSrc subtrahend
     * @param calDes 被subtrahend
     * @return 两个day期之间的差值
     */
    public static int dateDiff(char flag, Calendar calSrc, Calendar calDes) {

        long millisDiff = getMillis(calSrc) - getMillis(calDes);
        char year = 'y';
        char day = 'd';
        char hour = 'h';
        char minute = 'm';
        char second = 's';

        if (flag == year) {
            return (calSrc.get(Calendar.YEAR) - calDes.get(Calendar.YEAR));
        }

        if (flag == day) {
            return (int) (millisDiff / DAY_IN_MILLIS);
        }

        if (flag == hour) {
            return (int) (millisDiff / HOUR_IN_MILLIS);
        }

        if (flag == minute) {
            return (int) (millisDiff / MINUTE_IN_MILLIS);
        }

        if (flag == second) {
            return (int) (millisDiff / SECOND_IN_MILLIS);
        }

        return 0;
    }

    public static Long getCurrentTimestamp() {
        return Long.valueOf(DateUtils.yyyymmddhhmmss.get().format(new Date()));
    }

    /**
     * Stringtype Convert toDate, If the parameter length is10 转换Format”yyyy-MM-dd“ If the parameter length is19 转换Format”yyyy-MM-dd
     * HH:mm:ss“ * @param text Stringtype的hour间值
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            try {
                int length10 = 10;
                int length19 = 19;
                if (text.indexOf(SymbolConstant.COLON) == -1 && text.length() == length10) {
                    setValue(DateUtils.date_sdf.get().parse(text));
                } else if (text.indexOf(SymbolConstant.COLON) > 0 && text.length() == length19) {
                    setValue(DateUtils.datetimeFormat.get().parse(text));
                } else {
                    throw new IllegalArgumentException("Could not parse date, date format is error ");
                }
            } catch (ParseException ex) {
                IllegalArgumentException iae = new IllegalArgumentException("Could not parse date: " + ex.getMessage());
                iae.initCause(ex);
                throw iae;
            }
        } else {
            setValue(null);
        }
    }

    public static int getYear() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(getDate());
        return calendar.get(Calendar.YEAR);
    }

    /**
     * Willstring转成hour间
     * @param str
     * @return
     */
    public static Date parseDatetime(String str){
        try {
            return datetimeFormat.get().parse(str);
        }catch (Exception e){
        }
        return null;
    }

    /**
     * 判断两个hour间是否是同一天
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        boolean isSameYear = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
        boolean isSameMonth = isSameYear && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
        return isSameMonth && calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 计算与当前day期的hour间差
     *
     * @param targetDate
     * @return
     */
    public static long calculateTimeDifference(Date targetDate) {
        // 获取current time
        LocalDateTime currentTime = LocalDateTime.now();

        // Willjava.util.DateConvert tojava.time.LocalDateTime
        LocalDateTime convertedTargetDate = targetDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // 计算hour间差
        Duration duration = Duration.between(currentTime, convertedTargetDate);

        // 获取hour间差的milliseconds
        long timeDifferenceInMillis = duration.toMillis();

        return timeDifferenceInMillis;
    }

    /**
     * 计算与当前day期的day期天数差
     *
     * @param targetDate
     * @return
     */
    public static long calculateDaysDifference(Date targetDate) {
        // 获取当前day期
        LocalDate currentDate = LocalDate.now();
        // Willjava.util.DateConvert tojava.time.LocalDate
        LocalDate convertedTargetDate = targetDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        // 计算day期差
        long daysDifference = ChronoUnit.DAYS.between(currentDate, convertedTargetDate);
        return daysDifference;
    }

    /**
     * 判断两个hour间是否是同一周
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameWeek(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        boolean isSameYear = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
        return isSameYear && calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * 判断两个hour间是否是同一moon
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameMonth(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        boolean isSameYear = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
        return isSameYear && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
    }

    /**
     * 判断两个hour间是否是同一Year
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameYear(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
    }

    /**
     * 获取两个day期之间的所有day期列表，包含开始和结束day期
     *
     * @param begin
     * @param end
     * @return
     */
    public static List<Date> getDateRangeList(Date begin, Date end) {
        List<Date> dateList = new ArrayList<>();
        if (begin == null || end == null) {
            return dateList;
        }

        // 清除hour间部point，只比较day期
        Calendar beginCal = Calendar.getInstance();
        beginCal.setTime(begin);
        beginCal.set(Calendar.HOUR_OF_DAY, 0);
        beginCal.set(Calendar.MINUTE, 0);
        beginCal.set(Calendar.SECOND, 0);
        beginCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);
        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        if (endCal.before(beginCal)) {
            return dateList;
        }

        dateList.add(beginCal.getTime());
        while (beginCal.before(endCal)) {
            beginCal.add(Calendar.DAY_OF_YEAR, 1);
            dateList.add(beginCal.getTime());
        }
        return dateList;
    }

}