package Indexing;

import Classes.Path;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class PreProcessedCorpusReader {
	String type;
	BufferedReader br;
	String no = "";
	
	
	public PreProcessedCorpusReader(String type) throws IOException {
		// This constructor opens the pre-processed corpus file, Path.ResultHM1 + type
		// You can use your own version, or download from http://crystal.exp.sis.pitt.edu:8080/iris/resource.jsp
		// Close the file when you do not use it any more
		this.type = type;
		try {
			FileReader fr;
			fr = new FileReader(Path.ResultHM1+type);
			br = new BufferedReader(fr);
		}catch (Exception e){
			br.close();
		}

	}


	public Map<String, String> NextDocument() throws IOException {
		// read a line for docNo, put into the map with <"DOCNO", docNo>
		// read another line for the content , put into the map with <"CONTENT", content>
		Map<String, String> res = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		String line;

		// first time, no is empty, read first line to get it
		if(no.equals("")) no = br.readLine();
		// different patterns in different kind of documents
		if(this.type == "trectext"){
			// assume the pattern of the "trectext" is "XIE00000000.0000"
			line = br.readLine().trim();
			while(line!=null && line.length()!=0 && !(Pattern.matches("^XIE\\d+\\.\\d+",line) || Pattern.matches("^NYT\\d+\\.\\d+",line))){
				sb.append(line).append("\n");
				line = br.readLine();
			}
		}
		else{
			// assume the pattern of the "trectweb" is "lists-000-0000000"
			line = br.readLine().trim();
			while(line!=null && line.length()!=0 && !Pattern.matches("^lists-\\d+-\\d+",line)){
				sb.append(line).append("\n");
				line = br.readLine();
			}
		}

		// if read the end of the line, return null
		if(line==null) return null;
		// else return the map
//		res.put("<\"DOCNO\"," + no + "docNo>", "<\"CONTENT\"," + sb.toString() + "content>");
		res.put(no, sb.toString());
		// line is no for next document
		no = line;
		return res;
	}

}
