package com.message.ach.utils;



import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomeObjectMapper {

	@SuppressWarnings("unchecked")
	public Map<String,Map<String,Map<String,Object>>> getNestedMap(String filePath) throws IOException
	{
		ClassLoader classLoader = getClass().getClassLoader();
		String fileContent=FileUtils.readFileToString(new File(classLoader.getResource(filePath).getFile())
				,StandardCharsets.UTF_8);
		ObjectMapper mapper=new ObjectMapper();
		Map<String,Map<String,Map<String,Object>>> map=mapper.readValue(fileContent,Map.class);
		return map;
	}
	public Properties getProperties(String filePath) throws IOException
	{
		ClassLoader classLoader = getClass().getClassLoader();
		FileReader reader=new FileReader(classLoader.getResource(filePath).getFile());
		Properties p=new Properties();
		p.load(reader);
		return p;
		
	}
}
