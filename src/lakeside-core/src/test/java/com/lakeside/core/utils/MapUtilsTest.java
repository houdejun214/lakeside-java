package com.lakeside.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.junit.Test;

public class MapUtilsTest {

	@Test
	public void testSortedByValues() {
		Map<String, Double> map = new HashMap<String,Double>();
		map.put("1.02", 1.02);
		map.put("2.68", 2.68);
		map.put("2.38", 2.38);
		map.put("1.84", 1.84);
		map.put("0.89", 0.89);
		map.put("9.88", 9.88);
		map.put("5.84", 5.84);
		
		SortedSet<Entry<String, Double>> sortedByValues = MapUtils.sortedByValues(map,true);
		for(Entry<String,Double> entry:sortedByValues){
			String key = entry.getKey();
			Double value = entry.getValue();
			System.out.println(key+":"+value);
		}
	}

}
