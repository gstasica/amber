package com.amber

import _root_.junit.framework.TestCase
import com.amber.Person
import com.amber.PersonImpl

class PersonImplTest extends TestCase {

  // FIXME: use a Scala test framework to run a test
  def testCanCreatePersonImpl(): Unit = {
    def person: Person = new PersonImpl(List("bob", "smith"))
    person
  }

}
