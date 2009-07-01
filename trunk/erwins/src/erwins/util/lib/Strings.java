
package erwins.util.lib;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

/**
 * apache의 StringUtils에 없는것을 정의한다.
 */
public class Strings extends StringUtils {

    /**
     * strs들중 일부라도 매치가 되면 true를 리턴한다.
     */
    public static boolean isMatch(String body, String... strs) {
        for (String str : strs)
            if (Strings.contains(body, str)) return true;
        return false;
    }

    /**
     * strs들중 정확히 매치가 되면 true를 리턴한다.
     */
    public static boolean isEquals(String body, String... strs) {
        if (body == null) return false;
        for (String str : strs)
            if (body.equals(str)) return true;
        return false;
    }

    /**
     * strs들중 정확히 매치가 되면 true를 리턴한다. 대소문자를 구분하지 않는다.
     */
    public static boolean isEqualsIgnoreCase(String body, String... strs) {
        if (body == null) return false;
        for (String str : strs)
            if (body.equalsIgnoreCase(str)) return true;
        return false;
    }

    /**
     * Url은 '/'를 포함하는 root부터 시작한다. ex) /D:/qwe.qwe.go => 'D:/qwe.qwe' and 'go'
     */
    public static String[] getUrlAndExtention(HttpServletRequest req) {
        String requestedUrl = req.getRequestURI().substring(req.getContextPath().length());
        return getUrlAndExtention(requestedUrl);
    }

    /**
     * 첫번째 패턴까지의 문자열을 리턴한ㄷ. ex) getFirst("12345\qqq\asd","\n") => 12345
     */
    public static String getFirst(String str, String pattern) {
        return str.substring(0, str.indexOf(pattern));
    }

    /**
     * 마지막 패턴 이후로의 문자열을 리턴한다. ex) getFirst("12345\qqq\asd","\n") => asd
     */
    public static String getLast(String str, String pattern) {
        return str.substring(str.lastIndexOf(pattern) + 1);
    }

    /**
     * 확장자를 리턴한다. ex) getExtention("12345.qqq.asd") => "asd" ex)
     * getExtention("123asd") => ""
     */
    public static String getExtention(String str) {
        int index = str.lastIndexOf(".");
        if (index == -1) return "";
        return str.substring(index + 1);
    }

    /**
     * Url은 '/'를 포함하는 root부터 시작한다. ex) /D:/qwe.qwe.go => 'D:/qwe.qwe' and 'go'
     */
    public static String[] getUrlAndExtention(String url) {
        String[] str = new String[2];
        int index = url.lastIndexOf(".");
        if (index < 0) throw new RuntimeException(MessageFormat.format("[{0}] 확장자가 존재하지 않는 경로를 입력하셨습니다.", url));
        str[0] = url.substring(1, index);
        str[1] = url.substring(index + 1);
        return str;
    }

    /**
     * 다국어(한글등)이면 true를 리턴한다.
     */
    public static boolean isHan(char c) {
        if (Character.getType(c) == Character.OTHER_LETTER) { return true; }
        return false;
    }

    /**
     * 다국어(한글등)이면 true를 리턴한다.
     */
    public static boolean isHanAny(String str) {
        for (char c : str.toCharArray())
            if (Character.getType(c) == Character.OTHER_LETTER) { return true; }
        return false;
    }

    private static final String[] beanMethods = new String[] { "get", "set", "is" };

    /**
     * methodName(setter/geter/is)에서 fildName을 추출한다. 해당 조건이 아니면 null을 리턴한다.
     */
    public static String getFieldName(String name) {
        for (String type : beanMethods) {
            if (name.startsWith(type)) return getCamelize2(name, type);
        }
        return null;
    }

    private static final String[] getterMethods = new String[] { "get", "is" };

    /**
     * methodName(geter/is)에서 fildName을 추출한다. 해당 조건이 아니면 null을 리턴한다.
     */
    public static String getterName(String name) {
        for (String type : getterMethods) {
            if (name.startsWith(type)) return getCamelize2(name, type);
        }
        return null;
    }

