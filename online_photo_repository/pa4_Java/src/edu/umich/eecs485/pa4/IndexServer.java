package edu.umich.eecs485.pa4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import edu.umich.eecs485.pa4.utils.QueryHit;
import edu.umich.eecs485.pa4.utils.GenericIndexServer;

/*******************************************************
 * The <code>IndexServer</code> loads an inverted index and processes
 * user queries.  It returns Hit objects that are then returned to the
 * PHP server over the network.
 *
 * Its superclass is GenericIndexServer, which provides basic network
 * and serialization functionality.
 *******************************************************/
public class IndexServer extends GenericIndexServer {
  /**
   * Creates a new <code>IndexServer</code> instance.
   *
   * The superclass needs a port to listen on.
   * We store fname in a member variable for later use.
   */
  private static HashMap<String, List<Items>> map;

  public IndexServer(int port, File fname) throws IOException {
    super(port, fname);
  }

  /**
   * This method is called once when the server is first started.
   * Inside this method you should load the inverted index from disk.
   *
   * Fill in this method to do something useful!
   */
  public void initServer(File fname) {
      // Do something!
      System.err.println("Init server with fname " + fname);
      // init the map
      map = new HashMap<String,List<Items>>();

      try{  
        BufferedReader reader = new BufferedReader(new FileReader(fname));
        String line;
        while((line = reader.readLine()) != null){
          String[] terms=line.split(" ");
          String key = "";
          Double idf = 0.0,normol=0.0, nor_weight = 0.0;
          int tOccur = 0,docId =0 ,occur=0;
          List<Items> docItems = new ArrayList<Items>();
          int position = 0;
          for(int i = 0; i < terms.length; i++){
            if(i == 0){
              key = terms[i];
            }
            else if(i==1){
              idf = Double.parseDouble(terms[i]);
            }
            else if(i == 2){
              tOccur = Integer.parseInt(terms[i]);
            }else{
              // the remaining 
              switch(position){
                case 0: 
                  docId = Integer.parseInt(terms[i]);
                  position =1;
                  break;
                case 1:
                  occur = Integer.parseInt(terms[i]);
                  position = 2;
                  break;
                case 2:
                  normol = Double.parseDouble(terms[i]);
                  position =0;
                  nor_weight = (idf*((double)occur))/(Math.sqrt(normol));
                  Items t = new Items(idf,tOccur,docId,occur,normol,nor_weight);
                  docItems.add(t);
                  break;
              }
            }
          }
          map.put(key, docItems); 
        }
      }catch(Exception e){
        System.out.println("ERROR: initServer");
        e.printStackTrace();
      }
    }

  /**
   * The <code>processQuery</code> method takes a user query and
   * returns a relevance-ranked and scored list of document hits.
   * If the list is empty, then there are zero hits for the query. 
   *
   * This method should never return null.
   *
   * Fill in this method to do something useful!
   */
  public List<QueryHit> processQuery(String query) {
    // Do something!
    List<QueryHit> res = new ArrayList<QueryHit>();
    HashMap<String,queryVector> word_map = new HashMap<String,queryVector>();
    HashMap<String,score_frequency> doc_score_map = new HashMap<String,score_frequency>();
    System.err.println("Processing query '" + query + "'");  
    // split the query
    String [] cwords = query.toLowerCase().split(" ");
    List<String> words = new ArrayList<String>();
    int wLength  = 0;
    // covert the string
    for(int i = 0; i<cwords.length; i++ ){
      try{
        if(!isStopWord(cwords[i])){
          cwords[i] = convert(cwords[i]);
          if(!isStopWord(cwords[i])&& !cwords[i].equals("")){
            words.add(cwords[i]);
            wLength++;
          }
        }
      }catch(Exception e){
        e.printStackTrace();
      }
    }

    for(int i = 0; i < wLength; i++){
      if(word_map.containsKey(words.get(i))){
        int ori = word_map.get(words.get(i)).get_tf();
        word_map.get(words.get(i)).set_tf(ori+1);
      }else{
        queryVector qv = new queryVector();
        qv.set_tf(1);
        word_map.put(words.get(i), qv);
      }
    }
    
    // cal the weight for the query vector
    double sum_weight = 0.0;
    for(String k : word_map.keySet()){
      // System.out.println("term: "+ k);
      if(map.containsKey(k)){
        double idf = map.get(k).get(0).get_idf();
        word_map.get(k).set_weight(idf*((double)word_map.get(k).get_tf()));
      // System.out.println("term_weight: "+ word_map.get(k).get_weight());
        double sqrt = (word_map.get(k).get_weight()*word_map.get(k).get_weight());
       // System.out.println("sqrt: "+ sqrt);
        sum_weight += sqrt;
      }else{
        word_map.get(k).set_weight(0);
      }
    }
   // System.out.println(sum_weight);
    sum_weight = Math.sqrt(sum_weight);
    //System.out.println(sum_weight);

    // cal the normal weight for the query vector
    // cal the score for this doc
    for(String k:word_map.keySet()){
      double normal_weight = (word_map.get(k).get_weight())/sum_weight;
      word_map.get(k).set_normal_weight(normal_weight);
      if(map.containsKey(k)){
        // get the doc id
        for(int i = 0; i < map.get(k).size(); i++){
          Items temp = map.get(k).get(i);
          String did = Integer.toString(temp.get_docId());
          Double score = normal_weight*temp.get_nor_weight();
          if(doc_score_map.containsKey(did)){
            int oriF = doc_score_map.get(did).get_frequency();
            double oriS = doc_score_map.get(did).get_score();
            doc_score_map.get(did).set_socre(oriS+score);
            doc_score_map.get(did).set_frequency(oriF+1);
          }else{
            score_frequency sf =  new score_frequency();
            sf.set_frequency(1);
            sf.set_socre(score);
            doc_score_map.put(did, sf);
          }
        } 
      }
    }
    
    
    // pass to the res
    for(String k : doc_score_map.keySet()){
      if(doc_score_map.get(k).get_frequency() == wLength){
        QueryHit qh = new QueryHit(k,doc_score_map.get(k).get_score());
          res.add(qh);
      }
    }
    
    // sort the res
    Collections.sort(res, new QueryhitComparator());
    for(int i = 0; i < res.size() ; i++){
      System.out.println(res.get(i).getIdentifier() + " " + res.get(i).getScore());
    }

    // for(String k : word_map.keySet()){
    //   System.out.println("key: "+ k);
    //   System.out.println("weight: " + word_map.get(k).get_weight() + "normal_weight: "+ word_map.get(k).get_normal_weight());
    //   if(map.containsKey(k)){
    //     for(int i = 0; i < map.get(k).size(); i++){
    //       System.out.println("doc: "+map.get(k).get(i).get_docId()+" nor_weight: "+ map.get(k).get(i).get_nor_weight());
    //     }
    //   }
    // }
    
    
    return res;
  }
  
