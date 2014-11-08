package com.lakeside.core;

public interface ExceptionalFunction<S, T, E extends Exception> {

  T apply(S item) throws E;
}