package DynamoDB;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import Utils.IOUtils;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

public class AnchorPopulator implements Populator {
	static String tableName = "Anchor"; //need to sync with @DynamoDBTable(tableName="xx")
	static String word = "word";
	static String id = "id";
	static String hashKey = word;
	static String rangeKey = id;
	static long readCapacity = 1L;
	static long writeCapacity = 1000L;

	File input;

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public CreateTableRequest createTableRequest() {
		CreateTableRequest createTableRequest = new CreateTableRequest()
		.withTableName(tableName)
		.withProvisionedThroughput(
				new ProvisionedThroughput()
				.withReadCapacityUnits(readCapacity)
				.withWriteCapacityUnits(writeCapacity));

		//AttributeDefinitions
		ArrayList<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
		attributeDefinitions.add(new AttributeDefinition().withAttributeName(word).withAttributeType(ScalarAttributeType.S));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName(id).withAttributeType(ScalarAttributeType.B));
		createTableRequest.setAttributeDefinitions(attributeDefinitions);

		//KeySchema
		ArrayList<KeySchemaElement> tableKeySchema = new ArrayList<KeySchemaElement>();
		tableKeySchema.add(new KeySchemaElement().withAttributeName(hashKey).withKeyType(KeyType.HASH));
		tableKeySchema.add(new KeySchemaElement().withAttributeName(rangeKey).withKeyType(KeyType.RANGE));
		createTableRequest.setKeySchema(tableKeySchema);

		return createTableRequest;
	}

	@Override
	public void createTable() throws Exception {
		DynamoTable.creatTable(this);
	}

	@Override
	public void populate() {
		long total = IOUtils.countLines(input); 
		Scanner sc = IOUtils.getScanner(input);
		long count = 0;
		long current = 0;
		long begin = new Date().getTime();
		long last = begin;
		long failed = 0;
		
		ArrayList<Anchor> items = new ArrayList<Anchor>();
		if(sc == null) {
			throw new NullPointerException();
		}
		while(sc.hasNextLine()) {
			Anchor item = Anchor.parseInput(sc.nextLine());
			if(item != null) {
				items.add(item);
				if(items.size() >= 25) {
					failed += DynamoTable.batchInsert(items);
					count += items.size();
					current += items.size();
					items = new ArrayList<Anchor>();
				}
			}
			
			if(current >= 500) {
				long now = new Date().getTime();
				float time1 = (float)(now - begin) ;
				float time = (float)(now - last) ;
				if(time < 1) {
					time = 1;
				}
				if(time1 < 1) {
					time1 = 1;
				}
				System.out.println("======" + count + ", total speed:" 
				+ (((float)count / time1) *1000) + "item/sec, current speed: " 
				+ (((float)current / time) *1000) + "item/sec, " 
				+ ((float)count /(float) total) + "%, failed: "
				+ failed +"======");
				current = 0;
				last = now;
			}
		}
		if(!items.isEmpty()) {
			failed += DynamoTable.batchInsert(items);
		}
		System.out.println("done, count: " + count + ", failed: " + failed);
		sc.close();
	}

	public AnchorPopulator(String fileName) {
		this.input = new File(fileName);
		if(input == null) {
			throw new IllegalArgumentException();
		}
		if(!Utils.IOUtils.fileExists(input)) {
			throw new IllegalArgumentException();
		}
	}

	public static void main(String[] args) throws Exception {
		if(args.length != 1 || args[0].equals("")) {
			System.out.println("Usage: <jar_name> <input_file>");
		}
		String input = args[0];
		Populator instance = new AnchorPopulator(input);
		instance.createTable();
		instance.populate();
	}
}
