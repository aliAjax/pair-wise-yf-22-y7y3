package com.generated.qualityTrace.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.repositories.DefectRecordRepository;

@Service
public class DefectRecordService {

  private final DefectRecordRepository repo;

  public DefectRecordService(DefectRecordRepository repo) {
    this.repo = repo;
  }

  public List<DefectRecord> list() {
    return repo.findAll();
  }
}
