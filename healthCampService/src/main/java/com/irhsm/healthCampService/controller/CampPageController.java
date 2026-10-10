package com.irhsm.healthCampService.controller;

import com.irhsm.healthCampService.entity.*;                          // HealthCamp, Screening, all enums
import com.irhsm.healthCampService.service.CampService;
import com.irhsm.healthCampService.service.ScreeningService;
import jakarta.validation.Valid;                                      
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;                    //Returns HTML pages, not JSON
import org.springframework.ui.Model;                                  
import org.springframework.validation.BindingResult;                 
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // One-time messages after a redirect

import com.irhsm.healthCampService.client.ResidentClient;
import com.irhsm.healthCampService.dto.ResidentDto;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Controller                                   
@RequestMapping("/camps")                     // All URLs here start with /camps
@RequiredArgsConstructor                      // Injects the two services below through the constructor
public class CampPageController {

    private final CampService campService;            //Camps, staff, beneficiaries
    private final ScreeningService screeningService;  // Screenings + the follow-up rule
    private final ResidentClient residentClient;      

    //CAMP LIST
    // GET /camps?q=...&village=...&status=...
    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String village,
                       @RequestParam(required = false) CampStatus status,
                       Model model) {
        model.addAttribute("camps", campService.search(q, village, status)); // Filtered camps
        model.addAttribute("villages", campService.villages());             // Village dropdown
        model.addAttribute("statuses", CampStatus.values());                // Status dropdown
        model.addAttribute("q", q);                                         // Keep search text
        model.addAttribute("village", village);                             // Keep selected village
        model.addAttribute("status", status);                               // Keep selected status
        return "camps/list";                                                // templates/camps/list.html
    }

    //CAMP FORM

    // empty form
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("camp", new HealthCamp());
        addFormOptions(model);
        return "camps/form";
    }

    // GET /camps/5/edit - form filled with an existing camp
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("camp", campService.get(id));
        addFormOptions(model);
        return "camps/form";
    }

    // POST/camps/save - validate then save or show errors
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("camp") HealthCamp camp,
                       BindingResult result,          
                       Model model) {
        if (result.hasErrors()) {                     
            addFormOptions(model);
            return "camps/form";                      
        }
        HealthCamp saved = campService.save(camp);
        return "redirect:/camps/" + saved.getId();   
    }

    //CAMP DETAIL

    // GET /camps/5 -> camp detail page
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("camp", campService.get(id));
        model.addAttribute("staff", campService.staff(id));
        model.addAttribute("beneficiaries", campService.beneficiaries(id));
        model.addAttribute("screenings", campService.screenings(id));
        model.addAttribute("roles", StaffRole.values());              // Dropdown in the staff form
        return "camps/detail";
    }

    // POST /camps/5/staff - assign a health worker
    @PostMapping("/{id}/staff")
    public String assignStaff(@PathVariable Long id,
                              @RequestParam Long workerId,
                              @RequestParam String workerName,
                              @RequestParam StaffRole role,
                              @RequestParam(required = false) String duty,
                              RedirectAttributes flash) {
        campService.assignStaff(id, workerId, workerName, role, duty);
        flash.addFlashAttribute("message", workerName + " assigned as " + role); //shown once
        return "redirect:/camps/" + id;
    }

    // POST /camps/5/beneficiaries -> look up the resident in the Resident service, then register
    @PostMapping("/{id}/beneficiaries")
    public String registerBeneficiary(@PathVariable Long id,
                                      @RequestParam Long residentId,
                                      @RequestParam(required = false) String residentName, 
                                      @RequestParam(required = false) String village,      
                                      @RequestParam(required = false) Integer age,
                                      @RequestParam(required = false) String gender,
                                      RedirectAttributes flash) {
        String source;  // For the message, where data came from
        try {
            ResidentDto r = residentClient.getResident(residentId); // REST call to port 8081
            residentName = r.name();                                  
            village = r.village();
            age = r.age();
            gender = r.gender();
            source = "details from Resident service";
        } catch (HttpClientErrorException.NotFound e) { // 404: no such resident
            flash.addFlashAttribute("error", "Resident " + residentId + " not found in the Resident service");
            return "redirect:/camps/" + id;
        } catch (RestClientException e) {  // Service down or unreachable
            if (residentName == null || residentName.isBlank()) { 
                flash.addFlashAttribute("error",
                        "Resident service (port 8081) is unavailable. Type the name and village to register manually.");
                return "redirect:/camps/" + id;
            }
            source = "entered manually, Resident service unavailable";
        }

        try {
            campService.registerBeneficiary(id, residentId, residentName, village, age, gender);
            flash.addFlashAttribute("message", residentName + " registered (" + source + ")");
        } catch (IllegalStateException e) {                              // Already registered for this camp
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/camps/" + id;
    }

    //SCREENING
    // GET/camps/beneficiaries/7/screen - screening form
    @GetMapping("/beneficiaries/{bid}/screen")
    public String screenForm(@PathVariable Long bid, Model model) {
        model.addAttribute("beneficiary", campService.getBeneficiary(bid));
        model.addAttribute("screening", new Screening());
        addScreeningOptions(model);
        return "camps/screen-form";
    }

    // POST/camps/beneficiaries/7/screen - save + automatic folloup
    @PostMapping("/beneficiaries/{bid}/screen")
    public String saveScreening(@PathVariable Long bid,
                                @Valid @ModelAttribute("screening") Screening screening,
                                BindingResult result,
                                Model model,
                                RedirectAttributes flash) {
        CampBeneficiary beneficiary = campService.getBeneficiary(bid);
        if (result.hasErrors()) {                                     // Missing test, reading or result
            model.addAttribute("beneficiary", beneficiary);
            addScreeningOptions(model);
            return "camps/screen-form";
        }
        Screening saved = screeningService.record(bid, screening);    // Applies the followup rule
        String msg = "Screening saved for " + beneficiary.getResidentName();
        if (saved.getResult() != ScreeningResult.NORMAL) {
            msg += ". Follow-up created automatically (" + saved.getResult() + ")";
        }
        flash.addFlashAttribute("message", msg);
        return "redirect:/camps/" + beneficiary.getCamp().getId();
    }

    //HELPERS

    private void addFormOptions(Model model) {
        model.addAttribute("types", CampType.values());
        model.addAttribute("statuses", CampStatus.values());
    }

    private void addScreeningOptions(Model model) {
        model.addAttribute("types", ScreeningType.values());
        model.addAttribute("results", ScreeningResult.values());
    }
}