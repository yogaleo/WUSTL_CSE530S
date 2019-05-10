package hw2;

import java.util.ArrayList;

import hw1.Field;
import hw1.RelationalOperator;
import hw1.Tuple;
import hw1.TupleDesc;

/**
 * This class provides methods to perform relational algebra operations. It will be used
 * to implement SQL queries.
 * @author Doug Shook
 *
 */
public class Relation {

	private ArrayList<Tuple> tuples;
	private TupleDesc td;
	
	public Relation(ArrayList<Tuple> l, TupleDesc td) {
		//your code here
		this.td = td;
		
		tuples = new ArrayList<>();
		for(Tuple t: l) {
			tuples.add(t);
		}
	}
	
	/**
	 * This method performs a select operation on a relation
	 * @param field number (refer to TupleDesc) of the field to be compared, left side of comparison
	 * @param op the comparison operator
	 * @param operand a constant to be compared against the given column
	 * @return
	 */
	public Relation select(int field, RelationalOperator op, Field operand) {
		//your code here
		ArrayList<Tuple> newTuples = new ArrayList<>();
		
		for(Tuple t: this.tuples) {
			if(t.getField(field).compare(op, operand)) {
				newTuples.add(t);
			}
		}
		this.tuples = newTuples;
		
		return this;
	}
	
	/**
	 * This method performs a rename operation on a relation
	 * @param fields the field numbers (refer to TupleDesc) of the fields to be renamed
	 * @param names a list of new names. The order of these names is the same as the order of field numbers in the field list
	 * @return
	 */
	public Relation rename(ArrayList<Integer> fields, ArrayList<String> names) {
		//your code here
		// only temporary rename in this relation, not permanant for the table field name
		if(fields.size() != names.size()) {
			return null;
		}
		TupleDesc newTD = this.td.deepCopy();
		
		for(int i = 0; i < fields.size(); i++) {
			newTD.setFieldName(fields.get(i), names.get(i));
		}
		this.td = newTD;
		
		return this;
	}
	
	/**
	 * This method performs a project operation on a relation
	 * @param fields a list of field numbers (refer to TupleDesc) that should be in the result
	 * @return
	 */
	public Relation project(ArrayList<Integer> fields) {
		//your code here
		this.td.projectTupleDesc(fields);
		
		for(Tuple t: this.tuples) {
			t.projectTuple(fields);
		}
		return this;
	}
	
	/**
	 * This method performs a join between this relation and a second relation.
	 * The resulting relation will contain all of the columns from both of the given relations,
	 * joined using the equality operator (=)
	 * @param other the relation to be joined
	 * @param field1 the field number (refer to TupleDesc) from this relation to be used in the join condition
	 * @param field2 the field number (refer to TupleDesc) from other to be used in the join condition
	 * @return
	 */
	public Relation join(Relation other, int field1, int field2) {
		//your code here
		ArrayList<Tuple> newTuples = new ArrayList<>();
		TupleDesc newTd = this.td.joinTupleDesc(other.td, field1, field2);
		
		for(Tuple t: this.tuples) {
			for(Tuple ot: other.tuples) {
				if(t.getField(field1).equals(ot.getField(field2))) {
					Tuple newTuple = new Tuple(newTd);
					newTuple.joinTuple(t, ot, field1, field2);
					newTuples.add(newTuple);
				}
			}
		}
		
		Relation res = new Relation(newTuples, newTd);
		return res;
	}
	
	/**
	 * Performs an aggregation operation on a relation. See the lab write up for details.
	 * @param op the aggregation operation to be performed
	 * @param groupBy whether or not a grouping should be performed
	 * @return
	 */
	public Relation aggregate(AggregateOperator op, boolean groupBy) {
		//your code here
		Aggregator aggre = new Aggregator(op, groupBy, this.td);
		
		for(Tuple t: this.tuples) {
			aggre.merge(t);
		}
		
		ArrayList<Tuple> newTuples = aggre.getResults();
		Relation res = new Relation(newTuples, newTuples.get(0).getDesc());
		return res;
	}
	
	public TupleDesc getDesc() {
		//your code here
		return this.td;
	}
	
	public ArrayList<Tuple> getTuples() {
		//your code here
		return this.tuples;
	}
	
	/**
	 * Returns a string representation of this relation. The string representation should
	 * first contain the TupleDesc, followed by each of the tuples in this relation
	 */
	public String toString() {
		//your code here
		StringBuilder sb = new StringBuilder();
		sb.append(this.td.toString());
		for(Tuple t: this.tuples) {
			sb.append(t.toString());
		}
		return sb.toString();
	}
}
