package com.davit.springblog.execption;

public class EmptyOrNotNullException extends RuntimeException{

    /**
	 * 
	 */
	private static final long serialVersionUID = -7403235593925162051L;

	public EmptyOrNotNullException(String message) {
		super(message);
	}
}
