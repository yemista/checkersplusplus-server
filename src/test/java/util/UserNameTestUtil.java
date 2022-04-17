package util;

public class UserNameTestUtil {
	private static int count = 1;
	
	public static String getTestUserName() {
		return String.format("test_%d", count++);
	}
}
