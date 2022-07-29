package com.ismaelgf.awsmigrator.service;

import com.ismaelgf.awsmigrator.service.model.AwsImportType;
import org.springframework.boot.ApplicationArguments;

public interface AwsImportService {

  AwsImportType getType();

  void importService(ApplicationArguments args);
}
