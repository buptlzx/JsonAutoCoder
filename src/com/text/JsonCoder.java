package com.text;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.beanutils.DynaBean;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

public class JsonCoder {

	public static String BuildFinalStringValue = "public static final String %1s = \"%2s\";\n";

	public static String BuildNormalValue = "public %1s %2s;\n";
	public static String ArrayValue = "ArrayList<%1s>";

	public static String BuildClass = "public class %1s extends %2s{\n";
	public static String BuildMainMethod = "public %1s (JSONObject json){%2s}";

	public static String FinalFile = "JsonFinalString";
	public static FileOutputStream mFileFileout;
	public static ArrayList<String> mFileStrings = new ArrayList<String>();

	public static void main(String[] args) {
		try {
			File mFileFile = new File(FinalFile + ".java");
			mFileFile.delete();
			if (!mFileFile.exists()) {
				mFileFile.createNewFile();
				mFileFileout = new FileOutputStream(mFileFile);
				mFileStrings.add(String.format("public class %1s {\n",
						FinalFile));

				buildNewClass("Luozex", getStringFromFile("source.txt"));

				mFileStrings.add("\n}");
				for (String s : mFileStrings)
					mFileFileout.write(s.getBytes());
			}
		} catch (Exception e) {
		}

	}

	public static void buildNewClass(String name, String content) {

		String str = "";

		StringBuffer valueStringBuf = new StringBuffer();
		StringBuffer methedStringBuf = new StringBuffer();

		mFileStrings.add("\n//" + name + "\n");

		JSONObject json = new JSONObject(content);
		Iterator keys = json.keys();
		while (keys.hasNext()) {
			String key = keys.next().toString();
			Object value = json.get(key);

			// write to JsonFinalString.java
			str = String.format(BuildFinalStringValue, getUpperStart(key), key);
			if (!mFileStrings.contains(str))
				mFileStrings.add(str);
			else
				mFileStrings.add("//" + str);

			str = getValueString(key, value);
			valueStringBuf.append(str);

			str = getJSONOpt(key, value);
			methedStringBuf.append(str);
		}

		try {
			File file = new File(name + ".java");
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			str = String.format(BuildClass, name, FinalFile);
			out.write(str.getBytes());
			// begin

			String valueStr = new String(valueStringBuf);
			out.write(valueStr.getBytes());

			String methStr = String.format(BuildMainMethod, name, new String(
					methedStringBuf));
			out.write(methStr.getBytes());

			// end
			str = "\n}";
			out.write(str.getBytes());
		} catch (Exception e) {
		}
	}

	public static String getStringFromFile(String path) {
		try {
			File file = new File(path);
			FileInputStream in = new FileInputStream(file);
			byte[] buf = new byte[(int) file.length()];
			in.read(buf);
			String str = new String(buf);
			return str;
		} catch (Exception e) {
			return null;
		}
	}

	public static String getValueString(String key, Object value) {
		String dataType = null;
		if (value instanceof JSONObject) {
			dataType = getUpperStart(key);
			buildNewClass(dataType, value.toString());
		} else if (value instanceof JSONArray) {
			String newClassName = key;
			if (key.endsWith("s"))
				newClassName = key.substring(0, key.length() - 1);
			newClassName = getUpperStart(newClassName);
			dataType = String.format(ArrayValue, newClassName);
			JSONObject jo = null;
			if (((JSONArray) value).length() > 0)
			{
				//此次需判断是否为JsonObject或String或int等
				Object obj = ((JSONArray) value).get(0);
				if (obj instanceof JSONObject)
					jo = ((JSONArray) value).getJSONObject(0);
				else
					dataType = String.format(ArrayValue, obj.getClass().getSimpleName());
			}else
				jo = new JSONObject();
			
			if (jo != null)
				buildNewClass(newClassName, jo.toString());

		} else if (value instanceof Integer) {
			dataType = "int";
		} else if (value instanceof Boolean) {
			dataType = "boolean";
		} else if (value instanceof Double) {
			dataType = "double";
		} else if (value instanceof Long) {
			dataType = "long";
		} else if (value instanceof String) {
			dataType = "String";
		} else {
			dataType = "String";
		}

		String str = String.format(BuildNormalValue, dataType, key);
		return str;
	}

	public static final String JSONOptNormal = "%1s = json.opt%2s(%3s);\n";
	public static final String JSONOptJsonObject = "%1s = new %2s(json.optJSONObject(%3s));\n";
	public static final String JSONOptJsonObjectArray = 
			"%1s = new ArrayList<%2s>();\n"
			+ "JSONArray %3sArray = json.optJSONArray(%4s);\n"
			+ "for(int i=0; i<%5sArray.length(); i++){\n"
			+ "%6s a = new %7s(%8sArray.getJSONObject(i));\n" + "%9s.add(a);\n}";
	
	public static final String JSONOptJsonNormalArray = 
			"%1s = new ArrayList<%2s>();\n"
			+ "JSONArray %3sArray = json.optJSONArray(%4s);\n"
			+ "for(int i=0; i<%5sArray.length(); i++){\n"
			+ "%6s a = (%7s)(%8sArray.get(i));\n" + "%9s.add(a);\n}";

	public static String getJSONOpt(String key, Object value) {
		String type = value.getClass().getSimpleName();
		if (type.equals(JSONNull.class.getSimpleName())) {
			type = "String";
		} else if (type.equals("Integer"))
			type = "Int";

		String str = "";

		if (value instanceof JSONObject) {
			// status = new Status(json.optJSONObject(Status));
			str = String.format(JSONOptJsonObject, key, getUpperStart(key),
					getUpperStart(key));

		} else if (value instanceof JSONArray) {
			
			Object obj = new JSONObject();
			if (((JSONArray) value).length()>0)
				obj = ((JSONArray) value).get(0);
			
			if (obj instanceof JSONObject)
			{
				// users = new ArrayList<User>();
				// JSONArray array = json.optJSONArray(key);
				// for(int i=0;i<array.length();i++)
				// {
				// User user = new User(array.get(i));
				// users.add(user);
				// }
				
				String newClassName = key;
				if (key.endsWith("s"))
					newClassName = key.substring(0, key.length() - 1);
				newClassName = getUpperStart(newClassName);

				// str = String.format(JSONOptJsonArray, key, type, newClassName);
				String Key = getUpperStart(key);
				str = String.format(JSONOptJsonObjectArray, key, newClassName, key, Key,
						key, newClassName, newClassName, key, key);
			}
			else
			{
				// users = new ArrayList<String>();
				// JSONArray array = json.optJSONArray(key);
				// for(int i=0;i<array.length();i++)
				// {
				// String user = array.get(i);
				// users.add(user);
				// }
				
				
			String sttr =	"%1s = new ArrayList<%2s>();\n"
				+ "JSONArray %3sArray = json.optJSONArray(%4s);\n"
				+ "for(int i=0; i<%5sArray.length(); i++){\n"
				+ "%6s a = (%7s)(%8sArray.get(i));\n" + "%9s.add(a);\n}";
			
				String newClassName = obj.getClass().getSimpleName();
				String Key = getUpperStart(key);
				str = String.format(JSONOptJsonNormalArray, key, newClassName, key, Key,
						key, newClassName, newClassName, key, key);
				
			}
			

		} else {
			str = String.format(JSONOptNormal, key, type, getUpperStart(key));
		}
		return str;
	}

	public static String getUpperStart(String name) {
		name = name.substring(0, 1).toUpperCase() + name.substring(1);
		return name;
	}
}
