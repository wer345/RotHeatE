package com.altheadx.pgx.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class HashList <K,V> {
	public Hashtable <K,V> list = new Hashtable <K,V> ();
	public List <K> keys = new ArrayList <K> ();
	
	public void add(K key, V value) {
		if(list.get(key)==null) {
			keys.add(key);
		}
		list.put(key, value);
	}

	public void remove(K key) {
		list.remove(key);
		keys.remove(key);
	}

	public V get(K key) {
		return list.get(key);
	}
	
	public int size() {
		return list.size();
	}
	
	public NameSet getKeySet() {
		NameSet set= new NameSet();
		for (K key:keys) {
			set.add(key.toString());
		}
		return set;
	}

	public void clear() {
		keys.clear();
		list.clear();
	}
	
	public String toString()
	{
		String out="";
		for (K key:keys) {
			V value= list.get(key);
			if(out.length()>0)
				out+="\n";
			out+=String.format("%s: '%s'",key.toString(),value.toString());
		}
		return out;
	}
	
	public String valueList()
	{
		String out="";
		for (K key:keys) {
			V value= list.get(key);
			if(out.length()>0)
				out+="\n";
			out+=String.format("%s",value.toString());
		}
		return out;
	}
	
	public void sortKeys()
	{
//		Collections.sort(keys);		
	}
	
}
