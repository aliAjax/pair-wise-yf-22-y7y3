package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constructors.InspectionItemResultDtoFactory;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 检验项结果服务。
 */
@Service
public class InspectionItemResultService {

  private final InspectionItemResultRepository repo;

  public InspectionItemResultService(InspectionItemResultRepository repo) {
    this.repo = repo;
  }

  public List<Map<String, Object>> list() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (InspectionItemResult item : repo.findAll()) {
      out.add(InspectionItemResultDtoFactory.response(item));
    }
    return out;
  }
}
