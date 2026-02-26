package org.jeecg.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * online Interceptor permission judgment
 * cloud api The interface transfer object used
 * @author: jeecg-boot
 */
@Data
public class OnlineAuthDTO implements Serializable {
    private static final long serialVersionUID = 1771827545416418203L;


    /**
     * username
     */
    private String username;

    /**
     * Possible request addresses
     */
    private List<String> possibleUrl;

    /**
     * onlineDeveloped menu address
     */
    private String onlineFormUrl;

    //update-begin---author:chenrui ---date:20240123  for：[QQYUN-7992]【online】Work order applicationonlineform，Not configuredonlineform开发菜单，Operation error: No permissions------------
    /**
     * onlineThe address of the work order
     */
    private String onlineWorkOrderUrl;
    //update-end---author:chenrui ---date:20240123  for：[QQYUN-7992]【online】Work order applicationonlineform，Not configuredonlineform开发菜单，Operation error: No permissions------------

    public OnlineAuthDTO(){

    }

    public OnlineAuthDTO(String username, List<String> possibleUrl, String onlineFormUrl){
        this.username = username;
        this.possibleUrl = possibleUrl;
        this.onlineFormUrl = onlineFormUrl;
    }
}
