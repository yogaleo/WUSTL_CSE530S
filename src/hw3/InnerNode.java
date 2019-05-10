package hw3;

import java.util.ArrayList;

import hw1.Field;

public class InnerNode implements Node {
	
	private int degree;
	private int node;
	
	private boolean isRoot;
	
	private ArrayList<Field> keys; // inner nodes
	private ArrayList<Node> children; // children nodes
	
	public InnerNode(int degree) {
		//your code here
		this.degree = degree;
		this.node = degree - 1;
		this.isRoot = false;
		this.keys = new ArrayList<Field>();
		this.children = new ArrayList<Node>();
	}
	
	public ArrayList<Field> getKeys() {
		//your code here
		return this.keys;
	}
	
	public ArrayList<Node> getChildren() {
		//your code here
		return children;
	}

	public int getDegree() {
		//your code here
		return this.degree;
	}
	
	public int getNode() {
		return node;
	}
	
	public boolean isLeafNode() {
		return false;
	}
	
	public boolean canBorrow() {
		
		int limit = this.node % 2 == 0? (this.node / 2) : (this.node / 2 + 1);
		
		if(this.keys.size() >= limit + 1) {
			return true;
		}
		return false;
	}
	
	public void clearKeys() {
		keys.clear();
	}
	
	public void clearChildren() {
		children.clear();
	}
	
	public void addChildrenNode(int index, Node n) {
		if(index >= children.size()) {
			children.add(n);
		}else {
			children.add(index, n);
		}
		
	}
	
	public void addChildrenNode(Node n) {
		children.add(n);
	}
	
	public Node getNode(int index) {
		if(index < 0 || index >= this.children.size()) {
			return null;
		}
		
		return this.children.get(index);
	}
	
	public void setChildrenNode(int index, Node n) {
		if(index >= children.size()) {
			children.add(n);
		}else {
			children.remove(index);
			children.add(index, n);
		}
		
	}
	
	public void removeNode(int index) {
		children.remove(index);
	}
	
	public Field getField(int index) {
		if(index >= 0 && index < keys.size()) {
			return keys.get(index);
		}
		return null;
	}
	
	public void setKeyField(int index, Field f) {
		keys.add(index,  f);
	}
	
	public void changeKeyField(int index, Field f) {
		if(index >= keys.size()) {
			keys.add(f);
		}else {
			keys.remove(index);
			keys.add(index, f);
		}
	}
	
	public void removeKeyField(int index) {
		keys.remove(index);
	}
	
	public void addNewKey(Field f) {
		keys.add(f);
	}
	
	public void addNewKey(int index, Field f) {
		keys.add(index, f);
	}
	
	public void removeLastKey() {
		int last = this.keys.size() - 1;
		this.keys.remove(last);
	}
	
	public int includeKey(Field f) {
		for(int i = 0; i < this.keys.size(); i++) {
			if(f.equals(this.keys.get(i))) {
				return i;
			}
		}
		
		return -1;
	}

	@Override
	public boolean isAddable() {
		// TODO Auto-generated method stub
		if(keys.size() < node) {
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
		int last = keys.size() - 1;
		return keys.get(last);
	}

	@Override
	public Field getFirstValue() {
		// TODO Auto-generated method stub
		return keys.get(0);
	}

}