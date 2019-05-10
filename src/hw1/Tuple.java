package hw1;

import java.util.ArrayList;

/**
 * This class represents a tuple that will contain a single row's worth of
 * information from a table. It also includes information about where it is
 * stored
 * 
 * @author Sam Madden modified by Doug Shook
 *
 */
public class Tuple {
	private TupleDesc tupleDesc;
	private int pid; // page id
	private int id;  // index of the slots
	private Field[] data;

	/**
	 * Creates a new tuple with the given description
	 * 
	 * @param t the schema for this tuple
	 */
	public Tuple(TupleDesc t) {
		// your code here
		// pid = -1; // unsigned
		// id = -1; // unsigned

		tupleDesc = t;
		data = new Field[tupleDesc.numFields()];
	}

	public TupleDesc getDesc() {
		// your code here
		return tupleDesc;
	}

	/**
	 * retrieves the page id where this tuple is stored
	 * 
	 * @return the page id of this tuple
	 */
	public int getPid() {
		// your code here
		return pid;
	}

	public void setPid(int pid) {
		// your code here
		this.pid = pid;
	}

	/**
	 * retrieves the tuple (slot) id of this tuple
	 * 
	 * @return the slot where this tuple is stored
	 */
	public int getId() {
		// your code here
		return id;
	}

	public void setId(int id) {
		// your code here
		this.id = id;
	}

	public void setDesc(TupleDesc td) {
		// your code here;
		tupleDesc = td;
	}

	/**
	 * Stores the given data at the i-th field
	 * 
	 * @param i the field number to store the data
	 * @param v the data
	 */
	public void setField(int i, Field v) {
		// your code here
		data[i] = v;
	}

	public Field getField(int i) {
		// your code here
		return data[i];
	}

	/**
	 * Creates a string representation of this tuple that displays its contents. You
	 * should convert the binary data into a readable format (i.e. display the ints
	 * in base-10 and convert the String columns to readable text).
	 */
	public String toString() {
		// your code here
		StringBuilder result = new StringBuilder();
		for(Field f: data) {
			result.append(f.toString());
			result.append(' ');
		}
		result.deleteCharAt(result.length() - 1);
		
		return result.toString();
	}
	
	public void projectTuple(ArrayList<Integer> fields) {
		Field[] newField = new Field[fields.size()];
		
		for(int i = 0; i < fields.size(); i++) {
			newField[i] = this.data[fields.get(i)];
		}
		
		this.data = newField;
	}
	
	public void joinTuple(Tuple origin, Tuple other, int field1, int field2) {
		
		for(int i = 0; i < origin.getDesc().numFields(); i++) {
			this.setField(i, origin.getField(i));
		}
		
		int index = origin.getDesc().numFields();
		for(int i = 0; i < other.getDesc().numFields(); i++) {
			this.setField(index + i, other.getField(i));
		}
	}
}
