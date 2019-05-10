package hw1;

import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc {

	private Type[] types; // data type
	private String[] fields; // column name

	/**
	 * Create a new TupleDesc with typeAr.length fields with fields of the specified
	 * types, with associated named fields.
	 *
	 * @param typeAr  array specifying the number of and types of fields in this
	 *                TupleDesc. It must contain at least one entry.
	 * @param fieldAr array specifying the names of the fields. Note that names may
	 *                be null.
	 */
	public TupleDesc(Type[] typeAr, String[] fieldAr) {
		// your code here
		if(typeAr.length != fieldAr.length) {
			return;
		}
		
		types = new Type[typeAr.length];
		fields = new String[fieldAr.length];

		// deep copy
		System.arraycopy(typeAr, 0, types, 0, typeAr.length);
		System.arraycopy(fieldAr, 0, fields, 0, fieldAr.length);
	}

	/**
	 * @return the number of fields in this TupleDesc
	 */
	public int numFields() {
		// your code here
		return fields.length;
	}

	/**
	 * Gets the (possibly null) field name of the ith field of this TupleDesc.
	 *
	 * @param i index of the field name to return. It must be a valid index.
	 * @return the name of the ith field
	 * @throws NoSuchElementException if i is not a valid field reference.
	 */
	public String getFieldName(int i) throws NoSuchElementException {
		// your code here
		if (i < 0 || i >= fields.length)
			throw new NoSuchElementException();
		return fields[i];
	}
	
	public void setFieldName(int i, String input) throws NoSuchElementException {
		// your code here
		if (i < 0 || i >= fields.length)
			throw new NoSuchElementException();
		fields[i] = input;
	}


	/**
	 * Find the index of the field with a given name.
	 *
	 * @param name name of the field.
	 * @return the index of the field that is first to have the given name.
	 * @throws NoSuchElementException if no field with a matching name is found.
	 */
	public int nameToId(String name) throws NoSuchElementException {
		// your code here
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].contentEquals(name)) {
				return i;
			}
		}
		throw new NoSuchElementException();
	}

	/**
	 * Gets the type of the ith field of this TupleDesc.
	 *
	 * @param i The index of the field to get the type of. It must be a valid index.
	 * @return the type of the ith field
	 * @throws NoSuchElementException if i is not a valid field reference.
	 */
	public Type getType(int i) throws NoSuchElementException {
		// your code here
		if (i < 0 || i >= types.length)
			throw new NoSuchElementException();
		return types[i];
	}

	/**
	 * @return The size (in bytes) of tuples corresponding to this TupleDesc. Note
	 *         that tuples from a given TupleDesc are of a fixed size.
	 */
	public int getSize() {
		// your code here
		int size = 0;

		for (Type t : types) {
			size += sizeTable(t);
		}

		return size;
	}

	/**
	 * Compares the specified object with this TupleDesc for equality. Two
	 * TupleDescs are considered equal if they are the same size and if the n-th
	 * type in this TupleDesc is equal to the n-th type in td.
	 *
	 * @param o the Object to be compared for equality with this TupleDesc.
	 * @return true if the object is equal to this TupleDesc.
	 */
	public boolean equals(Object o) {
		// your code here

		TupleDesc td = (TupleDesc) (o);
		if (Arrays.equals(types, td.types) && Arrays.equals(fields, td.fields)) {
			return true;
		}

		return false;
		
	}

	public int hashCode() {
		// If you want to use TupleDesc as keys for HashMap, implement this so
		// that equal objects have equals hashCode() results
		throw new UnsupportedOperationException("unimplemented");
	}

	/**
	 * Returns a String describing this descriptor. It should be of the form
	 * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although the
	 * exact format does not matter.
	 * 
	 * @return String describing this descriptor.
	 */
	public String toString() {
		// your code here
		StringBuilder result = new StringBuilder();
		
		for (int i = 0; i < types.length; i++) {
			if(types[i] == Type.INT) {
				result.append("int");
			}else {
				result.append("String");
			}
			result.append('(');
			result.append(fields[i]);
			result.append(')');
			result.append(',');
		}
		result.deleteCharAt(result.length() - 1); // remove last comma

		return result.toString();
	}
	
	public TupleDesc deepCopy() {
		if(this.types == null || this.fields == null) {
			return null;
		}
		
		TupleDesc newTD = new TupleDesc(this.types, this.fields);
		return newTD;
	}
	
	public void projectTupleDesc(ArrayList<Integer> fields) {
		Type[] t = new Type[fields.size()];
		String[] s = new String[fields.size()];
		
		for(int i = 0; i < fields.size(); i++) {
			t[i] = this.types[fields.get(i)];
			s[i] = this.fields[fields.get(i)];
		}
		
		this.types = t;
		this.fields = s;
	}
	
	public TupleDesc joinTupleDesc(TupleDesc other, int field1, int field2) {
		int size = this.numFields() + other.numFields();
		Type[] t = new Type[size];
		String[] s = new String[size];
		
		for(int i = 0; i < this.numFields(); i++) {
			t[i] = this.types[i];
			s[i] = this.fields[i];
		}
	
		for(int i = 0; i < other.numFields(); i++) {
			t[i + this.numFields()] = other.getType(i);
			s[i + this.numFields()] = other.getFieldName(i);
		}
		
		TupleDesc newTd = new TupleDesc(t, s);
		return newTd;
 	} 

	private int sizeTable(Type type) {
		int size = 0;

		switch (type) {
		case INT:
			size = 4;
			break;
		case STRING:
			size = 129;
			break;
		}
		return size;
	}
}
