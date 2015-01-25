package com.maximgalushka.classifier.spellchecker;

import org.languagetool.JLanguageTool;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.RuleMatch;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

/**
 * @author Maxim Galushka
 */
public class SpellChecker {

  public static void main(String[] args)
  throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool langTool = new MultiThreadedJLanguageTool(
      new BritishEnglish(),
      null
    );
    langTool.activateDefaultPatternRules();
    langTool.activateDefaultFalseFriendRules();

    List<RuleMatch> matches = langTool.check(
      "A sentence " +
        "with a error in the Hitchhiker's Guide tot he Galaxy"
    );

    for (RuleMatch match : matches) {
      System.out.println(
        "Potential error at line " +
          match.getLine() + ", column " +
          match.getColumn() + ": " + match.getMessage()
      );
      System.out.println(
        "Suggested correction: " +
          match.getSuggestedReplacements()
      );
    }
  }
}
