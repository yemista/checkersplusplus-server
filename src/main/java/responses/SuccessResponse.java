package responses;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class SuccessResponse extends CheckersPlusPlusResponse {

	public SuccessResponse() {
		super(Status.OK);
	}
	
	@Override
	public String toString() {
		JsonPrimitive statusElement = new JsonPrimitive(status.toString());
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("statis", statusElement);
		return jsonObject.toString();	
	}
}
