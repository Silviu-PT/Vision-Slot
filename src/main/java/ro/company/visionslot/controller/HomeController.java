package ro.company.visionslot.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "application", "VisionSlot",
                "status", "running",
                "swagger", "/swagger-ui.html",
                "h2Console", "/h2-console"
        );
    }
}
