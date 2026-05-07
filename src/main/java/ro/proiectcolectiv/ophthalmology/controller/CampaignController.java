package ro.proiectcolectiv.ophthalmology.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ro.proiectcolectiv.ophthalmology.dto.CampaignResponse;
import ro.proiectcolectiv.ophthalmology.service.CampaignService;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping("/current")
    public CampaignResponse findCurrentOpenCampaign() {
        return campaignService.findCurrentOpenCampaign();
    }
}
