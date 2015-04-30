/**
 * 
 */
package DynamoDB;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import Utils.BinaryUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * @author dichenli
 *
 */
@DynamoDBTable(tableName="Anchor")
public class Anchor {
	byte[] id; //binary data, docID
	String word; 
	HashSet<Number> types; //position of the word in document


	public Anchor(String word2, byte[] id2, HashSet<Number> types) {
		this.word = word2;
		this.id = id2;
		this.types = types;
	}

	@DynamoDBRangeKey(attributeName="id")
	public ByteBuffer getId() { return ByteBuffer.wrap(id); }

	public void setId(ByteBuffer buf) { 
		this.id = buf.array(); 
	}

	public void setId(String hexString) {
		id = BinaryUtils.fromHex(hexString);
	}

	@DynamoDBHashKey(attributeName="word")
	public String getWord() { return word; }    

	public void setWord(String word) { this.word = word; }

	@DynamoDBAttribute(attributeName="types")
	public Set<Number> getPositions() {
		return types;
	}

	public void addType(Integer type) {
		types.add(type);
	}


	@Override
	public String toString() {
		return word + BinaryUtils.byteArrayToString(id);
	}

	public static Anchor parseInput(String line) {
		if (line == null) {
			System.err.println("parseInput: null line!");
			return null;
		}

		String[] splited = line.split("\t");
		if (splited.length != 3) {
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

		String[] typesStrs = splited[2].split(",");
		if (typesStrs.length == 0) {
			System.err.println("parseInput: positions wrong: " + line);
			return null;
		}

		HashSet<Number> types = new HashSet<Number>();
		for (String p : typesStrs) {
			try {
				Number pos = Integer.parseInt(p);
				types.add(pos);
			} catch(Exception e) {
				System.err.println("parseInput: positions wrong: " + line);
				return null;
			}
		}

		return new Anchor(word, id, types);
	}
	
}
