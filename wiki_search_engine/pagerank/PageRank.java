//package edu.umich.eecs485.pa5;
import java.io.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Formatter;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class PageRank {
	public static void main(String[] args) throws IOException {
		// check the args 
		if(args.length != 6){
			System.out.println("Error: make sure you have the correct command");
			System.out.println("eecs485pa5p <dvalue> (-k <numiterations> | -converge <maxchange>) <edges file (mining.edges.xml)> <article file (mining.articles.xml)> outputfile");
			return;
		}
		
		System.out.println("Process the command line argument");
		double d = Double.parseDouble(args[0]);
		double c = 0.0;
		int k = 0;
		int choice = 0;
		if(args[1].equals("-k")){
			System.out.println("user defines k");
			k = Integer.parseInt(args[2]);
			choice = 1;
		}else if(args[1].equals("-converge")){
			System.out.println("user defines converge");
			c = Double.parseDouble(args[2]);
			choice = 2;
		}else{
			System.out.println("Error on the flag: please specify -k or -converge");
			return;
		}

		String edgesfilename = args[3];
		String articlesfilename = args[4];
		String outputfile = args[5];
		
		if(choice == 1){
			System.out.println("d: " + d + " k: " + k + " inputfile: " + edgesfilename + " " + articlesfilename + " outputfile: " + outputfile);
		}else{
			System.out.println("d: " + d + " c: " + c + " inputfile: " + edgesfilename + " " + articlesfilename + " outputfile: " + outputfile);
		}
		System.out.println("Process the command line argument finished ");
		

		System.out.println("pre-process the data ");
		HashMap<Integer, Integer> numberOfOutGoingLinks = new HashMap<Integer, Integer>();
		HashMap<Integer, ArrayList<Integer>> inComingLinks = new HashMap<Integer, ArrayList<Integer>>();
		HashMap<Integer,Double> old_result = new HashMap<Integer,Double>();
		HashMap<Integer,Double> new_result = new HashMap<Integer,Double>();
		HashMap<Integer,Double> sinkNodes = new HashMap<Integer, Double>();

		// parsing the xml file
		File edgesFile = new File(edgesfilename).getCanonicalFile();
        File articlesFile = new File(articlesfilename).getCanonicalFile();

        DocumentBuilderFactory dbFactory;
        DocumentBuilder dBuilder;
        Document doc;
        
        try {
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(articlesFile);
        } catch (Exception e) {
            System.out.println("somthing wrong with parsing");
            e.printStackTrace();
            return;
        }

        NodeList articles = doc.getElementsByTagName("eecs485_article");

		int numVertices = 0;
		int numArcs = 0;
		double initial_pageRank = 0.0;

		numVertices = articles.getLength();
		initial_pageRank = 1.0 / (double) numVertices;

		for( int i = 0; i < articles.getLength(); i++){
			Node articleId = ((Element) articles.item(i)).getElementsByTagName("eecs485_article_id").item(0);
			int node = Integer.parseInt(articleId.getTextContent());
			new_result.put(node, initial_pageRank);
			old_result.put(node, initial_pageRank);
				
			inComingLinks.put(node, new ArrayList<Integer>());
			numberOfOutGoingLinks.put(node, 0);
			sinkNodes.put(node,0.0);
		}

		try {
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(edgesFile);
        } catch (Exception e) {
            System.out.println("exception from trying to parse edges");
            e.printStackTrace();
            return;
        }
      
        NodeList edges = doc.getElementsByTagName("eecs485_edge");

        numArcs = edges.getLength();

        for(int i = 0; i < edges.getLength(); i++){
        	Node edgeFrom = ((Element) edges.item(i)).getElementsByTagName("eecs485_from").item(0);
            Node edgeTo = ((Element) edges.item(i)).getElementsByTagName("eecs485_to").item(0);

            int node = Integer.parseInt(edgeFrom.getTextContent());
			int node_to = Integer.parseInt(edgeTo.getTextContent());

			if(node == node_to){
					continue;
			}
			// remove the sink node
			sinkNodes.remove(node);
			inComingLinks.get(node_to).add(node);
			numberOfOutGoingLinks.put(node, numberOfOutGoingLinks.get(node)+1);
        }

		System.out.println("finishing pre processing the data ");
		
		if(choice == 1){
			System.out.println("choice 1");
			//using k
			for(int i = 0; i < k; i++){	
				// get the pageRank from the sinkNode
				double sinkNodesRank = 0.0;
				
				for(int j : sinkNodes.keySet()){
					sinkNodesRank += old_result.get(j);
				}
				sinkNodesRank /= (double)(numVertices -1);	
				//calculte each of the node's new 
				System.out.println("iterate: "+i);
				for (int n : old_result.keySet()){
					// get the incoming link to calculate the new page rank
					double new_rank = 0.0;
					double tsr = 0.0;
					new_rank += sinkNodesRank;
					if(sinkNodes.containsKey(n)){
						tsr= (old_result.get(n) / (double)(numVertices-1));
					}
					new_rank -= tsr;
					// get the incoming link to calculate the new page rank
					double old_rank = old_result.get(n);
					List<Integer> inLinks = inComingLinks.get(n);
					for(int z = 0; z< inLinks.size(); z++){
						new_rank += old_result.get(inLinks.get(z))/(double)(numberOfOutGoingLinks.get(inLinks.get(z)));
					}
					
					new_rank = new_rank*d;
					new_rank = (1-d)/((double)(numVertices)) + new_rank;
					new_result.put(n,new_rank);	
				}
				old_result.clear();
				old_result.putAll(new_result);
			}
		}else if (choice == 2){
			// using converge
			System.out.println("choice 2");
			double X = 1.0;
			while(X > c){	
				double maxChange = 0.0;
				//calculte each of the node's new
				System.out.println("X : "+X);
				// get the pageRank from the sinkNode
				double sinkNodesRank = 0.0;
				for(int j : sinkNodes.keySet()){
					sinkNodesRank += old_result.get(j);
				}
				sinkNodesRank /= (double)(numVertices -1);				
				for (int n : old_result.keySet()){
					double new_rank = 0.0;
					double tsr = 0.0;
					new_rank += sinkNodesRank;
					if(sinkNodes.containsKey(n)){
						tsr= (old_result.get(n) / (double)(numVertices-1));
					}
					new_rank -= tsr;
					// get the incoming link to calculate the new page rank
					double old_rank = old_result.get(n);
					List<Integer> inLinks = inComingLinks.get(n);
					for(int z = 0; z< inLinks.size(); z++){
						new_rank += old_result.get(inLinks.get(z))/(double)(numberOfOutGoingLinks.get(inLinks.get(z)));
					}
					
					new_rank = new_rank*d;
					new_rank = (1-d)/((double)(numVertices)) + new_rank;
					new_result.put(n,new_rank);	
					double temp = Math.abs(new_rank - old_rank) /old_rank;
					if(temp > maxChange){
						maxChange = temp;
					}
				}
				//copy the new to old
				X = maxChange;
				old_result.clear();
				old_result.putAll(new_result);
			}
		}
		
		PrintWriter writer = new PrintWriter(outputfile, "UTF-8");
		double sum = 0.0;
		for(int i: new_result.keySet()){
			Formatter fmt = new Formatter();
			fmt.format("%5.4e", new_result.get(i));
			sum += new_result.get(i);
			writer.println(i + "," + fmt.toString());
		}
		System.out.println(sum);
		writer.close();
	}
}
