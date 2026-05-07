package ro.proiectcolectiv.ophthalmology.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ro.proiectcolectiv.ophthalmology.dto.CampaignResponse;
import ro.proiectcolectiv.ophthalmology.dto.CreateCampaignRequest;
import ro.proiectcolectiv.ophthalmology.service.CampaignService;

@RestController
@RequestMapping("/api/admin/campaigns")
public class AdminCampaignController {

    private final CampaignService campaignService;

    public AdminCampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping
    public CampaignResponse createCampaign(@Valid @RequestBody CreateCampaignRequest request) {
        return campaignService.createCampaign(request);
    }

    @GetMapping
    public List<CampaignResponse> findAll() {
        return campaignService.findAll();
    }

    @PutMapping("/{campaignId}/publish")
    public CampaignResponse publish(@PathVariable long campaignId) {
        return campaignService.publish(campaignId);
    }

    @PutMapping("/{campaignId}/close")
    public CampaignResponse close(@PathVariable long campaignId) {
        return campaignService.close(campaignId);
    }
}
