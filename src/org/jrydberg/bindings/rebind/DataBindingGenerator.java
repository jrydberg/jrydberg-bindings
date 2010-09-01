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
package org.jrydberg.bindings.rebind;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jrydberg.bindings.client.DataBinding;
import org.jrydberg.bindings.client.Property;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.util.collect.HashSet;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * @author jrydberg
 * 
 */
public class DataBindingGenerator extends Generator {

  static class PropertyDescription {

    JMethod getter;
    JMethod setter;

    public PropertyDescription() {
    }

  }

  private static class Binding {

    JClassType type;

    JClassType parameterType;

    String name;

    boolean nested;

    Binding(String name, JClassType type, boolean nested) {
      this.name = name;
      this.type = type;
      this.nested = nested;
    }

    @Override
    public boolean equals(Object other) {
      return ((Binding) other).name == this.name;
    }

  }

  private static PropertyDescription findPropertyDescription(JClassType type,
      String propertyName) {
    JMethod[] methods = type.getOverridableMethods();

    PropertyDescription descr = new PropertyDescription();

    for (JMethod method : methods) {
      if (!method.isPublic() || method.isStatic()) {
        continue;
      }

      String methodName = method.getName();

      if (methodName.startsWith("set")) {
        if (methodName.substring(3).toLowerCase().equals(propertyName)) {
          descr.setter = method;
        }
        continue;
      }

      if (methodName.startsWith("get")) {
        if (methodName.substring(3).toLowerCase().equals(propertyName)) {
          descr.getter = method;
        }
        continue;
      }

      if (methodName.startsWith("is")) {
        if (methodName.substring(2).toLowerCase().equals(propertyName)) {
          descr.getter = method;
        }
        continue;
      }

    }

    return descr;
  }

  private void findImplClasses(TypeOracle typeOracle, JClassType type,
      Map<JClassType, JClassType> classes) throws NotFoundException {

    JClassType[] interfaces = type.getImplementedInterfaces();
    if (interfaces.length != 1) {
      throw new NotFoundException();
    }

    JParameterizedType isParameterized = interfaces[0].isParameterized();
    if (isParameterized == null) {
      // logger.warn(
      // "The method 'getAssociatedType()' in '%s' does not return Type<? extends EventHandler>.",
      // eventType.getName());
      // return null;
    }

    JClassType[] argTypes = isParameterized.getTypeArgs();
    if ((argTypes.length != 1)) {
      // logger.warn(
      // "The method 'getAssociatedType()' in '%s' does not return Type<? extends EventHandler>.",
      // eventType.getName());
      // return null;
    }
    classes.put(type, argTypes[0]);

    // JClassType superClass = type.getSuperclass();

    // JGenericType genericType = superClass.isGenericType();
    // JTypeParameter[] typeParameters = genericType.getTypeParameters();
    // classes.put(type, typeParameters[0]);

    JClassType dataBindingClass = typeOracle.getType(DataBinding.class
        .getName());

    JMethod[] methods = type.getOverridableMethods();
    for (JMethod method : methods) {
      if (!method.isPublic() || method.isStatic()) {
        continue;
      }

      JType propertyType = method.getReturnType().getErasedType();
      assert propertyType != null;

      JClassType possibleImplClass = propertyType.isInterface();
      if (possibleImplClass != null) {
        if (possibleImplClass.isAssignableTo(dataBindingClass)) {
          if (!classes.containsKey(possibleImplClass))
            findImplClasses(typeOracle, possibleImplClass, classes);
        }
      }
    }
  }

  protected SourceWriter getSourceWriter(TreeLogger logger,
      GeneratorContext context, String packageName, String className,
      String superType) {
    PrintWriter printWriter = context.tryCreate(logger, packageName, className);
    if (printWriter == null) {
      return null;
    }
    ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(
        packageName, className);

    // composerFactory.addImport(PropertyDescriptor.class.getName());
    // composerFactory.addImport(Method.class.getName());
    // Do not use GwtBeanInfo.class as the BeanInfo interface is NOT fully
    // implemented
    // composerFactory
    // .addImport("com.googlecode.gwtx.java.introspection.client.GwtBeanInfo");
    // composerFactory
    // .addImport("com.googlecode.gwtx.java.introspection.client.GwtIntrospector");
    composerFactory.setSuperclass(superType);
    composerFactory.addImport("org.jrydberg.bindings.client.Property");
    composerFactory.addImport("org.jrydberg.bindings.client.AbstractBinding");

    return composerFactory.createSourceWriter(context, printWriter);
  }

  @Override
  public String generate(TreeLogger logger, GeneratorContext context,
      String typeName) throws UnableToCompleteException {

    TypeOracle typeOracle = context.getTypeOracle();
    JClassType type;

    Map<JClassType, JClassType> implClasses = new HashMap<JClassType, JClassType>();

    try {
      type = typeOracle.getType(typeName);

      // Iterate through the types and locate all "impl" classes
      // that we will need throughout the tree.
      findImplClasses(typeOracle, type, implClasses);
    } catch (NotFoundException e) {
      throw new UnableToCompleteException();
    }

    for (JClassType implClass : implClasses.keySet()) {
      generateImplClass(logger, context, implClass, implClasses.get(implClass));
    }

    // GENERATE ROOT CLASS IF NEEDED

    // String qualClassName = (type.getPackage().getName()
    // + type.getSimpleSourceName() + "_Root");

    return generateRootClass(logger, context, type);
  }

