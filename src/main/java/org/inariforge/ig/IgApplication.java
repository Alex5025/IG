package org.inariforge.ig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IgApplication {

  public static void main(String[] args) {
    SpringApplication.run(IgApplication.class, args);
  }

}
