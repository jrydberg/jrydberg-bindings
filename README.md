jrydberg-bindings
=================

jrydberg-bindings generates bindgen-like data bindings for *GWT* (Google Web Toolkit).

These bindings are intended to be used with *gwt-pectin*, but pectin does not support
binding directly to a *HasValue* yet.  That's why there's a simple *Binder*-class included
with the sample.


How to use them
===============

You got a set of beans that you want to be able to bind to.  Create a Binding-interface
for each of the beans.  It could look something like this:

    public class Person {
      String name;
      int age;
      Contact contact;

      // Setters and getters for the properties
    }

    public interface PersonBinding extends DataBinding<Person> {
      Property<String> name();
      Property<Integer> age();
      ContactBinding contact();
    }

Now we use the standard deferred binding mechanism in GWT to create our special
binding class:

    PersonBinding binding = (PersonBinding) GWT.create(PersonBinding.class);

Later you can use this to get `HasValue<T>` instances for a property.  For example 
`binding.name()` will return a `HasValue<String>` for the `name` property of the
Person bean.

Initialize the binding with setting a root bean;

    binding.initWithValue(personBean);

There's also `initWithValueBox` that takes a `HasValue<Person>`. Any changes
to the value box will be propagated through the binding.


(`ContactBinding` is another `DataBinding` interface for the Contact bean.
  To get a nested property simply do; `binding.contact().email()`).


Additional
==========

It's written by Johan Rydberg <johan.rydberg@gmail.com>

References:

 * bindgen: http://bindgen.org/
 * GWT: http://code.google.com/webtoolkit/
 * gwtx: http://code.google.com/p/gwtx/
