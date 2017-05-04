/*
 * Copyright 2009-2017 the original author or authors.
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
package org.eclipse.jdt.groovy.core.tests.basic;

import static org.eclipse.jdt.core.tests.util.GroovyUtils.isAtLeastGroovy;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public final class TraitsTests extends GroovyCompilerTestSuite {

    public TraitsTests(long level) {
        super(level);
    }

    @Before
    public void setUp() {
        assumeTrue(isAtLeastJava(JDK6));
        assumeTrue(isAtLeastGroovy(23));
    }

    @Test
    public void testTraits1() {
        String[] sources = {
            "Test.groovy",
            "class Person implements Greetable {\n" +
            "  String name() { 'Bob' }\n" +
            "}\n" +
            "public class Test {\n" +
            "  public static void main(String[] argv) {\n" +
            "    def p = new Person()\n" +
            "    print p.greeting()\n" +
            "  }\n" +
            "}",

            "Greetable.groovy",
            "trait Greetable {\n" +
            "  abstract String name()\n" +
            "  String greeting() { \"Hello, ${name()}!\" }\n" +
            "}"
        };

        runConformTest(sources, "Hello, Bob!");

        ClassNode classNode = getCUDeclFor("Greetable.groovy").getCompilationUnit().getClassNode("Greetable");
        assertTrue(classNode.isInterface());
    }

    @Test
    public void testTraits1a() {
        String[] sources = {
            "Test.groovy",
            "class Person implements Greetable {\n" +
            "  String name() { 'Bob' }\n" +
            "}\n" +
            "public class Test {\n" +
            "  public static void main(String[] argv) {\n" +
            "    def p = new Person()\n" +
            "    print p.greeting()\n" +
            "  }\n" +
            "}",

            "Greetable.groovy",
            "import groovy.transform.Trait;\n" +
            "@Trait\n" +
            "class Greetable {\n" +
            "  abstract String name()\n" +
            "  String greeting() { \"Hello, ${name()}!\" }\n" +
            "}"
        };

        runConformTest(sources, "Hello, Bob!");

        ClassNode classNode = getCUDeclFor("Greetable.groovy").getCompilationUnit().getClassNode("Greetable");
        assertTrue(classNode.isInterface());
    }

    @Test // Abstract Methods
    public void testTraits2() {
        String[] sources = {
            "A.groovy",
            "trait Greetable {\n" +
            "  abstract String name()\n" +
            "  String greeting() { \"Hello, ${name()}!\" }\n" +
            "}\n" +
            "class Person implements Greetable {\n" +
            "  String name() { 'Bob' }\n" +
            "}\n" +
            "def p = new Person()\n" +
            "print p.greeting()"
        };

        runConformTest(sources, "Hello, Bob!");
    }

    @Test // Private Methods - positive test
    public void testTraits3() {
        String[] sources = {
            "A.groovy",
            "trait Greeter {\n" +
            "  private String greetingMessage() {\n" +
            "    'Hello from a private method!'\n" +
            "  }\n" +
            "  String greet() {\n" +
            "    def m = greetingMessage()\n" +
            "    println m\n" +
            "    m\n" +
            "  }\n" +
            "}\n" +
            "class GreetingMachine implements Greeter {}\n" +
            "def g = new GreetingMachine()\n" +
            "g.greet()"
        };

        runConformTest(sources, "Hello from a private method!");
    }

    @Test // Private Methods - negative test
    public void testTraits4() {
        String[] sources = {
            "A.groovy",
            "trait Greeter {\n" +
            "  private String greetingMessage() {\n" +
            "    'Hello from a private method!'\n" +
            "  }\n" +
            "  String greet() {\n" +
            "    def m = greetingMessage()\n" +
            "    println m\n" +
            "    m\n" +
            "  }\n" +
            "}\n" +
            "class GreetingMachine implements Greeter {}\n" +
            "def g = new GreetingMachine()\n" +
            "try {\n" +
            "  g.greetingMessage()\n" +
            "} catch (MissingMethodException e) {\n" +
            "}"
        };

        runConformTest(sources);
    }

    @Test // Meaning of this
    public void testTraits5() {
        String[] sources = {
            "A.groovy",
            "trait Introspector {\n" +
            "  def whoAmI() { this.getClass() }\n" +
            "}\n" +
            "class Foo implements Introspector {}\n" +
            "def foo = new Foo()\n" +
            "print foo.whoAmI()"
        };

        runConformTest(sources, "class Foo");
    }

    @Test // Interfaces
    public void testTraits6() {
        String[] sources = {
            "A.groovy",
            "interface Named {\n" +
            "  String name()\n" +
            "}\n" +
            "trait Greetable implements Named {\n" +
            "  String greeting() { \"Hello, ${name()}!\" }\n" +
            "}\n" +
            "class Person implements Greetable {\n" +
            "  String name() { 'Bob' }\n" +
            "}\n" +
            "def p = new Person()\n" +
            "print p.greeting()"
        };

        runConformTest(sources, "Hello, Bob!");

        CompilationUnit unit = getCUDeclFor("A.groovy").getCompilationUnit();
        ClassNode classNode = unit.getClassNode("Person");
        ClassNode type = unit.getClassNode("Greetable");
        assertTrue(classNode.implementsInterface(type));
        type = unit.getClassNode("Named");
        assertTrue(classNode.implementsInterface(type));
    }

    @Test // Properties
    public void testTraits7() {
        String[] sources = {
            "A.groovy",
            "trait Named {\n" +
            "  String name\n" +
            "}\n" +
            "class Person implements Named {}\n" +
            "def p = new Person(name: 'Bob')\n" +
            "print p.name == 'Bob'\n" +
            "print p.getName()"
        };

        runConformTest(sources, "trueBob");
    }

    @Test // Private fields
    public void testTraits8() {
        String[] sources = {
            "A.groovy",
            "trait Counter {\n" +
            "  private int count = 0\n" +
            "  int count() { count += 1; count }\n" +
            "}\n" +
            "class Foo implements Counter {}\n" +
            "def f = new Foo()\n" +
            "print f.count()"
        };

        runConformTest(sources, "1");
    }

    @Test // Public fields
    public void testTraits9() {
        String[] sources = {
            "A.groovy",
            "trait Named {\n" +
            "    public String name\n" +
            "}\n" +
            "class Person implements Named {}\n" +
            "def p = new Person()\n" +
            "p.Named__name = 'Bob'\n" +
            "print p.Named__name"
        };

        runConformTest(sources, "Bob");
    }

    @Test // Composition of Behaviors
    public void testTraits10() {
        String[] sources = {
            "A.groovy",
            "trait FlyingAbility {\n" +
            "  String fly() { \"I'm flying!\" }\n" +
            "}\n" +
            "trait SpeakingAbility {\n" +
            "  String speak() { \"I'm speaking!\" }\n" +
            "}\n" +
            "class Duck implements FlyingAbility, SpeakingAbility {}\n" +
            "def d = new Duck()\n" +
            "print d.fly()\n" +
            "print d.speak()"
        };

        runConformTest(sources, "I'm flying!I'm speaking!");
    }

    @Test // Overriding default methods
    public void testTraits11() {
        String[] sources = {
            "A.groovy",
            "trait FlyingAbility {\n" +
            "  String fly() { \"I'm flying!\" }\n" +
            "}\n" +
            "trait SpeakingAbility {\n" +
            "  String speak() { \"I'm speaking!\" }\n" +
            "}\n" +
            "class Duck implements FlyingAbility, SpeakingAbility {\n" +
            "  String quack() { \"Quack!\" }\n" +
            "  String speak() { quack() }\n" +
            "}\n" +
            "def d = new Duck()\n" +
            "print d.fly()\n" +
            "print d.quack()\n" +
            "print d.speak()"
        };

        runConformTest(sources, "I'm flying!Quack!Quack!");
    }

    @Test // Simple Inheritance
    public void testTraits12() {
        String[] sources = {
            "A.groovy",
            "trait Named {\n" +
            "  String name\n" +
            "}\n" +
            "trait Polite extends Named {\n" +
            "  String introduce() { \"Hello, I am $name\" }\n" +
            "}\n" +
            "class Person implements Polite {}\n" +
            "def p = new Person(name: 'Alice')\n" +
            "print p.introduce()"
        };

        runConformTest(sources, "Hello, I am Alice");
    }

    @Test // Multiple Inheritance
    public void testTraits13() {
        String[] sources = {
            "A.groovy",
            "trait WithId {\n" +
            "  Long id\n" +
            "}\n" +
            "trait WithName {\n" +
            "  String name\n" +
            "}\n" +
            "trait Identified implements WithId, WithName {\n" +
            "}"
        };

        runConformTest(sources);
    }

    @Test // Dynamic code
    public void testTraits14() {
        String[] sources = {
            "A.groovy",
            "trait SpeakingDuck {\n" +
            "  String speak() { quack() }\n" +
            "}\n" +
            "class Duck implements SpeakingDuck {\n" +
            "  String methodMissing(String name, args) {\n" +
            "    \"${name.capitalize()}!\"\n" +
            "  }\n" +
            "}\n" +
            "def d = new Duck()\n" +
            "print d.speak()"
        };

        runConformTest(sources, "Quack!");
    }

    @Test // Dynamic methods in trait
    public void testTraits15() {
        String[] sources = {
            "A.groovy",
            "trait DynamicObject {\n" +
            "    private Map props = [:]\n" +
            "    def methodMissing(String name, args) {\n" +
            "      name.toUpperCase()\n" +
            "    }\n" +
            "    def propertyMissing(String prop) {\n" +
            "      props['prop']\n" +
            "    }\n" +
            "    void setProperty(String prop, Object value) {\n" +
            "      props['prop'] = value\n" +
            "    }\n" +
            "}\n" +
            "class Dynamic implements DynamicObject {\n" +
            "  String existingProperty = 'ok'\n" +
            "  String existingMethod() { 'ok' }\n" +
            "}\n" +
            "def d = new Dynamic()\n" +
            "print d.existingProperty\n" +
            "print d.foo\n" +
            "d.foo = 'bar'\n" +
            "print d.foo\n" +
            "print d.existingMethod()\n" +
            "print d.someMethod()"
        };

        runConformTest(sources, "oknullbarokSOMEMETHOD");
    }

    @Test // Multiple inheritance conflicts - Default conflict resolution
    public void testTraits16() {
        String[] sources = {
            "Sample.groovy",
            "trait A {\n" +
            "  String exec() { 'A' }\n" +
            "}\n" +
            "trait B {\n" +
            "  String exec() { 'B' }\n" +
            "}\n" +
            "class C implements A, B {}\n" +
            "def c = new C()\n" +
            "print c.exec()"
        };

        runConformTest(sources, "B");
    }

    @Test // Multiple inheritance conflicts - Default conflict resolution
    public void testTraits17() {
        String[] sources = {
            "Sample.groovy",
            "trait A {\n" +
            "  String exec() { 'A' }\n" +
            "}\n" +
            "trait B {\n" +
            "  String exec() { 'B' }\n" +
            "}\n" +
            "class C implements B, A {}\n" +
            "def c = new C()\n" +
            "print c.exec()"
        };

        runConformTest(sources, "A");
    }

    @Test // Multiple inheritance conflicts - User conflict resolution
    public void testTraits18() {
        String[] sources = {
            "Sample.groovy",
            "trait A {\n" +
            "  String exec() { 'A' }\n" +
            "}\n" +
            "trait B {\n" +
            "  String exec() { 'B' }\n" +
            "}\n" +
            "class C implements A, B {\n" +
            "  String exec() { A.super.exec() }\n" +
            "}\n" +
            "def c = new C()\n" +
            "print c.exec()"
        };

        runConformTest(sources, "A");
    }

    @Test // Implementing a trait at runtime
    public void testTraits19() {
        String[] sources = {
            "Sample.groovy",
            "trait Extra {\n" +
            "  String extra() { 'Extra' }\n" +
            "}\n" +
            "class Something {\n" +
            "  String doSomething() { 'Something' }\n" +
            "}\n" +
            "def s = new Something() as Extra\n" +
            "print s.extra()\n" +
            "print s.doSomething()"
        };

        runConformTest(sources, "ExtraSomething");
    }

    @Test // Implementing multiple traits at once - negative
    public void testTraits20() {
        String[] sources = {
            "Sample.groovy",
            "trait A { String methodFromA() { 'A' } }\n" +
            "trait B { String methodFromB() { 'B' } }\n" +
            "class C {}\n" +
            "def c = new C()\n" +
            "print c.methodFromA()\n" +
            "print c.methodFromB()"
        };

        runConformTest(sources, "", "groovy.lang.MissingMethodException: No signature of method: C.methodFromA() is applicable for argument types: () values: []");
    }

    @Test // Implementing multiple traits at once - positive
    public void testTraits21() {
        String[] sources = {
            "Sample.groovy",
            "trait A { String methodFromA() { 'A' } }\n" +
            "trait B { String methodFromB() { 'B' } }\n" +
            "class C {}\n" +
            "def c = new C()\n" +
            "def d = c.withTraits A, B\n" +
            "print d.methodFromA()\n" +
            "print d.methodFromB()"
        };

        runConformTest(sources, "AB");
    }

    @Test // Chaining behavior
    public void testTraits22() {
        String[] sources = {
            "Sample.groovy",
            "interface MessageHandler {\n" +
            "  void on(String message, Map payload)\n" +
            "}\n" +
            "trait DefaultHandler implements MessageHandler {\n" +
            "  void on(String message, Map payload) {\n" +
            "    println \"Received $message with payload $payload\"\n" +
            "  }\n" +
            "}\n" +
            "class SimpleHandler implements DefaultHandler {}\n" +
            "def handler = new SimpleHandler()\n" +
            "handler.on('test logging', [:])"
        };

        runConformTest(sources, "Received test logging with payload [:]");
    }

    @Test // Chaining behavior
    public void testTraits23() {
        String[] sources = {
            "Sample.groovy",
            "interface MessageHandler {\n" +
            "  void on(String message, Map payload)\n" +
            "}\n" +
            "trait DefaultHandler implements MessageHandler {\n" +
            "  void on(String message, Map payload) {\n" +
            "    println \"Received $message with payload $payload\"\n" +
            "  }\n" +
            "}\n" +
            "class SimpleHandlerWithLogging implements DefaultHandler {\n" +
            "  void on(String message, Map payload) {\n" +
            "    println \"Seeing $message with payload $payload\"\n" +
            "    DefaultHandler.super.on(message, payload)\n" +
            "  }\n" +
            "}\n" +
            "def handler = new SimpleHandlerWithLogging()\n" +
            "handler.on('test logging', [:])"
        };

        runConformTest(sources, "Seeing test logging with payload [:]\nReceived test logging with payload [:]");
    }

    @Test // Chaining behavior
    public void testTraits24() {
        String[] sources = {
            "Sample.groovy",
            "interface MessageHandler {\n" +
            "  void on(String message, Map payload)\n" +
            "}\n" +
            "trait DefaultHandler implements MessageHandler {\n" +
            "  void on(String message, Map payload) {\n" +
            "    println \"Received $message with payload $payload\"\n" +
            "  }\n" +
            "}\n" +
            "trait SayHandler implements MessageHandler {\n" +
            "  void on(String message, Map payload) {\n" +
            "    if (message.startsWith('say')) {\n" +
            "      println \"I say ${message - 'say'}!\"\n" +
            "    } else {\n" +
            "      super.on(message, payload)\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "trait LoggingHandler implements MessageHandler {\n" +
            "  void on(String message, Map payload) {\n" +
            "    println \"Seeing $message with payload $payload\"\n" +
            "    super.on(message, payload)\n" +
            "  }\n" +
            "}\n" +
            "class Handler implements DefaultHandler, SayHandler, LoggingHandler {}\n" +
            "def handler = new Handler()\n" +
            "handler.on('foo', [:])\n" +
            "handler.on('sayHello', [:])"
        };

        runConformTest(sources, "Seeing foo with payload [:]\nReceived foo with payload [:]\nSeeing sayHello with payload [:]\nI say Hello!");
    }

    @Test // Chaining behavior
    public void testTraits25() {
        String[] sources = {
            "Sample.groovy",
            "interface MessageHandler {\n" +
            "  void on(String message, Map payload)\n" +
            "}\n" +
            "trait DefaultHandler implements MessageHandler {\n" +
            "  void on(String message, Map payload) {\n" +
            "    println \"Received $message with payload $payload\"\n" +
            "  }\n" +
            "}\n" +
            "trait SayHandler implements MessageHandler {\n" +
            "  void on(String message, Map payload) {\n" +
            "    if (message.startsWith('say')) {\n" +
            "      println \"I say ${message - 'say'}!\"\n" +
            "    } else {\n" +
            "      super.on(message, payload)\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "trait LoggingHandler implements MessageHandler {\n" +
            "  void on(String message, Map payload) {\n" +
            "    println \"Seeing $message with payload $payload\"\n" +
            "    super.on(message, payload)\n" +
            "  }\n" +
            "}\n" +
            "class AlternateHandler implements DefaultHandler, LoggingHandler, SayHandler {}\n" +
            "def handler = new AlternateHandler()\n" +
            "handler.on('foo', [:])\n" +
            "handler.on('sayHello', [:])"
        };

        runConformTest(sources, "Seeing foo with payload [:]\nReceived foo with payload [:]\nI say Hello!");
    }

    @Test // Chaining behavior - Semantics of super inside a trait
    public void testTraits26() {
        String[] sources = {
            "Sample.groovy",
            "trait Filtering {\n" +
            "  StringBuilder append(String str) {\n" +
            "    def subst = str.replace('o', '')\n" +
            "    super.append(subst)\n" +
            "  }\n" +
            "  String toString() { super.toString() }\n" +
            "}\n" +
            "def sb = new StringBuilder().withTraits Filtering\n" +
            "sb.append('Groovy')\n" +
            "print sb.toString()"
        };

        runConformTest(sources, "Grvy");
    }

    @Test // SAM type coercion
    public void testTraits27() {
        String[] sources = {
            "Sample.groovy",
            "trait Greeter {\n" +
            "  String greet() { \"Hello $name\" }\n" +
            "  abstract String getName()\n" +
            "}\n" +
            "Greeter greeter = { 'Alice' }\n" +
            "print greeter.getName()"
        };

        runConformTest(sources, "Alice");
    }

    @Test // SAM type coercion
    public void testTraits28() {
        String[] sources = {
            "Sample.groovy",
            "trait Greeter {\n" +
            "  String greet() { \"Hello $name\" }\n" +
            "  abstract String getName()\n" +
            "}\n" +
            "void greet(Greeter g) { println g.greet() }\n" +
            "greet { 'Alice' }"
        };

        runConformTest(sources, "Hello Alice");
    }

    @Test // Differences with Java 8 default methods
    public void testTraits29() {
        String[] sources = {
            "Sample.groovy",
            "class Person {\n" +
            "  String name\n" +
            "}\n" +
            "trait Bob {\n" +
            "  String getName() { 'Bob' }\n" +
            "}\n" +
            "def p = new Person(name: 'Alice')\n" +
            "print p.name\n" +
            "def p2 = p as Bob\n" +
            "print p2.name"
        };

        runConformTest(sources, "AliceBob");
    }

    @Test // Differences with mixins
    public void testTraits30() {
        String[] sources = {
            "Sample.groovy",
            "class A { String methodFromA() { 'A' } }\n" +
            "class B { String methodFromB() { 'B' } }\n" +
            "A.metaClass.mixin B\n" +
            "def o = new A()\n" +
            "print o.methodFromA()\n" +
            "print o.methodFromB()\n" +
            "print(o instanceof A)\n" +
            "print(o instanceof B)"
        };

        runConformTest(sources, "ABtruefalse");
    }

    @Test // Static methods, properties and fields
    public void testTraits31() {
        String[] sources = {
            "Sample.groovy",
            "trait TestHelper {\n" +
            "  public static boolean called = false\n" +
            "  static void init() {\n" +
            "    called = true\n" +
            "  }\n" +
            "}\n" +
            "class Foo implements TestHelper {}\n" +
            "Foo.init()\n" +
            "print Foo.TestHelper__called"
        };

        runConformTest(sources, "true");
    }

    @Test // Static methods, properties and fields
    public void testTraits32() {
        String[] sources = {
            "Sample.groovy",
            "trait TestHelper {\n" +
            "  public static boolean called = false\n" +
            "  static void init() {\n" +
            "    called = true\n" +
            "  }\n" +
            "}\n" +
            "class Bar implements TestHelper {}\n" +
            "class Baz implements TestHelper {}\n" +
            "Bar.init()\n" +
            "print Bar.TestHelper__called\n" +
            "print Baz.TestHelper__called"
        };

        runConformTest(sources, "truefalse");
    }

    @Test // Inheritance of state gotchas
    public void testTraits33() {
        String[] sources = {
            "Sample.groovy",
            "trait IntCouple {\n" +
            "  int x = 1\n" +
            "  int y = 2\n" +
            "  int sum() { x+y }\n" +
            "}\n" +
            "class BaseElem implements IntCouple {\n" +
            "  int f() { sum() }\n" +
            "}\n" +
            "def base = new BaseElem()\n" +
            "print base.f()"
        };

        runConformTest(sources, "3");
    }

    @Test // Inheritance of state gotchas
    public void testTraits34() {
        String[] sources = {
            "Sample.groovy",
            "trait IntCouple {\n" +
            "  int x = 1\n" +
            "  int y = 2\n" +
            "  int sum() { x+y }\n" +
            "}\n" +
            "class Elem implements IntCouple {\n" +
            "  int x = 3\n" +
            "  int y = 4\n" +
            "  int f() { sum() }\n" +
            "}\n" +
            "def elem = new Elem()\n" +
            "print elem.f()"
        };

        runConformTest(sources, "3");
    }

    @Test // Inheritance of state gotchas
    public void testTraits35() {
        String[] sources = {
            "Sample.groovy",
            "trait IntCouple {\n" +
            "  int x = 1\n" +
            "  int y = 2\n" +
            "  int sum() { getX() + getY() }\n" +
            "}\n" +
            "class Elem implements IntCouple {\n" +
            "  int x = 3\n" +
            "  int y = 4\n" +
            "  int f() { sum() }\n" +
            "}\n" +
            "def elem = new Elem()\n" +
            "print elem.f()"
        };

        runConformTest(sources, "7");
    }

    @Test // Limitations - Prefix and postfix operations
    public void testTraits36() {
        String[] sources = {
            "Sample.groovy",
            "trait Counting {\n" +
            "  int x\n" +
            "  void inc() {\n" +
            "    x++\n" +
            "  }\n" +
            "  void dec() {\n" +
            "    --x\n" +
            "  }\n" +
            "}\n" +
            "class Counter implements Counting {}\n" +
            "def c = new Counter()\n" +
            "c.inc()"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Sample.groovy (at line 4)\n" +
            "\tx++\n" +
            "\t ^\n" +
            "Groovy:Postfix expressions on trait fields/properties  are not supported in traits. @ line 4, column 6.\n" +
            "----------\n" +
            "2. ERROR in Sample.groovy (at line 7)\n" +
            "\t--x\n" +
            "\t^\n" +
            "Groovy:Prefix expressions on trait fields/properties are not supported in traits. @ line 7, column 5.\n" +
            "----------\n");
    }

    @Test // Test @Trait annotation
    public void testTraits37() {
        String[] sources = {
            "Sample.groovy",
            "@groovy.transform.Trait\n" +
            "class MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MyClass implements MyTrait {\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()"
        };

        runConformTest(sources, "a");
    }

    @Test // Test @Trait annotation
    public void testTraits38() {
        String[] sources = {
            "Sample.groovy",
            "import groovy.transform.Trait\n" +
            "@Trait\n" +
            "class MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MyClass implements MyTrait {\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()"
        };

        runConformTest(sources, "a");
    }

    @Test // Test @Trait annotation
    public void testTraits39() {
        String[] sources = {
            "Sample.groovy",
            "import groovy.transform.*\n" +
            "@Trait\n" +
            "class MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MyClass implements MyTrait {\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()"
        };

        runConformTest(sources, "a");
    }

    @Test // Negative test for @Trait annotation
    public void testTraits40() {
        String[] sources = {
            "Sample.groovy",
            "@interface Trait{}\n" +
            "@Trait\n" +
            "class MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MyClass implements MyTrait {\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Sample.groovy (at line 6)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t      ^^^^^^^\n" +
            "Groovy:You are not allowed to implement the class 'MyTrait', use extends instead.\n" +
            "----------\n" +
            "2. ERROR in Sample.groovy (at line 6)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t                         ^^^^^^^^\n" +
            "The type MyTrait cannot be a superinterface of MyClass; a superinterface must be an interface\n" +
            "----------\n");
    }

    @Test // Negative test for @Trait annotation
    public void testTraits41() {
        String[] sources = {
            "Trait.groovy",
            "package a\n" +
            "@interface Trait {}",

            "Sample.groovy",
            "package b\n" +
            "import a.Trait\n" +
            "@Trait\n" +
            "class MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MyClass implements MyTrait {\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Sample.groovy (at line 7)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t      ^^^^^^^\n" +
            "Groovy:You are not allowed to implement the class 'b.MyTrait', use extends instead.\n" +
            "----------\n" +
            "2. ERROR in Sample.groovy (at line 7)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t                         ^^^^^^^^\n" +
            "The type MyTrait cannot be a superinterface of MyClass; a superinterface must be an interface\n" +
            "----------\n");
    }

    @Test // Negative test for @Trait annotation
    public void testTraits42() {
        String[] sources = {
            "Trait.groovy",
            "package a\n" +
            "@interface Trait {}",

            "Sample.groovy",
            "package b\n" +
            "@a.Trait\n" +
            "class MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MyClass implements MyTrait {\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Sample.groovy (at line 6)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t      ^^^^^^^\n" +
            "Groovy:You are not allowed to implement the class 'b.MyTrait', use extends instead.\n" +
            "----------\n" +
            "2. ERROR in Sample.groovy (at line 6)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t                         ^^^^^^^^\n" +
            "The type MyTrait cannot be a superinterface of MyClass; a superinterface must be an interface\n" +
            "----------\n");
    }

    @Test // Negative test for @Trait annotation
    public void testTraits43() {
        String[] sources = {
            "Trait.groovy",
            "package a\n" +
            "@interface Trait {}",

            "Sample.groovy",
            "package b\n" +
            "import a.Trait\n" +
            "import groovy.transform.*\n" +
            "@Trait\n" +
            "class MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MyClass implements MyTrait {\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Sample.groovy (at line 8)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t      ^^^^^^^\n" +
            "Groovy:You are not allowed to implement the class 'b.MyTrait', use extends instead.\n" +
            "----------\n" +
            "2. ERROR in Sample.groovy (at line 8)\n" +
            "\tclass MyClass implements MyTrait {\n" +
            "\t                         ^^^^^^^^\n" +
            "The type MyTrait cannot be a superinterface of MyClass; a superinterface must be an interface\n" +
            "----------\n");
    }

    @Test // Test protected method of superclass overriding by trait method - default package
    public void testTraits44() {
        String[] sources = {
            "Sample.groovy",
            "trait MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MySuperClass {\n" +
            "  protected def m() { 'b' }\n" +
            "}\n" +
            "class MyClass extends MySuperClass implements MyTrait {}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()"
        };

        runConformTest(sources, "a");
    }

    @Test // Test protected method of superclass overriding by trait method - the same package
    public void testTraits45() {
        String[] sources = {
            "Sample.groovy",
            "def myClass = new a.MyClass()\n" +
            "print myClass.m()",

            "Stuff.groovy",
            "package a\n" +
            "trait MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MySuperClass {\n" +
            "  protected def m() { 'b' }\n" +
            "}\n" +
            "class MyClass extends MySuperClass implements MyTrait {}"
        };

        runConformTest(sources, "a");
    }

    @Test // Test protected method of superclass overriding by trait method - different packages
    public void testTraits46() {
        String[] sources = {
            "Sample.groovy",
            "def myClass = new c.MyClass()\n" +
            "print myClass.m()",

            "MyTrait.groovy",
            "package a\n" +
            "trait MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}",

            "MySuperClass.groovy",
            "package b\n" +
            "class MySuperClass {\n" +
            "  protected def m() { 'b' }\n" +
            "}",

            "MyClass.groovy",
            "package c\n" +
            "class MyClass extends b.MySuperClass implements a.MyTrait {}"
        };

        runConformTest(sources, "a");
    }

    @Test // Test protected method of superclass overriding by trait method - different packages
    public void testTraits47() {
        String[] sources = {
            "Sample.groovy",
            "def myClass = new c.MyClass()\n" +
            "print myClass.m()",

            "MyTrait.groovy",
            "package a\n" +
            "trait MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}",

            "MySuperClass.groovy",
            "package b\n" +
            "class MySuperClass {\n" +
            "  protected def m() { 'b' }\n" +
            "}",

            "MyClass.groovy",
            "package c\n" +
            "import a.MyTrait\n" +
            "import b.MySuperClass\n" +
            "class MyClass extends MySuperClass implements MyTrait {}"
        };

        runConformTest(sources, "a");
    }

    @Test // Test protected method of superclass and traits method overriding by class
    public void testTraits48() {
        String[] sources = {
            "Sample.groovy",
            "trait MyTrait {\n" +
            "  def m() { 'a' }\n" +
            "}\n" +
            "class MySuperClass {\n" +
            "  protected def m() { 'b' }\n" +
            "}\n" +
            "class MyClass extends MySuperClass implements MyTrait {\n" +
            "  def m() { 'c' }\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()"
        };

        runConformTest(sources, "c");
    }

    @Test // Test protected method of superclass and traits method overriding by class - negative test
    public void testTraits49() {
        String[] sources = {
            "Sample.groovy",
            "trait MyTrait {\n" +
            "  abstract def m()\n" +
            "}\n" +
            "class MySuperClass {\n" +
            "  protected def m() { 'b' }\n" +
            "}\n" +
            "class MyClass extends MySuperClass implements MyTrait {}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()"
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Sample.groovy (at line 7)\n" +
            "\tclass MyClass extends MySuperClass implements MyTrait {}\n" +
            "\t      ^^^^^^^\n" +
            "The inherited method MySuperClass.m() cannot hide the public abstract method in MyTrait\n" +
            "----------\n");
    }

    @Test // Test protected method of superclass and traits method overriding by class - positive test
    public void testTraits50() {
        String[] sources = {
            "Sample.groovy",
            "trait MyTrait {\n" +
            "  abstract def m()\n" +
            "}\n" +
            "class MySuperClass {\n" +
            "  protected def m() { 'b' }\n" +
            "}\n" +
            "class MyClass extends MySuperClass implements MyTrait {\n" +
            "  def m() { 'c' }\n" +
            "}\n" +
            "def myClass = new MyClass()\n" +
            "print myClass.m()"
        };

        runConformTest(sources, "c");
    }

    @Test @Ignore("Java classes should be able to implement traits as well -- this doesn't work in groovyc either as of Groovy 2.4.8")
    public void testTraitsInteroperability() {
        String[] sources = {
            "Sample.java",
            "public class Sample implements Valuable {\n" +
            "  public String showMeTheMoney() {\n" +
            "    return \"$\" + getValue() + \"$\";\n" +
            "  }\n" +
            "}",

            "Valuable.groovy",
            "trait Valuable {\n" +
            "  String value\n" +
            "}"
        };

        runConformTest(sources);
    }
}
