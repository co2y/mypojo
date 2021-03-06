package erwins.util.dateTime;

import lombok.Data;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import erwins.util.dateTime.JodaUtil.Joda;

/**
 * BETWEEN  으로 조회할거면 end에 -1 해야하고 
 * NULL 체크 후 비교할거면 마이바티스 기준 태그를 적절히 사용하자.
 * @RangeVo 같은거도 활용
 * @author sin
 */
@Data
public class DateTimeVo{
    
    private DateTime start;
    private DateTime end;
    
    /** 어제포함 interval일간  (오늘 미포함) */
    public static DateTimeVo fromYesterday(int interval) {
    	return from(new LocalDate(),interval);
    }
	
	/** 
	 * 기준일로부터 X일 이후부터 기준일까지
	 * basicDate가 오늘이면 어제저녁 00시 가 end
	 * interval이 1이면 1일간의 자료 비교
	 *  */
	public static DateTimeVo from(LocalDate basicDate,int interval) {
		DateTimeVo vo = new DateTimeVo();
		vo.end = basicDate.toDateTime(LocalTime.MIDNIGHT);
		vo.start = vo.end.minusDays(interval);
		return vo;
	}
	
	@Test
	public void test(){
		DateTimeVo a = DateTimeVo.fromYesterday(2);
		System.out.println(Joda.TIME_KR.get(a.getStart()));
		System.out.println(Joda.TIME_KR.get(a.getEnd()));
	}

    
}
