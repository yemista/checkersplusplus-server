package com.checklersplusplus.server.entities.request;

import java.io.Serializable;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public class Move implements Serializable {
	
	@Positive(message = "Invalid coordinate for start column.")
	@Max(value = 7, message = "Invalid coordinate for start column.")
	@Min(value = 0, message = "Invalid coordinate for start column.")
	private int startCol;
	
	@Positive
	@Max(value = 7, message = "Invalid coordinate for start row.")
	@Min(value = 0, message = "Invalid coordinate for start row.")
	private int startRow;
	
	@Positive
	@Max(value = 7, message = "Invalid coordinate for end column.")
	@Min(value = 0, message = "Invalid coordinate for end column.")
	private int endCol;
	
	@Positive
	@Max(value = 7, message = "Invalid coordinate for end row.")
	@Min(value = 0, message = "Invalid coordinate for end row.")
	private int endRow;
	
	public Move(int startCol, int startRow, int endCol, int endRow) {
		super();
		this.startCol = startCol;
		this.startRow = startRow;
		this.endCol = endCol;
		this.endRow = endRow;
	}
	
	public int getStartCol() {
		return startCol;
	}
	
	public void setStartCol(int startCol) {
		this.startCol = startCol;
	}
	
	public int getStartRow() {
		return startRow;
	}
	
	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}
	
	public int getEndCol() {
		return endCol;
	}
	
	public void setEndCol(int endCol) {
		this.endCol = endCol;
	}
	
	public int getEndRow() {
		return endRow;
	}
	
	public void setEndRow(int endRow) {
		this.endRow = endRow;
	}
	
	@Override
	public String toString() {
		return String.format("c:%d,r:%d-c:%d,r:%d+", getStartCol(), getStartRow(), getEndCol(), getEndRow());
	}
}
