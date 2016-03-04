package com.altheadx.pgx.utils;

import java.util.*; 

public class NameSet {
	public List <String> nameList = new ArrayList<String>();
	
	// initialize with a list of arguments
	public NameSet(String... args) {
		for (String arg : args) {
			add(arg);
		}
	}

	// initialize with a list of string
	public NameSet(List <String> items) {
		for (String item : items) {
			add(item);
		}
	}

	public int size() {
		return nameList.size();
	}
	
	public String getItem(int index) {
		if (index>=0 && index<nameList.size())
			return nameList.get(index);
		else 
			return "";
	}

	public String toString(String delimiter) {
/*		
		String str="";
		boolean first=true;
		for(String name:nameList) {
			if (first) 
				first=false;
			else
				str+=delimiter;
			str+=name;
		}
		return str;
*/
		return Util.list2String(nameList, delimiter);
	}
	
	public String toString() {
		return "["+toString(",")+"]";
	}
	private boolean theSame(String drug1,String drug2) {
		return drug1.equalsIgnoreCase(drug2);
	}

	// add one element into this set
	public void add(String name) {
		for (String aName : nameList) {
			if (theSame(aName,name))
				return;
		}
		nameList.add(name);
	}

	// add a list of element into this set
	public void add(List <String> names) {
		for (String name : names) {
			add(name);
		}
	}
	
	// remove one element from this set
	public boolean remove(String name) {
		for (String aName : nameList) {
			if (theSame(aName,name)) {
				return nameList.remove(aName);
			}
		}
		return false;
	}

	// if <name> is in this set, return true, else return false
	public boolean in(String name) {
		for (String aName : nameList) {
			if (theSame(aName,name)) {
				return true;
			}
		}
		return false;
	}
	

	public NameSet and(NameSet names) {
		NameSet newNames= new NameSet();
		for (String aName : names.nameList) {
			if (in(aName)) {
				newNames.add(aName);
			}
		}
		return newNames;
	}

	public void orSelf(NameSet names) {
		if (names==null)
			return;
		for (String aName : names.nameList) {
				add(aName);
		}
	}

	public NameSet duplicate() {
		NameSet newNames= new NameSet();
		for (String aName : nameList) {
			newNames.add(aName);
		}
		return newNames;
	}
	public NameSet or(NameSet names) {
		NameSet newNames= new NameSet();
		for (String aName : nameList) {
			newNames.add(aName);
		}
		
		for (String aName : names.nameList) {
			if (!in(aName)) {
				newNames.add(aName);
			}
		}
		return newNames;
	}

	public NameSet sub(NameSet names) {
		NameSet newNames= new NameSet();
		for (String aName : nameList) {
			if (!names.in(aName)) {
				newNames.add(aName);
			}
		}
		return newNames;
	}

	public void clear() {
		nameList.clear();
	}
	
	public NameSet selectFirst(int n) {
		NameSet newNames= new NameSet();
		for (int i=0;i<n && i <nameList.size();i++) {
			newNames.add(nameList.get(i));
		}
		return newNames;
	}
	
	public NameSet randomSelection(int max) {
		if (max<=0)
			return new NameSet();
		
		NameSet names=duplicate();
		if(max<nameList.size()) {
			int size=nameList.size();
			int removeNumber=size-max;
			Random rand = new Random(); 
			while(removeNumber>0) {
				 int idx = rand.nextInt(size);
				 names.nameList.remove(idx);
				 removeNumber--;
				 size--;
			}
		}
		return names;
	}
	
	public void sort(Comparator <String> c) {
		java.util.Collections.sort(nameList,c);
	}

	public void sort() {
		java.util.Collections.sort(nameList);
	}
	
	public void print() {
		if (nameList.size()>0) {
			for (String aName : nameList) {
				Logger.debug("print","%s, ", aName);
			}
		}
		else 
			Logger.debug("","empty");
	}
}


