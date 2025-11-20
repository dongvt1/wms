package org.jeecg.common.util;

import io.netty.util.internal.StringUtil;

/**
 * Serial number generation rules(Increment by default rules，Numbers from1-99Start incrementing，numbers to99，Increment letters;Not enough digits. Add more digits.)
 * A001
 * A001A002
 * @Author zhangdaihao
 *
 */
public class YouBianCodeUtil {

	// Number of digits(Default generated3digits)

    /**代表Number of digits*/
	private static final int NUM_LENGTH = 2;

	public static final int ZHANWEI_LENGTH = 1+ NUM_LENGTH;

	public static final char LETTER= 'Z';

	/**
	 * According to the previouscode，Get the next one at the same levelcode
	 * For example:Current maximumcodeforD01A04，Nextcodefor：D01A05
	 * 
	 * @param code
	 * @return
	 */
	public static synchronized String getNextYouBianCode(String code) {
		String newcode = "";
		if (oConvertUtils.isEmpty(code)) {
			String zimu = "A";
			String num = getStrNum(1);
			newcode = zimu + num;
		} else {
			String beforeCode = code.substring(0, code.length() - 1- NUM_LENGTH);
			String afterCode = code.substring(code.length() - 1 - NUM_LENGTH,code.length());
			char afterCodeZimu = afterCode.substring(0, 1).charAt(0);
			Integer afterCodeNum = Integer.parseInt(afterCode.substring(1));
//			org.jeecgframework.core.util.LogUtil.info(after_code);
//			org.jeecgframework.core.util.LogUtil.info(after_code_zimu);
//			org.jeecgframework.core.util.LogUtil.info(after_code_num);

			String nextNum = "";
			char nextZimu = 'A';
			// First determine whether the number is equal to999*，Then the count starts from1restart，Increment
			if (afterCodeNum == getMaxNumByLength(NUM_LENGTH)) {
				nextNum = getNextStrNum(0);
			} else {
				nextNum = getNextStrNum(afterCodeNum);
			}
			// First determine whether the number is equal to999*，Then the letters start fromArestart,Increment
			if(afterCodeNum == getMaxNumByLength(NUM_LENGTH)) {
				nextZimu = getNextZiMu(afterCodeZimu);
			}else{
				nextZimu = afterCodeZimu;
			}

			// For exampleZ99，Nextcodethat isZ99A01
			if (LETTER == afterCodeZimu && getMaxNumByLength(NUM_LENGTH) == afterCodeNum) {
				newcode = code + (nextZimu + nextNum);
			} else {
				newcode = beforeCode + (nextZimu + nextNum);
			}
		}
		return newcode;

	}

	/**
	 * according to fathercode,获取下级的Nextcode
	 * 
	 * For example：FatherCODE:A01
	 *       currentCODE:A01B03
	 *       acquiredcode:A01B04
	 *       
	 * @param parentCode   Superiorcode
	 * @param localCode    Same levelcode
	 * @return
	 */
	public static synchronized String getSubYouBianCode(String parentCode,String localCode) {
		if(localCode!=null && localCode!=""){

//			return parentCode + getNextYouBianCode(localCode);
			return getNextYouBianCode(localCode);

		}else{
			parentCode = parentCode + "A"+ getNextStrNum(0);
		}
		return parentCode;
	}

	

	/**
	 * Pad the number in front with zeros
	 * 
	 * @param num
	 * @return
	 */
	private static String getNextStrNum(int num) {
		return getStrNum(getNextNum(num));
	}

	/**
	 * Pad the number in front with zeros
	 * 
	 * @param num
	 * @return
	 */
	private static String getStrNum(int num) {
		String s = String.format("%0" + NUM_LENGTH + "d", num);
		return s;
	}

	/**
	 * Increment获取下个数字
	 * 
	 * @param num
	 * @return
	 */
	private static int getNextNum(int num) {
		num++;
		return num;
	}

	/**
	 * Increment获取下个字母
	 * 
	 * @param num
	 * @return
	 */
	private static char getNextZiMu(char zimu) {
		if (zimu == LETTER) {
			return 'A';
		}
		zimu++;
		return zimu;
	}
	
	/**
	 * 根据Number of digits获取最大值
	 * @param length
	 * @return
	 */
	private static int getMaxNumByLength(int length){
		if(length==0){
			return 0;
		}
        StringBuilder maxNum = new StringBuilder();
		for (int i=0;i<length;i++){
            maxNum.append("9");
		}
		return Integer.parseInt(maxNum.toString());
	}
	public static String[] cutYouBianCode(String code){
		if(code==null || StringUtil.isNullOrEmpty(code)){
			return null;
		}else{
			//获取标准长度fornumLength+1,截取的数量forcode.length/numLength+1
			int c = code.length()/(NUM_LENGTH +1);
			String[] cutcode = new String[c];
			for(int i =0 ; i <c;i++){
				cutcode[i] = code.substring(0,(i+1)*(NUM_LENGTH +1));
			}
			return cutcode;
		}
		
	}
//	public static void main(String[] args) {
//		// org.jeecgframework.core.util.LogUtil.info(getNextZiMu('C'));
//		// org.jeecgframework.core.util.LogUtil.info(getNextNum(8));
//	    // org.jeecgframework.core.util.LogUtil.info(cutYouBianCode("C99A01B01")[2]);
//	}
}
