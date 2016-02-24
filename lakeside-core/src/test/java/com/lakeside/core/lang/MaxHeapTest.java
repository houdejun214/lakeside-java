package com.lakeside.core.lang;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by dejun on 25/2/16.
 */
public class MaxHeapTest {

    @Test
    public void testHeapsize() throws Exception {
        int i;
        Integer[] A = new Integer[20];
        for (i=0; i<20; i++)
            A[i] = i;
        DSutil.shuffle(A);
        StringBuffer out = new StringBuffer(100);
        MaxHeap<Integer> AH = new MaxHeap<>(A, 20, 20);
        for (i=0; i<20; i++)
            out.append(AH.removemax() + " ");
        assertEquals(out.toString(), "19 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3 2 1 0 ");
    }

    @Test
    public void testInsert() throws Exception {
        int i;
        Integer[] A = new Integer[20];
        for (i=0; i<20; i++)
            A[i] = i;
        DSutil.shuffle(A);
        MaxHeap<Integer> AH = new MaxHeap<>(A, 19, 20);
        assertEquals(Integer.valueOf(19), AH.at(0));
        AH.insert(21);
        assertEquals(Integer.valueOf(21), AH.at(0));
    }

    @Test
    public void testBuildheap() throws Exception {
        int i;
        Integer[] A = new Integer[20];
        for (i=0; i<20; i++)
            A[i] = i;
        DSutil.shuffle(A);
        MaxHeap<Integer> AH = new MaxHeap<>(A, 20, 20);
        assertEquals(Integer.valueOf(19), AH.at(0));
    }
}