package com.xunce.signin.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * <P>
 * 日期操作类，包括格式化获取时间，获取当时时、分、秒等方法。
 * <P>
 * 
 * @author Sunny Ding
 * @version 1.00
 */
public class DateUtil {

	public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat DATE_FORMAT_DATE    = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * long time to string
	 *
	 * @param timeInMillis
	 * @param dateFormat
	 * @return
	 */
	public static String getTime(long timeInMillis, SimpleDateFormat dateFormat) {
		return dateFormat.format(new Date(timeInMillis));
	}

	/**
	 * long time to string, format is {@link #DEFAULT_DATE_FORMAT}
	 *
	 * @param timeInMillis
	 * @return
	 */
	public static String getTime(long timeInMillis) {
		return getTime(timeInMillis, DEFAULT_DATE_FORMAT);
	}

	/**
	 * get current time in milliseconds
	 *
	 * @return
	 */
	public static long getCurrentTimeInLong() {
		return System.currentTimeMillis();
	}

	/**
	 * get current time in milliseconds, format is {@link #DEFAULT_DATE_FORMAT}
	 *
	 * @return
	 */
	public static String getCurrentTimeInString() {
		return getTime(getCurrentTimeInLong());
	}

	/**
	 * get current time in milliseconds
	 *
	 * @return
	 */
	public static String getCurrentTimeInString(SimpleDateFormat dateFormat) {
		return getTime(getCurrentTimeInLong(), dateFormat);
	}
	/**
	 * 获取时间字符串，格式为：yyyy.mm.dd HH:mm
	 * 
	 * @param date Date类的实例
	 * 
	 * @return dataStr String类的实例
	 */
	public static String getStringFromCurrentTime(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		String dateStr = sdf.format(date);
		return dateStr;
	}
	
	/**
	 * 获取格式：yyyy年mm月dd日 HH:mm.
	 * 
	 * @param date
	 *            the date
	 * @return the date cn
	 */
	public static String getDateCN(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
		String dateStr = sdf.format(date);
		return dateStr;
	}

	public static String getFormatedDateTime(long dateTime) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMdd");
		return sDateFormat.format(new Date(dateTime + 0));
	}
	
	/**
	 * 获取当前时间的小时数
	 * 
	 * @return int 小时
	 */
	public static int getCurrentHour(){
		long time= System.currentTimeMillis();
		final Calendar mCalendar= Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		return mCalendar.get(Calendar.HOUR_OF_DAY);

	}
	
	/**
	 * 获取当前时间的分钟
	 * 
	 * @return int 分
	 */
	public static int getCurrentMin(){
		long time= System.currentTimeMillis();
		final Calendar mCalendar= Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		return mCalendar.get(Calendar.MINUTE);
	}
	
	/**
	 * 获取当前时间的秒
	 * 
	 * @return int 秒
	 */
	public static int getCurrentSec(){
		long time= System.currentTimeMillis();
		final Calendar mCalendar= Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		return mCalendar.get(Calendar.SECOND);
	}
	
	/**
	 * 分钟转换小时（整数值）
	 * 
	 * @param minuter
	 * @return int 小时
	 */
	public static int minCastToHour(int minuter){
		return minuter/60;
	}
	
	/**
	 * 分钟转换小时（求余值）
	 * 
	 * @param minuter
	 * @return int 求余值
	 */
	public static int minCastToHourMore(int minuter){
		return minuter%60;
	}
	
	/**
	 * 小时转换分钟
	 * 
	 * @param hour
	 * @return int 分钟
	 */
	public static int hourCastToMin(int hour){
		return hour*60;
	}

	public static int millCastToMin(long mill){
		return (int) (mill/(1000*60));
	}

	public static int getWeekOfYear(String date){

		try{
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			Calendar cal = Calendar.getInstance();
			//这一句必须要设置，否则美国认为第一天是周日，而我国认为是周一，对计算当期日期是第几周会有错误
			cal.setFirstDayOfWeek(Calendar.MONDAY); // 设置每周的第一天为星期一
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);// 每周从周一开始
			cal.setMinimalDaysInFirstWeek(1); // 设置每周最少为1天
			cal.setTime(df.parse(date));
			return cal.get(Calendar.WEEK_OF_YEAR);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
}
