/**
* <p>FileName:VarUtil.java </p>
* <p>Description: VarUtil.java增删改操作</p>
* <p>Copyright: Copyright (c) 2016</p>
* <p>Company:yck</p>
* @author GuanJian
* @version revision: 1.0 2016年2月28日下午5:07:55 
*/

package com.yck.ktc.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>ClassName:VarUtil类</p>
 * <p>Description: VarUtil描述</p>
 * @author GuanJian
 * @version revision: 1.0 2016年2月28日下午5:07:55 
 */
public final  class FormatUtil {
	public static DateFormat _dfYmdHmss = new SimpleDateFormat("YYYYMMddhhmmssSSS");
	public static DateFormat _dfYmdHms = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public static int EMPTY_INT = Integer.MIN_VALUE;
	public static double EMPTY_double = Double.MIN_VALUE;
	public static boolean isEmpty(int v){
		return v == EMPTY_INT;
	}
	public static boolean isEmpty(double v){
		return Math.abs(v - Double.MIN_VALUE)<0.000001;
	}
	public static boolean isEmpty(String v){
		return v==null || v.trim().length() == 0;
	}
	/**
	 * @说明 判断一个字符串是否为空
	 * 使用org.apache.commons.lang
		org.apache.commons.lang.StringUtils;只能校验不含负号“-”的数字，即输入一个负数-199，输出结果将是false
		boolean is=StringUtils.isNumeric("aaa123456789");
	 */
	public static boolean isPhone(String str) {
		if (str == null  || "".equals(str.trim())) {
			return false;
		}
		
		if(str.length() != 11)
		{
			return false; 
		}
		Pattern pattern = Pattern.compile("[0-9]*"); 
		Matcher isNum = pattern.matcher(str);
		if( !isNum.matches() ){
		       return false; 
		 } 
		return true;
	}
	
	/**
	 * <p>MethodName: isPassword<p>
	 * <p>Description: 密码为6-20位数字或者下划线的任意组合<p>
	 * @param str
	 * @return boolean
	 * @see Class#Method
	 * @exception 无
	 */
	public static boolean isPasswordLegal(String str) {
		if (str == null || "".equals(str.trim())) {
			return false;
		}
		
		if(str.length() <6 || str.length() >20)
		{
			return false; 
		}
		Pattern pattern = Pattern.compile("[0-9A-Za-z_]*"); 
		Matcher isNum = pattern.matcher(str);
		if( !isNum.matches() ){
		       return false; 
		 } 
		return true;
	}
	public static double Distance(double long1, double lat1, double long2,
			double lat2) {
		double a, b, R;
		R = 6378137; // 地球半径
		lat1 = lat1 * Math.PI / 180.0;
		lat2 = lat2 * Math.PI / 180.0;
		a = lat1 - lat2;
		b = (long1 - long2) * Math.PI / 180.0;
		double d;
		double sa2, sb2;
		sa2 = Math.sin(a / 2.0);
		sb2 = Math.sin(b / 2.0);
		d = 2
				* R
				* Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1)
						* Math.cos(lat2) * sb2 * sb2));
		return d/1000;
	}
}
