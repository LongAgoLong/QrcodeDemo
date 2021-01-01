/*
 * Copyright 2009 ZXing authors
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

import com.google.zxing.FormatException;

/**
 * <p>Encapsulates functionality and implementation that is common to UPC and EAN families
 * of one-dimensional barcodes.</p>
 *
 * @author aripollak@gmail.com (Ari Pollak)
 * @author dsbnatut@gmail.com (Kazuki Nishiura)
 */
public abstract class UPCEANWriter extends OneDimensionalCodeWriter {

  /**
   * Start/end guard pattern.
   */
  static final int[] START_END_PATTERN = {1, 1, 1,};

  /**
   * Pattern marking the middle of a UPC/EAN pattern, separating the two halves.
   */
  static final int[] MIDDLE_PATTERN = {1, 1, 1, 1, 1};
  /**
   * end guard pattern.
   */
  static final int[] END_PATTERN = {1, 1, 1, 1, 1, 1};
  /**
   * "Odd", or "L" patterns used to encode UPC/EAN digits.
   */
  static final int[][] L_PATTERNS = {
          {3, 2, 1, 1}, // 0
          {2, 2, 2, 1}, // 1
          {2, 1, 2, 2}, // 2
          {1, 4, 1, 1}, // 3
          {1, 1, 3, 2}, // 4
          {1, 2, 3, 1}, // 5
          {1, 1, 1, 4}, // 6
          {1, 3, 1, 2}, // 7
          {1, 2, 1, 3}, // 8
          {3, 1, 1, 2}  // 9
  };

  /**
   * As above but also including the "even", or "G" patterns used to encode UPC/EAN digits.
   */
  static final int[][] L_AND_G_PATTERNS;

  static {
    L_AND_G_PATTERNS = new int[20][];
    System.arraycopy(L_PATTERNS, 0, L_AND_G_PATTERNS, 0, 10);
    for (int i = 10; i < 20; i++) {
      int[] widths = L_PATTERNS[i - 10];
      int[] reversedWidths = new int[widths.length];
      for (int j = 0; j < widths.length; j++) {
        reversedWidths[j] = widths[widths.length - j - 1];
      }
      L_AND_G_PATTERNS[i] = reversedWidths;
    }
  }

  /**
   * Computes the UPC/EAN checksum on a string of digits, and reports
   * whether the checksum is correct or not.
   *
   * @param s string of digits to check
   * @return true iff string of digits passes the UPC/EAN checksum algorithm
   * @throws FormatException if the string does not contain only digits
   */
  static boolean checkStandardUPCEANChecksum(CharSequence s) throws FormatException {
    int length = s.length();
    if (length == 0) {
      return false;
    }
    int check = Character.digit(s.charAt(length - 1), 10);
    return getStandardUPCEANChecksum(s.subSequence(0, length - 1)) == check;
  }

  static int getStandardUPCEANChecksum(CharSequence s) throws FormatException {
    int length = s.length();
    int sum = 0;
    for (int i = length - 1; i >= 0; i -= 2) {
      int digit = s.charAt(i) - '0';
      if (digit < 0 || digit > 9) {
        throw FormatException.getFormatInstance();
      }
      sum += digit;
    }
    sum *= 3;
    for (int i = length - 2; i >= 0; i -= 2) {
      int digit = s.charAt(i) - '0';
      if (digit < 0 || digit > 9) {
        throw FormatException.getFormatInstance();
      }
      sum += digit;
    }
    return (1000 - sum) % 10;
  }

  @Override
  public int getDefaultMargin() {
    // Use a different default more appropriate for UPC/EAN
    return 9;
  }

}
