package erwins.util.jdbc;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.IOUtils;

import erwins.util.collections.map.RequestMap;
import erwins.util.jdbc.TableInfos.ColumnInfo;
import erwins.util.jdbc.TableInfos.TableInfo;
import erwins.util.lib.FileUtil;
import erwins.util.lib.StringUtil;

/** 일단 각 DBMS별로 테이블 정보가 로드 된다면 SQL의 생성이 가능하다. */
public abstract class SqlGeneratorTemplate{
	
    /**
     * 각 Table별 DML을 출력하는 템플릿이다. 
     * 출력 String은 첫 문자에 공백이 없게 설정한다.
     */
	protected StringBuilder dml = new StringBuilder();
    protected final TableInfos generator;
    protected SqlGeneratorTemplate(TableInfos generator){
    	this.generator = generator;
    }
    public void toFile(String fileName) throws SQLException {
        for(TableInfo each : generator.tables){             
        	dml.append("---------------  ");
        	dml.append(each.name);
            if(each.comment!=null) dml.append("("+each.comment+")");
            dml.append("   ---------------");
            dml.append(IOUtils.LINE_SEPARATOR);
            writeEachTableSql(each);
            dml.append(IOUtils.LINE_SEPARATOR);
        }
        FileUtil.writeStr(dml.toString(), new File(fileName),"UTF-8");
        //Files.writeStr(RegEx.FIRST_BLANK.replace(dml.toString(),""), new File(fileName),"UTF-8");
    }
    protected abstract void writeEachTableSql(TableInfo table) throws SQLException;

    public static class MergeSql extends SqlGeneratorTemplate{
        String remoteName;
        public MergeSql(TableInfos generator,String remoteName){
        	super(generator);
            this.remoteName = remoteName;
        }
        /** key값은 무시 */
        @Override
        protected void writeEachTableSql(TableInfo table){
            dml.append("MERGE INTO ");
            dml.append(table.name+" t ");
            dml.append("USING "+table.name+"@"+remoteName+" r ");
            where(table);
            dml.append(IOUtils.LINE_SEPARATOR);
            update(table);
            dml.append(IOUtils.LINE_SEPARATOR);
            insert(table);
            dml.append(";");
            dml.append(IOUtils.LINE_SEPARATOR);
        }
        /** 비교 조건 입력 */
        private void where(TableInfo table) {
            dml.append(" ON ( ");
            boolean first = true;
            for(ColumnInfo each:table.columns){
            	if(each.isPk()) continue;
                if(first) first = false;
                else dml.append(" and ");
                dml.append(" t."+each.name+" = r."+each.name);
            }
            dml.append(" )");
        }   
        /** update문 */
        private void update(TableInfo table) {
            dml.append("WHEN MATCHED THEN ");
            dml.append(IOUtils.LINE_SEPARATOR);
            dml.append("UPDATE \n SET ");
            boolean first = true;
            for(ColumnInfo each:table.columns){
                if(each.isPk()) continue;
                if(first) first = false;
                else dml.append(",");
                dml.append(" t."+each.name+" = r."+each.name);
            }
        }   
        /** insert문 */
        private void insert(TableInfo table) {
            boolean first = true;
            dml.append("WHEN NOT MATCHED THEN ");
            dml.append(IOUtils.LINE_SEPARATOR);
            dml.append("INSERT ( ");
            for(ColumnInfo each:table.columns){
                if(first) first = false;
                else dml.append(",");
                dml.append(" t."+each.name);
            }
            dml.append(" ) \n VALUES ( ");
            first = true;
            for(ColumnInfo each:table.columns){
                if(first) first = false;
                else dml.append(",");
                dml.append(" r."+each.name);
            }           
            dml.append(" )");
        }        
    }
    
    public static class InsertSql extends SqlGeneratorTemplate{
		public InsertSql(TableInfos generator) {
			super(generator);
		}
		@Override
		protected void writeEachTableSql(TableInfo table) throws SQLException {
			List<RequestMap> list = generator.jdbc.select("SELECT * FROM " + table.name);
			for(RequestMap each : list){
				dml.append("INSERT INTO ");
	            dml.append(table.name );
	            dml.append("( ");
	            dml.append(StringUtil.join(each.keySet(),','));
	            dml.append(") ");
	            dml.append("VALUES ");
	            dml.append("( ");
	            boolean first = true;
	            for(Object value : each.values()){
	            	if(first) first = false;
	            	else dml.append(',');
	            	if(value==null || value instanceof Number){
	            		dml.append(value);
	            	}else{
	            		dml.append("'");
	            		dml.append(value);
	            		dml.append("'");
	            	}
	            }
	            dml.append(") ;");
	            dml.append(IOUtils.LINE_SEPARATOR);
			}
		}    	
    }    
}


