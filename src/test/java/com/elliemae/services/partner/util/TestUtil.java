package com.synkrato.services.partner.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Map;

public class TestUtil {

  private static final String EMPTY_STR = "";

  /**
   * Get Root path of the current thread
   *
   * @return
   */
  public static String getRootPath() {
    return Thread.currentThread().getContextClassLoader().getResource(EMPTY_STR).getPath();
  }

  /**
   * Build Map from json file
   *
   * @param jsonFilePath Json file path
   * @return convert json file as Map
   */
  public static Map convertToMap(String jsonFilePath) throws Exception {
    return new ObjectMapper().readValue(new File(jsonFilePath), Map.class);
  }
}
