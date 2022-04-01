package responses;

public enum Status {
	OK("ok"),
	ERROR("error"),
	;
	
	private String status;
	
	Status(String status) {
		this.status = status;
	}
	
	@Override 
	public String toString() {
		return status;
	}
}
