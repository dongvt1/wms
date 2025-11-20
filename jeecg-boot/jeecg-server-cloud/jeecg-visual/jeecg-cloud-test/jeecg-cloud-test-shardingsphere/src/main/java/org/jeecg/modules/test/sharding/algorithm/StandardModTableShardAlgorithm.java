package org.jeecg.modules.test.sharding.algorithm;


import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * For handling using a single key
 * Based on the value of the shard field andsharding-countPerform modulo operation
 * SQL There is in the statement>，>=, <=，<，=，IN and BETWEEN AND Operator，This sharding strategy can be applied to。
 *
 * @author zyf
 */
public class StandardModTableShardAlgorithm implements StandardShardingAlgorithm<Integer> {
    private Properties props = new Properties();


    /**
     * for processing=andINof shards
     *
     * @param collection           A collection of target shards(table name)
     * @param preciseShardingValue Logical table related information
     * @return
     */
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Integer> preciseShardingValue) {

        for (String name : collection) {
            Integer value = preciseShardingValue.getValue();
            //Modulo based on value，get a target value
            if (name.indexOf(value % 2+"") > -1) {
                return name;
            }
        }
        throw new UnsupportedOperationException();
    }

    /**
     * for processingBETWEEN ANDSharding，If not configuredRangeShardingAlgorithm，SQLinBETWEEN ANDWill be processed according to the whole database routing
     *
     * @param collection
     * @param rangeShardingValue
     * @return
     */
    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Integer> rangeShardingValue) {

        return collection;
    }

    /**
     * 对应Sharding算法（sharding-algorithms）type
     *
     * @return
     */
    @Override
    public String getType() {
        return "STANDARD_MOD";
    }
}