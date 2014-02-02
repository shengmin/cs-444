package joos

import joos.exceptions.ScanningException
import joos.regexp.{RegularExpression, Concatenation, Alternation}
import joos.tokens.{TokenKind, TokenKindRegexp, Token}
import org.scalatest.{FlatSpec, Matchers}
import joos.scanner.Scanner
import joos.automata.{Dfa, AcceptingDfaNode, NonAcceptingDfaNode, DfaNode}
import joos.tokens.TokenKind.TokenKindValue

class ScannerSpec extends FlatSpec with Matchers {

  private val CharacterA = 'A'
  private val CharacterB = 'B'
  private val CharacterC = 'C'

  private val TokenKind1 = TokenKind.Id
  private val TokenKind2 = TokenKind.Dot

  private val testDfaNoLoops = NonAcceptingDfaNode().
    addTransition(CharacterA, AcceptingDfaNode(TokenKind1)).
    addTransition(CharacterB,
      AcceptingDfaNode(TokenKind2).addTransition(CharacterA, NonAcceptingDfaNode()))

  private val testDfaWithLoops = NonAcceptingDfaNode()
  testDfaWithLoops.addTransition(CharacterA, testDfaWithLoops).
    addTransition(CharacterB,
      AcceptingDfaNode(TokenKind2).addTransition(CharacterA, AcceptingDfaNode(TokenKind1)))

  private val testDfaDeadEnds = NonAcceptingDfaNode().
    addTransition(CharacterA,
      NonAcceptingDfaNode().addTransition(CharacterC,
        AcceptingDfaNode(TokenKind1))).
    addTransition(CharacterB,
      NonAcceptingDfaNode().addTransition(CharacterB,
        AcceptingDfaNode(TokenKind2).addTransition(CharacterA,
          NonAcceptingDfaNode().addTransition(CharacterC,
            NonAcceptingDfaNode()))))

  final val joosDfa =
    Dfa(TokenKind.values.map(_.asInstanceOf[TokenKindValue].getRegexp()).reduceRight((a,b) => a | b))

  // Tests begin here
  "A state with no transition" should "backtrack once to accepting nodes" in {
    val scanner = new Scanner(Dfa(testDfaDeadEnds))
    val input = Seq(CharacterB, CharacterB, CharacterA, CharacterC, CharacterB, CharacterB)

    input.foreach(char => scanner.scan(char))

    val tokens = scanner.getTokens()
    tokens.map(token => token.kind) should contain theSameElementsInOrderAs Seq(TokenKind2, TokenKind1, TokenKind2)
  }

  it should "backtrack twice to accepting nodes" in {
    val scanner = new Scanner(Dfa(testDfaNoLoops))
    val input = Seq(CharacterB, CharacterA, CharacterB)

    input.foreach(char => scanner.scan(char))

    val tokens = scanner.getTokens()
    tokens.map(token => token.kind) should contain theSameElementsInOrderAs Seq(TokenKind2, TokenKind1, TokenKind2)
  }

  "A state with loops" should "loop correctly" in {
    val scanner = new Scanner(Dfa(testDfaWithLoops))
    val input = Seq(CharacterA, CharacterA, CharacterA, CharacterB, CharacterA, CharacterA, CharacterB)

    input.foreach(char => scanner.scan(char))

    val tokens = scanner.getTokens()
    tokens.map(token => token.kind) should contain theSameElementsInOrderAs Seq(TokenKind1, TokenKind2)
  }

  "Non-tokenizable input" should "throw a scanning exception" in {
    val scanner = new Scanner(Dfa(testDfaWithLoops))
    val input = Seq(CharacterA, CharacterA, CharacterA, CharacterB, CharacterB, CharacterB, CharacterA, CharacterA)

    intercept[ScanningException] {
      input.foreach(char => scanner.scan(char))
      scanner.getTokens()
    }
  }

  "An epsilon closure with multiple accepting states" should "accept the highest priority token" in {
    val testRegexp = Concatenation("final") := TokenKind.Id := TokenKind.Final

    val scanner = Scanner(Dfa(testRegexp))

    "final".toCharArray.foreach(c => scanner.scan(c))
    val tokens = scanner.getTokens()
    tokens should have length 1
    tokens should contain(Token(TokenKind.Final, "final"))
  }

