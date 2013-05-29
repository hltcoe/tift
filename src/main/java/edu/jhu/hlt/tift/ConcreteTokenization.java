/**
 * Copyright 2012-2013 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.tift;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import edu.jhu.hlt.concrete.Concrete;
import edu.jhu.hlt.concrete.Concrete.TokenTagging;
import edu.jhu.hlt.concrete.Concrete.Tokenization;
import edu.jhu.hlt.concrete.Concrete.TokenTagging.TaggedToken;
import edu.jhu.hlt.concrete.util.IdUtil;


/**
 * Utility class for {@link Tokenization} related code.
 * 
 * @author max
 *
 */
public class ConcreteTokenization {

    private static final String TIFT_TOOL_NAME = "edu.jhu.nlp.tift.Tokenizer";
    private static final Concrete.AnnotationMetadata tiftMetadata;
    
    static {
        tiftMetadata = Concrete.AnnotationMetadata.newBuilder()
                .setTool(TIFT_TOOL_NAME)
                // note: not bothering with a timestamp.
                .build();
    }
    
    /**
     * 
     */
    private ConcreteTokenization() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * Wrapper around {@link #generateConcreteTokenization(List, int[], int)} that takes an array of
     * Strings (tokens).
     * 
     * @see #generateConcreteTokenization(List, int[], int)
     * 
     * @param tokens
     *            - an array of tokens (Strings)
     * @param offsets
     *            - an array of integers (offsets)
     * @param startPos
     *            - starting position of the text
     * @return a {@link Tokenization} object with correct tokenization
     */
    public static Concrete.Tokenization generateConcreteTokenization(String[] tokens, int[] offsets, int startPos) {
        return generateConcreteTokenization(Arrays.asList(tokens), offsets, startPos);
    }
    
    /**
     * Generate a {@link Tokenization} object from a list of tokens,
     * list of offsets, and start position of the text (e.g., first text
     * character in the text).
     * 
     * @param tokens
     *            - a {@link List} of tokens (Strings)
     * @param offsets
     *            - an array of integers (offsets)
     * @param startPos
     *            - starting position of the text
     * @return a {@link Tokenization} object with correct tokenization
     */
    public static Concrete.Tokenization generateConcreteTokenization(List<String> tokens, int[] offsets, int startPos) {
        Concrete.Tokenization.Builder tokenizationBuilder = Concrete.Tokenization.newBuilder().setUuid(IdUtil.generateUUID())
                .setKind(Concrete.Tokenization.Kind.TOKEN_LIST);
        // Note: we use token index as token id.
        for (int tokenId = 0; tokenId < tokens.size(); ++tokenId) {
            String token = tokens.get(tokenId);
            int start = startPos + offsets[tokenId];
            int end = start + token.length();
            tokenizationBuilder.addTokenBuilder().setTokenId(tokenId).setText(token)
                    .setTextSpan(Concrete.TextSpan.newBuilder().setStart(start).setEnd(end));
        }

        return tokenizationBuilder.build();
    }
    
    /**
     * Wrapper for {@link #generateConcreteTokenization(List, int[], int)} that
     * takes a {@link List} of {@link Integer} objects.
     * 
     * @see #generateConcreteTokenization(List, int[], int)
     * 
     * @param tokens
     *            - a {@link List} of tokens (Strings)
     * @param offsets
     *            a {@link List} of offsets (Integer objects)
     * @param startPos
     *            - starting position of the text
     * @return a {@link Tokenization} object with correct tokenization
     */
    public static Concrete.Tokenization generateConcreteTokenization(List<String> tokens, List<Integer> offsets, int startPos) {
        return generateConcreteTokenization(tokens, convertIntegers(offsets), startPos);
    }

    /**
     * Generate a {@link Tokenization} object from a list of tokens,
     * list of tags, list of offsets, and start position of the text (e.g., first text
     * character in the text). Assumes tags are part of speech tags.
     * 
     * Invokes {@link #generateConcreteTokenization(List, int[], int)}
     * then adds tagging.
     * 
     * @see #generateConcreteTokenization(List, int[], int)
     * 
     * @param tokens
     *            - a {@link List} of tokens (Strings)
     * @param offsets
     *            - an array of integers (offsets)
     * @param startPos
     *            - starting position of the text
     * @return a {@link Tokenization} object with correct tokenization and token tagging
     */
    public static Concrete.Tokenization generateConcreteTokenization(List<String> tokens, List<String> tokenTags, 
            int[] offsets, int startPos) {
        Concrete.Tokenization tokenization = generateConcreteTokenization(tokens, offsets, startPos);
        TokenTagging.Builder ttBuilder = TokenTagging.newBuilder()
                .setUuid(IdUtil.generateUUID())
                .setMetadata(tiftMetadata);
        for (int i = 0; i < tokens.size(); i++) {
            String tag = tokenTags.get(i);
            if (tag != null) {
                TaggedToken tt = TaggedToken.newBuilder()
                    .setTokenId(offsets[i])
                    .setTag(tokenTags.get(i))
                    .build();
                ttBuilder.addTaggedToken(tt);
            }
        }
        
        Concrete.Tokenization.Builder builder = Tokenization.newBuilder(tokenization)
                .addPosTags(ttBuilder);
        return builder.build();
    }
    
    public static Concrete.Tokenization generateConcreteTokenization(TaggedTokenizationOutput tto) {
        return generateConcreteTokenization(tto.getTokens(), tto.getTokenTags(), convertIntegers(tto.getOffsets()), 0);
    }
    
    /**
     * Convert a {@link List} of {@link Integer} objects to an integer array primitive.
     * 
     * Will throw a {@link NullPointerException} if any element in the list is null. 
     * 
     * @param integers a {@link List} of {@link Integer} objects, none of which are <code>null</code>
     * @return a primitive array of ints
     */
    public static int[] convertIntegers(List<Integer> integers) {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++) 
            ret[i] = iterator.next().intValue();
        
        return ret;
    }
}
