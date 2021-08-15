package com.message.ach.utils;


public class StringValidator {

	public boolean validate(String value,String regex) {
		return value.matches(regex);
		
	}

}
