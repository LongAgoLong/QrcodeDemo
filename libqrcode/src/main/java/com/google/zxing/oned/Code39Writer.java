/*
 * Copyright 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;

import java.util.Collection;
import java.util.Collections;

/**
 * This object renders a CODE39 code as a {@link BitMatrix}.
 *
 * @author erik.barbara@gmail.com (Erik Barbara)
 */
public final class Code39Writer extends OneDimensionalCodeWriter {

  static final String ALPHABET_STRING = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%";

  /**
   * These represent the encodings of characters, as patterns of wide and narrow bars.
   * The 9 least-significant bits of each int correspond to the pattern of wide and narrow,
   * with 1s representing "wide" and 0s representing narrow.
   */
  static final int[] CHARACTER_ENCODINGS = {
          0x034, 0x121, 0x061, 0x160, 0x031, 0x130, 0x070, 0x025, 0x124, 0x064, // 0-9
          0x109, 0x049, 0x148, 0x019, 0x118, 0x058, 0x00D, 0x10C, 0x04C, 0x01C, // A-J
          0x103, 0x043, 0x142, 0x013, 0x112, 0x052, 0x007, 0x106, 0x046, 0x016, // K-T
          0x181, 0x0C1, 0x1C0, 0x091, 0x190, 0x0D0, 0x085, 0x184, 0x0C4, 0x0A8, // U-$
          0x0A2, 0x08A, 0x02A // /-%
  };

  static final int ASTERISK_ENCODING = 0x094;

  @Override
  protected Collection<BarcodeFormat> getSupportedWriteFormats() {
    return Collections.singleton(BarcodeFormat.CODE_39);
  }

  @Override
  public boolean[] encode(String contents) {
    int length = contents.length();
    if (length > 80) {
      throw new IllegalArgumentException(
          "Requested contents should be less than 80 digits long, but got " + length);
    }

    for (int i = 0; i < length; i++) {
      int indexInString = ALPHABET_STRING.indexOf(contents.charAt(i));
      if (indexInString < 0) {
        contents = tryToConvertToExtendedMode(contents);
        length = contents.length();
        if (length > 80) {
          throw new IllegalArgumentException(
              "Requested contents should be less than 80 digits long, but got " + length + " (extended full ASCII mode)");
        }
        break;
      }
    }

    int[] widths = new int[9];
    int codeWidth = 24 + 1 + (13 * length);
    boolean[] result = new boolean[codeWidth];
    toIntArray(ASTERISK_ENCODING, widths);
    int pos = appendPattern(result, 0, widths, true);
    int[] narrowWhite = {1};
    pos += appendPattern(result, pos, narrowWhite, false);
    //append next character to byte matrix
    for (int i = 0; i < length; i++) {
      int indexInString = ALPHABET_STRING.indexOf(contents.charAt(i));
      toIntArray(CHARACTER_ENCODINGS[indexInString], widths);
      pos += appendPattern(result, pos, widths, true);
      pos += appendPattern(result, pos, narrowWhite, false);
    }
    toIntArray(ASTERISK_ENCODING, widths);
    appendPattern(result, pos, widths, true);
    return result;
  }

  private static void toIntArray(int a, int[] toReturn) {
    for (int i = 0; i < 9; i++) {
      int temp = a & (1 << (8 - i));
      toReturn[i] = temp == 0 ? 1 : 2;
    }
  }

  private static String tryToConvertToExtendedMode(String contents) {
     int length = contents.length();
     StringBuilder extendedContent = new StringBuilder();
     for (int i = 0; i < length; i++) {
       char character = contents.charAt(i);
       switch (character) {
         case '\u0000':
           extendedContent.append("%U");
           break;
         case ' ':
         case '-':
         case '.':
           extendedContent.append(character);
           break;
         case '@':
           extendedContent.append("%V");
           break;
         case '`':
           extendedContent.append("%W");
           break;
         default:
           if (character <= 26) {
             extendedContent.append('$');
             extendedContent.append((char) ('A' + (character - 1)));
           } else if (character < ' ') {
             extendedContent.append('%');
             extendedContent.append((char) ('A' + (character - 27)));
           } else if (character <= ',' || character == '/' || character == ':') {
             extendedContent.append('/');
             extendedContent.append((char) ('A' + (character - 33)));
           } else if (character <= '9') {
             extendedContent.append((char) ('0' + (character - 48)));
           } else if (character <= '?') {
             extendedContent.append('%');
             extendedContent.append((char) ('F' + (character - 59)));
           } else if (character <= 'Z') {
             extendedContent.append((char) ('A' + (character - 65)));
           } else if (character <= '_') {
             extendedContent.append('%');
             extendedContent.append((char) ('K' + (character - 91)));
           } else if (character <= 'z') {
             extendedContent.append('+');
             extendedContent.append((char) ('A' + (character - 97)));
           } else if (character <= 127) {
             extendedContent.append('%');
             extendedContent.append((char) ('P' + (character - 123)));
           } else {
             throw new IllegalArgumentException(
               "Requested content contains a non-encodable character: '" + contents.charAt(i) + "'");
           }
           break;
       }
    }

    return extendedContent.toString();
  }

}
