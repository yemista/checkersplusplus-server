package logic;

import javax.sql.DataSource;

import database.PooledDataSourceWrapper;
import database.UsersDao;

public class CreateAccountLogic {
	private String email, password, alias;
	private PooledDataSourceWrapper pooledDataSourceWrapper = PooledDataSourceWrapper.getInstance();
	private UsersDao usersDao;
	
	public CreateAccountLogic(String email, String password, String alias) {
		this.email = email;
		this.password = password;
		this.alias = alias;
		usersDao = new UsersDao(pooledDataSourceWrapper.getPooledDataSource());
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
		return !usersDao.isAliasInUse(alias);
	}

	public boolean isEmailUnique() throws Exception {
		DataSource dataSource = pooledDataSourceWrapper.getPooledDataSource();
		return !usersDao.isEmailInUse(email);
	}

	public boolean createAccount() throws Exception {
		DataSource dataSource = pooledDataSourceWrapper.getPooledDataSource();
		return usersDao.createUser(email, password, alias);
	}
}
