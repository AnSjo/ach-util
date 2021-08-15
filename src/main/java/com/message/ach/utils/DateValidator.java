package com.message.ach.utils;




import java.text.SimpleDateFormat;

public class DateValidator {

	public boolean validate(String dateString,String format)
	{
		if(dateString.trim().equals(""))
			return false;
		else
		{
			SimpleDateFormat dateFormat=new SimpleDateFormat(format);
			dateFormat.setLenient(false);
			try {
				dateFormat.parse(dateString.trim());
			}
			catch(Exception e)
			{
				return false;
			}
			return true;
		}
			
		
	}

}
