package edu.umich.eecs485.pa4;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;


/*********************************************************
 * <code>Indexer</code> reads in some raw content and writes to
 * an inverted index file
 *********************************************************/
public class Indexer {
  public Indexer() {
  }

  /**
   * The <code>index</code> code transforms the content into the
   * actual on-disk inverted index file.
   *
   * Fill in this method to do something useful!
   */
    public void index(File contentFile, File outputFile) throws IOException {
        // Do something!
        
        FileReader reader=new FileReader(contentFile);
        BufferedReader buffered = new BufferedReader(reader);
        int n=1;   //doc id
        String line;
        Map<String, Term> termMap = new HashMap<String, Term>();
        Map<Integer, ArrayList<String>> docToTerms=new HashMap<Integer, ArrayList<String>>();
        while((line = buffered.readLine()) != null) {
            //doc.put(n,line);
            String[] terms=line.split(" ");
            
            //HashMap<String, Integer> termFrequency=new HashMap<String, Integer>();
            ArrayList<String> docTermList=new ArrayList<String>();
            for(int i=0; i<terms.length;i++){
                String term=terms[i].toLowerCase();
                if(!isStopWord(term)){
                    term=convert(term);
                    
                    if(!term.equals("")&&!isStopWord(term)){
                        
                        if(!docTermList.contains(term))
                            docTermList.add(term);
                        if(termMap.containsKey(term)){
                            Term t=termMap.get(term);
                            t.setOccurrence(t.getOccurrence()+1);  //add occurrence of the term
                            Map<Integer, Integer> docFreq=t.getDocFreq();
                            if(docFreq.containsKey(n)){
                                docFreq.put(n,docFreq.get(n)+1);
                            }else{
                                docFreq.put(n, 1);
                            }
                            
                        }else{
                            Term t=new Term();
                            t.setOccurrence(1);
                            Map<Integer, Integer> freq=new HashMap<Integer,Integer>();
                            freq.put(n,1);
                            t.setDocFreq(freq);
                            termMap.put(term, t);
                        }
                        
                    }
                }
            }
            docToTerms.put(n, docTermList);
            n++;
            
        }
        n=n-1;
        
        //calculate idf for each term
        Iterator termIt=termMap.entrySet().iterator();
        while(termIt.hasNext()){
            Map.Entry<String, Term> entry=(Map.Entry<String, Term>)termIt.next();
            Term t=entry.getValue();
            double idf=Math.log10((double)n/t.getDocFreq().size());
            t.setIdf(idf);
        }
        
        //calculate norm factor for each document
        Iterator it=termMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, Term> termEntry=(Map.Entry<String, Term>)it.next();
            Map<Integer, Integer> docFreq=termEntry.getValue().getDocFreq();
            Iterator docIt=docFreq.entrySet().iterator(); //iterate docFreq, docid->docFreq
            Map<Integer, Double> docNorms=new HashMap<Integer, Double>();
            while(docIt.hasNext()){
                double norm=0.0;
                Map.Entry<Integer, Integer> docFreqEntry=(Map.Entry<Integer, Integer>) docIt.next();
                int docid=docFreqEntry.getKey();
                ArrayList<String> docTermList= docToTerms.get(docid);
                Iterator<String> dtlIt=docTermList.iterator();
                while(dtlIt.hasNext()){
                    String t=dtlIt.next();
                    norm=norm+Math.pow(termMap.get(t).getDocFreq().get(docid),2)*Math.pow(termMap.get(t).getIdf(),2);
                }
                
                docNorms.put(docid, norm);
            }
            termEntry.getValue().setDocNorms(docNorms);
            
            
        }
        
        FileWriter fw=new FileWriter(outputFile);
        BufferedWriter bw=new BufferedWriter(fw);
        Iterator result=termMap.entrySet().iterator();
        
        while(result.hasNext()){
            Map.Entry<String, Term> entry=(Map.Entry<String, Term>)result.next();
            bw.write(entry.getKey()+" "+ entry.getValue().getIdf()+" "+entry.getValue().getOccurrence()+" ");
            Map<Integer, Integer> doc=entry.getValue().getDocFreq();
            Iterator docIt=doc.entrySet().iterator();
            while(docIt.hasNext()){
                Map.Entry<Integer, Integer> docEntry=(Map.Entry<Integer, Integer>)docIt.next();
                bw.write(docEntry.getKey()+" "+docEntry.getValue()+" "+entry.getValue().getDocNorms().get(docEntry.getKey())+" ");
            }
            bw.write('\n');
        }
        
        
        buffered.close();
        reader.close();
        bw.close();
        fw.close();
        
    }
    /*check if the word is a stop word*/
    public boolean isStopWord(String s) throws IOException{
        
        FileReader reader = new FileReader("../pa4/index/english.stop.txt");
        BufferedReader br = new BufferedReader(reader);
        String line;
        while((line=br.readLine())!=null){
            if(s.equals(line))
                return true;
        }
        return false;
    }
    
    /*ignore other symbols*/
    public String convert(String s){
        
        String result="";
        
        for(int i=0; i<s.length(); i++){
            char c=s.charAt(i);
            if(Character.isDigit(c)||Character.isAlphabetic(c)){
                result+=c;
            }
        }
        return result;
        
    }
    
public class Term {
	private int occurrence;
	private Map<Integer, Integer> docFreq;//frequency of each document
	private Map<Integer, Double> docNorms;//norm factor of each document
	private double idf;
	public int getOccurrence() {
		return occurrence;
	}
	public void setOccurrence(int occurrence) {
		this.occurrence = occurrence;
	}
	public Map<Integer, Integer> getDocFreq() {
		return docFreq;
	}
	public void setDocFreq(Map<Integer, Integer> docFreq) {
		this.docFreq = docFreq;
	}
	public Map<Integer, Double> getDocNorms() {
		return docNorms;
	}
	public void setDocNorms(Map<Integer, Double> docNorms) {
		this.docNorms = docNorms;
	}
	public double getIdf() {
		return idf;
	}
	public void setIdf(double idf) {
		this.idf = idf;
	}


}


  /**
   * Parse the command-line args.
   */
  public static void main(String argv[]) throws IOException {
    if (argv.length < 2) {
      System.err.println("Usage: Indexer <content-filename> <inverted-index-filename>");
      return;
    }
    int i = 0;
    File contentFname = new File(argv[i++]).getCanonicalFile();
    File invertedIndexFname = new File(argv[i++]).getCanonicalFile();

    Indexer indexer = new Indexer();
    indexer.index(contentFname, invertedIndexFname);
  }
}