package hw2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hw1.IntField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

/**
 * A class to perform various aggregations, by accepting one tuple at a time
 * 
 * @author Doug Shook
 *
 */
public class Aggregator {

	private AggregateOperator operation;
	private boolean groupBy;
	private TupleDesc td;

	private ArrayList<Tuple> tuples;

	public Aggregator(AggregateOperator o, boolean groupBy, TupleDesc td) {
		// your code here
		this.operation = o;
		this.groupBy = groupBy;
		this.td = td;
		tuples = new ArrayList<Tuple>();
	}

	/**
	 * Merges the given tuple into the current aggregation
	 * 
	 * @param t the tuple to be aggregated
	 */
	public void merge(Tuple t) {
		// your code here
		switch (operation) {
		case MAX:
			if (this.groupBy) {
				tuples.add(t);
			} else {
				mergeMax(t);
			}
			break;
		case MIN:
			if (this.groupBy) {
				tuples.add(t);
			} else {
				mergeMin(t);
			}
			break;
		case AVG:
			// lazy execution
			tuples.add(t);
			break;
		case COUNT:
			// lazy execution
			tuples.add(t);
			break;
		case SUM:
			// lazy execution
			tuples.add(t);
			break;
		default:
			throw new UnsupportedOperationException("Aggregate Functions only");
		}
	}

	/**
	 * Returns the result of the aggregation
	 * 
	 * @return a list containing the tuples after aggregation
	 */
	public ArrayList<Tuple> getResults() {
		// your code here
		switch (operation) {
		case MAX:
			if (groupBy) {
				return executeMax();
			}
			break;
		case MIN:
			if (groupBy) {
				return executeMin();
			}
			break;
		case AVG:
			// time to execution
			if (groupBy) {
				return executeAVGWithGroupBy();
			} else {
				Tuple avg = executeAVG();
				tuples.clear();
				tuples.add(avg);
			}
			break;
		case COUNT:
			// time to execution
			if (groupBy) {
				return executeCountWithGroupBy();
			} else {
				Tuple count = executeCOUNT();
				tuples.clear();
				tuples.add(count);
			}
			break;
		case SUM:
			// time to execution
			if (groupBy) {
				return executeSUMWithGroupBy();
			} else {
				Tuple sum = executeSUM();
				tuples.clear();
				tuples.add(sum);
			}
			break;
		default:
			throw new UnsupportedOperationException("Aggregate Functions only");
		}

		return tuples;
	}

	private void mergeMax(Tuple t) {
		// assume not groupby
		if (this.tuples.isEmpty()) {
			this.tuples.add(t);
		} else {
			if (t.getDesc().getType(0) == Type.INT) {
				if (t.getField(0).hashCode() > tuples.get(0).hashCode()) {
					tuples.remove(0);
					tuples.add(t);
				}
			} else if (t.getDesc().getType(0) == Type.STRING) {
				if (stringCompare(t.getField(0).toString(), tuples.get(0).getField(0).toString()) == 1) {
					tuples.remove(0);
					tuples.add(t);
				}
			}
		}
	}

	private void mergeMin(Tuple t) {
		// assume not group by
		if (this.tuples.isEmpty()) {
			this.tuples.add(t);
		} else {
			if (t.getDesc().getType(0) == Type.INT) {
				if (t.getField(0).hashCode() < tuples.get(0).getField(0).hashCode()) {
					tuples.clear();
					tuples.add(t);
				}
			} else if (t.getDesc().getType(0) == Type.STRING) {
				if (stringCompare(t.getField(0).toString(), tuples.get(0).getField(0).toString()) == -1) {
					tuples.clear();
					tuples.add(t);
				}
			}
		}
	}

