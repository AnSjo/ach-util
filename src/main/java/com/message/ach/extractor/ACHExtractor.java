package com.message.ach.extractor;



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import com.message.ach.exceptions.InvalidACHFormatException;
import com.message.ach.exceptions.ParserException;
import com.message.ach.utils.CustomeObjectMapper;
import com.message.ach.utils.DateValidator;
import com.message.ach.utils.StringValidator;



public class ACHExtractor {

	private final String ACH_FORMAT_PATH="ach/ach-format.json";
	private final String ACH_MSG_PATH="ach/ach-message.properties";
	private final String ACH_SPLITTER="[\\n]";
	private final String ACH_END_INDEX_KEY="end";
	private final String ACH_TYPE_INDEX_KEY="type";
	private final String ACH_PATTERN_INDEX_KEY="pattern";
	private final String DATE_IDENTIFIER="date";
	private final String INITIALIZATION_ERROR="Error during initializatioj";
	private final String BATCH_HEADER_KEY="Batch Header";
	private final String BATCH_CONTROL_KEY="Batch Control";
	private final String BATCH_KEY="Batch";
	private final String ENTRY_KEY="Entry";
	private final String FILE_HEADER_KEY="File Header";
	private final String FILE_CONTROL_KEY="File Control";
	private final String[] INDEX_POS= {"1","5","6","8","9"};
	private final String RES_FORMATTER="%s - %s";
	private final Map<String,Map<String,Map<String,Object>>> ACH_MAP;
	private final Properties ACH_PROPS;
	public ACHExtractor()
	{
		CustomeObjectMapper custMapper=new CustomeObjectMapper();
		try {
			this.ACH_MAP=custMapper.getNestedMap(ACH_FORMAT_PATH);
			this.ACH_PROPS=custMapper.getProperties(ACH_MSG_PATH);

		}
		catch(IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException(INITIALIZATION_ERROR);
		}
	}
	public Map<String,Object> extractMessage(String message)
	{
		String[] lines=message.split(ACH_SPLITTER);
		Properties achMsgProps=this.ACH_PROPS;

		Map<String,Map<String,Map<String,Object>>> achMap=this.ACH_MAP;
		Map<String,Object> resultMap=new HashMap<String,Object>();

		List<HashMap<String,Object>> batchList=new ArrayList<HashMap<String,Object>>();
		List<HashMap<String, Object>> entryList=new ArrayList<HashMap<String,Object>>();
		HashMap<String, Object> batchMap=new HashMap<String,Object>();
		if(lines.length<5)
			throw new InvalidACHFormatException(achMsgProps.getProperty("412"));
		for(int block=0;block<lines.length;block++)
		{
			String masterKey=String.valueOf(lines[block].charAt(0));
			if(!achMap.containsKey(masterKey))
				throw new InvalidACHFormatException(String.format(RES_FORMATTER, achMsgProps.getProperty("401"),
						String.valueOf(block+1)));
			if(masterKey.equals(INDEX_POS[0]))
			{
				//add current entry
				resultMap.put(FILE_HEADER_KEY, blockParser(masterKey,lines[block]));
			}
			if(masterKey.equals(INDEX_POS[1]))
			{
				//block sequence check
				if(!resultMap.containsKey(FILE_HEADER_KEY))
					throw new InvalidACHFormatException(String.format(RES_FORMATTER, achMsgProps.getProperty("415"),
							String.valueOf(block+1)));
				//add current entry
				batchMap.put(BATCH_HEADER_KEY, blockParser(masterKey,lines[block]));

			}
			if(masterKey.equals(INDEX_POS[2]))
			{
				//block sequence check
				if(!batchMap.containsKey(BATCH_HEADER_KEY))
					throw new InvalidACHFormatException(String.format(RES_FORMATTER, achMsgProps.getProperty("415"),
							String.valueOf(block+1)));

				//add current entry
				entryList.add(blockParser(masterKey,lines[block]));
				batchMap.put(ENTRY_KEY, blockParser(masterKey,lines[block]));

			}
			if(masterKey.equals(INDEX_POS[3]))
			{
				//block sequence check
				if(!batchMap.containsKey(BATCH_HEADER_KEY)||!batchMap.containsKey(ENTRY_KEY))
					throw new InvalidACHFormatException(String.format(RES_FORMATTER, achMsgProps.getProperty("415"),
							String.valueOf(block+1)));
				//add current entry
				batchMap.put(BATCH_CONTROL_KEY, blockParser(masterKey,lines[block]));

			}
			if(masterKey.equals(INDEX_POS[4]))
			{
				//block sequence check
				if(batchMap.isEmpty()||!resultMap.containsKey(FILE_HEADER_KEY))
					throw new InvalidACHFormatException(String.format(RES_FORMATTER, achMsgProps.getProperty("415"),
							String.valueOf(block+1)));
				//add current entry
				batchMap.put(FILE_CONTROL_KEY, blockParser(masterKey,lines[block]));
				//start a new batch map
				if(batchMap.size()!=0)
				{
					batchList.add(batchMap);
					batchMap=new HashMap<String,Object>();
					entryList=new ArrayList<HashMap<String,Object>>();
					resultMap.put(BATCH_KEY, batchList);
				}

			}
		}
		return resultMap;
	}
	private HashMap<String,Object> blockParser(String masterKey,String line)
	{
		Properties achMsgProps=this.ACH_PROPS;
		Map<String,Map<String,Map<String,Object>>> achMap=this.ACH_MAP;
		HashMap<String,Object> resultMap=new HashMap<String,Object>();
		int blockLength=line.trim().length();
		
		for(int index=0;index<blockLength;index++)
		{
			
			Optional<Map<String,Object>>valMapOp=null;
			int tempIndex=index+1;
			//get the value if exists
			valMapOp=achMap.get(masterKey).values().stream().filter(p->p.containsValue(tempIndex)).findAny();
			if(valMapOp.isPresent())
			{
				Map<String,Object> valMap=valMapOp.get();
				
				//get key from map values
				Stream<String> keys=achMap.get(masterKey)
						.entrySet().stream().filter(entry->valMap.equals(entry.getValue())).map(Map.Entry::getKey);
				Optional<String> keyOp=keys.findFirst();
				if(keyOp.isPresent())
				{
					if(!achMap.get(masterKey).get(keyOp.get()).containsKey(ACH_END_INDEX_KEY)||
							!achMap.get(masterKey).get(keyOp.get()).containsKey(ACH_PATTERN_INDEX_KEY)
							||!achMap.get(masterKey).get(keyOp.get()).containsKey(ACH_TYPE_INDEX_KEY))
						throw new ParserException(achMsgProps.getProperty("511"));

					int endIndex=(int) achMap.get(masterKey).get(keyOp.get()).get(ACH_END_INDEX_KEY);
					
					String extractedValue=line.substring(index, endIndex<blockLength?endIndex:blockLength).trim();

					String type=(String) achMap.get(masterKey).get(keyOp.get()).get(ACH_TYPE_INDEX_KEY);
					String regex=(String) achMap.get(masterKey).get(keyOp.get()).get(ACH_PATTERN_INDEX_KEY);
					
					boolean extractedValueValid=true;
					if(type.equals(DATE_IDENTIFIER))
						extractedValueValid=new DateValidator().validate(extractedValue,regex);
					else
						extractedValueValid=new StringValidator().validate(extractedValue,regex);
					if(extractedValueValid)
						resultMap.put(keyOp.get(), extractedValue);
					else
						throw new InvalidACHFormatException(String.format(RES_FORMATTER, 
								achMsgProps.getProperty("414"),extractedValue));
					index=endIndex-1;
				}

			}
			else
				throw new InvalidACHFormatException(String.format(RES_FORMATTER,
						achMsgProps.getProperty("413"),String.valueOf(tempIndex)));


		}
		return resultMap;


	}
}
