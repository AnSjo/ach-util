package com.message.ach.exceptions;


public class InvalidACHFormatException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidACHFormatException(String message)
	{
		super(message);
	}

}
