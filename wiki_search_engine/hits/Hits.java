//package pa5_v4y9q6ko6r.hits;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class Hits {
	private HashSet<HashMap<Integer, Integer>> graphInfo = new HashSet<HashMap<Integer, Integer>>();
	private ArrayList<String> stopWord=new ArrayList<String>();
	
	public void initStopWord() throws IOException{
		FileReader reader = new FileReader("english.stop.txt");
		BufferedReader br = new BufferedReader(reader);
		String line;
		while((line=br.readLine())!=null){
			stopWord.add(line);
		}
		br.close();
		
	}
	
	
	public void stopByIteration(Map<Integer, Double> auth, Map<Integer, Double> hub, Map<Integer, Double> authForCal,
			Map<Integer, Double> hubForCal, Map<Integer, HashSet<Integer>> connectedBy, Map<Integer, HashSet<Integer>> connectTo,
			int numIteration) {
		while (numIteration > 0) {
			Map<Integer, Double> previousAuth = new HashMap<Integer, Double>(auth);
			Map<Integer, Double> previousHub = new HashMap<Integer, Double>(hub);
			auth.clear();
			auth.putAll(getAuth(connectedBy, previousHub, authForCal));
			hub.clear();
			hub.putAll(getHub(connectTo, previousAuth, hubForCal));
			numIteration--;
		}
	}

	public void stopByConverge(Map<Integer, Double> auth, Map<Integer, Double> hub, Map<Integer, Double> authForCal,
			Map<Integer, Double> hubForCal, Map<Integer, HashSet<Integer>> connectedBy, Map<Integer, HashSet<Integer>> connectTo,
			double converge) {
		double c = Double.MAX_VALUE;
		while (c > converge) {
			System.out.println("iteration");
			Map<Integer, Double> previousAuth = new HashMap<Integer, Double>(auth);
			Map<Integer, Double> previousHub = new HashMap<Integer, Double>(hub);
			auth.clear();
			auth.putAll(getAuth(connectedBy, previousHub, authForCal));
			hub.clear();
			hub.putAll(getHub(connectTo, previousAuth, hubForCal));
			Iterator<Map.Entry<Integer, Double>> aIt = previousAuth.entrySet().iterator();
			Iterator<Map.Entry<Integer, Double>> hIt = previousHub.entrySet().iterator();
			int key = 0;
			double maxCon = 0;
			while (aIt.hasNext()) {
				Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) aIt.next();
				double diff;
				if (entry.getValue() == 0) {
					diff = 0;
				} else {
					diff = Math.abs(entry.getValue() - auth.get(entry.getKey())) / entry.getValue();
				}
				if (diff > maxCon) {
					maxCon = diff;
					key = entry.getKey();
				}
			}
			while (hIt.hasNext()) {
				Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) hIt.next();
				double diff;
				if (entry.getValue() == 0) {
					diff = 0;
				} else {
					diff = Math.abs(entry.getValue() - hub.get(entry.getKey())) / entry.getValue();
				}
				if (diff > maxCon) {
					maxCon = diff;
					key = entry.getKey();
				}
			}
			c = maxCon;
		}
	}

	public HashMap<Integer, Double> getHub(Map<Integer, HashSet<Integer>> connectTo, Map<Integer, Double> previousAuth,
			Map<Integer, Double> hubForCal) {
		Iterator<Map.Entry<Integer, HashSet<Integer>>> it = connectTo.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, HashSet<Integer>> entryForHub = (Map.Entry<Integer, HashSet<Integer>>) it.next();
			HashSet<Integer> setForHub = entryForHub.getValue();
			double hubValue = 0.0;
			Iterator<Integer> itr = setForHub.iterator();
			while (itr.hasNext()) {
				hubValue = hubValue + previousAuth.get(itr.next());
			}
			hubForCal.put(entryForHub.getKey(), hubValue);

		}
		return normalize(hubForCal);
	}

	public HashMap<Integer, Double> getAuth(Map<Integer, HashSet<Integer>> connectBy, Map<Integer, Double> previousHub,
			Map<Integer, Double> authForCal) {
		Iterator<Map.Entry<Integer, HashSet<Integer>>> it = connectBy.entrySet().iterator();
		while (it.hasNext()) {
			double authValue = 0.0;
			Map.Entry<Integer, HashSet<Integer>> entryForAuth = (Map.Entry<Integer, HashSet<Integer>>) it.next();
			HashSet<Integer> setForAuth = entryForAuth.getValue();
			Iterator<Integer> itr = setForAuth.iterator();
			while (itr.hasNext()) {
				authValue = authValue + previousHub.get(itr.next());
			}
			authForCal.put(entryForAuth.getKey(), authValue);
		}
		return normalize(authForCal);
	}

	public HashMap<Integer, Double> normalize(Map<Integer, Double> map) {
		HashMap<Integer, Double> result = new HashMap<Integer, Double>();
		Iterator<Map.Entry<Integer, Double>> it = map.entrySet().iterator();
		double sum = 0.0;
		while (it.hasNext()) {
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) it.next();
			sum = sum + entry.getValue() * entry.getValue();
		}
		Iterator<Map.Entry<Integer, Double>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) itr.next();
			double normalized = entry.getValue() / Math.sqrt(sum);
			result.put(entry.getKey(), normalized);
		}
		return result;
	}

	public HashMap<Integer, Double> initAuthHub(HashSet<Integer> set, double initVal) {
		HashMap<Integer, Double> result = new HashMap<Integer, Double>();
		Iterator<Integer> it = set.iterator();
		while (it.hasNext()) {
			result.put(it.next(), initVal);
		}
		return result;
	}

	public HashSet<Integer> getBaseSet(HashSet<Integer> seedSet, File inputNetFname) throws IOException {
		HashSet<Integer> baseSet = seedSet;
		Map<Integer, List<Integer>> network = new HashMap<Integer, List<Integer>>();
		Map<Integer, List<Integer>> rev_network = new HashMap<Integer, List<Integer>>();
		Map<Integer, TreeSet<Integer>> connPages = new HashMap<Integer, TreeSet<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(inputNetFname));
		String line;
		boolean start = false;
		while ((line = br.readLine()) != null) {
			if (start) {
				String[] n = line.split(" ");
				int page1 = Integer.parseInt(n[0]);
				int page2 = Integer.parseInt(n[1]);
				HashMap<Integer, Integer> graph = new HashMap<Integer, Integer>();
				if (page1 != page2) {
					graph.put(page1, page2);
					graphInfo.add(graph);
					if (!network.containsKey(page1)) {
						network.put(page1, new ArrayList<Integer>());
					}
					network.get(page1).add(page2);
					if (!rev_network.containsKey(page2)) {
						rev_network.put(page2, new ArrayList<Integer>());
					}
					rev_network.get(page2).add(page1);
				}

			}
			if (line.contains("*Arcs")) {
				String[] l = line.split(" ");

				start = true;
			}
		}
		baseSet = new HashSet<Integer>();
		Iterator<Integer> i = seedSet.iterator();
		while (i.hasNext()) {
			int docId = i.next();
			List<Integer> l = network.get(docId);
			List<Integer> rl = rev_network.get(docId);
			Set<Integer> toAdd = new TreeSet<Integer>();
			if (l != null)
				toAdd.addAll(l);
			if (rl != null)
				toAdd.addAll(rl);
			int top = 50;
			Iterator<Integer> li = toAdd.iterator();
			while (li.hasNext() && top > 0) {
				int pageId = li.next();
				if (!seedSet.contains(pageId)) {
					baseSet.add(pageId);
					top--;
				}
			}
		}
		baseSet.addAll(seedSet);
		return baseSet;

	}
	
	public String[] preProcess(String[] qWords){
		ArrayList<String> rs=new ArrayList<String>();
		for(int i=0; i<qWords.length;i++){
			if(!stopWord.contains(qWords[i])){
				qWords[i]=convert(qWords[i]);
				if(!qWords[i].equals("")){
					if(!stopWord.contains(qWords[i])){
						rs.add(qWords[i]);
					}
				}
			}
		}
		String[] result=new String[rs.size()];
		for(int i=0;i<result.length;i++){
			result[i]=rs.get(i);
		}
		
		return result;
	}
	
