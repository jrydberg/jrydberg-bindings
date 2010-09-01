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
package org.jrydberg.bindings.client.data;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.jrydberg.bindings.client.HasProperties;

/**
 * @author jrydberg
 * 
 */
public class Contact implements HasProperties {

  private PropertyChangeSupport support;

  String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    String oldValue = this.email;
    this.email = email;
    if (support != null)
      support.firePropertyChange("email", oldValue, email);
  }

  public void addPropertyChangeListener(String propertyName,
      PropertyChangeListener l) {
    if (support == null) {
      support = new PropertyChangeSupport(this);
    }
    support.addPropertyChangeListener(propertyName, l);
  }

  public void removePropertyChangeListener(String propertyName,
      PropertyChangeListener l) {
    support.removePropertyChangeListener(propertyName, l);
  }

}
