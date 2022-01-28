package java8;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Assert;
import optionalTest.Address;
import optionalTest.City;
import optionalTest.Person;
import org.junit.Test;

public class PersonTest {
  private static final String PERSON_NAME = "A. Name";
  private static final String CITY_NAME = "Young America";
  private static final String EMAIL_ADDR = "foo@bar.com";
  Person person1 = new Person();
  Person person2 = new Person();
  Person person3 = new Person();
  @Test
  public void testOptionalNotPresent() {
    Optional<String> email1 = person1.getEmailAddress();
    email1.ifPresent(value -> Assert.fail("Should not be here"));
    Assert.assertEquals("Optional.empty", email1.toString());
  }
  @Test
  public void testOptionalEmptyString_NotAllowed() {
    final String emailAddr = "";
    person1.setEmailAddress(emailAddr);
    Optional<String> email1 = person1.getEmailAddress();
    email1.ifPresent(value -> Assert.fail("Should not be here"));
    Assert.assertEquals("Optional.empty", email1.toString());
  }
  @Test
  public void testOptionalEmptyString_Allowed() {
    final String emailAddr = "";
    person1.setEmailAddress(emailAddr);
    person1.setAllowEmpty(true);
    Optional<String> email1 = person1.getEmailAddress();
    email1.ifPresent(value -> Assert.assertEquals(emailAddr, value));
    Assert.assertEquals(String.format("Optional[%s]", emailAddr), email1.toString());
  }

  @Test
  public void testOptionalPresent() {
    person1.setEmailAddress(EMAIL_ADDR);
    Optional<String> email1 = person1.getEmailAddress();
    email1.ifPresent(value -> Assert.assertEquals(EMAIL_ADDR, value));
    Assert.assertEquals(String.format("Optional[%s]", EMAIL_ADDR), email1.toString());
  }
  @SuppressWarnings("serial")
  public static class OptMap<T, U> extends HashMap<T, U> {
    public Optional<U> find(T key) {
      return Optional.ofNullable(super.get(key));
    }
  }

  public static void process(City city) {
    Assert.assertEquals(CITY_NAME, city.name);
  }

  @Test
  public void testOptional_Monad_NotFound() {
    OptMap<String, Person> personMap = new OptMap<>();
    personMap.find(PERSON_NAME)
      .flatMap(Person::getAddress)
      .flatMap(Address::getCity)
      .ifPresent(value -> Assert.fail("Should not be here"));
  }

  @Test
  public void testOptional_Monad_Found() {
    OptMap<String, Person> personMap = new OptMap<>();
    Person person = new Person(new Address(new City(CITY_NAME)));

    personMap.put(PERSON_NAME, person);

    personMap.find(PERSON_NAME)
      .flatMap(Person::getAddress)
      .flatMap(Address::getCity)
      .ifPresent(PersonTest::process);
  }

  @Test
  public void testFilter() {
    Assert.assertNotNull(person1);
    person1.setGender(Person.Sex.MALE);
    person2.setBirthday(LocalDate.now());

    List<Person> roster = new ArrayList<>();
    roster.add(person1);
    roster.add(person2);
    roster.add(person3);

    Person.filter(roster, p -> p.getGender() == Person.Sex.MALE);
    Assert.assertEquals(2, roster.size());
    Assert.assertFalse(roster.contains(person1));
    Assert.assertTrue(roster.contains(person2));
    Assert.assertTrue(roster.contains(person3));

    Person.filter(roster, p -> p.getBirthday() != null);
    Assert.assertEquals(1, roster.size());
    Assert.assertFalse(roster.contains(person1));
    Assert.assertFalse(roster.contains(person2));
    Assert.assertTrue(roster.contains(person3));
  }
}