	private ArrayList<Tuple> executeMax() {
		// assume group by
		ArrayList<Tuple> list = new ArrayList<>();

		Type[] types = new Type[] { this.td.getType(0), this.td.getType(1) };
		String[] fields = new String[] { this.td.getFieldName(0), "MAX" };
		TupleDesc newTd = new TupleDesc(types, fields);

		if (this.td.getType(0) == Type.INT) {
			Map<Integer, Integer> nameSpace = new HashMap<>(); // first int: name, second int: index
			for (Tuple t : tuples) {
				if (nameSpace.containsKey(t.getField(0).hashCode())) {
					Tuple temp = list.get(nameSpace.get(t.getField(0).hashCode()));
					if (this.td.getType(1) == Type.INT) {
						if (t.getField(1).hashCode() > temp.getField(1).hashCode()) {
							temp.setField(1, t.getField(1));
						}
					} else if (this.td.getType(1) == Type.STRING) {
						if (stringCompare(t.getField(1).toString(), temp.getField(1).toString()) == 1) {
							temp.setField(1, t.getField(1));
						}
					}
				} else {
					Tuple newTuple = new Tuple(newTd);
					newTuple.setField(0, t.getField(0));
					newTuple.setField(1, t.getField(1));
					list.add(newTuple);
					nameSpace.put(newTuple.getField(0).hashCode(), list.size() - 1);
				}
			}

		} else if (this.td.getType(0) == Type.STRING) {
			Map<String, Integer> nameSpace = new HashMap<>(); // first string: name, second int: index
			for (Tuple t : tuples) {
				if (nameSpace.containsKey(t.getField(0).toString())) {
					Tuple temp = list.get(nameSpace.get(t.getField(0).hashCode()));
					if (this.td.getType(1) == Type.INT) {
						if (t.getField(1).hashCode() > temp.getField(1).hashCode()) {
							temp.setField(1, t.getField(1));
						}
					} else if (this.td.getType(1) == Type.STRING) {
						if (stringCompare(t.getField(1).toString(), temp.getField(1).toString()) == 1) {
							temp.setField(1, t.getField(1));
						}
					}
				} else {
					Tuple newTuple = new Tuple(newTd);
					newTuple.setField(0, t.getField(0));
					newTuple.setField(1, t.getField(1));
					list.add(newTuple);
					nameSpace.put(newTuple.getField(1).toString(), list.size() - 1);
				}
			}
		}
		return list;
	}

	private ArrayList<Tuple> executeMin() {
		// assume group by
		ArrayList<Tuple> list = new ArrayList<>();

		Type[] types = new Type[] { this.td.getType(0), this.td.getType(1) };
		String[] fields = new String[] { this.td.getFieldName(0), "MIN" };
		TupleDesc newTd = new TupleDesc(types, fields);

		if (this.td.getType(0) == Type.INT) {
			Map<Integer, Integer> nameSpace = new HashMap<>(); // first int: name, second int: index
			for (Tuple t : tuples) {
				if (nameSpace.containsKey(t.getField(0).hashCode())) {
					Tuple temp = list.get(nameSpace.get(t.getField(0).hashCode()));
					if (this.td.getType(1) == Type.INT) {
						if (t.getField(1).hashCode() < temp.getField(1).hashCode()) {
							temp.setField(1, t.getField(1));
						}
					} else if (this.td.getType(1) == Type.STRING) {
						if (stringCompare(t.getField(1).toString(), temp.getField(1).toString()) == -1) {
							temp.setField(1, t.getField(1));
						}
					}
				} else {
					Tuple newTuple = new Tuple(newTd);
					newTuple.setField(0, t.getField(0));
					newTuple.setField(1, t.getField(1));
					list.add(newTuple);
					nameSpace.put(newTuple.getField(0).hashCode(), list.size() - 1);
				}
			}

		} else if (this.td.getType(0) == Type.STRING) {
			Map<String, Integer> nameSpace = new HashMap<>(); // first string: name, second int: index
			for (Tuple t : tuples) {
				if (nameSpace.containsKey(t.getField(0).toString())) {
					Tuple temp = list.get(nameSpace.get(t.getField(0).hashCode()));
					if (this.td.getType(1) == Type.INT) {
						if (t.getField(1).hashCode() < temp.getField(1).hashCode()) {
							temp.setField(1, t.getField(1));
						}
					} else if (this.td.getType(1) == Type.STRING) {
						if (stringCompare(t.getField(1).toString(), temp.getField(1).toString()) == -1) {
							temp.setField(1, t.getField(1));
						}
					}
				} else {
					Tuple newTuple = new Tuple(newTd);
					newTuple.setField(0, t.getField(0));
					newTuple.setField(1, t.getField(1));
					list.add(newTuple);
					nameSpace.put(newTuple.getField(1).toString(), list.size() - 1);
				}
			}
		}
		return list;
	}

