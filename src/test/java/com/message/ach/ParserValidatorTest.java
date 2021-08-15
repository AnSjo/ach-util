package com.message.ach;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.message.ach.exceptions.InvalidACHFormatException;
import com.message.ach.extractor.ACHExtractor;

public class ParserValidatorTest {

	ACHExtractor ex;
	String invalidSequenceFileContent;
	String invalidValuesFileContent;
	String validFileContent;
	@Before
	public void initialize() throws IOException
	{
		ex=new ACHExtractor();
		
		ClassLoader classLoader = getClass().getClassLoader();
		invalidSequenceFileContent=FileUtils.readFileToString(new File(classLoader.getResource("test-samples/ACH-Invalid1.txt")
				.getFile()),StandardCharsets.UTF_8);
		invalidValuesFileContent=FileUtils.readFileToString(new File(classLoader.getResource("test-samples/ACH-Invalid2.txt")
				.getFile()),StandardCharsets.UTF_8);
		validFileContent=FileUtils.readFileToString(new File(classLoader.getResource("test-samples/ACH-Valid.txt")
				.getFile()),StandardCharsets.UTF_8);
		
	}
	@Test(expected=InvalidACHFormatException.class)
	public void testInvalidSequence()
	{
		ex.extractMessage(invalidSequenceFileContent);
	}
	@Test(expected=InvalidACHFormatException.class)
	public void testInvalidContent()
	{
		ex.extractMessage(invalidValuesFileContent);
	}
	@Test
	public void testValidContent()
	{
		Assert.assertTrue(!ex.extractMessage(validFileContent).isEmpty());
	}
}