    /**
     * 사업자 등록번호인지 체크한다.
     */
    public static boolean isBusinessId(String str) {
        String[] strs = str.split(EMPTY);
        if (strs.length != 11) return false;
        int[] ints = new int[10];
        for (int i = 0; i < 10; i++)
            ints[i] = Integer.valueOf(strs[i + 1]);
        int sum = 0;
        int[] indexs = new int[] { 1, 3, 7, 1, 3, 7, 1, 3 };
        for (int i = 0; i < 8; i++) {
            sum += ints[i] * indexs[i];
        }
        int num = ints[8] * 5;
        sum += (num / 10) + (num % 10);
        sum = 10 - (sum % 10);
        return sum == ints[9] ? true : false;
    }

    /**
     * 랜덤한 사업자 등록번호를 가져온다.
     */
    public static String makeRandomBid() {
        while (true) {
            String bid = Strings.makeRandomInt(10);
            if (Strings.isBusinessId(bid)) { return bid; }
        }
    }

    /**
     * 주민등록번호인지 체크한다. 1. 주민등록번호의 앞 6자리의 수에 처음부터 차례대로 2,3,4,5,6,7 을 곱한다. 그 다음, 뒤
     * 7자리의 수에 마지막 자리만 제외하고 차례대로 8,9,2,3,4,5 를 곱한다. 2. 이렇게 곱한 각 자리의 수들을 모두 더한다.
     * 3. 모두 더한 수를 11로 나눈 나머지를 구한다. 4. 이 나머지를 11에서 뺀다. 5. 이렇게 해서 나온 최종 값을
     * 주민등록번호의 마지막 자리 수와 비교해서 같으면 유효한 번호이고 다르면 잘못된 값이다.
     */
    public static boolean isSid(String input) {
        input = getNumericStr(input);

        if (input.length() != 13) throw new RuntimeException("주민등록번호 자리수 13자리를 확인하기 바랍니다.");

        // 입력받은 주민번호 앞자리 유효성 검증============================
        String leftSid = input.substring(0, 6);
        String rightSid = input.substring(6, 13);

        int yy = Integer.parseInt(leftSid.substring(0, 2));
        int mm = Integer.parseInt(leftSid.substring(2, 4));
        int dd = Integer.parseInt(leftSid.substring(4, 6));

        if (yy < 1 || yy > 99 || mm > 12 || mm < 1 || dd < 1 || dd > 31) return false;

        int digit1 = Integer.parseInt(leftSid.substring(0, 1)) * 2;
        int digit2 = Integer.parseInt(leftSid.substring(1, 2)) * 3;
        int digit3 = Integer.parseInt(leftSid.substring(2, 3)) * 4;
        int digit4 = Integer.parseInt(leftSid.substring(3, 4)) * 5;
        int digit5 = Integer.parseInt(leftSid.substring(4, 5)) * 6;
        int digit6 = Integer.parseInt(leftSid.substring(5, 6)) * 7;

        int digit7 = Integer.parseInt(rightSid.substring(0, 1)) * 8;
        int digit8 = Integer.parseInt(rightSid.substring(1, 2)) * 9;
        int digit9 = Integer.parseInt(rightSid.substring(2, 3)) * 2;
        int digit10 = Integer.parseInt(rightSid.substring(3, 4)) * 3;
        int digit11 = Integer.parseInt(rightSid.substring(4, 5)) * 4;
        int digit12 = Integer.parseInt(rightSid.substring(5, 6)) * 5;

        int last_digit = Integer.parseInt(rightSid.substring(6, 7));

        int error_verify = (digit1 + digit2 + digit3 + digit4 + digit5 + digit6 + digit7 + digit8 + digit9 + digit10 + digit11 + digit12) % 11;

        int sum_digit = 0;
        if (error_verify == 0) {
            sum_digit = 1;
        } else if (error_verify == 1) {
            sum_digit = 0;
        } else {
            sum_digit = 11 - error_verify;
        }

        if (last_digit == sum_digit) return true;
        return false;
    }

    /**
     * 랜덤한 주민등록번호를 가져온다.
     * 연령을 20~60세로 제한한다.
     */
    public static String makeRandomSid() {
        int yy = Days.YY.getIntValue();
        while (true) {
            String bid = Strings.makeRandomInt(13);
            int birth = Integer.parseInt(bid.substring(0,2));
            if (Strings.isSid(bid)) {
                int value = Integer.parseInt(String.valueOf(bid.charAt(6)));
                int age = yy - birth;
                switch(value){
                    case 1: case 2: age += 100;
                    case 3: case 4: break;
                    default : continue;
                }
                if(age > 20 && age < 60) return bid;
            }
        }
    }

