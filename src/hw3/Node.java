package hw3;

import hw1.Field;

public interface Node {
	
	public int getNode();
	public int getDegree();
	public boolean isLeafNode();
	public boolean isAddable();
	
	public boolean isRoot();
	
	public void setAsRoot();
	public void unsetRoot();
	
	public Field getFirstValue();
	public Field getLastValue();
}
