/**
 * Copyright 2012-2013 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */

// Copyright 2010-2012 Benjamin Van Durme. All rights reserved.
// This software is released under the 2-clause BSD license.
// See jerboa/LICENSE, or http://cs.jhu.edu/~vandurme/jerboa/LICENSE

// Benjamin Van Durme, vandurme@cs.jhu.edu, 14 May 2012

package edu.jhu.hlt.tift;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recognizes various Twitter related tokens, runs PTB tokenization on the rest.
 * 
 * Based on combination of patterns from an older tokenizer of my own, the PTB
 * patterns, and those of two other Twitter tokenizers:
 * 
 * -------------------------------------- --------------------------------------
 * http://sentiment.christopherpotts.net/code-data/happyfuntokenizing.py
 * 
 * Which included header information:
 * 
 * __author__ = "Christopher Potts" __copyright__ =
 * "Copyright 2011, Christopher Potts" __credits__ = [] __license__ =
 * "Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License: http://creativecommons.org/licenses/by-nc-sa/3.0/"
 * __version__ = "1.0"
 * 
 * -------------------------------------- --------------------------------------
 * O'Connor's twokenize.py/scala package. Below is the relevant header/author
 * information as required by Apache 2.0, taken from twokenize.scala :
 * 
 * Code History Original version in TweetMotif in Python (2009-2010,
 * github.com/brendano/tweetmotif) having two forks: - (2011) Scala port and
 * improvements by David Snyder (dsnyder@cs.utexas.edu) and Jason Baldridge
 * (jasonbaldridge@gmail.com) https://bitbucket.org/jasonbaldridge/twokenize/ -
 * (2011) Modifications for POS tagging by Kevin Gimpel (kgimpel@cs.cmu.edu) and
 * Daniel Mills (dpmills@cs.cmu.edu) Merge to Scala by Brendan O'Connor, for ARK
 * TweetNLP package (2011-06)
 * 
 * Original paper:
 * 
 * TweetMotif: Exploratory Search and Topic Summarization for Twitter. Brendan
 * O'Connor, Michel Krieger, and David Ahn. ICWSM-2010 (demo track)
 * http://brenocon.com/oconnor_krieger_ahn.icwsm2010.tweetmotif.pdf
 * 
 * ---
 * 
 * Scala port of Brendar O'Connor's twokenize.py
 * 
 * This is not a direct port, as some changes were made in the aim of
 * simplicity.
 * 
 * - David Snyder (dsnyder@cs.utexas.edu) April 2011
 * 
 * Modifications to more functional style, fix a few bugs, and making output
 * more like twokenize.py. Added abbrevations. Tweaked some regex's to produce
 * better tokens.
 * 
 * - Jason Baldridge (jasonbaldridge@gmail.com) June 2011
 */
public class TwitterTokenizer {
    private static final List<PatternStringTuple> tupleList = new ArrayList<>();

    private static final String START = "(?<=^|\\s)";
    private static final String START_W_PAREN = "(?<=^|\\s|\\()";
    private static final String START_W_PAREN_DBQUOTE = "(?<=^|\\s|\\(|\"|\u201c|\u201d|\u201e|\u201f|\u275d|\u275e)";
    private static final String END = "(?=$|\\s)";
    private static final String END_W_PAREN = "(?=$|\\s|\\))";
    
    private static boolean rw = false;

    static {
        try {
            initializePatterns();
        } catch (IOException ioe) {
            throw new RuntimeException("Error initializing unicode tokenization.", ioe);
        }
    }

    private static void initializePatterns() throws IOException {
        // email before URL, as they both things like ".com", but email has
        // '@' email before mention, as anything not email with an '@' is
        // likely to be a mention
        tupleList.add(getEmailPatterns());
        tupleList.add(getMentionPatterns());
        tupleList.addAll(getURLPatterns());
        tupleList.add(getWesternEmoticonPatterns());
        tupleList.add(getEasternEmoticonPatterns());
        tupleList.add(getMiscEmoticonPatterns());
        tupleList.add(getHeartPatterns());
        tupleList.add(getHashtagPatterns());
        tupleList.add(getLeftArrowPatterns());
        tupleList.add(getRightArrowPatterns());
        tupleList.addAll(getRepeatedPatterns());
        tupleList.addAll(getUnicodePatterns());
        tupleList.add(getNumberPatterns());
    }

