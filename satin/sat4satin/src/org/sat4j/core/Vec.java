/*
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2006 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas E?n and Niklas S?rensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.sat4j.core;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.sat4j.specs.IVec;

/**
 * Simple but efficient vector implementation, based on the vector
 * implementation available in MiniSAT. Note that the elements are compared
 * using their references, not using the equals method.
 * 
 * @author leberre
 */
public class Vec<T> implements Serializable, IVec<T> {

    private static final long serialVersionUID = 1L;

    private static final int RANDOM_SEED = 91648253;

    /**
     * Create a Vector with an initial capacity of 5 elements.
     */
    public Vec() {
        this(5);
    }

    /**
     * Adapter method to translate an array of int into an IVec.
     * 
     * The array is used inside the Vec, so the elements may be modified outside
     * the Vec. But it should not take much memory. The size of the created Vec
     * is the length of the array.
     * 
     * @param elts
     *            a filled array of T.
     */
    public Vec(T[] elts) {
        myarray = elts;
        nbelem = elts.length;
    }

    /**
     * Create a Vector with a given capacity.
     * 
     * @param size
     *            the capacity of the vector.
     */
    @SuppressWarnings("unchecked")
    public Vec(int size) {
        myarray = (T[]) new Object[size];
    }

    /**
     * Construit un vecteur contenant de taille size rempli � l'aide de size
     * pad.
     * 
     * @param size
     *            la taille du vecteur
     * @param pad
     *            l'objet servant � remplir le vecteur
     */
    @SuppressWarnings("unchecked")
    public Vec(int size, T pad) {
        myarray = (T[]) new Object[size];
        for (int i = 0; i < size; i++) {
            myarray[i] = pad;
        }
        nbelem = size;
    }

    public int size() {
        return nbelem;
    }

    /**
     * Remove nofelems from the Vector. It is assumed that the number of
     * elements to remove is smaller or equals to the current number of elements
     * in the vector
     * 
     * @param nofelems
     *            the number of elements to remove.
     */
    public void shrink(int nofelems) {
        assert nofelems <= nbelem;
        while (nofelems-- > 0) {
            myarray[--nbelem] = null;
        }
    }

    /**
     * reduce the Vector to exactly newsize elements
     * 
     * @param newsize
     *            the new size of the vector.
     */
    public void shrinkTo(final int newsize) {
        assert newsize <= size();
        for (int i = nbelem; i > newsize; i--) {
            myarray[i - 1] = null;
        }
        nbelem = newsize;
        assert size() == newsize;
    }

    /**
     * Pop the last element on the stack. It is assumed that the stack is not
     * empty!
     */
    public void pop() {
        assert size() > 0;
        myarray[--nbelem] = null;
    }

    public void growTo(final int newsize, final T pad) {
        assert newsize >= size();
        ensure(newsize);
        for (int i = nbelem; i < newsize; i++) {
            myarray[i] = pad;
        }
        nbelem = newsize;
    }

    @SuppressWarnings("unchecked")
    public final void ensure(final int nsize) {
        // WAS: if (nsize >= myarray.length) {
        if (nsize > myarray.length) {
            T[] narray = (T[]) new Object[Math.max(nsize, nbelem * 2)];
            System.arraycopy(myarray, 0, narray, 0, nbelem);
            myarray = narray;
        }
    }

    public IVec<T> push(final T elem) {
        ensure(nbelem + 1);
        myarray[nbelem++] = elem;
        return this;
    }

    public void unsafePush(final T elem) {
        myarray[nbelem++] = elem;
    }

    /**
     * Insert an element at the very begining of the vector. The former first
     * element is appended to the end of the vector in order to have a constant
     * time operation.
     * 
     * @param elem
     *            the element to put first in the vector.
     */
    public void insertFirst(final T elem) {
        if (nbelem > 0) {
            push(myarray[0]);
            myarray[0] = elem;
            return;
        }
        push(elem);
    }

    public void insertFirstWithShifting(final T elem) {
        if (nbelem > 0) {
            ensure(nbelem + 1);
            for (int i = nbelem; i > 0; i--) {
                myarray[i] = myarray[i - 1];
            }
            myarray[0] = elem;
            nbelem++;
            return;
        }
        push(elem);
    }

    public void clear() {
        Arrays.fill(myarray, 0, nbelem, null);
        nbelem = 0;
    }

    /**
     * return the latest element on the stack. It is assumed that the stack is
     * not empty!
     * 
     * @return the last element on the stack (the one on the top)
     */
    public T last() {
        assert size() != 0;
        return myarray[nbelem - 1];
    }

    public T get(final int index) {
        return myarray[index];
    }

    public void set(int index, T elem) {
        myarray[index] = elem;
    }

