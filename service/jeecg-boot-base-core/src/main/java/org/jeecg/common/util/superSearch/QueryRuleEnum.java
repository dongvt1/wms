//package org.jeecg.common.util.superSearch;
//
//import org.jeecg.common.util.oConvertUtils;
//
///**
// * Query rule constant
// * @Author Scott
// * @Date 2019Year02moon14day
// */
//public enum QueryRuleEnum {
//
//    /**查询rule greater than*/
//    GT(">","greater than"),
//    /**查询rule greater thanequal*/
//    GE(">=","greater thanequal"),
//    /**查询rule less than*/
//    LT("<","less than"),
//    /**查询rule less thanequal*/
//    LE("<=","less thanequal"),
//    /**查询rule equal*/
//    EQ("=","equal"),
//    /**查询rule 不equal*/
//    NE("!=","不equal"),
//    /**查询rule Include*/
//    IN("IN","Include"),
//    /**查询rule full blur*/
//    LIKE("LIKE","full blur"),
//    /**查询rule Blurry left*/
//    LEFT_LIKE("LEFT_LIKE","Blurry left"),
//    /**查询rule Right blur*/
//    RIGHT_LIKE("RIGHT_LIKE","Right blur"),
//    /**查询rule CustomizeSQLfragment*/
//    SQL_RULES("EXTEND_SQL","CustomizeSQLfragment");
//
//    private String value;
//
//    private String msg;
//
//    QueryRuleEnum(String value, String msg){
//        this.value = value;
//        this.msg = msg;
//    }
//
//    public String getValue() {
//        return value;
//    }
//
//    public void setValue(String value) {
//        this.value = value;
//    }
//
//    public String getMsg() {
//        return msg;
//    }
//
//    public void setMsg(String msg) {
//        this.msg = msg;
//    }
//
//    public static QueryRuleEnum getByValue(String value){
//    	if(oConvertUtils.isEmpty(value)) {
//    		return null;
//    	}
//        for(QueryRuleEnum val :values()){
//            if (val.getValue().equals(value)){
//                return val;
//            }
//        }
//        return  null;
//    }
//}
