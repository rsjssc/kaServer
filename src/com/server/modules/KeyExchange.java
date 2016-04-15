package com.server.modules;

public class KeyExchange {
	/**
	 * 将redis中设备的key转换成存在数据库中设备表中的device_code
	 * @param redis_device_key redis中设备的key
	 * @return
	 */
	public static String getDeviceCodeFromRedisDeviceKey(String redis_device_key){
		String device_code = redis_device_key.split("_")[2];
		return device_code;
	}
	
	/**
	 * 将redis中场所的key转换成存在数据库中场所表中的place_code
	 * @param redis_site_key
	 * @return
	 */
	public static String getSiteCodeFromRedisSiteKey(String redis_site_key){
		String place_code = redis_site_key.split("_")[2];
		return place_code;
	}
	
	/**
	 * 将数据库中场所表中的place_code转换成redis中场所的key
	 * @param device_code
	 * @return
	 */
	public static String getRedisDeviceKeyFromDeviceCode(String device_code){
		String redis_device_key = "status_device_" + device_code;
		return redis_device_key;
	}
	
	/**
	 * 将数据库中场所表中的place_code转换成redis中场所的key
	 * @param device_code
	 * @return
	 */
	public static String getRedisSiteKeyFromPlaceCode(String place_code){
		String redis_site_key = "status_site_" + place_code;
		return redis_site_key;
	}
	
	/**
	 * 将数据库中场所表中的place_code转换成redis中场所所包含的设备的集合的key
	 * @param device_code
	 * @return
	 */
	public static String getRedisSiteSetKeyFromDeviceCode(String place_code){
		String redis_site_set_key = "rel_site_" + place_code;
		return redis_site_set_key;
	}
}
