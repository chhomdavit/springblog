package com.davit.springblog.execption;

public class ResourceNotFoundException extends RuntimeException{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1656211200731944906L;

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
