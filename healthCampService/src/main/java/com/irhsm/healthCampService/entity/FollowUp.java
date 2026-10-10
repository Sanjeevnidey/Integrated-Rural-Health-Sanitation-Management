package com.irhsm.healthCampService.entity;

import jakarta.persistence.*;                             
import lombok.AllArgsConstructor;                        
import lombok.Getter;                                     
import lombok.NoArgsConstructor;                         
import lombok.Setter;                                    

import java.time.LocalDate;   

@Entity
@Table(name = "follow_up")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 

public class FollowUp {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;  // Column: id; the dashboard's PUT /{id}/status uses this id

    @OneToOne  // Exactly ONE follow-up per screening

    @JoinColumn (name="screening_id", nullable = false, unique = true) // Required, and unique so a screening can't get two follow-ups
    private  Screening screening;
    
    private Long residentId;
    private String residentName;
    private String village;

    @Enumerated(EnumType.STRING)
    private FollowUpAction action;  // RECHECK, REFER_TO_CENTRE, HOME_VISIT

    @Enumerated(EnumType.STRING)
    private Priority priority;   // HIGH, MEDIUM, LOW

    private LocalDate dueDate;    // When the follow-up must be done

    @Enumerated(EnumType.STRING)
    private FollowUpStatus status = FollowUpStatus.PENDING;

    private String outcome;
    private LocalDate closedOn;
}
