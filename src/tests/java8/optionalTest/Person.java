package java8;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Person {

  public enum Sex {
    MALE, FEMALE
  }

  private Address address;
  
  private String name;

  private LocalDate birthday;

  private Sex gender;

  private String emailAddress;

  private boolean allowEmpty = false;

  public static void filter(List<Person> roster, Predicate<Person> tester) {
    roster.removeIf(tester);
  }

  public Person() {
  }

  public Person(Address address) {
    this.address = address;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDate getBirthday() {
    return birthday;
  }

  public void setBirthday(LocalDate birthday) {
    this.birthday = birthday;
  }

  public Sex getGender() {
    return gender;
  }

  public void setGender(Sex gender) {
    this.gender = gender;
  }

  public Optional<String> getEmailAddress() {
    return allowEmpty ? Optional.ofNullable(emailAddress) : Optional.ofNullable(emailAddress).filter(s -> !s.isEmpty());
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public void setAllowEmpty(boolean allowEmpty) {
    this.allowEmpty = allowEmpty;
  }

  public Optional<Address> getAddress() {
    return Optional.ofNullable(address);
  }
}