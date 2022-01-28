package java8;
import java.util.Optional;

public class Address {
  public String number;
  public String street;
  public City city;

  public Address(City city) {
    this.city = city;
  }
  
  public Optional<City> getCity() {
    return Optional.ofNullable(city);
  }
}
 