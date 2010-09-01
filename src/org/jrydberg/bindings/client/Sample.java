package org.jrydberg.bindings.client;

import org.jrydberg.bindings.client.data.Contact;
import org.jrydberg.bindings.client.data.Person;
import org.jrydberg.bindings.client.data.PersonBinding;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Sample implements EntryPoint {

  private PersonBinding pb = (PersonBinding) GWT.create(PersonBinding.class);

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {

    TextBox textBox = new TextBox();
    TextBox l = new TextBox();

    // Prepare data:
    final Person p = new Person();
    final Contact c = new Contact();
    p.setContact(c);
    c.setEmail("jrydberg@jrydberg.org");
    p.setName("jrydberg");
    p.setAge(30);

    pb.initWithValue(p);
    
    Binder binder = new Binder();
    binder.bind(textBox).to(pb.contact().email());
    binder.bind(l).to(pb.name());
    binder.bind();

    RootPanel.get().add(textBox);
    RootPanel.get().add(l);

    Button b = new Button("click me!");
    RootPanel.get().add(b);

    b.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        System.out.println("set new email-address");
        c.setEmail("OTHER");
      }
    });

  }

}
