package logic;

import javax.sql.DataSource;

import database.DbSchema;
import database.PooledDataSourceWrapper;
import database.QueryMaker;

public class CreateAccountLogic {
	private String email, password, alias;
	private PooledDataSourceWrapper pooledDataSourceWrapper = PooledDataSourceWrapper.getInstance();
	
	public CreateAccountLogic(String email, String password, String alias) {
		this.email = email;
		this.password = password;
		this.alias = alias;
	}
	
	public boolean isPasswordSafe() {
		return password.length() < 8 || !passwordContainsOnlyLettersAndDigits();
	}

	private boolean passwordContainsOnlyLettersAndDigits() {
		char[] chars = password.toCharArray();
		
		for (int charIndex = 0; charIndex < chars.length; ++charIndex) {
			if (!charIsLetterOrDigit(chars[charIndex])) {
				return false;				
			}
		}
		
		return true;
	}

	private boolean charIsLetterOrDigit(char c) {
		return (c >= '0' && c <= '9')
				|| (c >= 'A' && c <= 'Z')
				|| (c >= 'a' && c <= 'z');
	}

	public boolean isAliasUnique() throws Exception {
		DataSource dataSource = pooledDataSourceWrapper.getPooledDataSource();
		return !QueryMaker.checkStringFieldFromTable(dataSource, DbSchema.ACCOUNT_TABLE_NAME, DbSchema.ALIAS_FIELD_NAME, alias);
	}

	public boolean isEmailUnique() throws Exception {
		DataSource dataSource = pooledDataSourceWrapper.getPooledDataSource();
		return !QueryMaker.checkStringFieldFromTable(dataSource, DbSchema.ACCOUNT_TABLE_NAME, DbSchema.EMAIL_FIELD_NAME, email);
	}

	public boolean createAccount() throws Exception {
		DataSource dataSource = pooledDataSourceWrapper.getPooledDataSource();
		return QueryMaker.insertIntoUserTable(dataSource, email, password, alias);
	}
}
