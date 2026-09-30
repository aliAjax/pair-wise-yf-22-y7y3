package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constructors.DefectRecordDtoFactory;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 不良记录服务。
 */
@Service
public class DefectRecordService {

  private final DefectRecordRepository repo;

  public DefectRecordService(DefectRecordRepository repo) {
    this.repo = repo;
  }

  public List<Map<String, Object>> list() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (DefectRecord d : repo.findAll()) {
      out.add(DefectRecordDtoFactory.response(d));
    }
    return out;
  }
}