  behavior of "A static word regular expression (final) to DFA conversion"

  it should "accept tokenizable (final) inputs" in {
    val scanner = Scanner(Dfa(TokenKind.Final.getRegexp()))

    "final".toCharArray.foreach(c => scanner.scan(c))
    val tokens = scanner.getTokens()
    tokens should have length 1
    tokens should contain(Token(TokenKind.Final, "final"))
  }

  it should "reject non-tokenizable (final3) inputs" in {
    val scanner = Scanner(Dfa(TokenKind.Final.getRegexp()))

    intercept[ScanningException] {
      "final3".toCharArray.foreach(c => scanner.scan(c))
      scanner.getTokens()
    }
  }

  behavior of "An alternating word regular expression (T|test) to DFA conversion"

  it should "accept tokenizable (test) inputs" in {
    val testRegexp = Alternation("tT") + Concatenation("est") := TokenKind1

    val scanner = Scanner(Dfa(testRegexp))

    "test".toCharArray.foreach(c => scanner.scan(c))

    val tokens = scanner.getTokens()
    tokens should have length 1
    tokens should contain(Token(TokenKind1, "test"))
  }

  it should "accept tokenizable (Test) inputs" in {
    val testRegexp = Alternation("tT") + Concatenation("est") := TokenKind1
    val scanner = Scanner(Dfa(testRegexp))

    "Test".toCharArray.foreach(c => scanner.scan(c))

    val tokens = scanner.getTokens()
    tokens should have length 1
    tokens should contain(Token(TokenKind1, "Test"))
  }


  behavior of "A looping word regular expression (ID) to DFA conversion"

  it should "accept tokenizable (t998) inputs" in {
    val scanner = Scanner(Dfa(TokenKind.Id.getRegexp()))

    "t998".toCharArray.foreach(c => scanner.scan(c))

    val tokens = scanner.getTokens()
    tokens should have length 1
    tokens should contain(Token(TokenKind.Id, "t998"))
  }

  it should "reject non-tokenizable (9112abc) inputs" in {
    val scanner = Scanner(Dfa(TokenKind.Id.getRegexp()))

    intercept[ScanningException] {
      "9122abc".toCharArray.foreach(c => scanner.scan(c))
      scanner.getTokens()
    }
  }

  /*
  Things we want to test:
  Identifier
  Keyword
  Literal
  Separator
  Operator
  comment ??
*/
    //Identifiers
  "Scanner" should "recognize valid IDs" in {
    val test_ids = Seq[String]("String", "i3", "MAX_VALUE", "isLetterOrDigit")
    test_ids.map(id => {
      val scanner = Scanner(joosDfa)
      id.toCharArray.foreach(c => scanner.scan(c))
      val tokens = scanner.getTokens()
      tokens should have length 1
      tokens should contain(new Token(TokenKind.Id, id))
    })
  }

  it should "recognize all valid keywords" in {
    val test_keywords = Set[String]("abstract", "default", "if", "private", "this", "boolean", "do",
      "implements", "protected", "throw", "break", "double", "import", "public", "throws", "byte", "else",
      "instanceof", "return", "transient", "case", "extends", "int", "short", "try", "catch", "final",
      "interface", "static", "void", "char", "finally", "long", "strictfp", "volatile", "class", "float",
      "native", "super", "while", "const", "for", "new", "switch", "continue", "goto", "package", "synchronized")

    val scanner = Scanner(joosDfa)
    var counter = 1

    TokenKind.values.map(
      value =>
      {
        val token_kind_value = value.asInstanceOf[TokenKindValue]
        val keyword = token_kind_value.getName().toLowerCase()
        if (test_keywords.contains(keyword)) {
          keyword.toCharArray.foreach(c => scanner.scan(c))
          val tokens = scanner.getTokens()
          tokens should have length counter
          counter += 1
          tokens should contain(new Token(value, keyword))
        }
      }
    )
  }

