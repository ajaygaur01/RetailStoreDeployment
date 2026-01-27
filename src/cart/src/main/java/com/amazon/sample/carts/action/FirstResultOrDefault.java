/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *

 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.amazon.sample.carts.action;

import java.util.Collection;
import java.util.function.Supplier;

public class FirstResultOrDefault<T> implements Supplier<T> {

  private final Collection<T> collection;
  private final Supplier<T> nonePresent;

  public FirstResultOrDefault(
    final Collection<T> collection,
    final Supplier<T> nonePresent
  ) {
    this.collection = collection;
    this.nonePresent = nonePresent;
  }

  @Override
  public T get() {
    return collection.stream().findFirst().orElseGet(nonePresent);
  }
}
