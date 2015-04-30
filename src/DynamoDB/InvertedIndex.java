/**
 * 
 */
package DynamoDB;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Utils.BinaryUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
/**
 * @author dichenli
 *
 */
@DynamoDBTable(tableName="InvertedIndex")
public class InvertedIndex {
	byte[] id; //binary data, docID
	String word; 
	HashSet<Integer> positions; //position of the word in document
	float tf; //TF value
	
	public InvertedIndex() {}


	public InvertedIndex(String word2, byte[] id2, float tf2,
			HashSet<Integer> positions2) {
		this.word = word2;
		this.id = id2;
		this.positions = positions2;
		this.tf = tf2;
	}
	
	public InvertedIndex(String word2) {
		this.word = word2;
	}

	@DynamoDBRangeKey(attributeName="id")
	public ByteBuffer getId() { return ByteBuffer.wrap(id); }
	public void setId(ByteBuffer buf) { 
		this.id = buf.array(); 
	}

	public void setIdByHexString(String hexString) {
		id = BinaryUtils.fromHex(hexString);
	}

	@DynamoDBHashKey(attributeName="word")
	public String getWord() { return word; }  
	public void setWord(String word) { this.word = word; }

	@DynamoDBAttribute(attributeName="positions")
	public Set<Integer> getPositions() {
		return  positions;
	}
	
	public void setPositions(Set<Integer> positions) {
		this.positions = new HashSet<Integer>();
		this.positions.addAll(positions);
	}

	public void addPosition(Integer pos) {
		positions.add(pos);
	}

	@DynamoDBAttribute(attributeName="tf")
	public float getTF() {
		return tf;
	}
	public void setTF(float tf) {
		this.tf = tf;
	}

	@Override
	public String toString() {
		return word + BinaryUtils.byteArrayToString(id);
	}

	public static InvertedIndex parseInput(String line) {
		if (line == null) {
			System.err.println("parseInput: null line!");
			return null;
		}

		String[] splited = line.split("\t");
		if (splited.length != 4) {
			System.err.println("parseInput: bad line: " + line);
			return null;
		}

		String word = splited[0].trim();
		if (word.equals("")) {
			System.err.println("parseInput: word empty: " + line);
			return null;
		}

		byte[] id = BinaryUtils.fromHex(splited[1].trim());
		if (id.length == 0) {
			System.err.println("parseInput: id wrong: " + line);
			return null;
		}

		float tf;
		try {
			tf = Float.parseFloat(splited[2].trim());
		} catch(Exception e) {
			System.err.println("parseInput: tf wrong: " + line);
			return null;
		}

		String[] posStrs = splited[3].split(",");
		if (posStrs.length == 0) {
			System.err.println("parseInput: positions wrong: " + line);
			return null;
		}

		HashSet<Integer> positions = new HashSet<Integer>();
		for (String p : posStrs) {
			try {
				Integer pos = Integer.parseInt(p);
				positions.add(pos);
			} catch(Exception e) {
				System.err.println("parseInput: positions wrong: " + line);
				return null;
			}
		}

		return new InvertedIndex(word, id, tf, positions);
	}
	
	 public static List<InvertedIndex> query(String queryWord) {
		
		 if (DynamoTable.mapper == null) {
	    		try {
					DynamoTable.init();
				} catch (Exception e) {
					e.printStackTrace();
				}
	     }
		 
		 InvertedIndex wordKey = new InvertedIndex(queryWord);
		 DynamoDBQueryExpression<InvertedIndex> queryExpression = new DynamoDBQueryExpression<InvertedIndex>().withHashKeyValues(wordKey);
		 
		 List<InvertedIndex> collection = DynamoTable.mapper.query(InvertedIndex.class, queryExpression);
		 return collection;
	 }
	
	
}
