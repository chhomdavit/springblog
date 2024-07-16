package com.davit.springblog.execption;

public class AlreadyExistsException extends RuntimeException{

	    /**
		 * 
		 */
		private static final long serialVersionUID = 9211901989767460353L;

		public AlreadyExistsException(String message) {
			super(message);
		}
}
