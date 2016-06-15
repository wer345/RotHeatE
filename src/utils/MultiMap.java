package utils;
import java.util.*;

/*
 * One to Many mapping table
 * 
 */
public class MultiMap {

	HashList<String, NameSet> table = new HashList<String, NameSet>();
	String name="" ;
	
	MultiMap () {
	}
	
	public MultiMap (String _name) {
		name=_name;
	}
	
	public void add(String src,String dest) {
		NameSet target=(NameSet)table.get(src);
		if (target==null) {
			target = new NameSet();
			table.add(src,target);
		}
		target.add(dest);
	}
	
	// One to Many mapping
	public NameSet map(String src) {
		return table.get(src);
	}
	
	// Many to Many <or> mapping
	public NameSet map(NameSet src) {
		NameSet result =  new NameSet();
		for ( String key : src.nameList) {
			NameSet target =  table.get(key);
			result.orSelf(target);
		}
		return result;
	}
	
	public List <String> getKeys()
	{
		return table.keys;
	}
	
	public NameSet get(String key)
	{
		return table.get(key);
	}
	
	public void print() {
		for(String keyName:table.keys)
		{	
			NameSet target=(NameSet)table.get(keyName);
			Logger.debug("print","%s --> ",keyName);
			target.print();
		}
	}
}