	private Tuple executeAVG() {
		// assume not group by
		if (this.td.getType(0) == Type.STRING) {
			return null;
		}
		int sum = 0;
		Tuple res = new Tuple(this.td);

		if (tuples.size() == 0) {
			return res;
		}
		for (Tuple t : tuples) {
			sum += t.getField(0).hashCode();
		}

		int avg = (int) (sum / tuples.size());
		IntField intf = new IntField(avg);
		res.setField(0, intf);
		return res;
	}

	private ArrayList<Tuple> executeAVGWithGroupBy() {
		// assume group by
		ArrayList<Tuple> list = new ArrayList<>();

		Type[] types = new Type[] { this.td.getType(0), Type.INT };
		String[] fields = new String[] { this.td.getFieldName(0), "AVG" };
		TupleDesc newTd = new TupleDesc(types, fields);

		// String cannot operate with AVG
		if (this.td.getType(0) == Type.INT && this.td.getType(1) == Type.INT) {
			Map<Integer, Integer> nameSpace = new HashMap<>(); // first int: name, second int: index
			Map<Integer, Integer> count = new HashMap<>();
			for (Tuple t : tuples) {
				if (nameSpace.containsKey(t.getField(0).hashCode())) {
					int index = nameSpace.get(t.getField(0).hashCode());
					Tuple temp = list.get(index);
					IntField intf = new IntField(t.getField(1).hashCode() + temp.getField(1).hashCode());
					temp.setField(index, intf);

					Integer value = count.get(t.getField(0).hashCode());
					count.put(t.getField(0).hashCode(), value + 1);
				} else {
					Tuple newTuple = new Tuple(newTd);
					newTuple.setField(0, t.getField(0));
					newTuple.setField(1, t.getField(1));
					list.add(newTuple);
					nameSpace.put(newTuple.getField(1).hashCode(), list.size() - 1);

					count.put(t.getField(0).hashCode(), 1);
				}
			}
			for (Tuple t : list) {
				int avg = (int) (t.getField(1).hashCode() / count.get(t.getField(0).hashCode()));
				IntField intf = new IntField(avg);
				t.setField(1, intf);
			}
		} else if (this.td.getType(0) == Type.STRING && this.td.getType(1) == Type.INT) {
			Map<String, Integer> nameSpace = new HashMap<>(); // first string: name, second int: index
			Map<String, Integer> count = new HashMap<>();
			for (Tuple t : tuples) {
				if (nameSpace.containsKey(t.getField(0).toString())) {
					int index = nameSpace.get(t.getField(0).toString());
					Tuple temp = list.get(index);
					IntField intf = new IntField(t.getField(1).hashCode() + temp.getField(1).hashCode());
					temp.setField(index, intf);

					Integer value = count.get(t.getField(0).hashCode());
					count.put(t.getField(0).toString(), value + 1);
				} else {
					Tuple newTuple = new Tuple(newTd);
					newTuple.setField(0, t.getField(0));
					newTuple.setField(1, t.getField(1));
					list.add(newTuple);
					nameSpace.put(newTuple.getField(1).toString(), list.size() - 1);

					count.put(t.getField(0).toString(), 1);
				}
			}
			for (Tuple t : list) {
				int avg = (int) (t.getField(1).hashCode() / count.get(t.getField(0).toString()));
				IntField intf = new IntField(avg);
				t.setField(1, intf);
			}

		}
		return list;
	}

	private Tuple executeSUM() {

		if (groupBy) {

		} else {
			if (this.td.getType(0) == Type.STRING) {
				return null;
			}

			Type[] types = new Type[] { Type.INT };
			String[] fields = new String[] { "Sum" };
			TupleDesc newTd = new TupleDesc(types, fields);

			int sum = 0;
			Tuple res = new Tuple(newTd);

			if (tuples.size() == 0) {
				return res;
			}
			for (Tuple t : tuples) {
				sum += t.getField(0).hashCode();
			}

			IntField intf = new IntField(sum);
			res.setField(0, intf);
			return res;
		}
		return null;
	}

