package com.itemstorage.controller;

import com.itemstorage.service.PatientExcelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller
@RequestMapping("/discharge")
public class DischargeController {

    private final PatientExcelService patientExcelService;

    public DischargeController(PatientExcelService patientExcelService) {
        this.patientExcelService = patientExcelService;
    }

    @GetMapping
    public String dischargePage() {
        return "discharge";
    }

    @PostMapping("/import")
    public String importDischarged(@RequestParam("file") MultipartFile file, Model model) {
        Map<String, Object> result = patientExcelService.dischargePatients(file);
        model.addAttribute("result", result);
        return "discharge";
    }
}