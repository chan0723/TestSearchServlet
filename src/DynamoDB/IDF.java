/**
 * 
 */
package DynamoDB;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * @author dichenli
 * data of page idf
 */
@DynamoDBTable(tableName="IDF")
public class IDF {
	String word; //binary data
	double idf; //page idf
	
	@DynamoDBHashKey(attributeName="word")
    public String getWord() { return word; }
	public void setWord(String word) {
		this.word = word;
	}
    
    @DynamoDBAttribute(attributeName="idf")
    public double getidf() { return idf; }    
    public void setidf(double idf) {
    	this.idf = idf;
    }
    
    @Override
    public String toString() {
       return word+ idf;
    }
    
    public static IDF parseInput(String line) {
		if(line == null) {
			System.out.println("null line");
			return null;
		}
		
		String[] splited = line.split("\t");
		if(splited == null || splited.length != 2) {
			System.out.println("bad line: " + line);
			return null;
		}
		double idf;
		try {
			idf = Double.parseDouble(splited[1]);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		String word = splited[0];
		if(word.equals("")) {
			System.out.println("Empty line: " + line);
			return null;
		}
		
		IDF item = new IDF();
		item.word = word;
		item.idf = idf;
		return item;
	}
    
    
    // retrieve
    public static IDF load(String word) {
    	if (DynamoTable.mapper == null) {
    		try {
				DynamoTable.init();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	return DynamoTable.mapper.load(DynamoDB.IDF.class, word);
    }
	
}
