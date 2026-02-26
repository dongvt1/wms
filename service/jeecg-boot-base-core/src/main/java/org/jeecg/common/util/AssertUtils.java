package org.jeecg.common.util;


import org.jeecg.common.exception.JeecgBootAssertException;

/**
 * Assertion checking tool
 * for for [QQYUN-10990]AIRAG
 * @author chenrui
 * @date 2017-06-22 10:05:56
 */
public class AssertUtils {

    /**
     * Make sure the object is empty,Throws an exception if not empty
     *
     * @param msg
     * @param obj
     * @throws JeecgBootAssertException
     * @author chenrui
     * @date 2017-06-22 10:05:56
     */
    public static void assertEmpty(String msg, Object obj) {
        if (oConvertUtils.isObjectNotEmpty(obj)) {
            throw new JeecgBootAssertException(msg);
        }
    }


    /**
     * Make sure the object is not empty,Throws exception if empty
     *
     * @param msg
     * @param obj
     * @throws JeecgBootAssertException
     * @author chenrui
     * @date 2017-06-22 10:05:56
     */
    public static void assertNotEmpty(String msg, Object obj) {
        if (oConvertUtils.isObjectEmpty(obj)) {
            throw new JeecgBootAssertException(msg);
        }
    }


    /**
     * Verify that the objects are the same
     *
     * @param message
     * @param expected
     * @param actual
     * @author chenrui
     * @date 2018/9/12 15:45
     */
    public static void assertEquals(String message, Object expected,
                                    Object actual) {
        if (oConvertUtils.isEqual(expected, actual)) {
            return;
        }
        throw new JeecgBootAssertException(message);
    }

    /**
     * Authentication is not the same
     *
     * @param message
     * @param expected
     * @param actual
     * @author chenrui
     * @date 2018/9/12 15:45
     */
    public static void assertNotEquals(String message, Object expected,
                                       Object actual) {
        if (oConvertUtils.isEqual(expected, actual)) {
            throw new JeecgBootAssertException(message);
        }

    }

    /**
     * Verify equality
     *
     * @param message
     * @param expected
     * @param actual
     * @author chenrui
     * @date 2018/9/12 15:45
     */
    public static void assertSame(String message, Object expected,
                                  Object actual) {
        if (expected == actual) {
            return;
        }
        throw new JeecgBootAssertException(message);
    }

    /**
     * Validate not equal
     *
     * @param message
     * @param unexpected
     * @param actual
     * @author chenrui
     * @date 2018/9/12 15:45
     */
    public static void assertNotSame(String message, Object unexpected,
                                     Object actual) {
        if (unexpected == actual) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * Verify if it is true
     *
     * @param message
     * @param condition
     */
    public static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * verify conditionIs itfalse
     *
     * @param message
     * @param condition
     */
    public static void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }


    /**
     * verify是否存在
     *
     * @param message
     * @param obj
     * @param objs
     * @param <T>
     * @throws JeecgBootAssertException
     * @author chenrui
     * @date 2018/1/31 22:14
     */
    public static <T> void assertIn(String message, T obj, T... objs) {
        assertNotEmpty(message, obj);
        assertNotEmpty(message, objs);
        if (!oConvertUtils.isIn(obj, objs)) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * verify是否不存在
     *
     * @param message
     * @param obj
     * @param objs
     * @param <T>
     * @throws JeecgBootAssertException
     * @author chenrui
     * @date 2018/1/31 22:14
     */

    public static <T> void assertNotIn(String message, T obj, T... objs) {
        assertNotEmpty(message, obj);
        assertNotEmpty(message, objs);
        if (oConvertUtils.isIn(obj, objs)) {
            throw new JeecgBootAssertException(message);
        }
    }


    /**
     * make suresrcgreater thandes
     *
     * @param message
     * @param src
     * @param des
     * @author chenrui
     * @date 2018/9/19 15:30
     */
    public static void assertGt(String message, Number src, Number des) {
        if (oConvertUtils.isGt(src, des)) {
            return;
        }
        throw new JeecgBootAssertException(message);
    }

    /**
     * make suresrcgreater than等于des
     *
     * @param message
     * @param src
     * @param des
     * @author chenrui
     * @date 2018/9/19 15:30
     */
    public static void assertGe(String message, Number src, Number des) {
        if (oConvertUtils.isGe(src, des)) {
            return;
        }
        throw new JeecgBootAssertException(message);
    }


    /**
     * make suresrcless thandes
     *
     * @param message
     * @param src
     * @param des
     * @author chenrui
     * @date 2018/9/19 15:30
     */
    public static void assertLt(String message, Number src, Number des) {
        if (oConvertUtils.isGe(src, des)) {
            throw new JeecgBootAssertException(message);
        }
    }

    /**
     * make suresrcless than等于des
     *
     * @param message
     * @param src
     * @param des
     * @author chenrui
     * @date 2018/9/19 15:30
     */
    public static void assertLe(String message, Number src, Number des) {
        if (oConvertUtils.isGt(src, des)) {
            throw new JeecgBootAssertException(message);
        }
    }

}
