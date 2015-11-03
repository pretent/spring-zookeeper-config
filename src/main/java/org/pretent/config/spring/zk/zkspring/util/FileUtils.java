package org.pretent.config.spring.zk.zkspring.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {

	/**
	 * 对比两个文本文件是否内容相等,过滤首尾空格和换行
	 * 
	 * 如果一个文件的本行是空行(空字符串或换行)，用本文件下一行和另一个文件的本行相比，直到本文件行不为空行为止
	 * 
	 * @param filea
	 * @param fileb
	 * @return
	 */
	public static boolean compare(String fileaPath, String filebPath) {
		File filea = new File(fileaPath);
		File fileb = new File(filebPath);
		try {
			if (MD5Util.getFileMD5String(filea).equals(MD5Util.getFileMD5String(fileb))) {
				return true;
			}
			BufferedReader readera = new BufferedReader(new FileReader(filea));
			BufferedReader readerb = new BufferedReader(new FileReader(fileb));
			return compareLine(readera, readerb);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 从两个输入流对比内容是否相同
	 * 
	 * @param iisa
	 * @param iisb
	 * @return
	 * @throws IOException
	 */
	public static boolean compare(InputStream iisa, InputStream iisb) throws IOException {
		return compareLine(new BufferedReader(new InputStreamReader(iisa)),
				new BufferedReader(new InputStreamReader(iisb)));
	}

	/**
	 * 逐行对比两个文件,如果一致则放回true，否则false，过滤空格和空行
	 * 
	 * @param readera
	 * @param readerb
	 * @return
	 * @throws IOException
	 */
	private static boolean compareLine(BufferedReader readera, BufferedReader readerb) throws IOException {
		String linea = readera.readLine();
		String lineb = readerb.readLine();
		// System.out.println("==========================================================================");
		// System.out.println("1>:" + linea);
		// System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// System.out.println("2>:" + lineb);
		if (linea == null && lineb == null) {
			readera.close();
			readerb.close();
			return true;
		}
		if (linea == null && lineb != null) {
			String other = readToEnd(readerb);
			readera.close();
			readerb.close();
			return other.trim().length() == 0;
		}
		if (linea != null && lineb == null) {
			String other = readToEnd(readera);
			readera.close();
			readerb.close();
			return other.trim().length() == 0;
		}
		if (MD5Util.getMD5String(linea).equals(MD5Util.getMD5String(lineb))) {
			// 两个文件同一行数据相等，直接比下一行
			// System.out.println("MD5相等，本行数据一致");
			return compareLine(readera, readerb);
		} else {
			// System.out.println("MD5不相等");
			// 如果两行md5不同，全部取空格再比
			if (MD5Util.getMD5String(linea.trim()).equals(MD5Util.getMD5String(lineb.trim()))) {
				// 两行都去空格相比，如果一致直接比下一行
				// System.out.println("去空格后MD5相等，本行数据一致");
				return compareLine(readera, readerb);
			} else {
				// System.out.println("去空格后MD5不相等");
				// System.out.println("linea.length---->" +
				// linea.trim().length());
				// System.out.println("lineb.length---->" +
				// lineb.trim().length());
				// 如果两行去空格还不一样,
				// 判断是否其中一行是空格或者换行
				if (linea.trim().length() == 0) {
					// System.out.println("第一个文件的本行数据长度为0，用下一行比第二个文件的本行");
					if (compareNextLine(readera, lineb)) {
						return compareLine(readera, readerb);
					} else {
						readera.close();
						readerb.close();
						return false;
					}
				} else if (lineb.trim().length() == 0) {
					// System.out.println("第二个文件的本行数据长度为0，用下一行比第一个文件的本行");
					if (compareNextLine(readerb, linea)) {
						return compareLine(readera, readerb);
					} else {
						readera.close();
						readerb.close();
						return false;
					}
				} else {
					// 否则数据肯定不一致
					// System.out.println("两行数据去空格长度都不为零，两行不一致");
					readera.close();
					readerb.close();
					return false;
				}
			}
		}
	}

	/**
	 * 用readera读取一行和linb进行比较，如果相等则返回true，否则false
	 * 
	 * @param readera
	 * @param linb
	 * @return
	 * @throws IOException
	 */
	private static boolean compareNextLine(BufferedReader readera, String linb) throws IOException {
		String lina = readera.readLine();
		// String linb = readerb.readLine();
		// System.out.println("===============================================");
		// System.out.println("1>:" + lina);
		// System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
		// System.out.println("2>:" + linb);
		if (lina == null) {
			return true;
		}
		if (MD5Util.getMD5String(lina).equals(MD5Util.getMD5String(linb))) {
			// System.out.println("MD5相等");
			return true;
		} else {
			// System.out.println("MD5不相等");
			// 如果两行md5不同，全部取空格再比
			if (MD5Util.getMD5String(lina.trim()).equals(MD5Util.getMD5String(linb.trim()))) {
				// 两行都去空格相比，如果一致直接比下一行
				// System.out.println("去空格后MD5相等，本行数据一致");
				return true;
			} else {
				// System.out.println("去空格后MD5不相等");
				// System.out.println("linea.length---->" +
				// lina.trim().length());
				// System.out.println("lineb.length---->" +
				// linb.trim().length());
				// 如果两行去空格还不一样,
				// 判断是否其中一行是空格或者换行
				if (lina.trim().length() == 0) {
					// System.out.println("第一个文件的本行数据长度为0，用下一行比第二个文件的本行");
					return compareNextLine(readera, linb);
				} else {
					// 否则数据肯定不一致
					// System.out.println("两行数据去空格长度都不为零，两行不一致");
					return false;
				}
			}
		}
	}

	/**
	 * 从reader里读取到文件结尾，并返回读取的的数据
	 * 
	 * @param reader
	 * @return
	 */
	private static String readToEnd(BufferedReader reader) {
		StringBuilder sb = new StringBuilder();
		String str = null;
		try {
			while ((str = reader.readLine()) != null) {
				sb.append(str.trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		// 对比这两个文件
		System.out.println(compare("/Users/user/app.xml", "/Users/user/app1.xml"));
	}
}
