package org.jeecg.common.system.query;

import org.jeecg.common.util.oConvertUtils;

/**
 * Query rule constant
 * @Author Scott
 * @Date 2019Year02moon14day
 */
public enum QueryRuleEnum {

    /**查询rule greater than*/
    GT(">","gt","greater than"),
    /**查询rule greater thanequal*/
    GE(">=","ge","greater thanequal"),
    /**查询rule less than*/
    LT("<","lt","less than"),
    /**查询rule less thanequal*/
    LE("<=","le","less thanequal"),
    /**查询rule equal*/
    EQ("=","eq","equal"),
    /**查询rule 不equal*/
    NE("!=","ne","不equal"),
    /**查询rule Include*/
    IN("IN","in","Include"),
    /**查询rule full blur*/
    LIKE("LIKE","like","full blur"),
    /**查询rule 不模糊Include*/
    NOT_LIKE("NOT_LIKE","not_like","不模糊Include"),
    /**查询rule Blurry left*/
    LEFT_LIKE("LEFT_LIKE","left_like","Blurry left"),
    /**查询rule Right blur*/
    RIGHT_LIKE("RIGHT_LIKE","right_like","Right blur"),
    /**查询rule 带加号equal*/
    EQ_WITH_ADD("EQWITHADD","eq_with_add","带加号equal"),
    /**查询rule Multi-word fuzzy matching(and)*/
    LIKE_WITH_AND("LIKEWITHAND","like_with_and","Multi-word fuzzy matching————Not used yet"),
    /**查询rule Multi-word fuzzy matching(or)*/
    LIKE_WITH_OR("LIKEWITHOR","like_with_or","Multi-word fuzzy matching(or)"),
    /**查询rule CustomizeSQLfragment*/
    SQL_RULES("USE_SQL_RULES","ext","CustomizeSQLfragment"),

    /** query worksheet */
    LINKAGE("LINKAGE","linkage","query worksheet"),

    // ------- Exclusively within the current form designer -------
    /**查询rule Not with…ending*/
    NOT_LEFT_LIKE("NOT_LEFT_LIKE","not_left_like","Not with…ending"),
    /**查询rule Not with…beginning*/
    NOT_RIGHT_LIKE("NOT_RIGHT_LIKE","not_right_like","Not with…beginning"),
    /** Value is empty */
    EMPTY("EMPTY","empty","Value is empty"),
    /** value is not empty */
    NOT_EMPTY("NOT_EMPTY","not_empty","value is not empty"),
    /**查询rule 不Include*/
    NOT_IN("NOT_IN","not_in","不Include"),
    /**查询rule Exact multi-word match*/
    ELE_MATCH("ELE_MATCH","elemMatch","multi-word match"),
    /**查询rule Multiple word exact mismatch*/
    ELE_NOT_MATCH("ELE_NOT_MATCH","elemNotMatch","Multiple word exact mismatch"),
    /**查询rule range query*/
    RANGE("RANGE","range","range query"),
    /**查询rule Query not within range*/
    NOT_RANGE("NOT_RANGE","not_range","不在range query"),
    /** Customizemongodbquery statement */
    CUSTOM_MONGODB("CUSTOM_MONGODB","custom_mongodb","Customizemongodbquery statement");
    // ------- Exclusively within the current form designer -------

    private String value;
    
    private String condition; 

    private String msg;

    QueryRuleEnum(String value, String condition, String msg){
        this.value = value;
        this.condition = condition;
        this.msg = msg;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public static QueryRuleEnum getByValue(String value){
    	if(oConvertUtils.isEmpty(value)) {
    		return null;
    	}
        for(QueryRuleEnum val :values()){
            if (val.getValue().equals(value) || val.getCondition().equalsIgnoreCase(value)){
                return val;
            }
        }
        return  null;
    }
}
