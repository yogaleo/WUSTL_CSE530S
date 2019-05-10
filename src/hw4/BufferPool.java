package hw4;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import hw1.Catalog;
import hw1.Database;
import hw1.HeapFile;
import hw1.HeapPage;
import hw1.Tuple;

/**
 * BufferPool manages the reading and writing of pages into memory from disk.
 * Access methods call into it to retrieve pages, and it fetches pages from the
 * appropriate location.
 * <p>
 * The BufferPool is also responsible for locking; when a transaction fetches a
 * page, BufferPool which check that the transaction has the appropriate locks
 * to read/write the page.
 */
public class BufferPool {

	private int maxNumPages;
	private int size;

	private Entry head;
	private Entry tail;

	private Map<Pair, Entry> pageMap; // page, entry

	/** Bytes per page, including header. */
	public static final int PAGE_SIZE = 4096;

	/**
	 * Default number of pages passed to the constructor. This is used by other
	 * classes. BufferPool should use the numPages argument to the constructor
	 * instead.
	 */
	public static final int DEFAULT_PAGES = 50;

	/**
	 * Creates a BufferPool that caches up to numPages pages.
	 *
	 * @param numPages maximum number of pages in this buffer pool.
	 */
	public BufferPool(int numPages) {
		// your code here
		this.maxNumPages = numPages;
		this.size = 0;

		this.pageMap = new HashMap<Pair, Entry>();
	}

	/**
	 * Retrieve the specified page with the associated permissions. Will acquire a
	 * lock and may block if that lock is held by another transaction.
	 * <p>
	 * The retrieved page should be looked up in the buffer pool. If it is present,
	 * it should be returned. If it is not present, it should be added to the buffer
	 * pool and returned. If there is insufficient space in the buffer pool, an page
	 * should be evicted and the new page should be added in its place.
	 *
	 * @param tid     the ID of the transaction requesting the page
	 * @param tableId the ID of the table with the requested page
	 * @param pid     the ID of the requested page
	 * @param perm    the requested permissions on the page
	 */
	public HeapPage getPage(int tid, int tableId, int pid, Permissions perm) throws Exception {
		// your code here
		Catalog catalog = Database.getCatalog();
		HeapFile hf = catalog.getDbFile(tableId);
		HeapPage hp = hf.readPage(pid);

		Pair pair = new Pair(tableId, pid);
		Entry entry = pageMap.get(pair);

		if (entry == null) { // add to buffer reader and return
			if (size == this.maxNumPages) {
				evictPage();
			}

			hp.addPermission(tid, perm);
			entry = new Entry(hp);
			insertEntry(entry);
			pageMap.put(pair, entry);

			size++;

		} else { // possible upgrade lock
			entry.page.updatePermission(tid, perm);
		}

		return entry.page;
	}

	/**
	 * Releases the lock on a page. Calling this is very risky, and may result in
	 * wrong behavior. Think hard about who needs to call this and why, and why they
	 * can run the risk of calling it.
	 *
	 * @param tid     the ID of the transaction requesting the unlock
	 * @param tableID the ID of the table containing the page to unlock
	 * @param pid     the ID of the page to unlock
	 */
	public void releasePage(int tid, int tableId, int pid) {
		// your code here
		Entry entry = pageMap.get(new Pair(tableId, pid));
		if (entry != null) {
			entry.page.releaseLocks(tid);
		}

	}

	/** Return true if the specified transaction has a lock on the specified page */
	public boolean holdsLock(int tid, int tableId, int pid) {
		// your code here
		Entry entry = pageMap.get(new Pair(tableId, pid));

		return entry.page.hasLock(tid);
	}

	/**
	 * Commit or abort a given transaction; release all locks associated to the
	 * transaction. If the transaction wishes to commit, write
	 *
	 * @param tid    the ID of the transaction requesting the unlock
	 * @param commit a flag indicating whether we should commit or abort
	 */
	public void transactionComplete(int tid, boolean commit) throws IOException {
		// your code here
		Set<Integer> finished = new HashSet<>();
		Catalog catalog = Database.getCatalog();

		if (commit) {
			Entry cur = head;
			while (cur != null) {
				int fileId = cur.page.getTableId();
				int pid = cur.page.getId();
				if (finished.add(fileId)) {
					HeapFile hf = catalog.getDbFile(fileId);
					hf.writePage(cur.page);
				}
				cur.page.setdirty(false);
				releasePage(tid, fileId, cur.page.getId());
				flushPage(fileId, pid);
				cur = cur.next;
			}

		} else {
			Entry cur = head;
			while (cur != null) {
				if (cur.page.isDirty()) {
					int tableId = cur.page.getTableId();
					int pid = cur.page.getId();
					Pair pair = new Pair(tableId, pid);

					Entry lateEntry = pageMap.get(pair);
					removeEntry(lateEntry);
					pageMap.remove(pair);
				}

				cur = cur.next;
			}
		}
	}

