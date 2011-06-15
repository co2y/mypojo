package erwins.gsample.dsl



import org.junit.Test
import erwins.util.tools.*
import groovy.time.TimeCategory;

/** Groovy의 문법들. */
public class DSL{
	
	/** 디폴트 재정의 */
	@Test
	public void calendar(){
		use(TimeCategory){
			Date re = 1.week.from.now;
			assert re.toString().contains('20');
		}
   }
  
    /** use키워드를 사용하면 블럭구간동안 기본연산을 재정의 할 수 있다. */
    @Test
    public void use(){
        use (StringCalculationCategory.class) {
            assert 19    == '10' + '9'
            assert 2    == '1' + '1'
            assert 'x1' == 'x' + '1'
        }
    }
	
	/** 예제2 약간 다르다. */
	@Test
	public void category2(){
		use(NumberCategory2) {
			def dist = 300.meters
			assert dist instanceof Distance2
			assert dist.toString() == "300m"
		}
	}
	
	/** 1.5에서 1.7로 올라가면서 toString()에 변화가 생긴듯? */
	@Test
	public void closure(){
		// 이 USE키워드를 사용하면 구간 내의 모든 객체가 해당 클래스의 메소드를 사용할 수 있게 된다.
		use(DistanceCategory.class) {
			def d1 = 1.m
			def d2 = 1.yd
			def d3 = 1760.yd
			def d4 = 100.cm
			assert (d1 + 1.yd).toString() == '1.9144 meter' ;
			assert (1.yd + 1.mi).toString() == '1761 yard'
			assert (1.m - 1.yd).toString() == '0.0856 meter'
			assert d2.m.toString() == '0.9144 meter'
			assert d3.mi.toString() == '1 mile(s)'
			assert d4.m.toString() =='1 meter'
			assert 1000.yd.km.toString() =='0.9144000 kilometer'
			assert 1000.yd.toString() =='1000 yard'
		}
	}
	
	/** 구글링한 빌더. 나중에 수정해서 사용해보자 */
	@Test
	void sqlBuilder(){
		def sql = new SqlBuilder();
		Select select = sql.select ("table1") {
			join("table2", type: "INNER") {
				using(table1: "col1", table2: "col1")
			}
			join("table3", type: "OUTER") {
				using(table1: "col2", table2: "col2")
				using(table1: "col3", table2: "col3")
			}
			where "table1.col1 = 'test'"
			groupBy(table1: "col1", table2: "col1")
			orderBy(table1: "col1", table2: "col1")
		}
		assert select.getSql() != "";
	}
	
	/** 새로 추가된 신개념 DSL~ 직관적이고 멋지다.  */
	@Test
	void test2(){
		def please =  { action ->
			[the: { what ->
					[of: { n ->
							action(what(n))
						}]
				}]
		}
		def show = { it }
		def square_root = { Math.sqrt(it) }
		def qwe = please show the square_root of 100             // equivalent to: please(show).the(square_root).of(100)
		assert qwe == 10.0
	}
	
}

final class Distance2 {
	def number
	String toString() { "${number}m" }
}
/** @Category를 사용해서 static이 아니게 만들 수 있다. (this사용 가능)  */
@Category(Number)
class NumberCategory2 {
	Distance2 getMeters() {
		new Distance2(number: this)
	}
}
