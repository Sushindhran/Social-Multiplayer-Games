package org.scrabble.client;

import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<Integer> a = Lists.newArrayList();
		List<Integer> b = Lists.newArrayList();
		
		a.add(2);
		a.add(1);
		a.add(4);
		b.add(1);
		b.add(2);
		b.add(3);
		boolean y = a.equals(b);
		boolean x = ImmutableSet.copyOf(a).equals(ImmutableSet.copyOf(b));
		System.out.println(x+" "+y + " "+Sets.difference(ImmutableSet.copyOf(b), ImmutableSet.copyOf(a)));
	}

}
