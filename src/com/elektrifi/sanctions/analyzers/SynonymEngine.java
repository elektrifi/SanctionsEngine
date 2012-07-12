package com.elektrifi.sanctions.analyzers;

import java.io.IOException;

public interface SynonymEngine {
  String[] getSynonyms(String s) throws IOException;
}
