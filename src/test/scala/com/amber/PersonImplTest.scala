package com.amber

import junit.framework.TestCase

class PersonImplTest extends TestCase {

  // FIXME: use a Scala test framework to run a test
  def testCanCreatePersonImpl(): Unit = {
    def person: Person = new PersonImpl(List("bob", "smith"))
    person
  }

}
