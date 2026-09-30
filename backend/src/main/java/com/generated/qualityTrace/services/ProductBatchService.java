package com.generated.qualityTrace.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.repositories.ProductBatchRepository;

@Service
public class ProductBatchService {

  private final ProductBatchRepository repo;

  public ProductBatchService(ProductBatchRepository repo) {
    this.repo = repo;
  }

  public List<ProductBatch> list() {
    return repo.findAll();
  }
}
