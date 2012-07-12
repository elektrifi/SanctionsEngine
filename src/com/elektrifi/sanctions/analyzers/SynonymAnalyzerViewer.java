package com.elektrifi.sanctions.analyzers;

import com.elektrifi.util.AnalyzerUtils;
import java.io.IOException;

// From chapter 4
public class SynonymAnalyzerViewer {

  public static void main(String[] args) throws IOException {

    SynonymEngine engine = new SanctionsSynonymEngine();

    AnalyzerUtils.displayTokensWithPositions(
      new SynonymAnalyzer(engine),
      "robert gabriel mugabe");
  }
}