  public String generateRootClass(TreeLogger logger, GeneratorContext context,
      JClassType bindingType) {

    JClassType[] implementedInterfaces = bindingType.getImplementedInterfaces();
    if (implementedInterfaces.length != 1) {
      logger.log(TreeLogger.WARN, "Binding interface '" + bindingType.getName()
          + "' does not extend DataBinding<T>.");
      return null;
    }
    JClassType implementedInterface = implementedInterfaces[0];
    if (!implementedInterface.getErasedType().getQualifiedSourceName().equals(
        DATA_BINDING_CLASS_NAME)) {
      logger.log(TreeLogger.WARN, "Binding interface '" + bindingType.getName()
          + "' does not extend DataBinding<T>.");
      return null;
    }
    JParameterizedType isParameterized = implementedInterface.isParameterized();
    if (isParameterized == null) {
      logger.log(TreeLogger.WARN, "Binding interface '" + bindingType.getName()
          + "' does not parameterize DataBinding.");
      return null;
    }
    JClassType[] argTypes = isParameterized.getTypeArgs();
    if (argTypes.length != 1) {
      logger.log(TreeLogger.WARN, "Binding interface '" + bindingType.getName()
          + "' should have one parameter for DataBinding interface.");
      return null;
    }
    JClassType beanType = argTypes[0];

    String className = bindingType.getName() + "_ROOT";
    String qualClassName = bindingType.getPackage().getName() + "." + className;

    String superType = bindingType.getName() + "_IMPL<" + beanType.getName()
        + ">";

    PrintWriter printWriter = context.tryCreate(logger, bindingType
        .getPackage().getName(), className);
    if (printWriter == null) {
      return qualClassName;
    }

    ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(
        bindingType.getPackage().getName(), className);
    composerFactory.setSuperclass(superType);
    composerFactory.addImplementedInterface(bindingType.getName());

    composerFactory.addImport(bindingType.getQualifiedSourceName());
    composerFactory.addImport(bindingType.getQualifiedSourceName() + "_IMPL");
    composerFactory.addImport(HasValue.class.getName());
    composerFactory.addImport("org.jrydberg.bindings.client.ValueBox");

    SourceWriter sourceWriter = composerFactory.createSourceWriter(context,
        printWriter);

    sourceWriter.println("public " + className + "() {");
    sourceWriter.println("  super(null);");
    sourceWriter.println("}");

    sourceWriter.println("@Override");
    sourceWriter.println("public void initWithValueBox(HasValue<"
        + beanType.getName() + "> valueBox) {");
    sourceWriter.println("  setSourceBox(valueBox);");
    sourceWriter.println("}\n");

    sourceWriter.println("@Override");
    sourceWriter.println("public void initWithValue(" + beanType.getName()
        + " value) {");
    sourceWriter.println("  setSourceBox(new ValueBox<" + beanType.getName()
        + ">(value));");
    sourceWriter.println("}");

    sourceWriter.println("@Override");
    sourceWriter.println("public " + beanType.getName() + " getValue() {");
    sourceWriter.println("  return getSource();");
    sourceWriter.println("}");

    sourceWriter.println("@Override");
    sourceWriter.println("public void setValue(" + beanType.getName() + " value) {");
    sourceWriter.println("  setSource(value);");
    sourceWriter.println("}");

    sourceWriter.commit(null);
    return qualClassName;
  }

  private static final String DATA_BINDING_CLASS_NAME = DataBinding.class
      .getName();

  private static final String PROPERTY_CLASS_NAME = Property.class.getName();

  private Set<Binding> findBindings(JClassType type) {
    Set<Binding> types = new HashSet<Binding>();

    JMethod[] methods = type.getOverridableMethods();
    for (JMethod method : methods) {
      if (!method.isPublic() || method.isStatic()) {
        continue;
      }

      JType propertyType = method.getReturnType();
      assert propertyType != null;

      JParameterizedType isParameterized = propertyType.isParameterized();
      if (isParameterized != null) {
        JClassType enclosingType = isParameterized.getBaseType();
        if (enclosingType.getQualifiedSourceName().equals(PROPERTY_CLASS_NAME)) {
          JClassType[] argTypes = isParameterized.getTypeArgs();
          if (argTypes.length == 1) {
            types.add(new Binding(method.getName(), argTypes[0], false));
          }
        }
      }

      JClassType isInterface = propertyType.isInterface();
      if (isInterface != null) {
        JClassType[] interfaces = isInterface.getImplementedInterfaces();
        if (interfaces.length != 1) {
          continue;
        }

        JClassType dataBindingInterface = interfaces[0];
        if (dataBindingInterface.getQualifiedSourceName().equals(
            DATA_BINDING_CLASS_NAME)) {
          JParameterizedType bindingParameters = dataBindingInterface
              .isParameterized();
          JClassType[] argTypes = bindingParameters.getTypeArgs();
          if (argTypes.length != 1) {
            continue;
          }

          Binding binding = new Binding(method.getName(), isInterface, true);
          binding.parameterType = argTypes[0];
          types.add(binding);
        }
      }

    }

    return types;
  }

