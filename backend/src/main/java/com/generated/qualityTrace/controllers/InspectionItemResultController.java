package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generated.qualityTrace.constructors.InspectionItemResultDtoFactory;
import com.generated.qualityTrace.services.InspectionItemResultService;

@RestController
@RequestMapping("/api/inspection-item-results")
public class InspectionItemResultController {

  private final InspectionItemResultService service;

  public InspectionItemResultController(InspectionItemResultService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list().stream().map(InspectionItemResultDtoFactory::create).toList();
  }
}
