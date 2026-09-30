package com.generated.qualityTrace.services;

import com.generated.qualityTrace.constructors.ProductBatchDtoFactory;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 产品批次服务。
 */
@Service
public class ProductBatchService {

  private final ProductBatchRepository repo;

  public ProductBatchService(ProductBatchRepository repo) {
    this.repo = repo;
  }

  public List<Map<String, Object>> list() {
    List<Map<String, Object>> out = new ArrayList<>();
    for (ProductBatch b : repo.findAll()) {
      out.add(ProductBatchDtoFactory.response(b));
    }
    return out;
  }
}