	private ArrayList<Tuple> executeSUMWithGroupBy() {
		// assume group by
		ArrayList<Tuple> list = new ArrayList<>();

		Type[] types = new Type[] { this.td.getType(0), Type.INT };
		String[] fields = new String[] { this.td.getFieldName(0), "SUM" };
		TupleDesc newTd = new TupleDesc(types, fields);

		// String cannot operate with SUM
		if (this.td.getType(0) == Type.INT && this.td.getType(1) == Type.INT) {
			Map<Integer, Integer> nameSpace = new HashMap<>(); // first int: name, second int: index
			for (Tuple t : tuples) {
				if (nameSpace.containsKey(t.getField(0).hashCode())) {
					int index = nameSpace.get(t.getField(0).hashCode());
					Tuple temp = list.get(index);
					IntField intf = new IntField(t.getField(1).hashCode() + temp.getField(1).hashCode());
					temp.setField(1, intf);
				} else {
					Tuple newTuple = new Tuple(newTd);
					newTuple.setField(0, t.getField(0));
					newTuple.setField(1, t.getField(1));
					list.add(newTuple);
					nameSpace.put(newTuple.getField(0).hashCode(), list.size() - 1);
				}
			}
		} else if (this.td.getType(0) == Type.STRING && this.td.getType(1) == Type.INT) {
			Map<String, Integer> nameSpace = new HashMap<>(); // first int: name, second int: index
			for (Tuple t : tuples) {
				if (nameSpace.containsKey(t.getField(0).toString())) {
					int index = nameSpace.get(t.getField(0).toString());
					Tuple temp = list.get(index);
					IntField intf = new IntField(t.getField(1).hashCode() + temp.getField(1).hashCode());
					temp.setField(1, intf);
				} else {
					Tuple newTuple = new Tuple(newTd);
					newTuple.setField(0, t.getField(0));
					newTuple.setField(1, t.getField(1));
					list.add(newTuple);
					nameSpace.put(newTuple.getField(0).toString(), list.size() - 1);
				}
			}
		}
		return list;
	}

	private Tuple executeCOUNT() {

		if (groupBy) {

		} else {
			Type[] types = new Type[] { Type.INT };
			String[] fields = new String[] { "Count" };
			TupleDesc newTd = new TupleDesc(types, fields);

			IntField intf = new IntField(tuples.size());
			Tuple newTuple = new Tuple(newTd);
			newTuple.setField(0, intf);
			return newTuple;
		}
		return null;
	}

	private ArrayList<Tuple> executeCountWithGroupBy() {
		// assume group by
		ArrayList<Tuple> list = new ArrayList<>();

		Type[] types = new Type[] { this.td.getType(0), Type.INT };
		String[] fields = new String[] { this.td.getFieldName(0), "COUNT" };
		TupleDesc newTd = new TupleDesc(types, fields);

		if (this.td.getType(0) == Type.INT) {
			Map<Integer, Integer> nameSpace = new HashMap<>(); // first int: name, second int: index
			for (Tuple t : tuples) {
				if (nameSpace.containsKey(t.getField(0).hashCode())) {
					Tuple temp = list.get(nameSpace.get(t.getField(0).hashCode()));
					IntField intf = new IntField(temp.getField(1).hashCode() + 1);
					temp.setField(1, intf);
				} else {
					Tuple newTuple = new Tuple(newTd);
					IntField intf = new IntField(1);
					newTuple.setField(0, t.getField(0));
					newTuple.setField(1, intf);
					list.add(newTuple);
					nameSpace.put(newTuple.getField(1).hashCode(), list.size() - 1);
				}
			}

		} else if (this.td.getType(0) == Type.STRING) {
			Map<String, Integer> nameSpace = new HashMap<>(); // first string: name, second int: index
			for (Tuple t : tuples) {
				if (nameSpace.containsKey(t.getField(0).toString())) {
					Tuple temp = list.get(nameSpace.get(t.getField(0).toString()));
					IntField intf = new IntField(temp.getField(1).hashCode() + 1);
					temp.setField(1, intf);
				} else {
					Tuple newTuple = new Tuple(newTd);
					IntField intf = new IntField(1);
					newTuple.setField(0, t.getField(0));
					newTuple.setField(1, intf);
					list.add(newTuple);
					nameSpace.put(newTuple.getField(0).toString(), list.size() - 1);
				}
			}
		}
		return list;
	}

	private int stringCompare(String s1, String s2) {
		// response: -1 s1 < s2; -: s1 = s2; 1 s1 > s2
		if (s1.equals(s2))
			return 0;
		char[] array1 = s1.toCharArray();
		char[] array2 = s2.toCharArray();

		int size = Math.min(array1.length, array2.length);

		for (int i = 0; i < size; i++) {
			if (array1[i] > array2[i]) {
				return 1;
			} else if (array1[i] < array2[i]) {
				return -1;
			}
		}

		if (array1.length > size)
			return 1;
		return -1;
	}

}