    /**
     * 카멜 케이스를 "_" 형태로 연결한다. prototype의 underscore와는 달리 대문자 이다. ex) userName =>
     * USER_NAME
     */
    public static String getUnderscore(String str) {

        char[] chars = str.toCharArray();
        StringBuffer stringBuffer = new StringBuffer();
        for (char cha : chars) {
            if (cha >= 'A' && cha <= 'Z') stringBuffer.append('_');
            stringBuffer.append(cha);
        }
        return stringBuffer.toString().toUpperCase();
    }

    /**
     * "_" 형태의 연결을 카멜 케이스로 변환한다. ex) USER_NAME => userName
     */
    public static String getCamelize(String str) {
        char[] chars = str.toCharArray();
        boolean isUpper = false;
        StringBuffer stringBuffer = new StringBuffer();
        for (char cha : chars) {
            if (cha == '_' && cha == '-') {
                isUpper = true;
                continue;
            }
            if (isUpper) {
                stringBuffer.append(Character.toUpperCase(cha));
                isUpper = false;
            } else stringBuffer.append(Character.toLowerCase(cha));
        }
        return stringBuffer.toString();
    }

    /**
     * 두문자를 제거 후 첫 글자를 소문자로 바꾼다. ex) searchMapKey => mapKey
     */
    public static String getCamelize2(String str, String header) {
        return StringUtils.uncapitalize(str.replaceFirst(header, EMPTY));
    }

    /**
     * 분자열을 바이트 배열로 변형한다. 일단은 Cryptor에서만 사용한다. String 기본의 getByte와는 특수만자 입력시
     * 다르다. 왜인지는.. 몰라.
     **/
    public static byte[] getByte(String str) {
        char[] chs = str.toCharArray();
        byte[] bytes = new byte[chs.length];
        for (int i = 0; i < chs.length; i++) {
            bytes[i] = (byte) chs[i];
        }
        return bytes;
    }

