package nl.craftsmen.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private HelloClient helloClient;

    @GetMapping("/hello")
    public String getHello() {
        return helloClient.getHello();
    }

    @GetMapping("/backendA")
    public String getHello2() {
        throw new RuntimeException();
    }

}
