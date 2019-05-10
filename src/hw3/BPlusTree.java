package hw3;

import java.util.ArrayList;
import java.util.List;

import hw1.Field;

public class BPlusTree {

	private int innerDegree;
	private int leafDegree;
	private Node root;

	public BPlusTree(int pInner, int pLeaf) {
		// your code here

		this.innerDegree = pInner;
		this.leafDegree = pLeaf;

		this.root = new LeafNode(this.leafDegree);
		this.root.setAsRoot();
	}

	public LeafNode search(Field f) {
		// your code here
		if (root.isLeafNode()) { // the value must be in this node
			LeafNode leafRoot = (LeafNode) root;
			if (leafRoot.includeField(f)) {
				return leafRoot;
			} else {
				return null;
			}
		} else {
			InnerNode innerRoot = (InnerNode) root;
			return searchFromInner(innerRoot, f);
		}
	}

	public void insert(Entry e) {
		// your code here

		if (root.isLeafNode()) { // no inner nodes
			LeafNode leafRoot = (LeafNode) root;
			if (leafRoot.isAddable()) { // have space to add node
				leafRoot.addNewEntry(e);
			} else { // no room, need split
				LeafNode newLeaf = new LeafNode(this.leafDegree);
				splitLeafNodes(leafRoot, newLeaf, e);

				InnerNode newInner = new InnerNode(this.innerDegree);
				newInner.setKeyField(0, leafRoot.getLastEntry().getField());
				newInner.addChildrenNode(0, leafRoot);
				newInner.addChildrenNode(1, newLeaf);

				leafRoot.unsetRoot();
				newInner.setAsRoot();
				root = newInner;
			}
		} else { // root is a inner node
			InnerNode innerRoot = (InnerNode) root;
			// start insert
			insertFromInner(innerRoot, e);
		}

	}

	public void delete(Entry e) {
		// your code here
		if (root.isLeafNode()) { // the value must be in this node
			LeafNode leafRoot = (LeafNode) root;
			if (leafRoot.includeField(e.getField())) { // root is leaf, easy one
				leafRoot.removeKey(e.getField());
			}
			// else do nothing
		} else { // root is inner node
			// first we need to find key then make remove
			InnerNode innerRoot = (InnerNode) root;
			deleteFromInner(innerRoot, e);
		}
	}

	public Node getRoot() {
		// your code here
		return root;
	}

	private void splitLeafNodes(LeafNode a, LeafNode b, Entry e) {
		int sum = a.getEntries().size() + 1;
		int left = (sum % 2 == 0) ? (sum / 2) : (sum / 2 + 1);

		List<Entry> temp = new ArrayList<>();

		boolean added = false;
		temp.addAll(a.getEntries());
		for (int i = 0; i < temp.size(); i++) {
			if (e.getField().hashCode() < temp.get(i).getField().hashCode()) {
				temp.add(i, e);
				added = true;
				break;
			}
		}

		if (!added) {
			temp.add(e);
		}

		a.clearNodes();

		for (int i = 0; i < temp.size(); i++) {
			if (i < left) {
				a.addNewEntry(temp.get(i));
			} else {
				b.addNewEntry(temp.get(i));
			}
		}

		// add new leaf in linkedlist
		LeafNode next = a.getNext();

		a.setNext(b);
		b.setPrev(a);

		if (next != null) {
			b.setNext(next);
			next.setPrev(b);
		}

	}

	private void splitInnerNodes(InnerNode a, InnerNode b, backTrackEntry bt, int index) {
		int sum = a.getKeys().size() + 1;
		int left = (sum % 2 == 0) ? (sum / 2) : (sum / 2 + 1);

		List<Field> temp = new ArrayList<>();

		for (Field f : a.getKeys()) { // deep copy all nodes
			temp.add(f);
		}

		// add the new node
		temp.add(index, bt.entry.getField());

		// split keys
		a.clearKeys();

		for (int i = 0; i < temp.size(); i++) {
			if (i < left) {
				a.addNewKey(temp.get(i));
			} else {
				b.addNewKey(temp.get(i));
			}
		}

		// split children
		List<Node> tempNodes = new ArrayList<>();
		for (Node n : a.getChildren()) {
			tempNodes.add(n);
		}

		tempNodes.add(index, bt.left);

		tempNodes.remove(index + 1);
		tempNodes.add(index + 1, bt.right);

		a.clearChildren();

		Field lastField = a.getKeys().get(a.getKeys().size() - 1);

		for (Node node : tempNodes) {
			if (node.getLastValue().hashCode() <= lastField.hashCode()) {
				a.addChildrenNode(node);
			} else {
				b.addChildrenNode(node);
			}
		}
	}

