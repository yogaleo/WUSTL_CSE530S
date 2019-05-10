package hw3;

import java.util.ArrayList;

import hw1.Field;

public class LeafNode implements Node {
	
	private int degree;
	private int node;
	
	private boolean isRoot;
	
	private ArrayList<Entry> entries;
	private LeafNode next;
	private LeafNode prev;
	
	public LeafNode(int degree) {
		//your code here
		this.degree = degree;
		this.node = degree;
		this.isRoot = false;
		
		this.entries = new ArrayList<>();
		
		this.prev = null;
		this.next = null;
	}
	
	public ArrayList<Entry> getEntries() {
		//your code here
		return this.entries;
	}

	public int getDegree() {
		//your code here
		return this.degree;
	}
	
	public int getNode() {
		return node;
	}
	
	public boolean isLeafNode() {
		return true;
	}
	
	public LeafNode getNext() {
		return next;
	}
	
	public void setNext(LeafNode next) {
		this.next = next;
	}
	
	public LeafNode getPrev() {
		return this.prev;
	}
	
	public void setPrev(LeafNode prev) {
		this.prev = prev;
	}
	
	public boolean isAddable() {
		if(entries.size() < node) {
			return true;
		}
		return false;
	}
	
	public void addNewEntry(Entry e) {
		if(entries.size() == 0) {
			entries.add(e);
			return;
		}
		
		for(int i = 0; i < entries.size(); i++) {
			if(e.getField().hashCode() < entries.get(i).getField().hashCode()) {
				entries.add(i, e);
				return;
			}
		}
		entries.add(e);
	}
	
	public void clearNodes() {
		entries.clear();
	}
	
	public Entry getFirstEntry() {
		return entries.get(0);
	}
	
	public Entry getLastEntry() {
		int last = entries.size() - 1;
		return entries.get(last);
	}
	
	public boolean includeField(Field f) {
		if(entries.size() == 0) return false;
		
		for(Entry e: entries) {
			if(e.getField().equals(f)) {
				return true;
			}
		}
		return false;
	}
	
	public void removeKey(Field f) {
		for(int i = 0; i < entries.size(); i++) {
			if(entries.get(i).getField().equals(f)) {
				entries.remove(i);
				return;
			}
		}
	}
	
	public boolean maintainable() {
		int lowest = this.node % 2 == 0 ? (this.node / 2) : (this.node / 2 + 1);
		if(this.entries.size() >= lowest) {
			return true;
		}
		return false;
	}
	
	public boolean canBorrow() {
		int lowest = this.node % 2 == 0 ? (this.node / 2) : (this.node / 2 + 1);
		if(this.entries.size() >= lowest + 1) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isRoot() {
		// TODO Auto-generated method stub
		return this.isRoot;
	}

	@Override
	public void setAsRoot() {
		// TODO Auto-generated method stub
		this.isRoot = true;
	}

	@Override
	public void unsetRoot() {
		// TODO Auto-generated method stub
		this.isRoot = false;
	}

	@Override
	public Field getLastValue() {
		// TODO Auto-generated method stub
		int last = entries.size() - 1;
		return entries.get(last).getField();
	}

	@Override
	public Field getFirstValue() {
		// TODO Auto-generated method stub
		return entries.get(0).getField();
	}

}