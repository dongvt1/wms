package org.jeecg.modules.system.vo.thirdapp;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Synchronization result information，Contains success and failure information
 *
 * @author sunjianlei
 */
@Data
public class SyncInfoVo {

    /**
     * success message
     */
    private List<String> successInfo;
    /**
     * failure message
     */
    private List<String> failInfo;

    public SyncInfoVo() {
        this.successInfo = new ArrayList<>();
        this.failInfo = new ArrayList<>();
    }

    public SyncInfoVo(List<String> successInfo, List<String> failInfo) {
        this.successInfo = successInfo;
        this.failInfo = failInfo;
    }

    public SyncInfoVo addSuccessInfo(String info) {
        this.successInfo.add(info);
        return this;
    }

    public SyncInfoVo addFailInfo(String info) {
        this.failInfo.add(info);
        return this;
    }
}
