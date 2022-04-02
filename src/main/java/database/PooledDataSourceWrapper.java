package database;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.DataSources;

import logging.Logger;

public class PooledDataSourceWrapper {

	private static final String JDBC_URL = "jdbc:postgresql://localhost/checkersplusplus";
	private static final String USER_NAME = "checkers";
	private static final String PASSWORD = "Ch3ckers123!";
	
	private static PooledDataSourceWrapper me = null;
	
	private Logger logger = new Logger();
	private DataSource pooledDataSource = null;

	private PooledDataSourceWrapper() {
		DataSource ds_unpooled;
		
		try {
			ds_unpooled = DataSources.unpooledDataSource(JDBC_URL, USER_NAME, PASSWORD);
			pooledDataSource = DataSources.pooledDataSource(ds_unpooled);
		} catch (SQLException e) {
			logger.log("Failed to create datasource");
			logger.log(e);
		}
	}
	
	public static synchronized PooledDataSourceWrapper getInstance() {
		if (me == null) {
			me = new PooledDataSourceWrapper();
		}
		
		return me;
	}
	
	public DataSource getPooledDataSource() {
		return pooledDataSource;
	}

	public void shutdown() {
		try {
			DataSources.destroy(pooledDataSource);
		} catch (SQLException e) {
			logger.log("Failed to destroy datasource");
			logger.log(e);
		}
	}
}
