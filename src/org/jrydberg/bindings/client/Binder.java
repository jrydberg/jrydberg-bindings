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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;

/**
 * @author jrydberg
 * 
 */
public class Binder {
  static class Binding<T> {

    private HasValue<T> left;
    private HasValue<T> right;

    private final ValueChangeHandler<T> leftChanged = new ValueChangeHandler<T>() {
      @Override
      public void onValueChange(ValueChangeEvent<T> event) {
        right.setValue(left.getValue(), false);
      }
    };

    private final ValueChangeHandler<T> rightChanged = new ValueChangeHandler<T>() {
      @Override
      public void onValueChange(ValueChangeEvent<T> event) {
        left.setValue(right.getValue(), false);
      }
    };

    private HandlerRegistration leftHandler;
    private HandlerRegistration rightHandler;

    public Binding(HasValue<T> left, HasValue<T> right) {
      this.left = left;
      this.right = right;
    }

    public void bind() {
      leftHandler = left.addValueChangeHandler(leftChanged);
      rightHandler = right.addValueChangeHandler(rightChanged);
    }

    public void sync() {
      left.setValue(right.getValue(), false);
    }

    public void unbind() {
      leftHandler.removeHandler();
      rightHandler.removeHandler();
    }

  }

  public class BindingBuilder<ST> {

    HasValue<ST> left;
    Binding<ST> binding;

    public BindingBuilder(HasValue<ST> left) {
      this.left = left;
    }

    public BindingBuilder<ST> to(HasValue<ST> right) {
      binding = createBinding(left, right);
      return this;
    }

  }

  public <ST> BindingBuilder<ST> bind(HasValue<ST> left) {
    return new BindingBuilder<ST>(left);
  }

  private List<Binding<?>> bindings = new ArrayList<Binding<?>>();

  <ST> Binding<ST> createBinding(HasValue<ST> left, HasValue<ST> right) {
    Binding<ST> b = new Binding<ST>(left, right);
    bindings.add(b);
    return b;
  }

  public void bind() {
    for (Binding<?> b : bindings) {
      b.bind();
    }
    for (Binding<?> b : bindings) {
      b.sync();
    }
  }

}
