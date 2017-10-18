package allow.simulator.util;


public class Triple<V, E, T> {
  public V first;
  public E second;
  public T third;
  
  public Triple(V first, E second, T third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }
  
  public String toString() {
    return "[Triple " + first.toString() + ", " + second.toString() + ", " + third.toString() + "]";
  }
}
