package PDDL;

import java.util.ArrayList;

public class Predicate {
	public ArrayList<String> var = new ArrayList<String>();
	public String namevar;	
	public Predicate (String n, ArrayList<String> x) {
		namevar = n;
		var = x;
	}
}
