package org.jeecg.common.system.enhance;

import java.util.List;

/**
 * User enhancement
 */
public interface UserFilterEnhance {
    
    /**
     * Get userid
     * @param loginUserId Currently logged in userid
     * 
     * @return List<String> Return multiple usersid
     */
    default List<String> getUserIds(String loginUserId) {
        return null;
    }
}
