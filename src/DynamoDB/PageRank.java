/**
 * 
 */
package DynamoDB;

import java.nio.ByteBuffer;
import java.util.Arrays;

import Utils.BinaryUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * @author dichenli
 * data of page rank
 */
@DynamoDBTable(tableName="PageRank")
public class PageRank {
	byte[] id; //binary data
	float rank; //page rank
	
	@DynamoDBHashKey(attributeName="id")
    public ByteBuffer getId() { return ByteBuffer.wrap(id); }
	
    public void setId(ByteBuffer buf) { 
    	this.id = buf.array(); 
    }
    
    public void setId(String hexString) {
    	id = BinaryUtils.fromHex(hexString);
    }
    
    @DynamoDBAttribute(attributeName="rank")
    public float getRank() { return rank; }    
    public void setRank(float rank) { this.rank = rank; }
    
    @Override
    public String toString() {
       return Arrays.toString(id);  
    }
    
    public static PageRank parseInput(String line) {
		if(line == null) {
			System.out.println("null line");
			return null;
		}
		
		String[] splited = line.split("\t");
		if(splited == null || splited.length != 2) {
			System.out.println("bad line: " + line);
			return null;
		}
		String docID = splited[1];
		float rank;
		try {
			rank = Float.parseFloat(splited[0]);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		if(docID.equals("")) {
			System.out.println("Empty line: " + line);
			return null;
		}
		
		PageRank item = new PageRank();
		item.setId(docID);
		item.setRank(rank);
		return item;
	}
    
    public static PageRank load(String docID) {
    	if (DynamoTable.mapper == null) {
    		try {
				DynamoTable.init();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	return DynamoTable.mapper.load(PageRank.class, ByteBuffer.wrap(docID.getBytes()));
    }
    
	
}
