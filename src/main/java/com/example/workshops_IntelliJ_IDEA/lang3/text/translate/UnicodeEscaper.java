/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.workshops_IntelliJ_IDEA.lang3.text.translate;

import java.io.IOException;
import java.io.Writer;

/**
 * Translates codepoints to their Unicode escaped value.
 *
 * @since 3.0
 * @deprecated as of 3.6, use commons-text
 * <a href="https://commons.apache.org/proper/commons-text/javadocs/api-release/org/apache/commons/text/translate/UnicodeEscaper.html">
 * UnicodeEscaper</a> instead
 */
@Deprecated
public class UnicodeEscaper extends CodePointTranslator {

    private final int below;
    private final int above;
    private final boolean between;

    /**
     * <p>Constructs a {@code UnicodeEscaper} for all characters. </p>
     */
    public UnicodeEscaper() {
        this(0, Integer.MAX_VALUE, true);
    }

    /**
     * <p>Constructs a {@code UnicodeEscaper} for the specified range. This is
     * the underlying method for the other constructors/builders. The {@code below}
     * and {@code above} boundaries are inclusive when {@code between} is
     * {@code true} and exclusive when it is {@code false}. </p>
     *
     * @param below   int value representing the lowest codepoint boundary
     * @param above   int value representing the highest codepoint boundary
     * @param between whether to escape between the boundaries or outside them
     */
    protected UnicodeEscaper(final int below, final int above, final boolean between) {
        this.below = below;
        this.above = above;
        this.between = between;
    }

    /**
     * <p>Constructs a {@code UnicodeEscaper} below the specified value (exclusive). </p>
     *
     * @param codepoint below which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    public static UnicodeEscaper below(final int codepoint) {
        return outsideOf(codepoint, Integer.MAX_VALUE);
    }

    /**
     * <p>Constructs a {@code UnicodeEscaper} above the specified value (exclusive). </p>
     *
     * @param codepoint above which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    public static UnicodeEscaper above(final int codepoint) {
        return outsideOf(0, codepoint);
    }

    /**
     * <p>Constructs a {@code UnicodeEscaper} outside of the specified values (exclusive). </p>
     *
     * @param codepointLow  below which to escape
     * @param codepointHigh above which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    public static UnicodeEscaper outsideOf(final int codepointLow, final int codepointHigh) {
        return new UnicodeEscaper(codepointLow, codepointHigh, false);
    }

    /**
     * <p>Constructs a {@code UnicodeEscaper} between the specified values (inclusive). </p>
     *
     * @param codepointLow  above which to escape
     * @param codepointHigh below which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    public static UnicodeEscaper between(final int codepointLow, final int codepointHigh) {
        return new UnicodeEscaper(codepointLow, codepointHigh, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean translate(final int codepoint, final Writer out) throws IOException {
        if (between) {
            if (codepoint < below || codepoint > above) {
                return false;
            }
        } else if (codepoint >= below && codepoint <= above) {
            return false;
        }

        // TODO: Handle potential + sign per various Unicode escape implementations
        if (codepoint > 0xffff) {
            out.write(toUtf16Escape(codepoint));
        } else {
            out.write("\\u");
            out.write(HEX_DIGITS[(codepoint >> 12) & 15]);
            out.write(HEX_DIGITS[(codepoint >> 8) & 15]);
            out.write(HEX_DIGITS[(codepoint >> 4) & 15]);
            out.write(HEX_DIGITS[(codepoint) & 15]);
        }
        return true;
    }

    /**
     * Converts the given codepoint to a hex string of the form {@code "\\uXXXX"}
     *
     * @param codepoint a Unicode code point
     * @return the hex string for the given codepoint
     * @since 3.2
     */
    protected String toUtf16Escape(final int codepoint) {
        return "\\u" + hex(codepoint);
    }
}