    public static List<PatternStringTuple> getURLPatterns() {
        // "p:" gets picked up by the emoticon pattern, so order of patterns is
        // important. Matching <, > pairs without verifying both are present.
        List<PatternStringTuple> tupleList = new ArrayList<>();
        tupleList.add(new PatternStringTuple(START + "(" + "(https?:|www\\.)\\S+" + "|" +
        // inspired by twokenize
                "[^\\s@]+\\.(com|co\\.uk|org|net|info|ca|ly|mp|edu|gov)(/(\\S*))?" + ")" + END, "URL"));
        tupleList.add(new PatternStringTuple("(?<=\\(|<)" + "(" + "(https?:|www\\.)\\S+" + "|" +
        // inspired by twokenize
                "[^\\s@]+\\.(com|co\\.uk|org|net|info|ca|ly|mp|edu|gov)(/(\\S*))?" + ")" + "(?=\\)|>)", "URL"));
        return tupleList;

    }

    // emoticons: (here just for misc reference, not all nec. supported)
    // http://www.urbandictionary.com/define.php?term=emoticon
    //
    // :) smile
    // :( frown
    // ;) wink
    // :P or :
    // Public tongue sticking out: joke, sarcasm or disgusting
    // 8) has sunglasses: looking cool
    // :O surprised
    // :S confused
    // :'( shedding a tear
    // XD laughing, eyes shut (LOL)
    // XP Tongue out, eyes shut
    // ^_^ smiley
    // ^.^ see above, but rather than a wide, closed mouth, a small mouth is
    // present
    // ^_~ wink
    // >_< angry, frustrated
    // =_= bored
    // -_- annoyed
    // -_-' or ^_^' or ^_^;; nervousness, sweatdrop or embarrassed.
    //
    // I have observed :3 as semi-frequent, but could be either emoticon, or,
    // e.g.: 2:30
    public static PatternStringTuple getWesternEmoticonPatterns() {
        // Light modification of Potts

        String eyebrows = "[<>]";
        String eyes = "[:;=8xX]";
        String nose = "[\\-oO\\*\\']";
        // * can be a nose: :*)
        // or a mouth, for "kisses" : :*
        String mouth = "[\\*\\)\\]\\(\\[$sSdDpP/\\}\\{@\\|\\\\]";

        return new PatternStringTuple(START + "((" + eyebrows + "?" + eyes + nose + "?" + mouth + "+" + ")|(" +
        // reverse
                mouth + "+" + nose + "?" + eyes + eyebrows + "?" + "))" + END, "WEST_EMOTICON");
    }

    public static PatternStringTuple getEasternEmoticonPatterns() {
        return new PatternStringTuple(START + "((-_-)|(\\^_\\^)|(=_=)|(\\^\\.\\^)|(\\._\\.)|(>_<)|(\\*-\\*)|(\\*_\\*))" + END,
                "EAST_EMOTICON");
    }

    public static PatternStringTuple getNumberPatterns() {
        // times, dates, money, ...
        return new PatternStringTuple("(\\d+([:,\\./]\\d+)+)", "NUMBER");
    }

    public static PatternStringTuple getPhoneNumberPatterns() {
        // From Potts

        // Phone numbers:
        // (?:
        // (?: # (international)
        // \+?[01]
        // [\-\s.]*
        // )?
        // (?: # (area code)
        // [\(]?
        // \d{3}
        // [\-\s.\)]*
        // )?
        // \d{3} # exchange
        // [\-\s.]*
        // \d{4} # base
        return new PatternStringTuple(START + "((\\+?[01][\\-\\s.]*)?([\\(]?\\d{3}[\\-\\s.\\)]*)?\\d{3}[\\-\\s.]*\\d{4})" + END,
                "PhoneNumber");
    }

    public static PatternStringTuple getMentionPatterns() {
        // return Pattern.compile(START + "(@[_A-Za-z0-9]+)" + "(?=$|\\s|:)");
        return new PatternStringTuple(START_W_PAREN_DBQUOTE + "(@[_A-Za-z0-9]+)", "MENTION");
        // return getPairs("(?<=^|\\s|\\()" + "(@[_A-Za-z0-9]+)", "MENTION");
    }

    public static PatternStringTuple getHeartPatterns() {
        // grabbed from twokenize
        return new PatternStringTuple(START + "((<)|(&lt))+/?3+" + END, "HEART");
    }

    public static PatternStringTuple getMiscEmoticonPatterns() {
        return new PatternStringTuple(START + "((\\\\m/)|(\\\\o/))" + END, "MISC_EMOTICON");
    }

    public static PatternStringTuple getHashtagPatterns() {
        // Potts: "(\\#+[\\w_]+[\\w\\'_\\-]*[\\w_]+)"
        // twokenize: #[a-zA-Z0-9_]+
        // comment from twokenize:
        // "also gets #1 #40 which probably aren't hashtags .. but good as tokens"
        return new PatternStringTuple(START + "(\\#+[\\w_]+[\\w\\'_\\-]*[\\w_]+)" + END, "HASHTAG");
    }

