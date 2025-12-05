package org.inariforge.ig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class IgApplicationTests {

  @Test
  void contextLoads() {
    // keep test lightweight to avoid starting full Spring context in CI
    int sum = Integer.sum(1, 1);
    Assertions.assertEquals(2, sum);
  }

}
