package Indexing;

import Classes.Path;

import java.io.*;
import java.util.*;

public class MyIndexWriter {
	// We suggest you to write very efficient code here, otherwise, your memory cannot hold our corpus...
	// blockSize is the number of files each block has
	int blockSize = 10000;
	String type;

	// Build dictionaries
	// term -> termid
	Map<String, Integer> term2termid = new HashMap<>();
	// docno -> docid
	Map<String, Integer> doc2docid = new HashMap<>();
	// docid -> doc
	Map<Integer, String> docid2doc = new HashMap<>();
	// termid -> frequency
	Map<Integer, Integer> termid2fre = new HashMap<>();
	// termid -> docidMap
	Map<Integer, Map> termid2map = new HashMap<>();
	// docid -> termidMap
	Map<Integer, Map> docid2map = new HashMap<>();

	int termid = 0, docid = 0;
	Writer postFile, docFile, termFile;
	
	
	public MyIndexWriter(String type) throws IOException {
		// This constructor should initiate the FileWriter to output your index files
		// remember to close files if you finish writing the index
		this.type = type;
	}
	
	public void IndexADocument(String docno, String content) throws IOException {
		// you are strongly suggested to build the index by installments
		// you need to assign the new non-negative integer docId to each document, which will be used in MyIndexReader

		// Insert new doc into doc2docid and docid2doc
		docid++;
		doc2docid.put(docno, docid);
		docid2doc.put(docid, docno);

		// Insert every term in this doc into docid2map
		String[] terms = content.trim().split(" ");
		Map<Integer, Integer> docTerm = new HashMap<>();
		int thisTermid;
		for(String term: terms){
			// get termid
			if(term2termid.get(term)!=null) thisTermid = term2termid.get(term);
			else{
				thisTermid = ++termid;
				term2termid.put(term, thisTermid);
			}
			docTerm.put(thisTermid, docTerm.getOrDefault(thisTermid, 0)+1);
			// Merge this term's information into termid2map
			Map<Integer, Integer> temp = termid2map.getOrDefault(thisTermid, new HashMap());
			temp.put(docid, temp.getOrDefault(docid, 0)+1);
			termid2map.put(thisTermid, temp);
			termid2fre.put(thisTermid, termid2fre.getOrDefault(thisTermid, 0)+1);
		}
		docid2map.put(docid, docTerm);

//		// Merge all the terms' information into termid2map
//		for(Long tid: docTerm.keySet()){
//			Map<Long, Long> temp = termid2map.getOrDefault(tid, new HashMap());
//			temp.put(docid,docTerm.get(tid));
//			termid2map.put(tid, temp);
//			// update this term's frequency
//			termid2fre.put(tid, termid2fre.getOrDefault(tid,0L)+docTerm.get(tid));
//		}

		// When termid2map and docid2map is big enough, put it into disk
		if(docid%blockSize==0){
			WritePostingFile();
			WriteDocFile();
		}
	}


	public void WritePostingFile() throws IOException {
		postFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Path.ResultHM1+type+Path.PostingFileDir, true), "utf-8"));
		StringBuilder eachTerm = new StringBuilder();
		for(int termid: termid2map.keySet()){
			// Every line's format is "term termid docid1 frequency(doc1) docid2 frequency(doc2) ..."
			eachTerm.append(termid + " " + termid2fre.get(termid) + " ");
			Map<Integer, Integer> temp = termid2map.get(termid);
			for(Integer docid: temp.keySet()) eachTerm.append(docid + " " + temp.get(docid) + " ");
			eachTerm.append("\n");
		}
		postFile.write(eachTerm.toString());
		postFile.close();
		// Clean the termid2map and termin2fre
		termid2map.clear();
		termid2fre.clear();
	}


	public void WriteDocFile() throws IOException{
		docFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Path.ResultHM1+type+Path.DocVectorDir, true), "utf-8"));
		StringBuilder sb = new StringBuilder();
		for(int docid: docid2map.keySet()){
			sb.append(docid + " " + docid2doc.get(docid) + " ");
			Map<Integer, Integer> temp = docid2map.get(docid);
			for(int termid: temp.keySet())
				sb.append(termid + " " + temp.get(termid) + " ");
			sb.append("\n");

			// In case the StringBuilder overflow
			if(sb.length() > Integer.MAX_VALUE+100){
				docFile.write(sb.toString());
				sb.setLength(0);
			}
		}
		docFile.write(sb.toString());
		docFile.close();
		docid2doc.clear();
		doc2docid.clear();
		docid2map.clear();
	}

	
	public void Close() throws IOException {
		// close the index writer, and you should output all the buffered content (if any).
		// if you write your index into several files, you need to fuse them here.

		termFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Path.ResultHM1+type+Path.TermDir), "utf-8"));
		// Write to term file
		StringBuilder sb = new StringBuilder();
		for(String term: term2termid.keySet()){
			sb.append(term + " " + term2termid.get(term) + "\n");
		}
		termFile.write(sb.toString());
		termFile.close();
		term2termid.clear();

		// Write to docVector
		WriteDocFile();
		// Write to posting file
		WritePostingFile();

	}
	
}
