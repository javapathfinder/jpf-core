/*
* Written by Yifan Ning
* An example showing how jpf model checks thread-safe operations on 
* linked List
*/


import java.util.concurrent.locks.ReentrantLock; 
import java.lang.Thread;

public class ConcurrentList<Item> {
    private Node sentinel;
    private int size;
    private final ReentrantLock lock = new ReentrantLock();

    public ConcurrentList() {
        sentinel = new Node(null, null, null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }

    public void addFirst(Item x) {
        lock.lock();  
        try {
            Node temp = sentinel.next;
            sentinel.next = new Node(sentinel, x, temp);
            temp.prev = sentinel.next;
            size = size + 1;
        } 
        finally {
           lock.unlock();
        }

    }

    public void addLast(Item x) {
        lock.lock();
        try {
            Node temp = new Node(sentinel.prev, x, sentinel);
            sentinel.prev.next = temp;
            sentinel.prev = temp;
            size = size + 1;
        }
        finally {
            lock.unlock ();
        }
    }


    public int size() {
        return size;
    }

    public void printList() {
        lock.lock();
        try {
            Node temp = sentinel.next;
            while (temp != sentinel) {
                System.out.print(String.valueOf(temp.item) + " ");
                temp = temp.next;
            }
            System.out.println(" ");
        }
        finally {
            lock.unlock();
        }
    }

    public Item removeFirst() {
        lock.lock();
        try {
            if (sentinel.next == null) {
                return null;
            }
            Node removed = sentinel.next;
            sentinel.next = sentinel.next.next;
            sentinel.next.prev = sentinel;
            size = size - 1;
            removed.prev = null;
            removed.next = null;
            return removed.item;
        }
        finally {
            lock.unlock();
        }
    }

    public Item removeFirstUnsafe() {
        if (sentinel.next == null) {
            return null;
        }
        Node removed = sentinel.next;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel;
        size = size - 1;
        removed.prev = null;
        removed.next = null;
        return removed.item;
    }

    public Item removeLast() {
        lock.lock ();
        try {
            if (sentinel.prev == null) {
                return null;
            }
            Node removed = sentinel.prev;
            sentinel.prev = sentinel.prev.prev;
            sentinel.prev.next = sentinel;
            size = size - 1;
            removed.prev = null;
            removed.next = null;
            return removed.item;
        }
        finally {
            lock.unlock ();
        }
    }

    public boolean contains(Item x) {
        lock.lock ();
        try {
            Node pointer = sentinel.next;
            while (!pointer.item.equals(x) && pointer.next != sentinel) {
                pointer = pointer.next;
            }
            if (pointer.item.equals(x)) {
                return true;
            }
            return false;
        }
        finally {
            lock.unlock ();
        }
    }

    public Item get(int index) {
        lock.lock ();
        try {
            Node pointer = sentinel.next;
            while (pointer.next != sentinel && index > 0) {
                pointer = pointer.next;
                index = index - 1;
            }
            return pointer.item;
        }
        finally {
            lock.unlock ();
        }
    }

    private class Node {
        private Item item;
        private Node next;
        private Node prev;

        private Node(Node p, Item i, Node n) {
            prev = p;
            item = i;
            next = n;
        }
    }

  public static void main(String[] args) {
    ConcurrentList<Integer> myList = new ConcurrentList<>();
    myList.addFirst(6);
    myList.addFirst(5);
    myList.addLast(7);
    myList.printList();   // 5, 6, 7

    Thread thread1 =  new Thread() {
        
        @Override
        public void run() {
            myList.removeFirstUnsafe();
            //myList.removeFirst();
        }
    };
    

    Thread thread2 = new Thread() {
        
        @Override
        public void run() {
            myList.removeFirstUnsafe();
            //myList.removeFirst();
        }
    };

    thread1.start();
    thread2.start();

    try {
        System.out.println("Waiting for threads to finish.");
        thread1.join();
        thread2.join();
    } 
    catch (InterruptedException e) {
        System.out.println("Main thread Interrupted");
    }


   // assert myList.size() == 1;
    myList.printList();

  }
}
