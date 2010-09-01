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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;

/**
 * @author jrydberg
 * 
 */
public class AbstractBinding<S, T> implements Property<T>,
    PropertyChangeListener {

  private final HandlerManager handlerManager = new HandlerManager(this);

  private HasValue<S> sourceBox;

  private S cachedSource;

  private T cachedValue;

  private HandlerRegistration handlerRegistration;

  private final ValueChangeHandler<S> valueChangeHandler = new ValueChangeHandler<S>() {
    public void onValueChange(ValueChangeEvent<S> event) {
      onSourceChange(true);
    }
  };

  public AbstractBinding(HasValue<S> sourceBox) {
    setSourceBox(sourceBox);
  }

  protected S getSource() {
    return cachedSource;
  }

  protected void setSource(S source) {
    this.cachedSource = source;
  }

  protected String getPropertyName() {
    return null;
  }

  protected void setSourceBox(HasValue<S> sourceBox) {
    if (this.sourceBox != sourceBox) {
      if (this.sourceBox != null) {
        handlerRegistration.removeHandler();
      }
      this.sourceBox = sourceBox;
      if (sourceBox != null) {
        handlerRegistration = sourceBox.addValueChangeHandler(valueChangeHandler);
        onSourceChange(false);
      }
    }
  }

  HandlerRegistration propertyHandler;

  private void onSourceChange(boolean fireEvents) {
    S oldCachedSource = cachedSource;
    cachedSource = sourceBox.getValue();

    if (oldCachedSource != cachedSource) {
      if (getPropertyName() != null) {
        if (oldCachedSource instanceof HasProperties) {
          ((HasProperties) oldCachedSource).removePropertyChangeListener(
              getPropertyName(), this);
        }
        if (cachedSource instanceof HasProperties) {
          ((HasProperties) cachedSource).addPropertyChangeListener(
              getPropertyName(), this);
        }
      }
    }

    T oldCachedValue = cachedValue;
    cachedValue = this.getValue();
    if (cachedValue != oldCachedValue && fireEvents) {
      ValueChangeEvent.fire(this, cachedValue);
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    onSourceChange(true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object,
   * boolean)
   */
  @Override
  public void setValue(T value, boolean fireEvents) {
    this.setValue(value);
    if (fireEvents) {
      ValueChangeEvent.fire(this, value);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.google.gwt.event.logical.shared.HasValueChangeHandlers#
   * addValueChangeHandler
   * (com.google.gwt.event.logical.shared.ValueChangeHandler)
   */
  @Override
  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
    return handlerManager.addHandler(ValueChangeEvent.getType(), handler);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.google.gwt.event.shared.HasHandlers#fireEvent(com.google.gwt.event.
   * shared.GwtEvent)
   */
  @Override
  public void fireEvent(GwtEvent<?> event) {
    handlerManager.fireEvent(event);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.user.client.ui.HasValue#getValue()
   */
  @Override
  public T getValue() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue(T value) {
    // TODO Auto-generated method stub

  }

}
