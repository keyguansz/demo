/**
* <p>FileName:VarUtil.java </p>
* <p>Description: VarUtil.java增删改操作</p>
* <p>Copyright: Copyright (c) 2016</p>
* <p>Company:yck</p>
* @author GuanJian
* @version revision: 1.0 2016年2月28日下午5:07:55 
*/

package kg.ddemo.mapdemo.util;

import java.util.Locale;

public final  class FormatUtil {
	public static String formatMB(long size){
		return String.format(Locale.US,"大小:%.2fMB",Math.ceil(size/(1024 * 1024f)));
	}
}
