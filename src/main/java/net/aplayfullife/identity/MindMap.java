package net.aplayfullife.identity;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.*;

public class MindMap {
	private static final Pattern UNDESIRABLES = Pattern.compile("[(){},.;!?<>%]");
	public int[][] mapMatrix;
	public String[] pages;
	private HttpServlet myServlet;
	
	public MindMap(String directory, HttpServlet myServlet) throws IOException{
		this.myServlet = myServlet;
		// Content readers.
		InputStream resourceContent;
		StringWriter writer = new StringWriter();
		
		// 
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		resourceContent = classLoader.getResourceAsStream("/content/identity-pages");
		writer.getBuffer().setLength(0);
		IOUtils.copy(resourceContent, writer, "UTF-8");
		pages = writer.toString().split("\\r?\\n");
		IOUtils.closeQuietly(resourceContent);
		
		mapMatrix = new int[pages.length][pages.length];
		
		for(int i=0; i<pages.length; i++) {
			resourceContent = classLoader.getResourceAsStream("/content/identity-pages/" + pages[i]);
			writer.getBuffer().setLength(0);
			IOUtils.copy(resourceContent, writer, "UTF-8");
			ArrayList<String> blockIds = new ArrayList<String>(Arrays.asList(writer.toString().split(",")));
			IOUtils.closeQuietly(resourceContent);
			blockIds.remove(0);
			int blockNumber=0;
			for(String blockId : blockIds) {
				blockId = blockId.trim();
				resourceContent = classLoader.getResourceAsStream("/content/identity-blocks/" + blockId);
				writer.getBuffer().setLength(0);
				
				try {
					IOUtils.copy(resourceContent, writer, "UTF-8");
					String blockContent = writer.toString();
					if (blockId.contains("mindtext")) {
						Parse(blockContent, i, blockNumber++);
					}
				} catch (IOException | java.lang.NullPointerException ex) {
					
				}
				IOUtils.closeQuietly(resourceContent);
			}
		}
	}
	
	public void Parse(String blockContent, int i, int blockNumber) throws IOException {
		String stripped = UNDESIRABLES.matcher(blockContent).replaceAll("");
		stripped = stripped.toLowerCase();
		for(int j=0; j<pages.length; j++) {
			if (j==i)
				continue;
			
			// Content readers.
			StringWriter writer = new StringWriter();
			// 
			ServletContext context = myServlet.getServletContext();
			InputStream resourceContent =
				context.getResourceAsStream("/words/" + pages[j] + ".json");
			IOUtils.copy(resourceContent, writer, "UTF-8");
			String wordJSON = writer.toString();
			IOUtils.closeQuietly(resourceContent);
			
			JSONObject target = (JSONObject) JSONValue.parse(wordJSON);
			
			for (String word : stripped.split(" ")) {
				if (word.equals(target.get("Word"))) {
					mapMatrix[i][j] += 10;
				}
			}
		}
	}
}
