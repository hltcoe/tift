package edu.jhu.hlt.tift;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.jhu.hlt.concrete.TaggedToken;
import edu.jhu.hlt.concrete.TextSpan;
import edu.jhu.hlt.concrete.Token;
import edu.jhu.hlt.concrete.TokenTagging;
import edu.jhu.hlt.concrete.Tokenization;

public class TokenizerTest {

  private static final Logger logger = LoggerFactory.getLogger(TokenizerTest.class);

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testTokenizeToConcreteWhitespace() {
    String text = "hello world test tokens foo";
    int expectedTokenCount = 5;
    Tokenization ct = Tokenizer.WHITESPACE.tokenizeToConcrete(text, 0);
    List<Token> tokenList = ct.getTokenList().getTokens();
    assertEquals(expectedTokenCount, tokenList.size());
    for (Token t : tokenList) {
      logger.info("Got token: {} with text: {}", t.getTokenIndex(), t.getText());
      TextSpan ts = t.getTextSpan();
      logger.info("Text span of this token: {} - {}", ts.getStart(), ts.getEnding());
    }
  }

  @Test
  public void testTokenizeToConcreteTwitter() {
    String text = "hello world test foo :-)";
    int expectedTokenCount = 5;
    Tokenization ct = Tokenizer.TWITTER.tokenizeToConcrete(text, 0);
    List<Token> tokenList = ct.getTokenList().getTokens();
    assertEquals(expectedTokenCount, tokenList.size());
    for (Token t : tokenList) {
      logger.info("Got token: {} with text: {}", t.getTokenIndex(), t.getText());
      TextSpan ts = t.getTextSpan();
      logger.info("Text span of this token: {} - {}", ts.getStart(), ts.getEnding());
    }

    TokenTagging tt = ct.getPosTagList();
    for (TaggedToken t : tt.getTaggedTokenList()) {
      logger.info("Got tagging: {} on token: {}", t.getTag(), t.getTokenIndex());
    }
  }

  @Test
  public void testTokenize() {
    String text = "hello world test tokens";
    List<String> tokens = Tokenizer.BASIC.tokenize(text);
    assertEquals(4, tokens.size());
  }
  
  @Test
  public void thriftReadWrite() throws TException {
    String text = "hello world test tokens";
    Tokenization t = Tokenizer.TWITTER.tokenizeToConcrete(text, 0);
    byte[] bytez = new TSerializer(new TBinaryProtocol.Factory()).serialize(t);
    Tokenization dT = new Tokenization();
    new TDeserializer(new TBinaryProtocol.Factory()).deserialize(dT, bytez);
    assertEquals(4, dT.getTokenList().getTokensSize());
  }

  static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return encoding.decode(ByteBuffer.wrap(encoded)).toString();
  }
}