  public class QueryhitComparator implements Comparator<QueryHit>{      
      @Override
      public int compare(QueryHit q1, QueryHit q2) {
          if (q1.getScore() < q2.getScore() ) {
              return 1;
          } else if (q1.getScore() == q2.getScore() ) {
              return 0;
          } else {
              return -1;
          }
      }
  }

  public class score_frequency{
    private int frequency;
    private double score;
    
    void set_frequency(int f){
      this.frequency = f;
    }
    int get_frequency(){
      return this.frequency;
    }
    
    void set_socre(double s){
      this.score = s;
    }
    
    double get_score(){
      return this.score;
    }
  }
  
  public class queryVector{
    private int tf;
    private double weight;
    private double normal_weight;
    void set_tf(int tf){
      this.tf = tf;
    }
    
    int get_tf(){
      return this.tf;
    }
    
    
    void set_weight(double weight){
      this.weight = weight;
    }
    
    double get_weight(){
      return this.weight;
    }
    
    void set_normal_weight(double normal_weight){
      this.normal_weight = normal_weight; 
    }
    
    double get_normal_weight(){
      return this.normal_weight;
    } 
    
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
  
  public class Items {
    private double idf;
    private int tOccurrences;
    private int docId;
    private int occurences;
    private double normolization;
    private double nor_weight;

    Items(double idf, int tOccurrences ,int docId, int occurences, double normolization,double nor_weight){
      this.idf = idf;
      this.tOccurrences = tOccurrences;
      this.docId = docId;
      this.occurences = occurences;
      this.normolization = normolization;
      this.nor_weight = nor_weight;
    }  

    double get_idf(){
      return this.idf;
    }

    int get_tOccurrences(){
       return this.tOccurrences;
    }
    int get_docId(){
      return this.docId;
    }

    int get_occurrences(){
      return this.occurences;
    }

    double get_normolization(){
      return this.normolization;
    }
    double get_nor_weight(){
        return this.nor_weight;
      }
  }
  
  /**
   * Parse the command-line args.  Then start up the server.
   */
  
  public static void main(String argv[]) throws IOException {
    if (argv.length < 2) {
      System.err.println("Usage: IndexServer <portnum> <inverted-index-filename>");
      return;
    }

    // Parse args
    int i = 0;
    int portnum = -1;
    try {
      portnum = Integer.parseInt(argv[i++]);
    } catch (NumberFormatException nfe) {
      System.err.println("Cannot parse port number: " + argv[i-1]);
      return;
    }
    File fname = new File(argv[i++]).getCanonicalFile();

    // Run server.  Note that because server.serve() creates a new
    // thread, the process will not terminate even though serve() returns.
    IndexServer server = new IndexServer(portnum, fname);
    //server.processQuery("sushi -");
    server.serve();
  }
}