	private LeafNode mergeLeafNode(Node a, Node b) {
		LeafNode newLeaf = new LeafNode(a.getDegree());
		LeafNode leafA = (LeafNode) a;
		LeafNode leafB = (LeafNode) b;

		for (Entry l : leafA.getEntries()) {
			newLeaf.addNewEntry(l);
		}

		for (Entry l : leafB.getEntries()) {
			newLeaf.addNewEntry(l);
		}

		newLeaf.setPrev(leafA.getPrev());
		newLeaf.setNext(leafB.getNext());

		return newLeaf;
	}

	private LeafNode searchFromInner(Node root, Field f) {
		if (root.isLeafNode()) {
			LeafNode leafRoot = (LeafNode) root;
			if (leafRoot.includeField(f)) { // can find this entry in node
				return leafRoot;
			} else { // field does not exist
				return null;
			}
		} else {
			int index = -1;
			InnerNode innerRoot = (InnerNode) root;
			List<Field> keys = innerRoot.getKeys();
			for (int i = 0; i < keys.size(); i++) {
				if (f.hashCode() <= keys.get(i).hashCode()) {
					index = i;
					break;
				}
			}
			if (index == -1)
				index = keys.size();
			List<Node> children = innerRoot.getChildren();

			return searchFromInner(children.get(index), f);
		}
	}

	private backTrackEntry insertFromInner(Node root, Entry e) {
		// base case
		if (root.isLeafNode()) {
			LeafNode leafRoot = (LeafNode) root;
			if (leafRoot.isAddable()) { // can add node
				leafRoot.addNewEntry(e);
				return null;
			} else { // cannot add node, split
				// System.out.println("insert:" + e.getField().hashCode());
				LeafNode newLeaf = new LeafNode(this.leafDegree);
				LeafNode nextLeaf = leafRoot.getNext();

				splitLeafNodes(leafRoot, newLeaf, e);

				// build linkedlist relationship
				leafRoot.setNext(newLeaf);
				newLeaf.setPrev(leafRoot);

				if (nextLeaf != null) {
					newLeaf.setNext(nextLeaf);
					nextLeaf.setPrev(newLeaf);
				}

				backTrackEntry backEntry = new backTrackEntry(leafRoot.getLastEntry(), leafRoot, newLeaf);

				return backEntry;
			}
		} else { // inner node
			int index = -1;
			InnerNode innerRoot = (InnerNode) root;
			List<Field> keys = innerRoot.getKeys();
			for (int i = 0; i < keys.size(); i++) {
				if (e.getField().hashCode() <= keys.get(i).hashCode()) {
					index = i;
					break;
				}
			}
			if (index == -1) {
				index = keys.size();
			}

			List<Node> children = innerRoot.getChildren();

			// index will always hold which index does tree get inside
			backTrackEntry backEntry = insertFromInner(children.get(index), e);

			if (backEntry != null) { // add one entry in inner node
				if (innerRoot.isRoot()) { // consider this is root
					if (innerRoot.isAddable()) {
						innerRoot.setKeyField(index, backEntry.entry.getField());
						innerRoot.addChildrenNode(index, backEntry.left);
						innerRoot.setChildrenNode(index + 1, backEntry.right);
					} else { // root cannot add, split to 2 inner and create new root
						InnerNode newInner = new InnerNode(this.innerDegree);
						splitInnerNodes(innerRoot, newInner, backEntry, index);

						// create new root
						InnerNode newRoot = new InnerNode(this.innerDegree);
						int last = innerRoot.getKeys().size() - 1;
						Field lastField = innerRoot.getKeys().get(last);

						newRoot.addNewKey(lastField);
						innerRoot.removeLastKey();

						if (Math.abs(innerRoot.getKeys().size() - newRoot.getKeys().size()) >= 2) {
							newInner.addNewKey(0, lastField);
						}

						newRoot.addChildrenNode(innerRoot);
						newRoot.addChildrenNode(newInner);

						innerRoot.unsetRoot();
						newRoot.setAsRoot();
						this.root = newRoot;
					}
				} else { // not root, then consider addable
					if (innerRoot.isAddable()) {
						innerRoot.setKeyField(index, backEntry.entry.getField());
						innerRoot.addChildrenNode(index, backEntry.left);
						innerRoot.setChildrenNode(index + 1, backEntry.right);
					} else { // just split inner and return to upper level
						InnerNode newInner = new InnerNode(this.innerDegree);
						splitInnerNodes(innerRoot, newInner, backEntry, index);
					}
				}
			}
			// else do nothing
		}

		return null;
	}

