package Indexing;

import Classes.Path;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class MyIndexReader {
	// We suggest you to write very efficient code here, otherwise, your memory cannot hold our corpus...

	String type;
	// Build dictionaries
	// term -> termid
	Map<String, Integer> term2termid = new HashMap<>();
//	// docno -> docid
//	Map<String, Integer> doc2docid = new HashMap<>();
//	// docid -> doc
//	Map<Integer, String> docid2doc = new HashMap<>();
	// termid -> frequency
	Map<Integer, Integer> termid2fre = new HashMap<>();
	// termid -> docidMap
	Map<Integer, Map<Integer, Integer>> termid2map = new HashMap<>();
	// docid -> termidMap
	Map<Integer, Map> docid2map = new HashMap<>();

	BufferedReader br_PostFile;
	BufferedReader br_docFile;
	BufferedReader br_termFile;



	public MyIndexReader( String type ) throws IOException {
		//read the index files you generated in task 1
		//remember to close them when you finish using them
		//use appropriate structure to store your index
		this.type = type;

		br_termFile = new BufferedReader(new FileReader(Path.ResultHM1+type+Path.TermDir));
		String line = "";
		// term2termid
		while((line = br_termFile.readLine()) != null){
			if(line.trim().equals("")) continue;
			String[] eachTerm = line.trim().split(" ");
			if(eachTerm.length != 2) continue;
			term2termid.put(eachTerm[0], Integer.parseInt(eachTerm[1]));
		}

		br_termFile.close();

//		// doc2docid, docid2doc, docid2map
//		while((line = br_docFile.readLine()) != null){
//			String[] eachDoc = line.split(" ");
//			doc2docid.put(eachDoc[1], Long.parseLong(eachDoc[0]));
//			docid2doc.put(Long.parseLong(eachDoc[0]), eachDoc[1]);
//			Map temp = new HashMap();
//			for(int i=2;i<eachDoc.length;i=i+2)
//				temp.put(eachDoc[i], eachDoc[i+1]);
//			docid2map.put(Long.parseLong(eachDoc[0]), temp);
//		}
//		br_docFile.close();
//
//		// termid2map, needs merge and termid2fre
//		while((line = br_PostFile.readLine()) != null){
//			String[] eachTerm = line.split(" ");
//			long termid = Long.parseLong(eachTerm[0]);
//
//			// Update termid2map
//			Map<Long, Long> temp;
//			if(termid2map.get(termid)!=null) temp = termid2map.get(termid);
//			else temp = new HashMap<>();
//			for(int i=2;i<eachTerm.length;i=i+2)
//				temp.put(Long.parseLong(eachTerm[i]), temp.getOrDefault(Long.parseLong(eachTerm[i]), 0L)+Long.parseLong(eachTerm[i+1]));
//			termid2map.put(termid, temp);
//
//			// Update termid2fre
//			termid2fre.put(termid, termid2fre.getOrDefault(termid, 0L)+ Long.parseLong(eachTerm[1]));
//		}
//		br_PostFile.close();

	}

	
	//get the non-negative integer dociId for the requested docNo
	//If the requested docno does not exist in the index, return -1
	public int GetDocid( String docno ) throws IOException {
		String line;
		br_docFile = new BufferedReader(new FileReader(Path.ResultHM1+type+Path.DocVectorDir));
		while((line = br_docFile.readLine())!=null){
			String[] doc = line.split(" ");
			if(doc[1] == docno) return Integer.parseInt(doc[0]);
		}
		return -1;
	}

	// Retrieve the docno for the integer docid
	public String GetDocno( int docid ) throws IOException {
		String line;
		br_docFile = new BufferedReader(new FileReader(Path.ResultHM1+type+Path.DocVectorDir));
		while((line = br_docFile.readLine())!=null){
			String[] doc = line.split(" ");
			if(Integer.parseInt(doc[0]) == docid) return doc[1];
		}
		return null;
	}
	
	/**
	 * Get the posting list for the requested token.
	 * 
	 * The posting list records the documents' docids the token appears and corresponding frequencies of the term, such as:
	 *  
	 *  [docid]		[freq]
	 *  1			3
	 *  5			7
	 *  9			1
	 *  13			9
	 * 
	 * ...
	 * 
	 * In the returned 2-dimension array, the first dimension is for each document, and the second dimension records the docid and frequency.
	 * 
	 * For example:
	 * array[0][0] records the docid of the first document the token appears.
	 * array[0][1] records the frequency of the token in the documents with docid = array[0][0]
	 * ...
	 * 
	 * NOTE that the returned posting list array should be ranked by docid from the smallest to the largest. 
	 * 
	 * @param token
	 * @return
	 */
	public int[][] GetPostingList( String token ) throws IOException {
		int termid = term2termid.get(token);
		readPostList(termid);

		int[][] postList = null;
		if(!termid2map.containsKey(termid)) return postList;
		int total = termid2map.get(termid).keySet().size();
		postList = new int[total][2];
		int i = 0;
		for(int key: termid2map.get(termid).keySet()){
			postList[i][0] = key;
			postList[i][1] = termid2map.get(termid).get(key);
			i++;
		}
		return postList;
	}


	public void readPostList(int termid) throws IOException {
		br_PostFile = new BufferedReader(new FileReader(Path.ResultHM1+type+Path.PostingFileDir));
		String line;
		while((line = br_PostFile.readLine())!=null){
			String[] term = line.split(" ");
			if(Integer.parseInt(term[0]) == termid){
				// Update frequency
				termid2fre.put(termid, termid2fre.getOrDefault(termid, 0)+Integer.parseInt(term[1]));
				// Update termid2map
				for(int i=2;i<term.length;i=i+2){
					Map<Integer, Integer> temp = termid2map.getOrDefault(termid, new HashMap<>());
					temp.put(Integer.parseInt(term[i]), Integer.parseInt(term[i+1]));
					termid2map.put(termid, temp);
				}
			}
		}
		br_PostFile.close();
	}


	// Return the number of documents that contains the token.
	public int GetDocFreq( String token ) throws IOException {
		int termid = term2termid.get(token);
		if(termid2fre.containsKey(termid)) return termid2fre.get(termid);
		readPostList(termid);
		if(termid2fre.containsKey(termid)) return termid2fre.get(termid);
		return 0;
	}
	
	// Return the total number of times the token appears in the collection.
	public int GetCollectionFreq( String token ) throws IOException {
		int termid = term2termid.get(token);
		if(termid2map.containsKey(termid)) return termid2map.get(termid).keySet().size();
		readPostList(termid);
		if(termid2map.containsKey(termid)) return termid2map.get(termid).keySet().size();
		return 0;
	}
	
	public void Close() throws IOException {
		term2termid.clear();
		br_docFile.close();
		br_PostFile.close();
		br_termFile.close();
	}
	
}