  it should "recognize all separators" in {
    val separators =
      Map[String, TokenKindValue](
        "(" -> TokenKind.LeftParen,
        ")" -> TokenKind.RightParen,
        "{" -> TokenKind.LeftBrace,
        "}" -> TokenKind.RightBrace,
        "[" -> TokenKind.LeftBracket,
        "]" -> TokenKind.RightBracket,
        ";" -> TokenKind.SemiColon,
        "," -> TokenKind.Comma,
        "." -> TokenKind.Dot
      )
    val scanner = Scanner(joosDfa)
    var counter = 1

    separators.keys.foreach(
      sep => {
        sep.toCharArray.foreach(c => scanner.scan(c))
        val tokens = scanner.getTokens()
        tokens should have length counter
        counter += 1
        tokens should contain(new Token(separators(sep), sep))
      }
    )
  }

  it should "recognize valid IntegerLiterals" in {
    val integers =
      Map[String, TokenKindValue](
        "0" -> TokenKind.DecimalInteger,
        "2" -> TokenKind.DecimalInteger,
        "0372" -> TokenKind.OctalInteger,
        "0xDadaCafe" -> TokenKind.HexInteger,
        "1996" -> TokenKind.DecimalInteger,
        "0x00FF00FF" -> TokenKind.HexInteger,
        "0l" -> TokenKind.DecimalInteger,
        "0x100000000L" -> TokenKind.HexInteger,
        "2147483648L" -> TokenKind.DecimalInteger,
        "0xC0B0L" -> TokenKind.HexInteger
      )
    val scanner = Scanner(joosDfa)
    var counter = 1

    integers.keys.foreach(
      sep => {
        sep.toCharArray.foreach(c => scanner.scan(c))
        val tokens = scanner.getTokens()
        tokens should have length counter
        counter += 1
        tokens should contain(new Token(integers(sep), sep))
      }
    )
  }

  it should "recognize floating point values" in {
    val floating_points =
      Map[String, TokenKindValue](
        "1e1f" -> TokenKind.FloatingPoint,
        "2.f" -> TokenKind.FloatingPoint,
        ".3f" -> TokenKind.FloatingPoint,
        "0f" -> TokenKind.FloatingPoint,
        "3.14f" -> TokenKind.FloatingPoint,
        "6.022137e+23f" -> TokenKind.FloatingPoint,
        "1e1" -> TokenKind.FloatingPoint,
        "2." -> TokenKind.FloatingPoint,
        ".3" -> TokenKind.FloatingPoint,
        "0.0" -> TokenKind.FloatingPoint,
        "3.14" -> TokenKind.FloatingPoint,
        "1e-9d" -> TokenKind.FloatingPoint,
        "1e137" -> TokenKind.FloatingPoint
      )
    val scanner = Scanner(joosDfa)
    var counter = 1

    floating_points.keys.foreach(
      num => {
        num.toCharArray.foreach(c => scanner.scan(c))
        val tokens = scanner.getTokens()
        tokens should have length counter
        counter += 1
        tokens should contain(new Token(floating_points(num), num))
      }
    )
  }

  it should "recognize boolean literals" in {
    val floating_points =
      Map[String, TokenKindValue](
        "true" -> TokenKind.True,
        "false" -> TokenKind.False
      )
    val scanner = Scanner(joosDfa)
    var counter = 1

    floating_points.keys.foreach(
      num => {
        num.toCharArray.foreach(c => scanner.scan(c))
        val tokens = scanner.getTokens()
        tokens should have length counter
        counter += 1
        tokens should contain(new Token(floating_points(num), num))
      }
    )
  }

  it should "recognize character literals" in {
    val characters =
      Map[String, TokenKindValue](
        "'a'" -> TokenKind.Character,
        "'%'" -> TokenKind.Character,
        "'\\t'" -> TokenKind.Character,
        "'\\\\'" -> TokenKind.Character,
        "'\\''" -> TokenKind.Character,
        "'\\u03a9'" -> TokenKind.Character,
        "'\\uFFFF'" -> TokenKind.Character,
        "'\\177'" -> TokenKind.Character
      )

    val scanner = Scanner(joosDfa)
    var counter = 1

    characters.keys.foreach(
      char => {
        char.toCharArray.foreach(c => scanner.scan(c))
        val tokens = scanner.getTokens()
        tokens should have length counter
        counter += 1
        tokens should contain(new Token(characters(char), char))
      }
    )
  }
}