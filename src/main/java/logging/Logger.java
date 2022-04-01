package logging;

import java.sql.SQLException;

public class Logger {
	
	public void log(String message) {
		System.out.println(message);
	}

	public void log(Exception e) {
		System.out.println(e.getMessage());
	}

	public void log(Exception e, String message) {
		log(e);
		log(message);
	}
}
