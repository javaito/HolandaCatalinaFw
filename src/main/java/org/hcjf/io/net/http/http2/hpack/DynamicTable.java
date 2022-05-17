package org.hcjf.io.net.http.http2.hpack;

import org.hcjf.io.net.http.HttpHeader;

import java.util.ArrayList;
import java.util.List;

public class DynamicTable {

    private static final int HEADER_ENTRY_OVERHEAD = 32;

    // a circular queue of header fields
    private List<HttpHeader> headers;
    private int head;
    private int tail;
    private int size;
    private int capacity = -1; // ensure setCapacity creates the array

    /**
     * Creates a new dynamic table with the specified initial capacity.
     */
    DynamicTable(int initialCapacity) {
        setCapacity(initialCapacity);
    }

    /**
     * Return the number of header fields in the dynamic table.
     */
    public int length() {
        int length;
        if (head < tail) {
            length = headers.size() - tail + head;
        } else {
            length = head - tail;
        }
        return length;
    }

    /**
     * Return the current size of the dynamic table.
     * This is the sum of the size of the entries.
     */
    public int size() {
        return size;
    }

    /**
     * Return the maximum allowable size of the dynamic table.
     */
    public int capacity() {
        return capacity;
    }

    /**
     * Return the header field at the given index.
     * The first and newest entry is always at index 1,
     * and the oldest entry is at the index length().
     */
    public HttpHeader getEntry(int index) {
        if (index <= 0 || index > length()) {
            throw new IndexOutOfBoundsException();
        }
        int i = head - index;
        if (i < 0) {
            return headers.get(i + headers.size());
        } else {
            return headers.get(i);
        }
    }

    /**
     *
     * @param header
     * @return
     */
    private Integer calculateHeaderSize(HttpHeader header) {
        return header.getHeaderName().getBytes().length + header.getHeaderValue().getBytes().length + HEADER_ENTRY_OVERHEAD;
    }

    /**
     * Add the header field to the dynamic table.
     * Entries are evicted from the dynamic table until the size of the table
     * and the new header field is less than or equal to the table's capacity.
     * If the size of the new entry is larger than the table's capacity,
     * the dynamic table will be cleared.
     */
    public void add(HttpHeader header) {
        int headerSize = calculateHeaderSize(header);
        if (headerSize > capacity) {
            clear();
            return;
        }
        while (size + headerSize > capacity) {
            remove();
        }
        headers.set(head++, header);
        size += calculateHeaderSize(header);
        if (head == headers.size()) {
            head = 0;
        }
    }

    /**
     * Remove and return the oldest header field from the dynamic table.
     */
    public HttpHeader remove() {
        HttpHeader removed = headers.get(tail);
        if (removed != null) {
            size -= calculateHeaderSize(removed);
            headers.set(tail++, null);
            if (tail == headers.size()) {
                tail = 0;
            }
        }
        return removed;
    }

    /**
     * Remove all entries from the dynamic table.
     */
    public void clear() {
        while (tail != head) {
            headers.set(tail++, null);
            if (tail == headers.size()) {
                tail = 0;
            }
        }
        head = 0;
        tail = 0;
        size = 0;
    }

    /**
     * Set the maximum size of the dynamic table.
     * Entries are evicted from the dynamic table until the size of the table
     * is less than or equal to the maximum size.
     */
    public void setCapacity(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: "+ capacity);
        }

        // initially capacity will be -1 so init won't return here
        if (this.capacity == capacity) {
            return;
        }
        this.capacity = capacity;

        if (capacity == 0) {
            clear();
        } else {
            // initially size will be 0 so remove won't be called
            while (size > capacity) {
                remove();
            }
        }

        int maxEntries = capacity / HEADER_ENTRY_OVERHEAD;
        if (capacity % HEADER_ENTRY_OVERHEAD != 0) {
            maxEntries++;
        }

        // check if capacity change requires us to reallocate the array
        if (headers != null && headers.size() == maxEntries) {
            return;
        }

        List<HttpHeader> tmp = new ArrayList<>();

        // initially length will be 0 so there will be no copy
        int len = length();
        int cursor = tail;
        for (int i = 0; i < len; i++) {
            HttpHeader entry = headers.get(cursor++);
            tmp.set(i, entry);
            if (cursor == headers.size()) {
                cursor = 0;
            }
        }

        this.tail = 0;
        this.head = tail + len;
        this.headers = tmp;
    }
}
