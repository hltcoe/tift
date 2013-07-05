/**
 * 
 */
package edu.jhu.hlt.tift;

import edu.jhu.hlt.concrete.Concrete;
import edu.jhu.hlt.concrete.Concrete.Communication;
import edu.jhu.hlt.concrete.Concrete.Section;
import edu.jhu.hlt.concrete.Concrete.SectionSegmentation;
import edu.jhu.hlt.concrete.Concrete.Sentence;
import edu.jhu.hlt.concrete.Concrete.SentenceSegmentation;
import edu.jhu.hlt.concrete.Concrete.TextSpan;
import edu.jhu.hlt.concrete.Concrete.Tokenization;
import edu.jhu.hlt.concrete.util.IdUtil;

/**
 * @author max
 *
 */
public class ConcreteSectionSegmentation {

    protected static final String TIFT_TOOL_NAME = "edu.jhu.nlp.tift.Tokenizer";
    protected static final Concrete.AnnotationMetadata tiftMetadata;
    
    static {
        tiftMetadata = Concrete.AnnotationMetadata.newBuilder()
                .setTool(TIFT_TOOL_NAME)
                // note: not bothering with a timestamp.
                .build();
    }
    
    /**
     * Generate a {@link SectionSegmentation} object for appending to a
     * communication given a {@link Tokenizer} type and text.
     * 
     * Assumes text starts at position 0.
     * 
     * @see #generateSectionSegmentation(Tokenization, TextSpan)
     * 
     * @param tokenization
     *            {@link Tokenizer} type to use
     * @param text
     *            text to tokenize (assumes start position 0)
     * @return {@link SectionSegmentation} object suitable for use in a
     *         {@link Communication}
     */
    public static SectionSegmentation generateSectionSegmentation(Tokenizer tokenization, 
            String text) {
        Tokenization t = tokenization.tokenizeToConcrete(text, 0);
        TextSpan ts = TextSpan.newBuilder()
                .setStart(0).setEnd(text.length())
                .build();
        return generateSectionSegmentation(t, ts);
    }
    
    /**
     * Generate a {@link SectionSegmentation} object for appending to a
     * communication given a {@link Tokenization} and {@link TextSpan}.
     * 
     * @param t
     *            {@link Tokenization} to use
     * @param ts
     *            {@link TextSpan} to use
     * @return a {@link SectionSegmentation} for use in a {@link Communication}
     */
    public static SectionSegmentation generateSectionSegmentation(Tokenization t, TextSpan ts) {
        Sentence.Builder ss = Sentence.newBuilder()
                .addTokenization(t)
                .setTextSpan(ts)
                .setUuid(IdUtil.generateUUID());
        SentenceSegmentation.Builder ssb = SentenceSegmentation.newBuilder()
                .addSentence(ss)
                .setMetadata(tiftMetadata)
                .setUuid(IdUtil.generateUUID());
        Section.Builder scb = Section.newBuilder()
                .addSentenceSegmentation(ssb)
                .setTextSpan(ts)
                .setKind(Section.Kind.OTHER)
                .setUuid(IdUtil.generateUUID());
        SectionSegmentation.Builder scsb = SectionSegmentation.newBuilder()
                .addSection(scb)
                .setMetadata(tiftMetadata)
                .setUuid(IdUtil.generateUUID());
        return scsb.build();
    }
    
    /**
     * Append a {@link SectionSegmentation} to a {@link Communication}.
     * 
     * The {@link Communication} must have a text field. This method assumes the
     * text starts at position 0.
     * 
     * @param tokenization
     *            {@link Tokenizer} type to use
     * @param comm
     *            the {@link Communication} to append to (must have text)
     * @return the {@link Communication} with an additional
     *         {@link SectionSegmentation}
     */
    public static Communication appendSectionSegmentation(Tokenizer tokenization,
            Communication comm) {
        SectionSegmentation ss = 
                generateSectionSegmentation(tokenization, comm.getText());
        return Communication.newBuilder(comm)
                .addSectionSegmentation(ss)
                .build();
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