    /**
     * 바이트배열을 문자형으로 변형한다.
     **/
    public static String getStr(byte[] bytes) {
        char[] chars = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            chars[i] = (char) bytes[i];
        }
        return String.valueOf(chars);
    }

    /**
     * n개의 랜덤 문자열을 가져온다. 개선의 여지가 있음.
     */
    public static String getRandomSring(int len) {
        String randomStr = "abcdefghijklmnopqrstuvwxyz123456789";
        StringBuffer strB = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < len; i++) {
            int rdIdx = random.nextInt(35);
            strB.append(randomStr.substring(rdIdx, rdIdx + 1));
        }
        return strB.toString();
    }

    /**
     * n개의 랜덤 숫자를 가져온다. 개선의 여지가 있음.
     */
    public static String makeRandomInt(int len) {
        StringBuffer strB = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < len; i++) {
            strB.append(random.nextInt(10));
        }
        return strB.toString();
    }

    /**
     * 입력 문자열을 ''로 묶은 후 ,로 구분한다. 영감&할멈 => '영감','할멈'
     */
    public static String getBundleStr(List<String> bundle, String defaultValue) {
        if (bundle == null || bundle.size() == 0) return defaultValue;
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bundle.size(); i++) {
            stringBuffer.append((i == 0) ? EMPTY : ",");
            stringBuffer.append("'");
            stringBuffer.append(bundle.get(i));
            stringBuffer.append("'");
        }
        return stringBuffer.toString();
    }

    /**
     * 문자열을 특정 문자의 개수를 구한다.
     */
    public static int getCharCount(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }

    /**
     * 문자열을 구분하여 화면에 보여질 문자의 길이값을 리턴한다. 폰트 브라우저 등에 따라 가변적임으로 알아서 조정하자.
     */
    public static int getStrLength(String str) {
        int strLength = 0;
        int hangleHtmlWidth = 20;
        int elseHtmlWidth = 12;
        for (int i = 0; i < str.length(); i++) {
            if (Character.getType(str.charAt(i)) == 5) {
                strLength += hangleHtmlWidth;
            } else {
                strLength += elseHtmlWidth;
            }
        }
        return strLength;
    }

    /**
     * 두개의 String을 받아 산술연산(+) 후 String으로 리턴한다. ex) plus('08','-2') => 06
     */
    public static String plus(String str, String str2) {
        int len = str.length();
        String result = StringCalculator.Plus(str, str2, 0);
        return StringUtils.leftPad(result, len, "0");
    }

    // ===========================================================================================
    //                                      숫자 치환            
    // ===========================================================================================    
    /**
     * String을 받아 숫자(절대값)형태만 추출해서 반환한다.
     */
    public static String getNumericStr(Object str) {
        if (str == null) return null;
        return getNumericStr(str.toString());
    }

    /**
     * String을 받아 숫자(절대값)형태만 추출해서 반환한다. 음수라면 따로 판별 컬럼을 나누자.
     * 
     * @return Parsing가능한 String값
     */
    public static String getNumericStr(String str) {
        if (str == null) return EMPTY;
        StringBuffer result = new StringBuffer();
        int dotCount = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c >= '0' && c <= '9')) result.append(c);
            else if ((c == '.' && dotCount < 1)) { //소수점 1개만 허용
                result.append(c);
                dotCount++;
            }
        }
        return result.toString();
    }

    /**
     * String을 받아 숫자(절대값)형태만 추출해서 반환. <br> null safe하다. 음수라면 따로 판별 컬럼을 나누자.
     */
    public static BigDecimal getDecimal(String str) {
        if (str == null || str.equals(EMPTY)) return BigDecimal.ZERO;
        String temp = Strings.getNumericStr(str);
        if (temp.equals(EMPTY)) return BigDecimal.ZERO;
        return new BigDecimal(temp);
    }

    /**
     * String을 받아 숫자(절대값)형태만 추출해서 반환. 음수라면 따로 판별 컬럼을 나누자.
     */
    public static double getDoubleValue(String str) {
        return Double.parseDouble(Strings.getNumericStr(str));
    }

    /**
     * String을 받아 숫자(절대값)형태만 추출해서 반환. 음수라면 따로 판별 컬럼을 나누자.
     */
    public static int getIntValue(String str) {
        return Integer.parseInt(Strings.getNumericStr(str));
    }

    /**
     * join시 디폴드값으로 ","를 준다.
     */
    /*
     * public static String join(List<?> list){ return join(list,","); }
     */

    /**
     * 배열을 seperators로 연결해서 반환한다. Weblogic 10.0/10.3에서 join사용(2.3이후버전)시 오류발생으로
     * 이것으로 대체
     */
    public static String joinTemp(List<?> list, String... seperators) {
        StringBuffer stringBuffer = new StringBuffer();
        String seperator = seperators.length == 0 ? "," : seperators[0]; // ""인가 ","인가.
        boolean first = true;
        for (Object string : list) {
            if (!first) stringBuffer.append(seperator);
            else first = false;
            stringBuffer.append(string);
        }
        return stringBuffer.toString();
    }

    /**
     * 배열을 seperators로 연결해서 반환한다. Weblogic 10.0/10.3에서 join사용(2.3이후버전)시 오류발생으로
     * 이것으로 대체
     */
    public static <T> String joinTemp(T[] list, String... seperators) {
        StringBuffer stringBuffer = new StringBuffer();
        String seperator = seperators.length == 0 ? "," : seperators[0];
        boolean first = true;
        for (Object string : list) {
            if (!first) stringBuffer.append(seperator);
            else first = false;
            stringBuffer.append(string);
        }
        return stringBuffer.toString();
    }

    // ===========================================================================================
    //                                      NVL            
    // ===========================================================================================

    /**
     * null 또는 공백문자사를 처리 stripToEmpty을 대신한다.
     */
    public static String nvl(String str) {
        return nvl(str, EMPTY);
    }

    /**
     * null 또는 공백문자사를 처리 stripToEmpty을 대신한다.
     */
    public static String nvl(String str, String defaultStr) {
        return StringUtils.isEmpty(str) ? defaultStr : str.trim();
    }

    public static Integer nvl(Integer integer) {
        return (integer == null) ? 0 : integer;
    }

    public static Integer nvl(String str, Integer defaultint) {
        return StringUtils.isEmpty(str) ? defaultint : Integer.parseInt(str.trim());
    }

    /**
     * null safe한 toString
     */
    public static String toString(Object str) {
        if (str == null) return EMPTY;
        else return str.toString();
    }

}
