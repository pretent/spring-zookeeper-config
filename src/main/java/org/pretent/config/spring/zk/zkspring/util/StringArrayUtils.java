package org.pretent.config.spring.zk.zkspring.util;

public class StringArrayUtils {

	/**
	 * 给指定数组的每一个元素的指定位置插入指定得字符串,默认给第0的位置上插入,
	 * 
	 * 不会改变原数组内容
	 * 
	 * @param stringaArr
	 * @param str
	 */
	public static String[] insertString(String[] stringaArr, String str) {
		return insertString(stringaArr, str, 0);
	}

	/**
	 * 给指定数组的每一个元素的指定位置插入指定得字符串
	 * 
	 * @param arr
	 * @param str
	 */
	public static String[] insertString(String[] stringArr, String str, int postion) {
		StringBuilder sb = new StringBuilder();
		String[] retarr = new String[stringArr.length];
		for (int i = 0; i < stringArr.length; i++) {
			sb.delete(0, sb.length());
			sb.append(stringArr[i]);
			sb.insert(postion, str);
			retarr[i] = sb.toString();
			// stringaArr[i] = sb.toString();
		}
		return retarr;
	}
	
	/**
	 * 删除指定数组的每一个元素的指定字符串（字符串开始位置）
	 * @param stringArr
	 * @param str
	 * @return
	 */
	public static String[] removeString(String[] stringArr, String str) {
		StringBuilder sb = new StringBuilder();
		String[] retarr = new String[stringArr.length];
		for (int i = 0; i < stringArr.length; i++) {
			sb.delete(0, sb.length());
			if(!stringArr[i].contains(str)){
				continue;
			}
			sb.append(stringArr[i].substring(str.length()));
			retarr[i] = sb.toString();
			// stringaArr[i] = sb.toString();
		}
		return retarr;
	}
	

	public static void main(String[] args) {
		String str = "zk:";
		String[] files = { "/app.properties", "/app.xml", "/applicationContext.xml" };
		String[] added = insertString(files, str);
		for (String file : added) {
			System.out.println(file);
		}
		
		String[] removed = removeString(added, str);
		for (String file : removed) {
			System.out.println(file);
		}
	}
}
