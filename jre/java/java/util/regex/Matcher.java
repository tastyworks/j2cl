package java.util.regex;

public final class Matcher implements MatcherResult {

  public static String quoteReplacement(String s) {
    throw new UnsupportedOperationException();
  }

  private final Pattern pattern;

  private final CharSequence input;

  private String[] matchResult;

  private int offset = 0;

  Matcher(Pattern pattern, CharSequence input) {
    this.pattern = pattern;
    this.input = input;
    this.matchResult = pattern.exec(input.toString());
  }

  public Pattern pattern() {
    return pattern;
  }

  public MatcherResult toMatcherResult() {
    return this;
  }

  public Matcher reset() {
    return this;
  }

  public Matcher reset(CharSequence string) {
    return this;
  }

  @Override
  public int start() {
    return start(0);
  }

  @Override
  public int start(int group) {
    String groupMatch = group(group);
    return input.toString().indexOf(groupMatch);
  }

  @Override
  public int end() {
    return end(0);
  }

  @Override
  public int end(int group) {
    String groupMatch = group(group);
    int startIndex = input.toString().indexOf(groupMatch);
    return startIndex + groupMatch.length();
  }

  @Override
  public String group() {
    return group(0);
  }

  @Override
  public String group(int group) {
    return matchResult[group];
  }

  public String group(String name) {
    throw new UnsupportedOperationException();
  }

  public int groupCount() {
    return null == matchResult ? 0 : matchResult.length - 1;
  }

  public boolean matches() {
    return this.pattern.matches(this.input);
  }

  public boolean find() {
    return this.find(this.offset);
  }

  public boolean find(int offset) {
    CharSequence targetSequence = this.input.subSequence(offset, this.input.length());
    this.matchResult = this.pattern.exec(targetSequence);
    boolean isNotDone = null != matchResult;
    if (isNotDone) {
      this.offset = end();
    }
    return isNotDone;
  }

  public boolean lookingAt() {
    return true;
  }

  public Matcher appendReplacement(StringBuffer sb, String replacement) {
    throw new UnsupportedOperationException();
  }

  public StringBuffer appendTail(StringBuffer sb) {
    throw new UnsupportedOperationException();
  }

  public String replaceAll(String replacement) {
    return input.toString().replaceAll(pattern.pattern(), replacement);
  }

  public String replaceFirst(String replacement) {
    return input.toString().replaceFirst(pattern.pattern(), replacement);
  }

  public Matcher region(int start, int end) {
    throw new UnsupportedOperationException();
  }

  public int regionStart() {
    throw new UnsupportedOperationException();
  }

  public int regionEnd() {
    throw new UnsupportedOperationException();
  }

  public boolean hasTransparentBounds() {
    return false;
  }

  public Matcher useTransparentBounds(boolean b) {
    throw new UnsupportedOperationException();
  }

  public boolean hasAnchoringBounds() {
    return true;
  }

  public Matcher useAnchoringBounds(boolean b) {
    throw new UnsupportedOperationException();
  }

  public boolean hitEnd() {
    return true;
  }

  public boolean requireEnd() {
    throw new UnsupportedOperationException();
  }
}