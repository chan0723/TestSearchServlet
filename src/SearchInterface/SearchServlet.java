package SearchInterface;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import snowballstemmer.PorterStemmer;
import DynamoDB.DocURL;
import DynamoDB.InvertedIndex;
import DynamoDB.PageRank;

/**
 * Servlet implementation class SearchInterface
 */

public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String PARSER = " \t\n\r";

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *            response)
	 */

	public static List<String> stemContent(String content) {
		StringTokenizer tokenizer = new StringTokenizer(content, PARSER);
		String word = "";
		PorterStemmer stemmer = new PorterStemmer();
		List<String> parseQuery = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			word = tokenizer.nextToken();
			if (word.equals(""))
				continue;
			boolean flag = false;
			for (int i = 0; i < word.length(); i++) {
				if (Character.UnicodeBlock.of(word.charAt(i)) != Character.UnicodeBlock.BASIC_LATIN) {
					flag = true;
					break;
				}
			}
			if (flag)
				continue;
			int i = 0;
			while (i < word.length()
					&& (!Character.isLetter(word.charAt(i)) && !Character
							.isDigit(word.charAt(i)))) {
				i++;
			}
			if (i >= word.length())
				continue;
			word = word.substring(i);
			i = word.length() - 1;
			while (i >= 0
					&& (!Character.isLetter(word.charAt(i)) && !Character
							.isDigit(word.charAt(i)))) {
				i--;
			}
			if (i < 0)
				continue;
			word = word.substring(0, i + 1);
			stemmer.setCurrent(word);
			if (stemmer.stem()) {
				parseQuery.add(stemmer.getCurrent());
			}
		}
		return parseQuery;
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("Content-Type", "text/html");
		PrintWriter out = response.getWriter();
		out.println("<html><body>");
		out.println("<form action=\"\" method=\"POST\">");
		out.println("<input type=\"text\" name=\"search\">");
		out.println("<br><input type=\"submit\" value=\"Search!!!\">");
		out.println("</form>");
		out.println("</body></html>");
		System.out.println("1");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *            response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("Content-Type", "text/html");
		PrintWriter out = response.getWriter();
		String query = request.getParameter("search").toLowerCase();
		List<String> parseQuery = stemContent(query);
		/*
		 * search query
		 */
		out.println("<html><body>");
		out.println("<h1>" + query + "</h1>");
	
		HashMap<String, DocResult> set = new HashMap<String, DocResult>();
		for (int i = 0; i < parseQuery.size(); i++) {
			
			List<InvertedIndex> wordCollection = InvertedIndex.query(parseQuery.get(i));
			
			for (InvertedIndex item : wordCollection) {
				String id = new String(item.getId().array());
				if (!set.containsKey(id)) {
					PageRank rankItem = PageRank.load(id);
					if (rankItem == null) {
						continue;
					}
					
					float rank = rankItem.getRank();
					String url = DocURL.load(id).getURL();
					DocResult docResult = new DocResult(id, url, parseQuery.size(), rank);
					set.put(id, docResult);
				}
				
				DocResult result = set.get(id);
				float tf = item.getTF();
				Set<Integer> positionList = item.getPositions();
				result.setTF(i, tf);
				result.setPositionList(i, positionList);
			}
		}
		
		for (String id : set.keySet()) {
			DocResult result = set.get(id);
			out.println(result);
			out.println("<br>");
		}
		out.println("</body></html>");

	}

}

class DocResult {
	String id;
	float[] wordtf;
	Set<Integer>[] positions;
	float rank;
	String url;
	int size;

	DocResult(String id, String url, int size, float rank) {
		this.id = id;
		this.url = url;
		this.size = size;
		wordtf = new float[size];
		positions = (Set<Integer>[]) new Set[size];
		this.rank = rank;
		for (int i = 0; i < size; i++) {
			wordtf[i] = 0;
		}
	}

	public void setTF(int index, float tf) {
		wordtf[index] = tf;
	}

	public void setPositionList(int index, Set<Integer> position) {
		positions[index] = position;
	}

	public float getRank() {
		return rank;
	}

	public float getTF(int index) {
		return wordtf[index];
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(url);
		sb.append("\t");
		sb.append(rank);
		sb.append("\t");
		for (int i = 0; i < size; i++) {
			sb.append("word");
			sb.append(i);
			sb.append(":  ");
			sb.append(wordtf[i]);
		}
		return sb.toString();
	}
}