public String convert(String s){
		
		String result="";
		
			for(int i=0; i<s.length(); i++){
				char c=s.charAt(i);
				if(Character.isDigit(c)||Character.isAlphabetic(c)||c=='_'){
					result+=c;
				}
			}
			return result;
		
	}
	public HashSet<Integer> processQuery(int h, String query, File invertedIndexFname) throws IOException {
		HashSet<Integer> result = new HashSet<Integer>();
		String[] qWords = query.toLowerCase().split(" ");
		qWords=preProcess(qWords);
		for(int i=0;i<qWords.length;i++){
		//System.out.println("qWords "+qWords[i]);
		}
		if (qWords.length == 0)
			return new HashSet<Integer>();
		Map<String, ArrayList<Integer>> termToDoc = new HashMap<String, ArrayList<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(invertedIndexFname));
		String line;
		while ((line = br.readLine()) != null) {
			String[] termDoc = line.toLowerCase().split(" ");
			
			String term = termDoc[0];
			term=convert(term);
			if (termToDoc.containsKey(term)) {
				ArrayList<Integer> termDocList = termToDoc.get(term);
				termDocList.add(Integer.parseInt(termDoc[1]));
				termToDoc.put(term, termDocList);
			} else {
				ArrayList<Integer> termDocList = new ArrayList<Integer>();
				termDocList.add(Integer.parseInt(termDoc[1]));
				termToDoc.put(term, termDocList);
			}
		}
		TreeSet<Integer> rootSet = null;
		for (int i = 0; i < qWords.length; i++) {
			if (!termToDoc.containsKey(qWords[i])) {
				break;
			} else {
				ArrayList<Integer> dList = termToDoc.get(qWords[i]);
				if (rootSet == null) {
					rootSet = new TreeSet<Integer>(dList);
				} else {
					rootSet.retainAll(dList);
				}
				if (rootSet.isEmpty()) {
					break;
				}

			}
		}
		br.close();
		if (rootSet == null)
			return result;
		Iterator<Integer> it = rootSet.iterator();
		while (it.hasNext() && h > 0) { // return top h
			result.add(it.next());
			h--;
		}
		return result;
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 7) {
			System.err.print(
					"./eecs485pa5h <h value> (-k <numiterations> | -converge <maxchange>) “queries” <input-net-file> <input-inverted-index-file> <output-file>");
			return;
		}
		
		int h = Integer.valueOf(args[0]);
		String query = args[3];

		File inputNetFname = new File(args[4]).getCanonicalFile();
		File invertedIndexFname = new File(args[5]).getCanonicalFile();
		File output = new File(args[6]).getCanonicalFile();
		Hits hits = new Hits();
		hits.initStopWord();
		HashSet<Integer> seedSet = hits.processQuery(h, query, invertedIndexFname);
		HashSet<Integer> baseSet = hits.getBaseSet(seedSet, inputNetFname);
		Map<Integer, Double> auth = hits.initAuthHub(baseSet, 1.0);
		Map<Integer, Double> hub = hits.initAuthHub(baseSet, 1.0);
		Map<Integer, HashSet<Integer>> connectTo = new HashMap<Integer, HashSet<Integer>>();
		Map<Integer, HashSet<Integer>> connectedBy = new HashMap<Integer, HashSet<Integer>>();
		Map<Integer, Double> authForCal = hits.initAuthHub(baseSet, 0.0);
		Map<Integer, Double> hubForCal = hits.initAuthHub(baseSet, 0.0);

		Iterator<HashMap<Integer, Integer>> it = hits.graphInfo.iterator();
		while (it.hasNext()) {
			HashMap<Integer, Integer> nodeInfo = it.next();

			Iterator<Map.Entry<Integer, Integer>> itr = nodeInfo.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) itr.next();

				Integer page1 = entry.getKey();
				Integer page2 = entry.getValue();
				if (baseSet.contains(page1) && baseSet.contains(page2)) {
					if (connectTo.containsKey(page1)) {
						HashSet<Integer> connectToSet = connectTo.get(page1);
						connectToSet.add(page2);
						connectTo.put(page1, connectToSet);
					} else {
						HashSet<Integer> connectToSet = new HashSet<Integer>();
						connectToSet.add(page2);
						connectTo.put(page1, connectToSet);
					}
					if (connectedBy.containsKey(page2)) {
						HashSet<Integer> connectedBySet = connectedBy.get(page2);
						connectedBySet.add(page1);
						connectedBy.put(page2, connectedBySet);
					} else {
						HashSet<Integer> connectedBySet = new HashSet<Integer>();
						connectedBySet.add(page1);
						connectedBy.put(page2, connectedBySet);
					}
				}
			}

		}
		if (args[1].equals("-k")) {
			hits.stopByIteration(auth, hub, authForCal, hubForCal, connectedBy, connectTo, Integer.parseInt(args[2]));

		} else if (args[1].equals("-converge")) {
			double maxChange = Double.parseDouble(args[2]);
			hits.stopByConverge(auth, hub, authForCal, hubForCal, connectedBy, connectTo, maxChange);

		}

		Iterator<Map.Entry<Integer, Double>> authIt = auth.entrySet().iterator();
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));

		while (authIt.hasNext()) {
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) authIt.next();

			bw.write(entry.getKey() + "," + hub.get(entry.getKey()) + "," + entry.getValue() + "\n");

		}
		System.out.print("finish");
		bw.close();

	}

}
