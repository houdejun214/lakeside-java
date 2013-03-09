package com.lakeside.core.regex;

public class CommonPattern {
	public static final String CN = "[\u4e00-\u9fa5]+";
	public static final String EN = "[A-Za-z]+";
	public static final String DIGIT = "\\d+";
	public static final String EMAIL = "[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+";
	public static final String NUMBER = "-?([1-9]\\d+|\\d)(\\.\\d+)?";
	public static final String DECIMAL = "-?([1-9]\\d+|\\d)(\\.\\d+)";
	public static final String INT = "-?([1-9]\\d+|\\d)";
	public static final String POSTCODE = "[1-9]\\d{5}";
	public static final String HOMEPHONE = "[0][0-9]{2,3}-[0-9]{7,8}";
	public static final String MOBILEPHONE = "[1][3,5,8,6][0-9]{9}";
	//年份的基本样式 0001-9999
	public static final String YEAR = "([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]|[0-9][1-9][0-9]{2}|[1-9][0-9]{3})";
	public static final String LEAPYEAR = "((([0-9]{2})(0[48]|[2468][048]|[13579][26]))|((0[48]|[2468][048]|[3579][26])00))";
	private static final String month31 = "((0[13578]|1[02])(0[1-9]|[12][0-9]|3[01]))";
	private static final String month30 = "((0[469]|11)(0[1-9]|[12][0-9]|30))";
	private static final String month28 = "(02(0[1-9]|[1][0-9]|2[0-8]))";
	public static final String MONTHDAY = "(" + month31 + "|" + month30 + "|" + month28 + "|0229)";
	public static final String YEARMONTH = YEAR + "(0[1-9]|1[0-2])";
	public static final String DATE = "(" + YEAR + "(" + month31 + "|" + month30 + "|" + month28 + "))|(" + LEAPYEAR
			+ "0229)";
	public static final String TIME = "(([0,1][0-9])|2[0-3]):([0-5][0-9]):([0-5][0-9])";
	public static final String DATETIME = "(" + DATE + ") (" + TIME + ")";
	public static final String PROVINCENUM = "(11|12|13|14|15|21|22|23|31|32|33|34|35|36|37|41|42|43|44|45|46|50|51|52|53|54|61|62|63|64|65|71|81|82|91)";
	public static final String IDCARD = PROVINCENUM + "[0-9]{4}(" + DATE + ")[0-9]{3}([0-9]|x|X)";
	public static final String EMPTY = "\\s+";

}
