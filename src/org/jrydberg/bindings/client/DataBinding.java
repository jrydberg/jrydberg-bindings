/**
 * Copyright 2010 Johan Rydberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jrydberg.bindings.client;

import com.google.gwt.user.client.ui.HasValue;

/**
 * @author jrydberg
 *
 */
public interface DataBinding<T> extends Property<T> {

  /**
   * Initialize binding with value.
   *
   * @param value the initial value of the binding
   */
  void initWithValue(T value);

  /**
   * Initialize binding with value from the value box. 
   * 
   * The binding will change with the content of the box.
   * 
   * @param valueBox
   */
  void initWithValueBox(HasValue<T> valueBox);
  
}
