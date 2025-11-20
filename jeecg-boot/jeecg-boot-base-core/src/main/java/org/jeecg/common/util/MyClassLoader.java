package org.jeecg.common.util;

import org.jeecg.common.constant.SymbolConstant;

/**
 * @Author  Zhang Daihao
 */
public class MyClassLoader extends ClassLoader {
	public static Class getClassByScn(String className) {
		Class myclass = null;
		try {
			myclass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(className+" not found!");
		}
		return myclass;
	}

    /**
     * Get the full name of the class，include package name
     * @param object
     * @return
     */
	public static String getPackPath(Object object) {
		// Check whether the parameters passed in by the user are empty
		if (object == null) {
			throw new java.lang.IllegalArgumentException("Parameter cannot be empty！");
		}
		// Get the full name of the class，include package name
		String clsName = object.getClass().getName();
		return clsName;
	}

	public static String getAppPath(Class cls) {
		// Check whether the parameters passed in by the user are empty
		if (cls == null) {
			throw new java.lang.IllegalArgumentException("Parameter cannot be empty！");
		}
		ClassLoader loader = cls.getClassLoader();
		// Get the full name of the class，include package name
		String clsName = cls.getName() + ".class";
		// Get the package where the incoming parameters are located
		Package pack = cls.getPackage();
		String path = "";
		// If it is not an anonymous package，Convert package name to path
		if (pack != null) {
			String packName = pack.getName();
			String javaSpot="java.";
			String javaxSpot="javax.";
			// Here we simply determine whether it isJavaBasic class library，Prevent users from passing inJDKBuilt-in class library
			if (packName.startsWith(javaSpot) || packName.startsWith(javaxSpot)) {
				throw new java.lang.IllegalArgumentException("Do not transfer system classes！");
			}
			// in the name of the class，Remove the package name part，Get the file name of the class
			clsName = clsName.substring(packName.length() + 1);
			// Determine whether the package name is a simple package name，in the case of，Then directly convert the package name into a path，
			if (packName.indexOf(SymbolConstant.SPOT) < 0) {
				path = packName + "/";
			} else {
                // Otherwise, according to the components of the package name，Convert package name to path
				int start = 0, end = 0;
				end = packName.indexOf(".");
				StringBuilder pathBuilder = new StringBuilder();
				while (end != -1) {
                    pathBuilder.append(packName, start, end).append("/");
					start = end + 1;
					end = packName.indexOf(".", start);
				}
				if(oConvertUtils.isNotEmpty(pathBuilder.toString())){
                    path = pathBuilder.toString();
                }
				path = path + packName.substring(start) + "/";
			}
		}
		// callClassLoaderofgetResourcemethod，传入包含路径信息of类文件名
		java.net.URL url = loader.getResource(path + clsName);
		// fromURLGet path information from object
		String realPath = url.getPath();
		// 去掉路径信息中of协议名"file:"
		int pos = realPath.indexOf("file:");
		if (pos > -1) {
			realPath = realPath.substring(pos + 5);
		}
		// 去掉路径信息最后包含类文件信息of部分，得到类所在of路径
		pos = realPath.indexOf(path + clsName);
		realPath = realPath.substring(0, pos - 1);
		// If the class file is packaged intoJARWaiting for the file to arrive，去掉对应ofJARWait for the package file name
		if (realPath.endsWith(SymbolConstant.EXCLAMATORY_MARK)) {
			realPath = realPath.substring(0, realPath.lastIndexOf("/"));
		}
		/*------------------------------------------------------------  
		 ClassLoaderofgetResourcemethod使用了utf-8Path information is encoded，when path  
		  When there are Chinese characters and spaces in，He will convert these characters，so，得到of往往不是我们想要  
		  of真实路径，here，call了URLDecoderofdecodemethod进行解码，以便得到原始of  
		  Chinese and space path  
		-------------------------------------------------------------*/
		try {
			realPath = java.net.URLDecoder.decode(realPath, "utf-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return realPath;
	}// getAppPathEnd of definition
}