  private void writeProperty(SourceWriter sourceWriter, JClassType valueType,
      JClassType sourceType, PropertyDescription propDescr) {

    if (propDescr.getter != null) {
      sourceWriter.println("@Override");
      sourceWriter.println("public " + valueType.getName() + " getValue() {");
      sourceWriter.println("  " + sourceType.getName()
          + " source = getSource();");
      sourceWriter.println("  if (source != null) {");
      sourceWriter.println("    return source." + propDescr.getter.getName()
          + "();");
      sourceWriter.println("  }");
      sourceWriter.println("  return null;");
      sourceWriter.println("}");
    }

    if (propDescr.setter != null) {
      sourceWriter.println("@Override");
      sourceWriter.println("public void setValue(" + valueType.getName()
          + " value) {");
      sourceWriter.println("  " + sourceType.getName()
          + " source = getSource();");
      sourceWriter.println("  if (source != null) {");
      sourceWriter.println("    source." + propDescr.setter.getName()
          + "(value);");
      sourceWriter.println("  }");
      sourceWriter.println("}");
    }

  }

  private void stubBindingMethods(SourceWriter sourceWriter, String type) {
    sourceWriter.println("@Override");
    sourceWriter.println("public void initWithValue(" + type
        + " value) {");
    sourceWriter.println("}");
    sourceWriter.println("@Override");
    sourceWriter.println("public void initWithValueBox(HasValue<"
        + type + "> valueBox) {");
    sourceWriter.println("}");
  }

  public void generateImplClass(TreeLogger logger, GeneratorContext context,
      JClassType type, JClassType beanType) {
    String superType = "AbstractBinding<ST, " + beanType.getName() + ">";
    String className = type.getName() + "_IMPL";

    PrintWriter printWriter = context.tryCreate(logger, type.getPackage()
        .getName(), className);
    if (printWriter == null) {
      return;
    }

    Set<Binding> bindings = findBindings(type);

    ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(
        type.getPackage().getName(), className + "<ST>");
    composerFactory.addImport("org.jrydberg.bindings.client.AbstractBinding");
    composerFactory.addImport("org.jrydberg.bindings.client.DataBinding");
    composerFactory.addImport("org.jrydberg.bindings.client.Property");
    composerFactory.addImport(type.getQualifiedSourceName());
    composerFactory.addImport(beanType.getQualifiedSourceName());
    composerFactory.addImport(HasValue.class.getName());
    for (Binding binding : bindings) {
      composerFactory.addImport(binding.type.getQualifiedSourceName());
      if (binding.nested) {
        composerFactory.addImport(binding.type.getQualifiedSourceName()
            + "_IMPL");
      }
    }

    composerFactory.setSuperclass(superType);
    composerFactory.addImplementedInterface(type.getName());

    SourceWriter sourceWriter = composerFactory.createSourceWriter(context,
        printWriter);

    sourceWriter.println("public " + className + "(HasValue<ST> sourceBox) {");
    sourceWriter.println("  super(sourceBox);");
    sourceWriter.println("}");
    
    for (Binding binding : bindings) {
      PropertyDescription propDescr = findPropertyDescription(beanType,
          binding.name);

      if (!binding.nested) {
        // This should return a Property<type>
        sourceWriter.println("@Override");
        sourceWriter.println("public Property<" + binding.type.getName() + "> "
            + binding.name + "() {");
        sourceWriter.indent();

        sourceWriter.println("return new AbstractBinding<" + beanType.getName()
            + "," + binding.type.getName() + ">(this) {");
        sourceWriter.indent();

        sourceWriter.println("@Override");
        sourceWriter.println("public String getPropertyName() {");
        sourceWriter.println("  return \"" + binding.name + "\";");
        sourceWriter.println("}");
        writeProperty(sourceWriter, binding.type, beanType, propDescr);
        sourceWriter.outdent();
        sourceWriter.println("};");
        sourceWriter.outdent();
        sourceWriter.println("}");
      } else {
        // This should return a Property<type>
        sourceWriter.println("@Override");
        sourceWriter.println("public " + binding.type.getName() + " "
            + binding.name + "() {");
        sourceWriter.indent();
        sourceWriter.println("return new " + binding.type.getName() + "_IMPL<"
            + beanType.getName() + ">(this) {");
        sourceWriter.indent();
        writeProperty(sourceWriter, binding.parameterType, beanType, propDescr);

        stubBindingMethods(sourceWriter, binding.parameterType.getName());
        // FIXME: do we need to overwrite the root-methods?

        sourceWriter.outdent();
        sourceWriter.println("};");
        sourceWriter.outdent();
        sourceWriter.println("}");
      }

    }

    stubBindingMethods(sourceWriter, beanType.getName());
    
    sourceWriter.commit(null);
  }

}
