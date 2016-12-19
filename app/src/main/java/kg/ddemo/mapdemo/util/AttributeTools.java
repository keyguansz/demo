package kg.ddemo.mapdemo.util;

import java.lang.reflect.Method;

public final  class AttributeTools {
	
  Method method = null;	
  public AttributeTools(){
	  
  }


 /* public  Object getEntityAttributeValue(List list, String filedname){
	  try {
		   // 遍历集合
		   for (Object object : list) {
		        Class objectClass = object.getClass();// 获取List集合中的对象类型
		        Field[] fields = objectClass.getDeclaredFields();// 获取它的字段数组

		        for (Field field : fields) {
		             filedname = field.getName();// 得到字段名，
		             // 根据字段名找到对应的get方法，null表示无参数
		             method = objectClass.getMethod("get" + change(filedname), null);
		             
		             // 比较是否在字段数组中存在filedname字段，如果不存在短路，如果存在继续判断该字段的get方法是否存在，同时存在继续执行
		             if ("name".equals(filedname) && method != null) {
		            	 // 调用该字段的get方法
		                  Object name = method.invoke(object, null);
		                  System.out.print("姓名:" + name);// 输出结果
		             }

		             if ("sex".equals(filedname) && method != null) {// 同上
		                  Object sex = method.invoke(object, null);
		                  System.out.println("\t性别:" + sex);
		             }
		        }
		       }
		  } catch (SecurityException e) {
		   e.printStackTrace();
		  } catch (NoSuchMethodException e) {
		   e.printStackTrace();
		  } catch (IllegalArgumentException e) {
		   e.printStackTrace();
		  } catch (IllegalAccessException e) {
		   e.printStackTrace();
		  } catch (InvocationTargetException e) {
		   e.printStackTrace();
		  }
 }
 


 *//**
  * @param src 源字符串
  * @return 字符串，将src的第一个字母转换为大写，src为空时返回null
  *//*
 public static String change(String src) {
      if (src != null) {
           StringBuffer sb = new StringBuffer(src);
           sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
           return sb.toString();
      } 
      else {
           return null;
      }
  }*/
}