    /**
     * Remove an element that belongs to the Vector. The method will break if
     * the element does not belong to the vector.
     * 
     * @param elem
     *            an element from the vector.
     */
    public void remove(T elem) {
        assert size() > 0;
        int j = 0;
        for (; myarray[j] != elem; j++) {
            assert j < size();
        }
        // arraycopy is always faster than manual copy
        System.arraycopy(myarray, j + 1, myarray, j, size() - j - 1);
        myarray[--nbelem] = null;
    }

    /**
     * Delete the ith element of the vector. The latest element of the vector
     * replaces the removed element at the ith indexer.
     * 
     * @param index
     *            the indexer of the element in the vector
     * @return the former ith element of the vector that is now removed from the
     *         vector
     */
    public T delete(int index) {
        assert index >= 0 && index < nbelem;
        T ith = myarray[index];
        myarray[index] = myarray[--nbelem];
        myarray[nbelem] = null;
        return ith;
    }

    /**
     * Ces op�rations devraient se faire en temps constant. Ce n'est pas le
     * cas ici.
     * 
     * @param copy
     */
    public void copyTo(IVec<T> copy) {
        final Vec<T> ncopy = (Vec<T>) copy;
        final int nsize = nbelem + ncopy.nbelem;
        copy.ensure(nsize);
        System.arraycopy(myarray, 0, ncopy.myarray, ncopy.nbelem, nbelem);
        ncopy.nbelem = nsize;
    }

    /**
     * @param dest
     */
    public <E> void copyTo(E[] dest) {
        assert dest.length >= nbelem;
        System.arraycopy(myarray, 0, dest, 0, nbelem);
    }

    /*
     * Copie un vecteur dans un autre (en vidant le premier), en temps constant.
     */
    public void moveTo(IVec<T> dest) {
        copyTo(dest);
        clear();
    }

    public void moveTo(int dest, int source) {
        myarray[dest] = myarray[source];
        myarray[source] = null;
    }

    public T[] toArray() {
        return myarray;
    }

    private int nbelem;

    private T[] myarray;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < nbelem - 1; i++) {
            stb.append(myarray[i]);
            stb.append(","); //$NON-NLS-1$
        }
        if (nbelem > 0) {
            stb.append(myarray[nbelem - 1]);
        }
        return stb.toString();
    }

    private static Random rand = new Random(RANDOM_SEED);

    void selectionSort(int from, int to, Comparator<T> cmp) {
        int i, j, best_i;
        T tmp;

        for (i = from; i < to - 1; i++) {
            best_i = i;
            for (j = i + 1; j < to; j++) {
                if (cmp.compare(myarray[j], myarray[best_i]) < 0)
                    best_i = j;
            }
            tmp = myarray[i];
            myarray[i] = myarray[best_i];
            myarray[best_i] = tmp;
        }
    }

    void sort(int from, int to, Comparator<T> cmp) {
        int width = to - from;
        if (to - from <= 15)
            selectionSort(from, to, cmp);

        else {
            T pivot = myarray[rand.nextInt(width) + from];
            T tmp;
            int i = from - 1;
            int j = to;

            for (;;) {
                do
                    i++;
                while (cmp.compare(myarray[i], pivot) < 0);
                do
                    j--;
                while (cmp.compare(pivot, myarray[j]) < 0);

                if (i >= j)
                    break;

                tmp = myarray[i];
                myarray[i] = myarray[j];
                myarray[j] = tmp;
            }

            sort(from, i, cmp);
            sort(i, to, cmp);
        }
    }

    /**
     * @param comparator
     */
    public void sort(Comparator<T> comparator) {
        sort(0, nbelem, comparator);
    }

    public void sortUnique(Comparator<T> cmp) {
        int i, j;
        T last;

        if (nbelem == 0)
            return;

        sort(0, nbelem, cmp);

        i = 1;
        last = myarray[0];
        for (j = 1; j < nbelem; j++) {
            if (cmp.compare(last, myarray[j]) < 0) {
                last = myarray[i] = myarray[j];
                i++;
            }
        }

        nbelem = i;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IVec) {
            IVec<?> v = (IVec<?>) obj;
            if (v.size() != size())
                return false;
            for (int i = 0; i < size(); i++) {
                if (!v.get(i).equals(get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int sum = 0;
        for (int i = 0; i < nbelem; i++) {
            sum += myarray[i].hashCode() / nbelem;
        }
        return sum;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int i = 0;

            public boolean hasNext() {
                return i < nbelem;
            }

            public T next() {
                if (i == nbelem)
                    throw new NoSuchElementException();
                return myarray[i++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean isEmpty() {
        return nbelem == 0;
    }
}
