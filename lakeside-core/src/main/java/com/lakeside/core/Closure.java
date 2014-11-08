package com.lakeside.core;

public interface Closure<T> {

    /**
     * Performs a unit of work on item, possibly throwing {@code E} in the process.
     *
     * @param item the item to perform work against
     */
    void execute(T item);
}