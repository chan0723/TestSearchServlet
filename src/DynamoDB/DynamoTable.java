/**
 * 
 */
package DynamoDB;

import java.util.List;

import Utils.nameUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.Tables;

/**
 * @author dichenli
 * Consists of static methods to facilitate connection to DynamoDB, using my own credentials.
 */
public class DynamoTable {



	private static AmazonDynamoDBClient dynamoDB = null; //DB, collection of tables, one instance only, shared with tables
	static DynamoDBMapper mapper;
	/**
	 * The only information needed to create a client are security credentials
	 * consisting of the AWS Access Key ID and Secret Access Key. All other
	 * configuration, such as the service endpoints, are performed
	 * automatically. Client parameters, such as proxies, can be specified in an
	 * optional ClientConfiguration object when constructing a client.
	 *
	 * @see com.amazonaws.auth.BasicAWSCredentials
	 * @see com.amazonaws.auth.ProfilesConfigFile
	 * @see com.amazonaws.ClientConfiguration
	 */
	
	public DynamoTable() {}
	
	static void init() throws Exception {
		/*
		 * The ProfileCredentialsProvider will return your [aws150415]
		 * credential profile by reading from the credentials file located at
		 * (/Users/dichenli/.aws/credentials).
		 */
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("aws150415").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
							"Please make sure that your credentials file is at the correct " +
							"location (/Users/dichenli/.aws/credentials), and is in valid format.",
							e);
		}
		dynamoDB = new AmazonDynamoDBClient(credentials);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		dynamoDB.setRegion(usWest2);
		mapper = new DynamoDBMapper(dynamoDB);
	}
	

	private static void createTable(String tableName, CreateTableRequest createTableRequest) throws InterruptedException {

		try {
			// Create table if it does not exist yet
			if (Tables.doesTableExist(dynamoDB, tableName)) {
				System.out.println("Table " + tableName + " is already ACTIVE");
			} else {
				TableDescription createdTableDescription = dynamoDB.createTable(createTableRequest).getTableDescription();
				System.out.println("Created Table: " + createdTableDescription);

				// Wait for it to become active
				System.out.println("Waiting for " + tableName + " to become ACTIVE...");
				try {
					Tables.awaitTableToBecomeActive(dynamoDB, tableName);
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw e;
				}
			}

			// Describe our new table
			DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
			TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
			System.out.println("Table Description: " + tableDescription);

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with AWS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}
	
	
	
	

//	private String tableName; //self defined table name. new table will be created if not exists

	/**
	 * populate the table by the populator
	 * @param populator
	 * @throws Exception
	 */
	public static void creatTable(Populator populator) throws Exception {
		String tableName = populator.getTableName();
		CreateTableRequest createTableRequest = populator.createTableRequest();
		
		if(tableName == null || createTableRequest == null || !nameUtils.isValidName(tableName)) {
			throw new IllegalArgumentException();
		}
		if(dynamoDB == null) { //singleton
			try {
				init();
			} catch(Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
		try {
			createTable(tableName, createTableRequest);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw e;
		}
	}	

	public static void insert(Object item) {
		mapper.save(item);
	}
	
	public static int batchInsert(List items) {
		return mapper.batchSave(items).size();
	}
	
}