	/**
	 * Add a tuple to the specified table behalf of transaction tid. Will acquire a
	 * write lock on the page the tuple is added to. May block if the lock cannot be
	 * acquired.
	 * 
	 * Marks any pages that were dirtied by the operation as dirty
	 *
	 * @param tid     the transaction adding the tuple
	 * @param tableId the table to add the tuple to
	 * @param t       the tuple to add
	 */
	public void insertTuple(int tid, int tableId, Tuple t) throws Exception {
		// your code here
		Catalog catalog = Database.getCatalog();
		HeapFile hf = catalog.getDbFile(tableId);

		HeapPage hp = getPage(tid, tableId, hf.findAvailablePage(t).getId(), Permissions.READ_WRITE);
		hp.addTuple(t);
		hp.setdirty(true);
	}

	/**
	 * Remove the specified tuple from the buffer pool. Will acquire a write lock on
	 * the page the tuple is removed from. May block if the lock cannot be acquired.
	 *
	 * Marks any pages that were dirtied by the operation as dirty.
	 *
	 * @param tid     the transaction adding the tuple.
	 * @param tableId the ID of the table that contains the tuple to be deleted
	 * @param t       the tuple to add
	 */
	public void deleteTuple(int tid, int tableId, Tuple t) throws Exception {
		// your code here
		Catalog catalog = Database.getCatalog();
		HeapFile hf = catalog.getDbFile(tableId);

		HeapPage hp = getPage(tid, tableId, hf.findDeletePage(t).getId(), Permissions.READ_WRITE);
		hp.deleteTuple(t);
		hp.setdirty(true);
	}

	/**
	 * Discards a page from the buffer pool. Flushes the page to disk to ensure
	 * dirty pages are updated on disk.
	 */
	private synchronized void flushPage(int tableId, int pid) throws IOException {
		// your code here
		Catalog catalog = Database.getCatalog();
		HeapFile hf = catalog.getDbFile(tableId);

		Pair pair = new Pair(tableId, pid);
		Entry entry = pageMap.get(pair);

		hf.writePage(entry.page);

		removeEntry(entry);
		pageMap.remove(pair);
	}

	private synchronized void evictPage() throws Exception {
		// your code here
		Entry cur = head;
		while (cur != null) {
			if (!cur.page.isDirty()) {
				size--;
				Pair pair = new Pair(cur.page.getTableId(), cur.page.getId());
				pageMap.remove(pair);
				removeEntry(cur);
				return;
			}
			cur = cur.next;
		}
		throw new Exception();
	}

	private void insertEntry(Entry entry) {
		if (head == null) {
			head = entry;
			tail = entry;
		} else {
			tail.next = entry;
			entry.prev = tail;
			tail = tail.next;
		}
	}

	private void removeEntry(Entry entry) {
		if (entry == head) {
			if (head != null) {
				head = head.next;
			}
		} else if (entry == tail) {
			if (tail != null) {
				tail = tail.prev;
			}
		} else {
			if (entry.prev != null)
				entry.prev.next = entry.next;
			if (entry.next != null)
				entry.next.prev = entry.prev;
		}
	}

	class Pair {
		private int tableId;
		private int pageId;

		public Pair(int table, int page) {
			this.tableId = table;
			this.pageId = page;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Pair))
				return false;
			Pair p = (Pair) o;
			if ((p.tableId == this.tableId) && (p.pageId == this.pageId))
				return true;
			return false;
		}

		@Override
		public int hashCode() {
			int result = this.tableId;
			result = 31 * result + this.pageId;
			return result;
		}
	}

	class Entry {
		private Entry prev;
		private Entry next;
		private HeapPage page;

		public Entry(HeapPage page) {
			this.page = page;
		}

		public void setPage(HeapPage hp) {
			this.page = hp;
		}

	}

}
