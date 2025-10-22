package ca.uqac.lif.piglet;
import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

public class Probe {
  public static void main(String[] args) {
    ParserConfiguration cfg = new ParserConfiguration()
      .setStoreTokens(true)
      .setLexicalPreservationEnabled(true)
      .setAttributeComments(true);

    StaticJavaParser.setConfiguration(cfg);

    String src = ""
      + "class A {\n"
      + "  void m() {\n"
      + "    int x = 0; // trailing\n"
      + "    // leading\n"
      + "    x++;\n"
      + "  }\n"
      + "}\n";

    CompilationUnit cu = StaticJavaParser.parse(src);
    LexicalPreservingPrinter.setup(cu);

    System.out.println("TokenRange present? " + cu.getTokenRange().isPresent());
    System.out.println("All comments: " + cu.getAllContainedComments().size());
    System.out.println("Orphans: " + cu.getOrphanComments().size());
    cu.walk(n -> n.getComment().ifPresent(c ->
      System.out.println("Attached → " + n.getClass().getSimpleName() + " :: " + c)));
  }
}
