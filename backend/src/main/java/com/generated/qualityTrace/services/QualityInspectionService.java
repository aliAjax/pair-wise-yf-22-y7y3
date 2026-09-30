package com.generated.qualityTrace.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;

@Service
public class QualityInspectionService {

  private final QualityInspectionRepository repo;

  public QualityInspectionService(QualityInspectionRepository repo) {
    this.repo = repo;
  }

  public List<QualityInspection> list() {
    return repo.findAll();
  }
}
