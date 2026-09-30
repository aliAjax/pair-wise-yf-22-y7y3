package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constructors.QualityInspectionDtoFactory;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 质量检验服务。
 */
@Service
public class QualityInspectionService {

  private final QualityInspectionRepository repo;

  public QualityInspectionService(QualityInspectionRepository repo) {
    this.repo = repo;
  }

  public List<Map<String, Object>> list() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (QualityInspection ins : repo.findAll()) {
      out.add(QualityInspectionDtoFactory.response(ins));
    }
    return out;
  }
}
