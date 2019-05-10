package hw2;

import java.util.ArrayList;
import java.util.List;

import hw1.Catalog;
import hw1.Database;
import hw1.Tuple;
import hw1.TupleDesc;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class Query {

	private String q;

	public Query(String q) {
		this.q = q;
	}

	public Relation execute() {
		Statement statement = null;
		try {
			statement = CCJSqlParserUtil.parse(q);
		} catch (JSQLParserException e) {
			System.out.println("Unable to parse query");
			e.printStackTrace();
		}
		Select selectStatement = (Select) statement;
		PlainSelect sb = (PlainSelect) selectStatement.getSelectBody();

		// your code here
		
		Catalog catalog = Database.getCatalog();
		ColumnVisitor colVisitor = new ColumnVisitor();
		List<String> tableList = getTableList(selectStatement);

		// origin table
		String tableName = tableList.get(0);
		int tableId = catalog.getTableId(tableName);
		ArrayList<Tuple> tupleList = catalog.getDbFile(tableId).getAllTuples();
		TupleDesc originTd = catalog.getTupleDesc(tableId);
		Relation origin = new Relation(tupleList, originTd);

		// consider join
		Relation joined = origin;
		List<Join> joins = sb.getJoins();

		if (joins != null) {
			// continuous join
			for (Join join : joins) {
				// get current relation
				FromItem joinTable = join.getRightItem();
				int joinTableId = catalog.getTableId(joinTable.toString());
				TupleDesc joinTupleDesc = catalog.getTupleDesc(joinTableId);
				ArrayList<Tuple> joinTupleList = catalog.getDbFile(joinTableId).getAllTuples();
				Relation joinRelation = new Relation(joinTupleList, joinTupleDesc);

				String[] exp = join.getOnExpression().toString().split("=");

				String[] fieldWithTable1 = exp[0].trim().split("\\.");
				String[] fieldWithTable2 = exp[1].trim().split("\\.");

				String tableName2 = fieldWithTable2[0];

				String fieldName1 = fieldWithTable1[1];
				String fieldName2 = fieldWithTable2[1];

				if (!joinTable.toString().toLowerCase().equals(tableName2.toLowerCase())) {
					// swap
					String temp = fieldName1;
					fieldName1 = fieldName2;
					fieldName2 = temp;
				}

				int fieldId1 = joined.getDesc().nameToId(fieldName1);
				int fieldId2 = joinRelation.getDesc().nameToId(fieldName2);
				joined = joined.join(joinRelation, fieldId1, fieldId2);
			}
		}

		// consider where
		Relation whered = joined;
		WhereExpressionVisitor whereVisitor = new WhereExpressionVisitor();
		if (sb.getWhere() != null) {
			sb.getWhere().accept(whereVisitor);
			whered = joined.select(joined.getDesc().nameToId(whereVisitor.getLeft()), whereVisitor.getOp(),
					whereVisitor.getRight());
		}

		// consider select
		Relation selected = whered;
		List<SelectItem> selectList = sb.getSelectItems();

		ArrayList<Integer> projectFields = new ArrayList<Integer>();

		for (SelectItem item : selectList) {
			item.accept(colVisitor);
			String selectCol;
			if (colVisitor.isAggregate()) {
				selectCol = colVisitor.getColumn();
			} else {
				selectCol = item.toString();
			}

			int colField = 0;
			if (selectCol.equals("*")) {
				if (colVisitor.isAggregate()) {
					colField = 0;
				} else {
					projectFields = new ArrayList<Integer>();
					for (int i = 0; i < whered.getDesc().numFields(); i++) {
						projectFields.add(i);
					}
					break;
				}
			} else {
				colField = whered.getDesc().nameToId(selectCol);
			}

			if (!projectFields.contains(colField)) {
				projectFields.add(colField);
			}
		}
		selected = whered.project(projectFields);

		// consider aggregate
		Relation aggreated = selected;
		List<Expression> groupByExpressions = sb.getGroupByColumnReferences();
		boolean groupBy = true;

		if (groupByExpressions == null) {
			groupBy = false;
		}

		if (colVisitor.isAggregate()) {
			aggreated = selected.aggregate(colVisitor.getOp(), groupBy);
		}

		return aggreated;
	}

	public List<String> getTableList(Select statement) {
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(statement);
		return tableList;
	}
}
