import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
public class SkipGram {
  public static void main( String args[] ) {
    SkipGramDetector Detector = new SkipGramDetector();
    Scanner reader = new Scanner(System.in);
    System.out.println("Enter a potential skip gram");
    String inputSkipGram = reader.nextLine();
    System.out.println("Enter a pattern");
    String inputPattern = reader.nextLine();
    boolean result = Detector.checkForSkipGram(inputPattern, inputSkipGram);
    if(result)
      System.out.println("Match successful");
    else
      System.out.println("match unsuccessful");
  }//end MAIN
}//end SkipGram class
class SkipGramDetector {
  ArrayList<Letter> haystackChars;
  String pattern;
  String haystack;

  public SkipGramDetector() {
    haystackChars = new ArrayList<Letter>();
    pattern = "";
    haystack = "";
  }
  String getPatternForChars(ArrayList<Letter> inputChars) {
    int totalIterations = 0;
    String reversePattern = "";
    ArrayList<Integer> recordedPositions = new ArrayList<Integer>();
    for(Letter currentLetter : inputChars)
      totalIterations += currentLetter.occurancePositions.size();
    for(int inc = 0; inc < totalIterations; inc++) {
      int lowestPos = -1;
      String patternSymbol = "";
      int letterCount = 0;
      for(Letter letterInc : inputChars) {
        letterCount++;
        for(Integer occurancePos : letterInc.occurancePositions) {
          boolean hasBeenRecorded = false;
          for(Integer recordInc : recordedPositions) {
            if(recordInc == occurancePos) {
              hasBeenRecorded = true;
            }
          }
          if(!hasBeenRecorded) {
            if(lowestPos == -1 || occurancePos < lowestPos) {
              lowestPos = occurancePos;
              patternSymbol = String.valueOf(letterCount);
            }
          }
        }
      }//loop through index chars
      if(lowestPos != -1) {
        recordedPositions.add(lowestPos);
        if(recordedPositions.size() > 1) {
          int currentPos = recordedPositions.get(recordedPositions.size() - 1);
          int prevPos = recordedPositions.get(recordedPositions.size() - 2);
          if(currentPos - prevPos > 1)
            patternSymbol = "*" + patternSymbol;
        }
        reversePattern = reversePattern + patternSymbol;
      }
    }
    return reversePattern;
  }
  void analyzeLetters() {
    for(int haystackInc = 0; haystackInc < this.haystack.length(); haystackInc++) {
      char currentChar = this.haystack.charAt(haystackInc);
      boolean hasBeenRecorded = false;
      for(Letter recordedLetter : haystackChars) {
        if(recordedLetter.name.equals(String.valueOf(currentChar))) {
          recordedLetter.occurancePositions.add(haystackInc);
          hasBeenRecorded = true;
        }
      }//end iteration through existing records
      if(!hasBeenRecorded) {
        this.haystackChars.add(new Letter(String.valueOf(currentChar), haystackInc));
      }
    }//end char iteration over pattern
    Collections.sort(this.haystackChars);
  }
  boolean checkForSkipGram(String pattern, String haystack) {
    ArrayList<List<Letter>> allCombos = new ArrayList<List<Letter>>();
    ArrayList<List<Letter>> allPerms = new ArrayList<List<Letter>>();
    ArrayList<String> reversePatterns = new ArrayList<String>();
    this.pattern = pattern;
    this.haystack = haystack;
    if(!patternFormatCheck())
      return false;
    if(!haystackFormatCheck())
      return false;
    analyzeLetters();
    List<Letter> haystackList = this.haystackChars;
    for(int lengthInc = 2; lengthInc < haystackList.size(); lengthInc++) {
      allCombos.addAll(generateCombo(haystackList, lengthInc));
    }
    for(List<Letter> myll : allCombos) {
      ArrayList<Letter> tempList = new ArrayList<Letter>();
      tempList.addAll(myll);
      allPerms.addAll(generatePerm(tempList));
    }
    for(List<Letter> letterGroup : allPerms) {
      ArrayList<Letter> tempList = new ArrayList<Letter>();
      tempList.addAll(letterGroup);
      reversePatterns.add(getPatternForChars(tempList));
    }
    for(String str : reversePatterns) {
      if(str.contains(this.pattern))
        return true;
      if(str.equals(this.pattern))
        return true;
    }
    return false;
  }
  ArrayList<ArrayList<Letter>> generatePerm(ArrayList<Letter> original) {
    if (original.size() == 0) {
      ArrayList<ArrayList<Letter>> result = new ArrayList<ArrayList<Letter>>();
      result.add(new ArrayList<Letter>());
      return result;
    }
    Letter firstElement = original.remove(0);
    ArrayList<ArrayList<Letter>> returnValue = new ArrayList<ArrayList<Letter>>();
    ArrayList<ArrayList<Letter>> permutations = generatePerm(original);
    for (ArrayList<Letter> smallerPermutated : permutations) {
      for (int index=0; index <= smallerPermutated.size(); index++) {
        ArrayList<Letter> temp = new ArrayList<Letter>(smallerPermutated);
        temp.add(index, firstElement);
        returnValue.add(temp);
      }
    }
    return returnValue;
  }
  <T> List<List<T>> generateCombo(List<T> values, int size) {
    if (0 == size)
      return Collections.singletonList(Collections.<T> emptyList());
    if (values.isEmpty())
      return Collections.emptyList();
    List<List<T>> combination = new LinkedList<List<T>>();
    T actual = values.iterator().next();
    List<T> subSet = new LinkedList<T>(values);
    subSet.remove(actual);
    List<List<T>> subSetCombination = generateCombo(subSet, size - 1);
    for (List<T> setInc : subSetCombination) {
      List<T> newSet = new LinkedList<T>(setInc);
      newSet.add(0, actual);
      combination.add(newSet);
    }
    combination.addAll(generateCombo(subSet, size));
    return combination;
  }
  boolean patternFormatCheck() {
    if(this.pattern.length() == 0)
      return false;
    String noStars = this.pattern.replace("*", "");
    if(!noStars.matches("-?\\d+(\\.\\d+)?"))
      return false;
    return true;
  }
  boolean haystackFormatCheck() {
    this.haystack = this.haystack.replaceAll("[^a-zA-Z]", "");
    if(this.haystack.length() == 0 || this.haystack.length() > 50)
      return false;
    this.haystack = this.haystack.toLowerCase();
    return true;
  }
}//end SkipGramDetector class
class Letter implements Comparable<Letter> {
  String name;
  ArrayList<Integer> occurancePositions;

  public Letter(String name) {
    this.name = name;
    this.occurancePositions = new ArrayList<Integer>();
  }
  public Letter(String name, int index) {
    this.name = name;
    this.occurancePositions = new ArrayList<Integer>();
    this.occurancePositions.add(index);
  }
  void addOccurance(int indexToAdd) {
    this.occurancePositions.add(indexToAdd);
  }
  public int compareTo(Letter other) {
    return -Integer.valueOf(this.occurancePositions.size()).compareTo(Integer.valueOf(other.occurancePositions.size()));
  }
}//end Letter class
