package com.maximgalushka.classifier.storage.mysql;

import java.sql.ResultSet;

/**
 * @since 9/16/2014.
 */
public interface Command<T> {

  public T process(ResultSet set);
}