	private deleteTrackEntry deleteFromInner(Node root, Entry e) {

		// base case
		if (root.isLeafNode()) {
			LeafNode leafRoot = (LeafNode) root;
			if (leafRoot.includeField(e.getField())) { // can find this entry in node
				leafRoot.removeKey(e.getField());
				if (!leafRoot.maintainable()) {
					// start to borrow
					if (leafRoot.getPrev() != null && leafRoot.getPrev().canBorrow()) {
						LeafNode prev = leafRoot.getPrev();
						Entry lastEntry = prev.getLastEntry();
						leafRoot.addNewEntry(lastEntry);
						prev.removeKey(lastEntry.getField());
						return new deleteTrackEntry(true, false, false); // need check
					} else if (leafRoot.getNext() != null && leafRoot.getNext().canBorrow()) {
						LeafNode next = leafRoot.getNext();
						Entry firstEntry = next.getFirstEntry();
						leafRoot.addNewEntry(firstEntry);
						next.removeKey(firstEntry.getField());
						return new deleteTrackEntry(true, false, false); // need check
					} else {
						// now need push through, consider about merge at inner
						// TODO: add backtrack
						return new deleteTrackEntry(false, true, false); // need merge
					}
				} else {
					// do nothing
				}
			} else {
				// field does not exist, do nothing
			}
		} else {
			int index = -1;
			InnerNode innerRoot = (InnerNode) root;
			List<Field> keys = innerRoot.getKeys();
			for (int i = 0; i < keys.size(); i++) {
				if (e.getField().hashCode() <= keys.get(i).hashCode()) {
					index = i;
					break;
				}
			}
			if (index == -1)
				index = keys.size();

			deleteTrackEntry backTrackEntry = deleteFromInner(innerRoot.getNode(index), e);

			if (innerRoot.isRoot()) {

				if (backTrackEntry != null) {

					if (backTrackEntry.needCheck) {
						for (int i = 0; i < keys.size(); i++) {
							innerRoot.changeKeyField(i, innerRoot.getNode(i).getLastValue());
						}
					}

					if (backTrackEntry.needMerge) {
						if (innerRoot.getNode(index).isLeafNode()) { // merge 2 leaf node
							if (index == 0) {
								// can only merge with 1
								LeafNode newNode = mergeLeafNode(innerRoot.getNode(0), innerRoot.getNode(1));
								innerRoot.removeNode(0);
								innerRoot.setChildrenNode(0, newNode);
								innerRoot.removeKeyField(0);
								innerRoot.changeKeyField(0, newNode.getLastValue());
							} else if (index < innerRoot.getKeys().size()) {
								LeafNode newNode = mergeLeafNode(innerRoot.getNode(index - 1),
										innerRoot.getNode(index));
								innerRoot.removeNode(index);
								innerRoot.setChildrenNode(index - 1, newNode);
								innerRoot.removeKeyField(index);
								innerRoot.changeKeyField(index - 1, newNode.getLastValue());
							} else if (index == innerRoot.getKeys().size()) {
								LeafNode newNode = mergeLeafNode(innerRoot.getNode(index - 1),
										innerRoot.getNode(index));
								innerRoot.removeNode(index);
								innerRoot.setChildrenNode(index - 1, newNode);
								innerRoot.changeKeyField(index - 1, newNode.getLastValue());
							}
						}

						// after merge, if there is only 1 children node, remove tree node and reset the
						// root
						if (innerRoot.getChildren().size() == 1) {
							this.root = innerRoot.getNode(0);
						}
					}

					if (backTrackEntry.needPushThrough) {
						InnerNode curInnerNode = (InnerNode) (innerRoot.getNode(index));
						if (index - 1 >= 0) { // ask for left
							InnerNode prevInnerNode = (InnerNode) innerRoot.getNode(index - 1);
							if (prevInnerNode.canBorrow()) {
								int last = prevInnerNode.getChildren().size() - 1;
								Node helpNode = prevInnerNode.getNode(last);
								Field lastField = prevInnerNode.getLastValue();

								prevInnerNode.removeLastKey();
								prevInnerNode.removeNode(last);

								Field curField = innerRoot.getField(index - 1);
								innerRoot.changeKeyField(index - 1, lastField);

								curInnerNode.removeKeyField(0);
								curInnerNode.addNewKey(curField);
								curInnerNode.addChildrenNode(0, helpNode);
							} else {
								// left cannot borrow, move together with left
								InnerNode newRoot = new InnerNode(innerRoot.getDegree());

								for (Field fields : prevInnerNode.getKeys()) {
									newRoot.addNewKey(fields);
								}

								for (Field fields : innerRoot.getKeys()) {
									newRoot.addNewKey(fields);
								}

								for (Node n : prevInnerNode.getChildren()) {
									newRoot.addChildrenNode(n);
								}

								for (Node n : curInnerNode.getChildren()) {
									newRoot.addChildrenNode(n);
								}

								this.root = newRoot;
							}
						} else { // ask for right

						}
					}
				}
			} else { // just a normal inner node
				if (backTrackEntry != null) {
					if (backTrackEntry.needCheck) {
						// in this part, we can guarantee that we have make a borrow at lower level,
						// we need update keys to maintain the whole tree;
						for (int i = 0; i < keys.size(); i++) {
							innerRoot.changeKeyField(i, innerRoot.getNode(i).getLastValue());
						}
					}

					if (backTrackEntry.needMerge) {
						// if left is ok, merge left, otherwise merge right
						// it's impossible that a leaf without 2 siblings
						// decide merge node type
						if (innerRoot.getNode(index).isLeafNode()) { // merge 2 leaf node
							if (innerRoot.getNode(index).isLeafNode()) { // merge 2 leaf node
								if (index == 0) {
									// can only merge with 1
									LeafNode newNode = mergeLeafNode(innerRoot.getNode(0), innerRoot.getNode(1));
									innerRoot.removeNode(0);
									innerRoot.setChildrenNode(0, newNode);
									innerRoot.removeKeyField(0);
									innerRoot.changeKeyField(0, newNode.getLastValue());
								} else if (index < innerRoot.getKeys().size()) {
									LeafNode newNode = mergeLeafNode(innerRoot.getNode(index - 1),
											innerRoot.getNode(index));
									innerRoot.removeNode(index);
									innerRoot.setChildrenNode(index - 1, newNode);
									innerRoot.removeKeyField(index);
									innerRoot.changeKeyField(index - 1, newNode.getLastValue());
								} else if (index == innerRoot.getKeys().size()) {
									LeafNode newNode = mergeLeafNode(innerRoot.getNode(index - 1),
											innerRoot.getNode(index));
									innerRoot.removeNode(index);
									innerRoot.setChildrenNode(index - 1, newNode);
									innerRoot.changeKeyField(index - 1, newNode.getLastValue());
								}

								// after merge, it is possible to push through
								if (innerRoot.getChildren().size() == 1) {
									return new deleteTrackEntry(false, false, true);
								}
							}
						} else {
							// consider about merge inner (push through)
						}
					}

					if (backTrackEntry.needPushThrough) {

					}

				} else {
					// direct delete success
					// but we'd better update node
				}
			}
		}
		return null;
	}
	
	private class backTrackEntry {
		private Entry entry;
		private Node left;
		private Node right;

		public backTrackEntry(Entry entry, Node left, Node right) {
			this.entry = entry;
			this.left = left;
			this.right = right;
		}
	}

	private class deleteTrackEntry {
		private boolean needCheck;
		private boolean needMerge;
		private boolean needPushThrough;

		public deleteTrackEntry(boolean check, boolean merge, boolean push) {
			this.needCheck = check;
			this.needMerge = merge;
			this.needPushThrough = push;
		}
	}
}
