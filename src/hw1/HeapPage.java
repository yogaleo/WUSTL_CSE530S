package hw1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import hw4.Permissions;

public class HeapPage {

	private int id;
	private byte[] header;
	private Tuple[] tuples;
	private TupleDesc td;
	private int numSlots;
	private int tableId;

	// extra fields for hw 4
	private boolean modified;
	private List<Integer> writePermission;
	private Set<Integer> readPermission;

	public HeapPage(int id, byte[] data, int tableId) throws IOException {
		this.id = id;
		this.tableId = tableId;

		this.td = Database.getCatalog().getTupleDesc(this.tableId);
		this.numSlots = getNumSlots();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));

		// initialization for hw 4
		this.modified = false;
		this.writePermission = new ArrayList<Integer>();
		this.readPermission = new HashSet<Integer>();

		// allocate and read the header slots of this page
		header = new byte[getHeaderSize()];
		for (int i = 0; i < header.length; i++)
			header[i] = dis.readByte();

		try {
			// allocate and read the actual records of this page
			tuples = new Tuple[numSlots];
			for (int i = 0; i < tuples.length; i++)
				tuples[i] = readNextTuple(dis, i);
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		}
		dis.close();
	}

	public int getId() {
		// your code here
		return this.id;
	}
	
	public int getTableId() {
		return this.tableId;
	}

	/**
	 * Computes and returns the total number of slots that are on this page
	 * (occupied or not). Must take the header into account!
	 * 
	 * @return number of slots on this page
	 */
	public int getNumSlots() {
		// your code here
		int num = (HeapFile.PAGE_SIZE * 8) / (td.getSize() * 8 + 1);
		return num;
	}

	/**
	 * Computes the size of the header. Headers must be a whole number of bytes (no
	 * partial bytes)
	 * 
	 * @return size of header in bytes
	 */
	private int getHeaderSize() {
		// your code here
		int slots = this.numSlots;
		return slots % 8 == 0 ? slots / 8 : slots / 8 + 1;
	}

	/**
	 * Checks to see if a slot is occupied or not by checking the header
	 * 
	 * @param s the slot to test
	 * @return true if occupied
	 */
	public boolean slotOccupied(int s) {
		// your code here
		int index = s / 8;
		int step = s % 8;
		return (header[index] >> step & 1) == 1;
	}

	/**
	 * Sets the occupied status of a slot by modifying the header
	 * 
	 * @param s     the slot to modify
	 * @param value its occupied status
	 */
	public void setSlotOccupied(int s, boolean value) {
		// your code here
		int index = s / 8;
		int step = s % 8;
		// byte mask = (byte) (value == true ? 1 : 0);
		header[index] = value ? (byte) (header[index] | (1 << step)) : (byte) (header[index] & ~(1 << step));
	}

	/**
	 * Adds the given tuple in the next available slot. Throws an exception if no
	 * empty slots are available. Also throws an exception if the given tuple does
	 * not have the same structure as the tuples within the page.
	 * 
	 * @param t the tuple to be added.
	 * @throws Exception
	 */
	public void addTuple(Tuple t) throws Exception {
		// your code here
		if (!t.getDesc().equals(this.td))
			throw new Exception();

		for (int i = 0; i < this.numSlots; i++) {
			if (!slotOccupied(i)) {
				setSlotOccupied(i, true);
				t.setPid(this.id);
				t.setId(i);
				this.tuples[i] = t;
				return;
			}
		}
		throw new Exception();

	}

	/**
	 * Removes the given Tuple from the page. If the page id from the tuple does not
	 * match this page, throw an exception. If the tuple slot is already empty,
	 * throw an exception
	 * 
	 * @param t the tuple to be deleted
	 * @throws Exception
	 */
	public void deleteTuple(Tuple t) throws Exception {
		// your code here
		if (t.getPid() != this.id)
			throw new Exception("tuple pid != page id");
		if (!slotOccupied(t.getId()))
			throw new Exception("tuple not in this page");

		int index = t.getId();
		setSlotOccupied(index, false);
		tuples[index] = null;
	}

	/**
	 * Suck up tuples from the source file.
	 */
	private Tuple readNextTuple(DataInputStream dis, int slotId) {
		// if associated bit is not set, read forward to the next tuple, and
		// return null.
		if (!slotOccupied(slotId)) {
			for (int i = 0; i < td.getSize(); i++) {
				try {
					dis.readByte();
				} catch (IOException e) {
					throw new NoSuchElementException("error reading empty tuple");
				}
			}
			return null;
		}

		// read fields in the tuple
		Tuple t = new Tuple(td);
		t.setPid(this.id);
		t.setId(slotId);

		for (int j = 0; j < td.numFields(); j++) {
			if (td.getType(j) == Type.INT) {
				byte[] field = new byte[4];
				try {
					dis.read(field);
					t.setField(j, new IntField(field));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				byte[] field = new byte[129];
				try {
					dis.read(field);
					t.setField(j, new StringField(field));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return t;
	}

	/**
	 * Generates a byte array representing the contents of this page. Used to
	 * serialize this page to disk.
	 *
	 * The invariant here is that it should be possible to pass the byte array
	 * generated by getPageData to the HeapPage constructor and have it produce an
	 * identical HeapPage object.
	 *
	 * @return A byte array correspond to the bytes of this page.
	 */
	public byte[] getPageData() {
		int len = HeapFile.PAGE_SIZE;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
		DataOutputStream dos = new DataOutputStream(baos);

		// create the header of the page
		for (int i = 0; i < header.length; i++) {
			try {
				dos.writeByte(header[i]);
			} catch (IOException e) {
				// this really shouldn't happen
				e.printStackTrace();
			}
		}

		// create the tuples
		for (int i = 0; i < tuples.length; i++) {

			// empty slot
			if (!slotOccupied(i)) {
				for (int j = 0; j < td.getSize(); j++) {
					try {
						dos.writeByte(0);
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
				continue;
			}

			// non-empty slot
			for (int j = 0; j < td.numFields(); j++) {
				Field f = tuples[i].getField(j);
				try {
					dos.write(f.toByteArray());

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// padding
		int zerolen = HeapFile.PAGE_SIZE - (header.length + td.getSize() * tuples.length); // - numSlots * td.getSize();
		byte[] zeroes = new byte[zerolen];
		try {
			dos.write(zeroes, 0, zerolen);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return baos.toByteArray();
	}

	/**
	 * Returns an iterator that can be used to access all tuples on this page.
	 * 
	 * @return
	 */
	public Iterator<Tuple> iterator() {
		// your code here
		List<Tuple> list = new ArrayList<>();
		for (int i = 0; i < tuples.length; i++) {
			if (slotOccupied(i))
				list.add(tuples[i]);
		}

		return list.iterator();
	}

	public boolean isWritable() {

		for (int i = 0; i < this.numSlots; i++) {
			if (!slotOccupied(i)) {
				return true;
			}
		}
		return false;
	}

	public boolean isDirty() {
		return this.modified;
	}

	public void setdirty(boolean value) {
		this.modified = value;
	}

	public void addPermission(int tid, Permissions perm) {
		if (perm == Permissions.READ_ONLY) {
			if (this.writePermission.size() == 0) { // only when no exclusive lock.
				// if the write lock is tid itself, we cannot add read lock because one
				// transaction can onl add 1 time.
				this.readPermission.add(tid);
			}
		} else if (perm == Permissions.READ_WRITE) {
			if (this.writePermission.size() == 0) {
				if (this.readPermission.size() == 0) { // only lock
					this.writePermission.add(tid);
				} else if (this.readPermission.size() == 1 && this.readPermission.contains(tid)) { // lock upgrade
					this.readPermission.remove(tid);
					this.writePermission.add(tid);
				}else if(this.readPermission.size() != 1 && this.readPermission.contains(tid)) {
					// update lock failed
					this.readPermission.remove(tid);
				}
			}
		}

	}

	public void updatePermission(int tid, Permissions perm) {
		if (perm == Permissions.READ_ONLY) {
			if (this.writePermission.size() == 0) {
				this.readPermission.add(tid);
			}
		} else if (perm == Permissions.READ_WRITE) {
			if (this.writePermission.size() == 0 && this.readPermission.size() == 0) {
				this.writePermission.add(tid);
			} else if (this.readPermission.size() == 1 && this.readPermission.contains(tid)) {
				this.readPermission.remove(tid);
				this.writePermission.add(tid);
			}else if(this.readPermission.size() != 1 && this.readPermission.contains(tid)) {
				// lock uograde failed, reove the failed lock
				this.readPermission.remove(tid);
				if(this.writePermission.size() == 1 && this.writePermission.get(0) == tid) this.writePermission.remove(0);
			}

		}
	}

	public boolean allowedTowrite(int tid) {
		if (this.writePermission.size() == 0 || this.writePermission.get(0) == tid) {
			return true;
		}

		return false;
	}

	public boolean hasLock(int tid) {
		if (this.readPermission.contains(tid))
			return true;

		for (int i = 0; i < this.writePermission.size(); i++) {
			if (this.writePermission.get(i) == tid)
				return true;
		}

		return false;
	}

	public boolean hasWriteLock(int tid) {
		if (this.writePermission.size() == 0)
			return true;
		if (this.writePermission.size() == 1 && this.writePermission.get(0) == tid)
			return true;
		return false;
	}

	public void releaseLocks(int tid) {
		this.readPermission.remove(tid);
		if (this.writePermission.size() == 1 && this.writePermission.get(0) == tid) {
			this.writePermission.remove(0);
		}
	}
}
