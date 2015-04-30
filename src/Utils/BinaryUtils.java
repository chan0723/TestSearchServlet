package Utils;

import java.math.BigInteger;

public class BinaryUtils {

	public static BigInteger fromHexToBigInteger(String hex) {
		return new BigInteger(hex, 16);
	}
	
	/**
	 * convert from string representation of hex bytes to byte[]
	 * @throws NumberFormatException
	 * @param hex
	 * @return
	 */
	public static byte[] fromHex(String hex) {
		return fromHexToBigInteger(hex).toByteArray();
	}
	
	public static String byteArrayToString(byte[] array) {
		StringBuilder sb = new StringBuilder();
		for(byte b : array) {
    		sb.append(b);
    	}
       return sb.toString();
	}
}
