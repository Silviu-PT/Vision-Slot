package ro.company.visionslot.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.company.visionslot.dto.SchedulingConfigurationRequest;
import ro.company.visionslot.dto.SchedulingConfigurationResponse;
import ro.company.visionslot.service.SchedulingConfigurationService;

@RestController
@RequestMapping("/api/admin/configuration")
public class AdminSchedulingConfigurationController {

    private final SchedulingConfigurationService schedulingConfigurationService;

    public AdminSchedulingConfigurationController(SchedulingConfigurationService schedulingConfigurationService) {
        this.schedulingConfigurationService = schedulingConfigurationService;
    }

    @GetMapping
    public SchedulingConfigurationResponse getActiveConfiguration() {
        return schedulingConfigurationService.getActiveConfiguration();
    }

    @PutMapping
    public SchedulingConfigurationResponse saveConfiguration(@Valid @RequestBody SchedulingConfigurationRequest request) {
        return schedulingConfigurationService.saveConfiguration(request);
    }
}
