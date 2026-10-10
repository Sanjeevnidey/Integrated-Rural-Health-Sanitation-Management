package com.irhsm.healthCampService.service;

import com.irhsm.healthCampService.entity.CampStatus;
import com.irhsm.healthCampService.entity.CampType;
import com.irhsm.healthCampService.entity.HealthCamp;
import com.irhsm.healthCampService.service.CampService;
import jakarta.validation.Valid;                          // Triggers the @NotBlank/@NotNull checks
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;        
import org.springframework.ui.Model;                      
import org.springframework.validation.BindingResult;     
import org.springframework.web.bind.annotation.*;

@Controller                               
@RequestMapping("/camps")                 
@RequiredArgsConstructor
public class CampPageController {

    private final CampService campService;

    @GetMapping
    public String list(@RequestParam(required = false) String q,            
                       @RequestParam(required = false) String village,      
                       @RequestParam(required = false) CampStatus status,   
                       Model model) {
        model.addAttribute("camps", campService.search(q, village, status)); 
        model.addAttribute("villages", campService.villages());             
        model.addAttribute("statuses", CampStatus.values());                
        model.addAttribute("q", q);                                         
        model.addAttribute("village", village);                             
        model.addAttribute("status", status);                              
        return "camps/list";                                                
    }

    // GET /camps/new -empty form
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("camp", new HealthCamp());      
        addFormOptions(model);                             
        return "camps/form";                                
    }

    // GET /camps/5/edit -same form, pre-filled
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("camp", campService.get(id));    // Existing camp
        addFormOptions(model);
        return "camps/form";
    }

    // POST /camps/save -alidate, then save or show errors
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

    private void addFormOptions(Model model) {
        model.addAttribute("types", CampType.values());     // GENERAL, NCD, EYE
        model.addAttribute("statuses", CampStatus.values());// PLANNED, ONGOING 
    }
}    

