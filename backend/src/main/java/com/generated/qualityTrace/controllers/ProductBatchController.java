package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generated.qualityTrace.constructors.ProductBatchDtoFactory;
import com.generated.qualityTrace.services.ProductBatchService;

@RestController
@RequestMapping("/api/batches")
public class ProductBatchController {

  private final ProductBatchService service;

  public ProductBatchController(ProductBatchService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list().stream().map(ProductBatchDtoFactory::create).toList();
  }
}
