package com.starclub.syndicator.utils;

import org.json.JSONArray;
import org.json.JSONObject;


public class MyJSON {

	public static JSONArray addJSONArray(JSONArray orgArray,  JSONArray jarray) {
		if (orgArray == null) {
			return jarray;
		}
		if (jarray == null) {
			return orgArray;
		}
		
		try{
			for(int i=0;i<jarray.length();i++){     
				orgArray.put(jarray.get(i));     
			}
		}catch (Exception e){e.printStackTrace();}
		
		return orgArray;
		
	}

	public static JSONArray copyJSONArray(JSONArray jarray) {
		
		JSONArray Njarray=new JSONArray();
		try{
		for(int i=0;i<jarray.length();i++){     
			Njarray.put(jarray.get(i));     
		}
		}catch (Exception e){e.printStackTrace();}
		
		return Njarray;
		
	}
	
	
	public static JSONArray clearJSONArray(JSONArray jarray) {
		
		JSONArray Njarray=new JSONArray();
		
		return Njarray;
	}
	
	public static JSONArray removeJSONArray(JSONArray jarray, int pos) {

		JSONArray Njarray=new JSONArray();
		try{
		for(int i=0;i<jarray.length();i++){     
		    if(i!=pos)
		        Njarray.put(jarray.get(i));     
		}
		}catch (Exception e){e.printStackTrace();}
		
			
		return Njarray;

	}

	public static JSONArray removeJSONArray(JSONArray jarray, JSONObject obj) {

		JSONArray Njarray=new JSONArray();
		try{
		for(int i=0;i<jarray.length();i++){
			String key1 = obj.toString();
			String key2 = jarray.get(i).toString();
			if (!key1.equals(key2))
		        Njarray.put(jarray.get(i));     
		}
		}catch (Exception e){e.printStackTrace();}
		
			
		return Njarray;

	}

	public static JSONArray replaceJSONObject(JSONArray jarray, int pos, JSONObject obj) {


		JSONArray array = removeJSONArray(jarray, pos);


		try{
			for(int i=0;i<array.length();i++){
				if(i==pos) {
					array.put(obj);
					break;
				}
			}
		}catch (Exception e){e.printStackTrace();}

		return array;

	}

	public static int indexOfJSONArray(JSONArray orgArray,  JSONObject findObj) {
		if (orgArray == null) {
			return -1;
		}
		if (findObj== null) {
			return -1;
		}

		try{
			for(int i=0;i<orgArray.length();i++){
				Object obj = orgArray.get(i);

				if (obj.equals(findObj)) {
					return i;
				}
			}
		}catch (Exception e){e.printStackTrace();}

		return -1;

	}

	public static JSONArray addOneJSONArray(JSONArray orgArray,  JSONArray jarray) {
		if (orgArray == null) {
			return jarray;
		}
		if (jarray == null) {
			return orgArray;
		}
		
		try{
			for(int i=0;i<jarray.length();i++){
				Object obj = jarray.get(i);
				
				boolean bExist = false;
				for (int j = 0 ; j < orgArray.length() ; j ++) {
					if (orgArray.get(j).equals(obj)) {
						bExist = true;
						break;
					}
				}
				if (!bExist)
					orgArray.put(jarray.get(i));     
			}
		}catch (Exception e){e.printStackTrace();}
		
		return orgArray;
		
	}

	public static JSONArray addJSONObject(JSONArray orgArray,  JSONObject obj) {
		if (orgArray == null) {
			return null;
		}
		if (obj == null) {
			return orgArray;
		}

		try{

			orgArray.put(obj);

		}catch (Exception e){e.printStackTrace();}

		return orgArray;

	}
	
}


