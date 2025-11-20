package org.jeecg.modules.system.service;

import org.jeecg.modules.system.entity.SysDictItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  Service category
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
public interface ISysDictItemService extends IService<SysDictItem> {

    /**
     * via dictionaryidQuery dictionary item
     * @param mainId dictionaryid
     * @return
     */
    public List<SysDictItem> selectItemsByMainId(String mainId);
}
