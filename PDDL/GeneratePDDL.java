package PDDL;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GeneratePDDL {
	
	public static void main (String []args) throws IOException { 
		
		//READ VGDL
		ArrayList<SpriteSet> SList = new ArrayList<SpriteSet>();
		ArrayList<LevelMapping> LMList = new ArrayList<LevelMapping>();
		ArrayList<InteractionSet> iList = new ArrayList<InteractionSet>();
		ArrayList<TerminationSet> tList = new ArrayList<TerminationSet>();
		
		getDataVGDL(SList, LMList, iList, tList);
		/*
		//Check it's working	
		System.out.println("Sprite Set:");
		for (int i = 0; i < SList.size(); ++i){
			System.out.println(SList.get(i).name + " " + SList.get(i).type);
		}
		
		System.out.println("\nLevel Mapping:");
		for (int i = 0; i < LMList.size(); ++i) {
			System.out.println(LMList.get(i).value + " " + LMList.get(i).name);
		}
		
		System.out.println("\nInteraction Set:");
		for (int i = 0; i < iList.size(); ++i) {
			System.out.println(iList.get(i).object1 + " " + iList.get(i).object2 + " " + iList.get(i).interaction);
		}
		*/
		System.out.println("\nInteraction Graph:");
		HashMap<String,String> interactionGraph = new HashMap<String,String>();
		for (int i = 0; i < iList.size(); ++i) {
			//the key may have different order of objects
			String concatKey = iList.get(i).object1 + iList.get(i).object2;
			interactionGraph.put(concatKey, iList.get(i).interaction);
			concatKey = iList.get(i).object2 + iList.get(i).object1;
			interactionGraph.put(concatKey, iList.get(i).interaction);
		}
		/*
		System.out.println(interactionGraph.get("avatarwall"));
		System.out.println(interactionGraph.get("boxwall"));
		System.out.println(interactionGraph.get("boxbox"));
		System.out.println(interactionGraph.get("avatarbox"));
		System.out.println(interactionGraph.get("boxhole"));
		
		//Interactions Deep
		*/
		int deep = 0;	
		ArrayList<ArrayList<InteractionSet>> listOfBranches = new ArrayList<ArrayList<InteractionSet>>();
		deep = getDeep(iList, interactionGraph, deep, listOfBranches);
		System.out.println("\nInteractions Deep: " + deep + "\n");
		/*
		
		System.out.println("\nTermination Set:");
		for (int i = 0; i < tList.size();++i) {
			System.out.println(tList.get(i).stype + " " + tList.get(i).limit + " " + tList.get(i).win);
		}

		
		//READ MAP
		BufferedReader in = null;	
		String filepath = "src/sokoban_lvl0.txt";
		in = new BufferedReader (new FileReader(filepath));
		String line = in.readLine();
		ArrayList<ObjectMap> objectList = new ArrayList<ObjectMap>();
		int j = 0;
		while (line != null) {
			initObjects(line, objectList, j, LMList);
			++j;
			line = in.readLine();
		}
		in.close();
		
		System.out.println("\nObject Map:");
		for (int i = 0; i < objectList.size(); ++i) {
			System.out.println(objectList.get(i).name + " X" + objectList.get(i).xpos + " Y" + objectList.get(i).ypos);
		}
		//goal
		for (int i = 0; i < tList.size(); ++i) {
			String s = "num_";
			if (tList.get(i).win.equals("True")){
				s += tList.get(i).stype + " " + tList.get(i).limit;
			}
			System.out.println(s);
		}*/
		generateProblem(tList, LMList, deep, listOfBranches);
		
		
	}
	
	private static void generateProblem(ArrayList<TerminationSet> tList, ArrayList<LevelMapping> LMList, int deep, ArrayList<ArrayList<InteractionSet>> listOfBranches) throws IOException{
		BufferedReader in = null;	
		String filepath = "src/sokoban_easy.txt";
		in = new BufferedReader (new FileReader(filepath));
		String line = null;
		line = in.readLine();
		int size = line.length();
		ArrayList<ObjectMap> objectList = new ArrayList<ObjectMap>();
		//ArrayList<TerminationSet> goalList = new ArrayList<TerminationSet>();
		int j = 0;
		while (line != null) {
			initObjects(line, objectList, j, LMList);
			++j;
			line = in.readLine();
		}
		//It's supposed that SpriteCounter is the only type of TerminationSet. In another game needs to check which type of Termination is
		ArrayList<String> iterators = new ArrayList<String>();
		for (int i = 0; i < tList.size(); ++i) {
			//Searching how many objects are in the map
			String obj = tList.get(i).stype;	
			int count = 0;
			for (int k = 0; k < objectList.size(); ++k) {
				if (objectList.get(k).name.equals(obj)) {
						++count;
				}
			}
			for (int k = 0; k <= count; ++k) {
					iterators.add("i" + Integer.toString(k));
			}
						
		}
		in.close();
		ArrayList<Predicate> predList = new ArrayList<Predicate>();
		ArrayList<Predicate> precon = new ArrayList<Predicate>();
		ArrayList<String> var = new ArrayList<String>();
		System.out.println(defineProblem() + domainProblem() + objectProblem(size, iterators) + initProblem(iterators, objectList, size, tList, predList) + goalProblem(tList));
		System.out.println(defineDomain() + typesDomain() + predicatesDomain(predList) + parameters(deep, var) + precondition(var, precon) + effect(listOfBranches, deep, var, precon));
	}
	
	private static String defineDomain() {
		String define;
		String nameDomain = "domain0";
		define = "(define (domain ";
		define += nameDomain;
		define += ")";
		return define;
	}
	
	private static String typesDomain() {
		String types;
		types = "\n\t(:types xpos ypos value)";
		return types;
	}
	
	private static String predicatesDomain(ArrayList<Predicate> predList) {
		String predicates;
		predicates = "\n\t(:predicates";
		for (int i = 0; i < predList.size(); ++i) {
			if (predList.get(i).var.size() <= 1) {
				predicates += "\n\t\t(" + predList.get(i).namevar + " " + predList.get(i).var.get(0) +  ")";
			}
			else predicates += "\n\t\t(" + predList.get(i).namevar + " " + predList.get(i).var.get(0) + " " + predList.get(i).var.get(1) +  ")";
		}
		predicates += "\n\t)";
		return predicates;
	}
	
	private static String parameters(int deep, ArrayList<String> var) {
		String p = "\n\t(:action right"; 
		p += "\n\t\t:parameters (\n\t\t\t";
		for (int i = 0; i < deep; ++i) {
			var.add("?x"+i);
			p += "?x" + i + " ";
		}
		p += "\n\t\t\t?y";
		//Condition Spritecounter / Iterator
		p += "\n\t\t\t?v0 ?v1";
		p += "\n\t\t)";
		return p;
	}
	
	private static String precondition(ArrayList<String> var, ArrayList<Predicate> precon) {
		String p = "\n\t\t:preconditions";
		p += "\n\t\t(and";
		p += "\n\t\t\t(avatar " + var.get(0) + " " + var.get(0) + ")";
		Predicate predicate = new Predicate ("avatar",var);
		precon.add(predicate);
		//Condition Spritecounter / Iterator
		p += "\n\t\t\t(iterator ?i0 ?i1)";
		//Always Connection Locations
		p += "\n\t\t\t(suc ?x0 ?x1)";
		p += "\n\t\t)";
		return p;
	}
	
	private static String effect(ArrayList<ArrayList<InteractionSet>> listOfBranches, int deep, ArrayList<String> var, ArrayList<Predicate> precon) {
		String e = "\n\t\t:effect";
		
		//Condition 0 Avatar-->
		e += "\n\t\t\t(when (not (wall " + var.get(1) + " " + var.get(0) + "))";
		for (int i = 0; i < precon.size(); ++i) {
			if (precon.get(i).namevar.equals("avatar")) {
				//Change var for ArrayList of Int to increment o dicrease the number depending of stepback o stepforward
				//Check interaction between objects
				e += "\n\t\t\t(" + precon.get(i).namevar + " " + precon.get(i).var.get(0) + " " + precon.get(i).var.get(0) + ")";
			}
		}
		
		for (int i = 0; i < listOfBranches.size(); ++i) {
			for (int j = 0; j < listOfBranches.get(i).size(); ++j) {
				String obj1 = listOfBranches.get(i).get(j).object1;
				String obj2 = listOfBranches.get(i).get(j).object2;
				//Condition 1 Avatar-Box
				//if (listOfBranches.get(i).size() < deep)
				
			}
		}
		return e;
	}
	
	private static void actionsDomain(){
	}
	
	private static String defineProblem() {
		String define;
		String nameProblem = "problem0";
		define = "(define (problem ";
		define += nameProblem;
		define += ")";
		return define;
	}

	private static String domainProblem() {
		String domain;
		String nameDomain = "domain0";
		domain = "\n\t(:domain ";
		domain += nameDomain;
		domain += ")";
		return domain;
	}

	private static String objectProblem(int size, ArrayList<String> iterators) {
		String objects;
		objects = "\n\t(: objects\n";
		objects += "\t\t";
		for (int i = 0; i < size; ++i) {
			objects += "x" + i + " ";
		}
		objects += "- xpos\n";
		objects += "\t\t";
		for (int i = 0; i < size; ++i) {
			objects += "y" + i + " ";
		}
		objects += "- ypos\n";
		objects += "\t\t";
		
		for (int i = 0; i < iterators.size(); ++i) {
			objects += iterators.get(i) + " ";
		}
		objects += "- value\n\t)";
		return objects;
	}
	
	private static String initProblem(ArrayList<String> iterators , ArrayList<ObjectMap> objList, int size, ArrayList<TerminationSet> tList, ArrayList<Predicate> predList) {	
		String init = "\n\t(: init\n";
		int sizeList = objList.size();
		for (int i = 0; i < sizeList; ++i) {
			boolean found = false;
			for (int j = 0; j < predList.size(); ++j) {
				if (predList.get(j).namevar.equals(objList.get(i).name)) {
					found = true;
				}
			}
			ArrayList<String> var = new ArrayList<String>();
			var.add("?x");
			var.add("?y");
			Predicate pred = new Predicate(objList.get(i).name,var);
			if (!found) predList.add(pred);
			//future update: order name objects
			init += "\t\t(";
			init += objList.get(i).name;
			int x = objList.get(i).xpos;
			int y = objList.get(i).ypos;
			init += " x" + Integer.toString(x) + " y" + Integer.toString(y) + ")\n";
		}
		//iterator for successive locations, It may simplify in one coordinate, not x and y
		for (int i = 0; i < size-1; ++i) {
			init += "\t\t(suc-x x" + Integer.toString(i) + " x" + Integer.toString(i+1) +")\n";
			init += "\t\t(suc-y y" + Integer.toString(i) + " y" + Integer.toString(i+1) +")\n";
		}
		ArrayList<String> var = new ArrayList<String>();
		var.add("?x");
		var.add("?y");
		Predicate pred = new Predicate("suc-x", var);
		predList.add(pred);
		pred = new Predicate("suc-y", var);
		predList.add(pred);
		
		for (int i = 0; i < iterators.size()-1; ++i) {
			init += "\t\t(iterator " + iterators.get(i) + " " + iterators.get(i+1) + ")\n";
		}
		var = new ArrayList<String>();
		var.add("?i");
		var.add("?i");
		pred = new Predicate("iterator", var);
		predList.add(pred);
		
		//It's supposed there is only SpriteCounter
		for (int i = 0; i < tList.size(); ++i) {
			String obj = tList.get(i).stype;	
			int count = 0;
			for (int k = 0; k < objList.size(); ++k) {
				if (objList.get(k).name.equals(obj)) {
						++count;
				}
			}
			var = new ArrayList<String>();
			var.add("?i");
			pred = new Predicate(obj + "_counter", var);
			predList.add(pred);
			init += "\t\t(" + obj + "_counter i" + count + ")\n";
		}
		init += "\t)";
		return init;
	}
	
	private static String goalProblem(ArrayList<TerminationSet> tList) {
		String goal = "\n\t(:goal\n";;
		int size = tList.size();
		if (size > 1) goal += "(and ";
		for (int i = 0; i < size; ++i) {
			goal += "\t\t(";
			goal += tList.get(i).stype;
			goal += "_counter i";
			goal += tList.get(i).limit;
			goal += ")\n\t)\n";
		}
		goal += ")\n";
		return goal;
	}
	
	public static int getDeep(ArrayList<InteractionSet> interactionList, HashMap<String,String> interactionGraph, int deep, ArrayList<ArrayList<InteractionSet>> listOfBranches){
		ArrayList<InteractionSet> branches = new ArrayList<InteractionSet>();
		//ArrayList<ArrayList<InteractionSet>> listOfBranches = new ArrayList<ArrayList<InteractionSet>>(); 
		
		String parent = "";
		String child = "avatar";
		
		getAllBranchesDim1(parent, child, interactionList, interactionGraph, branches, listOfBranches);
		
		for (int i = 0; i < branches.size(); ++i) {
			System.out.println("Branches: " + branches.get(i).object1 + "-" + branches.get(i).object2);
		}
		
		ArrayList<InteractionSet> aux = new ArrayList<InteractionSet>();
		ArrayList<InteractionSet> aux2 = new ArrayList<InteractionSet>();
		
		int k = 0;
		getBranches(parent, child, branches, listOfBranches,aux, aux2, k);
		
		int maxdeep = 0;
		for (int i = 0; i < listOfBranches.size(); ++i) {
			if (listOfBranches.get(i).size() > maxdeep){
				deep = listOfBranches.get(i).size() + 1;
			}
		}
		
		for (int i = 0; i < listOfBranches.size(); ++i) {
			for (int j = 0; j < listOfBranches.get(i).size(); ++j) {
				System.out.println("[" + i + "," + j + "]: " + listOfBranches.get(i).get(j).object1 + "-" + listOfBranches.get(i).get(j).object2);
			}
		}
		
		return deep;
	}
	
	public static void getBranches(String parent, String child, ArrayList<InteractionSet> branches, ArrayList<ArrayList<InteractionSet>> listOfBranches, ArrayList<InteractionSet> aux, ArrayList<InteractionSet> aux2, int i){
		while (i < branches.size()) {
			//System.out.println("iteration" + i);
			//System.out.println(parent + " " + child);
			//First branch is the interaction of avatar with another object
			if (branches.get(i).object1.equals(child)) {
				aux2 = new ArrayList<InteractionSet>(aux);
				ArrayList<InteractionSet> aux3 = new ArrayList<InteractionSet>(aux);
				aux2.add(branches.get(i));
				listOfBranches.add(aux2);
				
				//for (int k = 0; k < aux2.size(); ++k ) System.out.println("Aux: " + aux2.get(k).object1 + " " + aux2.get(k).object2);
				if (i+1 < branches.size()) {
					if (branches.get(i+1).object1.equals(branches.get(i).object2)){
						aux = new ArrayList<InteractionSet>(aux2);
						String p = branches.get(i).object1;
						String c = branches.get(i).object2;
						++i;
						getBranches(p,c,branches,listOfBranches, aux, aux2, i);
						aux = new ArrayList<InteractionSet>(aux3);
					}
				}
			}
			++i;
		}
	}
	
	public static void getAllBranchesDim1(String parent, String child, ArrayList<InteractionSet> interactionList, HashMap<String,String> interactionGraph, ArrayList<InteractionSet> branches, ArrayList<ArrayList<InteractionSet>> listOfBranches){
		for (int i = 0; i < interactionList.size(); ++i) {
			//System.out.println("Parent: " + parent + " - Child: " + child);
			//System.out.println(interactionList.get(i).object1 + "-" + interactionList.get(i).object2);
			
			//One direction, we need to check obj2-obj1 in hashmap, because the order
			if (interactionList.get(i).object1.equals(child) && !interactionList.get(i).object2.equals(parent)) {
				
				String concatKey = interactionList.get(i).object1 + interactionList.get(i).object2;
				
				if (interactionGraph.get(concatKey).equals("bounceForward") || interactionGraph.get(concatKey).equals("killSprite")) {
					String p = interactionList.get(i).object1;
					String c = interactionList.get(i).object2;
					
					//could check if there is parent-child in branches list
					InteractionSet interaction = new InteractionSet(p,c,interactionGraph.get(concatKey));
					branches.add(interaction);
					//Check for infinite recursion, make a limit
					getAllBranchesDim1(p, c, interactionList, interactionGraph, branches, listOfBranches);
				}
			}
			else if (interactionList.get(i).object2.equals(child) && !interactionList.get(i).object1.equals(parent)) {
				
				String concatKey = interactionList.get(i).object1 + interactionList.get(i).object2;
				
				if (interactionGraph.get(concatKey).equals("bounceForward") || interactionGraph.get(concatKey).equals("killSprite")) {
					String p = interactionList.get(i).object2;
					String c = interactionList.get(i).object1;
					
					InteractionSet interaction = new InteractionSet(p,c,interactionGraph.get(concatKey));
					branches.add(interaction);
					
					//Check for infinite recursion, make a limit
					getAllBranchesDim1(p, c, interactionList, interactionGraph, branches, listOfBranches);
				}
			}
		}
	}
	
	public static void getDataVGDL(ArrayList<SpriteSet> SpriteList, ArrayList<LevelMapping> MapList, ArrayList<InteractionSet> interList, ArrayList<TerminationSet> termList) throws IOException{
		BufferedReader in = null;
		String filepath = "src/sokoban.txt";
		in = new BufferedReader (new FileReader(filepath));
		String line = in.readLine();
		while (line != null){
			//Review condition part 4 ifs, to do more efficient, maybe use select case
			
			if (line.contains("SpriteSet")){
				line = in.readLine();

				while (!line.contains("LevelMapping")){
					String[] str = line.split("\\s+");
					SpriteSet sprite = new SpriteSet(str[1],str[3]);
					SpriteList.add(sprite);
					line = in.readLine();
				}
				//SpriteSet sprite = new SpriteSet("wall","Immovable");
				//SpriteList.add(sprite);
			}	
			if (line.contains("LevelMapping")) {
				line = in.readLine();
				while (!line.contains("InteractionSet")) { 
					String[] str = line.split("\\s+");
					LevelMapping levelMap = new LevelMapping((str[1]).charAt(0),str[3]);
					MapList.add(levelMap);
					line = in.readLine();
				}
				LevelMapping levelMap = new LevelMapping('A',"avatar");
				MapList.add(levelMap);
				levelMap = new LevelMapping('w',"wall");
				MapList.add(levelMap);		
			}
			if (line.contains("InteractionSet")) {
				line = in.readLine();
				while (!line.contains("TerminationSet")) { 
					String[] str = line.split("\\s+");
					InteractionSet interaction = new InteractionSet(str[1], str[2], str[4]);
					interList.add(interaction);
					line = in.readLine();		
				}
			}
			//For terminationSet I suppose that it's only SpriteCounter type, but in other games there're more types it has to implement an improvement
			if (line.contains("TerminationSet")){
				line = in.readLine();
				
				while (line != null) {
					String[] str = line.split("\\s+");
					String[] str2 = null; 
					String[] str3 = null;
					String[] str4 = null;
					for (int i = 0; i < str.length; ++i) {
						
						if (str[i].contains("stype")) {
							str2 = str[i].split("=");
						}
						else if (str[i].contains("limit")) {
							str3 = str[i].split("=");
						}
						else if (str[i].contains("win")) {
							str4 = str[i].split("=");
						}
					}
					TerminationSet termination = new TerminationSet(str2[1],str3[1],str4[1]);
					termList.add(termination);
					line = in.readLine();
				}
			}
			line = in.readLine();
		}
	}
	
	private static void initObjects(String line, ArrayList<ObjectMap> objList, int y, ArrayList<LevelMapping> LMList) {
		String nameObj = "";
		for (int i = 0; i < line.length(); ++i) {
			boolean found = false;
			while (!found){
				for (int j = 0; j < LMList.size(); ++j) {
					if (line.charAt(i) == LMList.get(j).value) {
						ObjectMap obj = new ObjectMap (LMList.get(j).name,i,y);
						objList.add(obj);
						found = true;
					}
				}
				found = true;
			}		
		}
	}
}