    public static PatternStringTuple getLeftArrowPatterns() {
        // twokenize: """(<*[-=]*>+|<+[-=]*>*)"""
        // this is more conservative
        return new PatternStringTuple("((<|(&lt))+[-=]+)" + END, "LEFT_ARROW");
    }

    public static PatternStringTuple getRightArrowPatterns() {
        // twokenize: """(<*[-=]*>+|<+[-=]*>*)"""
        // this is more conservative
        return new PatternStringTuple(START + "([-=]+(>|(&gt))+)", "RIGHT_ARROW");
    }

    /**
     * Best to run these patterns before mentionPattern
     */
    public static PatternStringTuple getEmailPatterns() {
        // modified from twokenize
        return new PatternStringTuple(START_W_PAREN +
        // added the [^.] guard: much more likely to catch punctuation ahead of
        // an
        // @-mention then an email address that ends in '.'
        // That guard also requires email address to be at least 2 characters
        // long
                "([a-zA-Z0-9\\._%+-]+[^\\.\\!\\?\\:\\;\\s]@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4})" + END_W_PAREN, "EMAIL");
    }

    public static List<PatternStringTuple> getRepeatedPatterns() {
        List<PatternStringTuple> tupleList = new ArrayList<>();

        Object[] x = { 
                "(\\.)", 28, -1, ".....", 
                "(\\.)", 9, 27, "....", 
                "(\\.)", 4, 8, "...", 
                "(\\.)", 2, 3, "..", 
                "(\\?)", 28, -1, "?????", 
                "(\\?)", 9, 27, "????", 
                "(\\?)", 4, 8, "???", 
                "(\\?)", 2, 3, "??", 
                "(!)", 28, -1, "!!!!!", 
                "(!)", 9, 27, "!!!!",
                "(!)", 4, 8, "!!!", 
                "(!)", 2, 3, "!!",
                // ! inverted
                "(\u00a1)", 28, -1, "\u00a1\u00a1\u00a1\u00a1\u00a1", 
                "(\u00a1)", 9, 27, "\u00a1\u00a1\u00a1\u00a1", 
                "(\u00a1)", 4, 8, "\u00a1\u00a1\u00a1", 
                "(\u00a1)", 2, 3, "\u00a1\u00a1",
                // ? inverted
                "(\u00bf)", 28, -1, "\u00bf\u00bf\u00bf\u00bf\u00bf", 
                "(\u00bf)", 9, 27, "\u00bf\u00bf\u00bf\u00bf", 
                "(\u00bf)", 4, 8, "\u00bf\u00bf\u00bf", 
                "(\u00bf)", 2, 3, "\u00bf\u00bf" 
            };
        for (int i = 0; i < x.length - 3; i += 4)
            tupleList.add(new PatternStringTuple(Pattern.compile(x[i] + "{" + x[i + 1] + ","
                    + ((Integer) x[i + 2] > 0 ? x[i + 2] + "}" : "}")), (String) x[i + 3]));

        String[] y = { 
                "[eEaA]?[hH]([eEaA]|[hH]){54,}", "hahahahaha", 
                "[eEaA]?[hH]([eEaA]|[hH]){18,53}", "hahahaha",
                "[eEaA]?[hH]([eEaA]|[hH]){6,17}", "hahaha", 
                "[eEaA]?[hH]([eEaA]|[hH]){3,5}", "haha", 
                "[jJ]([jJ]|[eEaA]){54,}", "jajajajaja", 
                "[jJ]([jJ]|[eEaA]){18,53}", "jajajaja", 
                "[jJ]([jJ]|[eEaA]){6,17}", "jajaja", 
                "[jJ]([jJ]|[eEaA]){3,5}", "jaja", 
                "[hH]+([mM]){54,}", "hmmmmm", 
                "[hH]+([mM]){18,53}", "hmmmm", 
                "[hH]+([mM]){6,17}", "hmmm", 
                "[hH]+([mM]){3,5}", "hmm", 
                "([mM]){54,}", "mmmmm", 
                "([mM]){18,53}", "mmmm", 
                "([mM]){6,17}", "mmm", 
                "([mM]){3,5}", "mm" 
            };
        for (int i = 0; i < y.length - 1; i += 2)
            tupleList.add(new PatternStringTuple(Pattern.compile(y[i]), y[i + 1]));

        return tupleList;
    }

