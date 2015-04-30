package DynamoDB;

import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;

public interface Populator {

	/**
	 * get table name, must be the same as the table name on @DynamoDBTable(tableName="XXX")
	 * @return
	 */
	String getTableName();
	
	/**
	 * set table specs, like key name, key type, etc.
	 * @return
	 */
	CreateTableRequest createTableRequest();
	
	/**
	 * call DynamoTable.createTable(this) to instantiate the table 
	 * @throws Exception
	 */
	void createTable() throws Exception;
	
	/**
	 * execute code to populate the database. 
	 * @param table
	 */
	void populate();
}
