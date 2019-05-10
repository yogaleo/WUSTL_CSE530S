package hw1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A heap file stores a collection of tuples. It is also responsible for managing pages.
 * It needs to be able to manage page creation as well as correctly manipulating pages
 * when tuples are added or deleted.
 * @author Sam Madden modified by Doug Shook
 *
 */
public class HeapFile {
	
	public static final int PAGE_SIZE = 4096;
	File file;
	TupleDesc tb;
	
	/**
	 * Creates a new heap file in the given location that can accept tuples of the given type
	 * @param f location of the heap file
	 * @param types type of tuples contained in the file
	 */
	public HeapFile(File f, TupleDesc type) {
		//your code here
		this.file = f;
		this.tb = type;
	}
	
	public File getFile() {
		//your code here
		return this.file;
	}
	
	public TupleDesc getTupleDesc() {
		//your code here
		return this.tb;
	}
	
	/**
	 * Creates a HeapPage object representing the page at the given page number.
	 * Because it will be necessary to arbitrarily move around the file, a RandomAccessFile object
	 * should be used here.
	 * @param id the page number to be retrieved
	 * @return a HeapPage at the given page number
	 */
	public HeapPage readPage(int id) {
		//your code here
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(this.file, "r");
			raf.seek(PAGE_SIZE * id);
			byte[] buf = new byte[PAGE_SIZE];
			raf.read(buf);
			HeapPage hp = new HeapPage(id, buf, this.getId());
			raf.close();
			return hp;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Returns a unique id number for this heap file. Consider using
	 * the hash of the File itself.
	 * @return
	 */
	public int getId() {
		//your code here
		return file.hashCode();
	}
	
	/**
	 * Writes the given HeapPage to disk. Because of the need to seek through the file,
	 * a RandomAccessFile object should be used in this method.
	 * @param p the page to write to disk
	 */
	public void writePage(HeapPage p) {
		//your code here
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(this.file, "rw");
			raf.getFilePointer();
			// put pointer to the end of the file
			raf.seek(PAGE_SIZE * p.getId());
			// System.out.println("Current file size：" + raf.length());
			raf.write(p.getPageData());
			// System.out.println("Current length after adding file：" + raf.length());	
			raf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Adds a tuple. This method must first find a page with an open slot, creating a new page
	 * if all others are full. It then passes the tuple to this page to be stored. It then writes
	 * the page to disk (see writePage)
	 * @param t The tuple to be stored
	 * @return The HeapPage that contains the tuple
	 */
	public HeapPage addTuple(Tuple t) {
		//your code here
		
		for(int i = 0; i < this.getNumPages(); i++) {
			HeapPage hp = this.readPage(i);
			if(hp.isWritable()) {
				try {
					// can find a empty slot to store the tuple
					hp.addTuple(t);
					this.writePage(hp);
					return hp;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		// can't find an empty slot in any existing heap page
		try {
			HeapPage hp = new HeapPage(this.getNumPages(), new byte[PAGE_SIZE], this.getId());
			hp.addTuple(t);
			this.writePage(hp);
			return hp;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * This method will examine the tuple to find out where it is stored, then delete it
	 * from the proper HeapPage. It then writes the modified page to disk.
	 * @param t the Tuple to be deleted
	 */
	public void deleteTuple(Tuple t){
		//your code here
		HeapPage hp = this.readPage(t.getPid());
		try {
			hp.deleteTuple(t);
			this.writePage(hp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Returns an ArrayList containing all of the tuples in this HeapFile. It must
	 * access each HeapPage to do this (see iterator() in HeapPage)
	 * @return
	 */
	public ArrayList<Tuple> getAllTuples() {
		//your code here
		ArrayList<Tuple> result = new ArrayList<>();
		
		for(int i = 0; i < this.getNumPages(); i++) {
			HeapPage hp = this.readPage(i);
			Iterator<Tuple> iter = hp.iterator();
			while(iter.hasNext()) {
				result.add((Tuple) iter.next());
			}
//			ArrayList<Tuple> temp = hp.toList();
//			for(Tuple t: temp) {
//				result.add(t);
//			}
		}
		return result;
	}
	
	/**
	 * Computes and returns the total number of pages contained in this HeapFile
	 * @return the number of pages
	 */
	public int getNumPages() {
		//your code here
		RandomAccessFile raf;
		
		try {
			raf = new RandomAccessFile(this.file, "r");
			int size = (int)(raf.length() / PAGE_SIZE);
			raf.close();
			return size;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public HeapPage findAvailablePage(Tuple t) {
		//your code here
		
		for(int i = 0; i < this.getNumPages(); i++) {
			HeapPage hp = this.readPage(i);
			if(hp.isWritable()) {
				try {
					// can find a empty slot to store the tuple
					return hp;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public HeapPage findDeletePage(Tuple t){
		//your code here
		HeapPage hp = this.readPage(t.getPid());
		return hp;
	}
}