    public static List<PatternStringTuple> getUnicodePatterns() throws IOException {
        List<PatternStringTuple> tupleList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(TwitterTokenizer.class.getClassLoader()
                .getResourceAsStream("unicode.csv")));
        String line;
        String[] toks;
        char[] pair;
        String regexp;
        while ((line = reader.readLine()) != null) {
            toks = line.split(",");
            if (toks[0].length() > 4) {
                pair = Character.toChars(Integer.decode("0x" + toks[0]));
                regexp = "(" + pair[0] + pair[1] + ")";
                tupleList.add(new PatternStringTuple(Pattern.compile(regexp), toks[1]));
            }
            regexp = "(\\u" + toks[0] + ")";
            tupleList.add(new PatternStringTuple(Pattern.compile(regexp), toks[1]));
        }
        
        reader.close();
        
        return tupleList;
        // SimpleImmutableEntry<Pattern,String>[] y = new
        // SimpleImmutableEntry[x.length/2];
        // for (int i = 0; i < x.length -1; i+=2)
        // y[i/2] = new SimpleImmutableEntry(Pattern.compile(x[i]),x[i+1]);

        // return y;
    }

    /**
     * Returns 3 arrays: 
     * 
     * tokenization 
     * tokenzation tags 
     * code point offsets
     */
    public static String[][] tokenizeToArray(String text) {
        List<TokenTagTuple> x = recursiveTokenize(text.trim(), 0, Tokenizer.BASIC);

        String[][] y = new String[3][];
        y[0] = new String[x.size()];
        y[1] = new String[x.size()];
        y[2] = new String[x.size()];

        for (int i = 0; i < x.size(); i++) {
            y[0][i] = x.get(i).getToken();
            y[1][i] = x.get(i).getTag();
        }
        int[] z = Tokenizer.getOffsets(text, y[0]);
        for (int i = 0; i < z.length; i++)
            y[2][i] = "" + z[i];

        return y;
    }
    
    public static TwitterTokenizationOutput tokenize(String text) {
        return new TwitterTokenizationOutput(tokenizeToArray(text));
    }

    public static String[] tokenizeTweet(String text) {
        return tokenizeTweet(text, Tokenizer.BASIC);
    }

    public static String[] tokenizeTweet(String text, Tokenizer tokenization) {
        List<TokenTagTuple> x = recursiveTokenize(text.trim(), 0, tokenization);

        String[] y = new String[x.size()];
        if (!rw) {
            for (int i = 0; i < x.size(); i++)
                y[i] = x.get(i).getToken();
        } else {
            for (int i = 0; i < x.size(); i++) {
                if (x.get(i).getTag() != null)
                    y[i] = "[" + x.get(i).getTag() + "]";
                else
                    y[i] = x.get(i).getToken();
            }
        }
        return y;
    }

    private static List<TokenTagTuple> recursiveTokenize(String text, int index, Tokenizer tokenization) {
        if (index < tupleList.size()) {
            PatternStringTuple pst = tupleList.get(index);
            Pattern pattern = pst.getPattern();
            String tag = pst.getEntry();
            Matcher matcher;
            matcher = pattern.matcher(text);
            int groupCount = matcher.groupCount();
            String textFragment = "";
            if (groupCount > 0) {
                List<List<TokenTagTuple>> arrays = new ArrayList<>();
                int lastEnd = 0;
                while (matcher.find()) {
                    if (matcher.start() > lastEnd) {
                        textFragment = text.substring(lastEnd, matcher.start()).trim();
                        if (textFragment.length() > 0) // possible could have
                                                       // started all as
                                                       // whitespace
                            arrays.add(recursiveTokenize(textFragment, index + 1, tokenization));
                    }
                    // System.out.println("[" + matcher.group() + "] " +
                    // matcher.start() + " " + matcher.end());
                    List<TokenTagTuple> tmpList = new ArrayList<>();
                    tmpList.add(new TokenTagTuple(matcher.group(), tag));
                    arrays.add(tmpList);
                    lastEnd = matcher.end();
                }
                if (lastEnd < text.length())
                    arrays.add(recursiveTokenize(text.substring(lastEnd, text.length()).trim(), index + 1, tokenization));

                return concatAll(arrays);
            } else {
                return recursiveTokenize(text.trim(), index + 1, tokenization);
            }
        } else {
            List<String> tokenList = tokenization.tokenize(text);
            List<TokenTagTuple> y = new ArrayList<>(tokenList.size());
            for (String token : tokenList)
                y.add(new TokenTagTuple(token, null));
            return y;
        }
    }

    public static List<TokenTagTuple> concatAll(List<List<TokenTagTuple>> arrays) {
        int totalLength = 0;
        for (List<TokenTagTuple> array : arrays)
            totalLength += array.size();
        
        List<TokenTagTuple> result = new ArrayList<>(totalLength);
        for (List<TokenTagTuple> array : arrays)
            result.addAll(array);
        
        return result;
    }
}