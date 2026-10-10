package com.irhsm.healthCampService.controller;

import com.irhsm.healthCampService.entity.FollowUp;
import com.irhsm.healthCampService.entity.FollowUpStatus;
import com.irhsm.healthCampService.service.FollowUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/camps/followups")        
@RequiredArgsConstructor
public class FollowUpPageController {

    private final FollowUpService followUpService;

    // GET /camps/followups?status=PENDING -followups list
    @GetMapping
    public String list(@RequestParam(required = false) FollowUpStatus status, Model model) {
        List<FollowUp> list = followUpService.all().stream()
                .filter(f -> status == null || f.getStatus() == status)   // Optional status filter
                .toList();
        model.addAttribute("followUps", list);
        model.addAttribute("statuses", FollowUpStatus.values());
        model.addAttribute("status", status);
        model.addAttribute("today", LocalDate.now());    // To highlight overdue rows
        return "camps/followups";
    }

    // POST /camps/followups/3/status -close or update a followup from the page
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam FollowUpStatus status,
                               @RequestParam(required = false) String outcome,
                               RedirectAttributes flash) {
        followUpService.updateStatus(id, status, outcome);                // Same method the dashboard API uses
        flash.addFlashAttribute("message", "Follow-up " + id + " marked " + status);
        return "redirect:/camps/followups";
    }
}