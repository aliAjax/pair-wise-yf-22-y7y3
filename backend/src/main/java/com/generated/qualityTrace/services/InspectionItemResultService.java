package com.generated.qualityTrace.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;

@Service
public class InspectionItemResultService {

  private final InspectionItemResultRepository repo;

  public InspectionItemResultService(InspectionItemResultRepository repo) {
    this.repo = repo;
  }

  public List<InspectionItemResult> list() {
    return repo.findAll();
  }
}
