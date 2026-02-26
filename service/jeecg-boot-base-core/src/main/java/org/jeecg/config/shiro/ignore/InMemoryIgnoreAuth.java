package org.jeecg.config.shiro.ignore;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import java.util.ArrayList;
import java.util.List;

/**
 * Use in-memory storage via@IgnoreAuthannotatedurl，CooperateJwtFilterPerform login-free verification
 * PS：not availableThreadLocalstore，becauseThreadLocalwhile loading，JwtFilterAlready initialized，Causes this class to obtainThreadLocalis empty
 * @author eightmonth
 * @date 2024/4/18 15:02
 */
public class InMemoryIgnoreAuth {
    private static final List<String> IGNORE_AUTH_LIST = new ArrayList<>();

    private static PathMatcher MATCHER = new AntPathMatcher();
    public InMemoryIgnoreAuth() {}

    public static void set(List<String> list) {
        IGNORE_AUTH_LIST.addAll(list);
    }

    public static List<String> get() {
        return IGNORE_AUTH_LIST;
    }

    public static void clear() {
        IGNORE_AUTH_LIST.clear();
    }

    public static boolean contains(String url) {
        for (String ignoreAuth : IGNORE_AUTH_LIST) {
            if(MATCHER.match(ignoreAuth,url)){
                return true;
            }
        }

        return false;
    }
}
