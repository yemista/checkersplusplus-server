package responses;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ErrorResponse extends CheckersPlusPlusResponse {
	private String message;
	
	public ErrorResponse(String message) {
		super(Status.ERROR);
		this.message = message;
	}
	
	@Override
	public String toString() {
		JsonPrimitive messageElement = new JsonPrimitive(message);
		JsonPrimitive statusElement = new JsonPrimitive(status.toString());
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("error", messageElement);
		jsonObject.add("statis", statusElement);
		return jsonObject.toString();	
	}
}
