package erwins.util.tools;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import oracle.jdbc.driver.OracleDriver;
import erwins.util.lib.Clazz;


/** 테스트용 간이 템플릿. 사용후 반드시 닫을것!
 *  Connection을 풀링하지 않는다면 conn만 닫으면 하위의 자원(Statement, ResultSet)도 함께 닫힌다.
 *  만약 풀링한다면 conn이 닫히지 않기 때문에 수많은 state들이 남아있어 공간이 부족해질 수 있다.
 *  여기에서는 임시 커넥션을 만을 사용함으로 명시적으로 닫지 않는다. 
 * */
public class JDBC{	
    
	//private static final String DRIVER_ORACLE = "oracle.jdbc.driver.OracleDriver" ;	
	//private static final String DRIVER_MSSQL = "com.microsoft.jdbc.sqlserver.SQLServerDriver";;
	private static final String URL_ORACLE = "jdbc:oracle:thin:@{0}:{1}:{2}" ;
	//private static final String URL_MSSQL = "jdbc:microsoft:sqlserver://211.255.6.117:1433;database=tjkasa";
	
	private static final String COUNT = "COUNT(*)";
	
	private Connection connection_oracle = null;
	
	/** oracle용 입니다. 
	 * @throws SQLException */
	public JDBC(String ip,String sid,String userId,String pass) throws SQLException {
		//Class.forName(URL_MSSQL);
        DriverManager.registerDriver(new OracleDriver());
        String url = MessageFormat.format(URL_ORACLE, ip,"1521",sid);
        connection_oracle = DriverManager.getConnection(url,userId, pass);
        connection_oracle.setAutoCommit(false);
	}
	
	public JDBC(String ip,String port,String sid,String userId,String pass)  throws SQLException {
		//Class.forName(URL_MSSQL);
        DriverManager.registerDriver(new OracleDriver());
        String url = MessageFormat.format(URL_ORACLE, ip,port,sid);
        connection_oracle = DriverManager.getConnection(url,userId, pass);
        connection_oracle.setAutoCommit(false);
	}	
	
	public void close(){
        try {
            connection_oracle.close();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }	    
	}
	
	public void commit() throws SQLException{
	    connection_oracle.commit();
	}
	
	public void rollback(){
        try {
            connection_oracle.rollback();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
	}
	
    public List<Mapp> select(String sql) throws SQLException{
        List<Mapp> results = new ArrayList<Mapp>();
        Statement statement_oracle = connection_oracle.createStatement();
        ResultSet resultSet = statement_oracle.executeQuery(sql);
        
        while(resultSet.next()){
            results.add(resultsetToMapp(resultSet));
        }
        resultSet.close();
        return results;
    }
    
    public <T> List<T> select(String sql,Class<T> clazz) throws SQLException{
    	List<T> results = new ArrayList<T>();
		Statement statement_oracle = connection_oracle.createStatement();
		ResultSet resultSet = statement_oracle.executeQuery(sql);
		
		while(resultSet.next()){
			results.add(resultsetToClass(resultSet,clazz));
		}
		resultSet.close();
    	return results;
    }
    
    
    /**
     *  select count(*) from user_tables 등의 소속 여부를 리턴한다.
     * @throws SQLException 
     */
    public boolean isContain(String sql) throws SQLException{
    	Statement statement_oracle = connection_oracle.createStatement();
        ResultSet resultSet = statement_oracle.executeQuery(sql);
        resultSet.next();
        boolean result = resultSet.getBoolean(COUNT);
        resultSet.close();
        return result;
    }
    
    public void execute(String sql) throws SQLException{
    	Statement statement_oracle = connection_oracle.createStatement();
        if(statement_oracle.execute(sql)) throw new SQLException(sql+" is fail");
    }
    
    public void execute(Collection<String> sqls) throws SQLException{
    	Statement statement_oracle = connection_oracle.createStatement();
    	for(String sql : sqls) if(statement_oracle.execute(sql)) throw new SQLException(sql+" is fail");
    }
    
    public int update(String sql) throws SQLException{
        Statement statement_oracle = connection_oracle.createStatement();
        int result = statement_oracle.executeUpdate(sql);
        return result;
    }
    public void updateOne(String sql) throws SQLException{
    	Statement statement_oracle = connection_oracle.createStatement();
    	int result = statement_oracle.executeUpdate(sql);
    	if(result!=0) throw new SQLException(sql+" 's result must be 1");
    }
    
    // =============================  static ====================================    
    
    /** ResultSet을 Map으로 매핑한다. */
    public static Map<String,Object> resultsetToLinkedHashMap(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int columnCount = meta.getColumnCount();
		Map<String,Object> resultMap = new LinkedHashMap<String,Object>(columnCount);
		for(int i = 1; i <= columnCount; i++){
			String key = lookupColumnName(meta, i);
			Object obj = getResultSetValue(rs, i);
			resultMap.put(key, obj);
		}
		return resultMap;
    }
    public static Mapp resultsetToMapp(ResultSet rs) throws SQLException {
    	ResultSetMetaData meta = rs.getMetaData();
    	int columnCount = meta.getColumnCount();
    	Mapp resultMap = new Mapp();
    	for (int i = 1; i <= columnCount; i++) {
    		String key = lookupColumnName(meta, i);
    		Object obj = getResultSetValue(rs, i);
    		resultMap.put(key, obj);
    	}
    	return resultMap;
    }
    /** field에 직접 매핑한다. 간단한것만 사용할것!. */
    public static <T> T resultsetToClass(ResultSet rs,Class<T> clazz) throws SQLException {
    	ResultSetMetaData meta = rs.getMetaData();
    	int columnCount = meta.getColumnCount();
    	T one = Clazz.instance(clazz);
    	for (int i = 1; i <= columnCount; i++) {
    		String key = lookupColumnName(meta, i);
    		Object obj = getResultSetValue(rs, i);
    		Clazz.setObject(one, key, obj);
    	}
    	return one;
    }
    
    /** 각 벤더의 고유한 처리 담당. */
    private static Object getResultSetValue(ResultSet rs, int index) throws SQLException {
		Object obj = rs.getObject(index);
		String className = null;
		if (obj != null) {
			className = obj.getClass().getName();
		}
		if (obj instanceof Blob) {
			obj = rs.getBytes(index);
		}
		else if (obj instanceof Clob) {
			obj = rs.getString(index);
		}
		else if (className != null &&
				("oracle.sql.TIMESTAMP".equals(className) ||
				"oracle.sql.TIMESTAMPTZ".equals(className))) {
			obj = rs.getTimestamp(index);
		}
		else if (className != null && className.startsWith("oracle.sql.DATE")) {
			String metaDataClassName = rs.getMetaData().getColumnClassName(index);
			if ("java.sql.Timestamp".equals(metaDataClassName) ||
					"oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
				obj = rs.getTimestamp(index);
			}
			else {
				obj = rs.getDate(index);
			}
		}
		else if (obj != null && obj instanceof java.sql.Date) {
			if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
				obj = rs.getTimestamp(index);
			}
		}
		return obj;
	}    
    
    /** 메타정보로 컬럼 이름을 반환한다. */
	public static String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
		String name = resultSetMetaData.getColumnLabel(columnIndex);
		if (name == null || name.length() < 1) {
			name = resultSetMetaData.getColumnName(columnIndex);
		}
		return name;
	}
}


