package erwins.util.spring.batch.component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lombok.Data;

import org.springframework.jdbc.core.RowMapper;

import com.google.common.collect.Lists;

import erwins.util.spring.batch.JdbcCursorItemReaderMultirow;

/** SQL에 마지막 로우임을 알리는 마킹이 필요하다.
 *  @see JdbcCursorItemReaderMultirow 와 같이 사용된다.
 *  ex) , CASE WHEN KEYWORD = MAX(KEYWORD) OVER(PARTITION BY MEDIA_CATEGORY_ID  ) THEN 1 ELSE 0 END IS_LAST */
@Data
public class MultiRowMapper<T> implements RowMapper<List<T>>{
	
	private String key = "IS_LAST";
	private RowMapper<T> rowMapper;
	private List<T> list = Lists.newArrayList();
	
	public MultiRowMapper(RowMapper<T> rowMapper){
		this.rowMapper = rowMapper;
	}
	
	/** null이 리턴된다면 다시 매퍼를 호출하도록 해야한다. */
	@Override
	public List<T> mapRow(ResultSet arg0, int arg1) throws SQLException {
		list.add(rowMapper.mapRow(arg0, arg1));
		if(arg0.getBoolean(key)){
			List<T> result = list;
			list = Lists.newArrayList();
			return result;	
		}
		return null;
	}

}
