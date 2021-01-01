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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.FormatException;
import com.google.zxing.common.BitMatrix;

import java.util.Collection;
import java.util.Collections;

/**
 * This object renders an UPC-E code as a {@link BitMatrix}.
 *
 * @author 0979097955s@gmail.com (RX)
 */
public final class UPCEWriter extends UPCEANWriter {

    /**
     * See {@link #L_AND_G_PATTERNS}; these values similarly represent patterns of
     * even-odd parity encodings of digits that imply both the number system (0 or 1)
     * used, and the check digit.
     */
    static final int[][] NUMSYS_AND_CHECK_DIGIT_PATTERNS = {
            {0x38, 0x34, 0x32, 0x31, 0x2C, 0x26, 0x23, 0x2A, 0x29, 0x25},
            {0x07, 0x0B, 0x0D, 0x0E, 0x13, 0x19, 0x1C, 0x15, 0x16, 0x1A}
    };

    private static final int CODE_WIDTH = 3 + // start guard
            (7 * 6) + // bars
            6; // end guard

    @Override
    protected Collection<BarcodeFormat> getSupportedWriteFormats() {
        return Collections.singleton(BarcodeFormat.UPC_E);
    }

    @Override
    public boolean[] encode(String contents) {
        int length = contents.length();
        switch (length) {
            case 7:
                // No check digit present, calculate it and add it
                int check;
                try {
                    check = UPCEANWriter.getStandardUPCEANChecksum(convertUPCEtoUPCA(contents));
                } catch (FormatException fe) {
                    throw new IllegalArgumentException(fe);
                }
                contents += check;
                break;
            case 8:
                try {
                    if (!UPCEANWriter.checkStandardUPCEANChecksum(convertUPCEtoUPCA(contents))) {
                        throw new IllegalArgumentException("Contents do not pass checksum");
                    }
                } catch (FormatException ignored) {
                    throw new IllegalArgumentException("Illegal contents");
                }
                break;
            default:
                throw new IllegalArgumentException(
                        "Requested contents should be 7 or 8 digits long, but got " + length);
        }

        checkNumeric(contents);

        int firstDigit = Character.digit(contents.charAt(0), 10);
        if (firstDigit != 0 && firstDigit != 1) {
            throw new IllegalArgumentException("Number system must be 0 or 1");
        }

        int checkDigit = Character.digit(contents.charAt(7), 10);
        int parities = NUMSYS_AND_CHECK_DIGIT_PATTERNS[firstDigit][checkDigit];
        boolean[] result = new boolean[CODE_WIDTH];

        int pos = appendPattern(result, 0, UPCEANWriter.START_END_PATTERN, true);

        for (int i = 1; i <= 6; i++) {
            int digit = Character.digit(contents.charAt(i), 10);
            if ((parities >> (6 - i) & 1) == 1) {
                digit += 10;
            }
            pos += appendPattern(result, pos, UPCEANWriter.L_AND_G_PATTERNS[digit], false);
        }

        appendPattern(result, pos, UPCEANWriter.END_PATTERN, false);

        return result;
    }

    /**
     * Expands a UPC-E value back into its full, equivalent UPC-A code value.
     *
     * @param upce UPC-E code as string of digits
     * @return equivalent UPC-A code as string of digits
     */
    public static String convertUPCEtoUPCA(String upce) {
        char[] upceChars = new char[6];
        upce.getChars(1, 7, upceChars, 0);
        StringBuilder result = new StringBuilder(12);
        result.append(upce.charAt(0));
        char lastChar = upceChars[5];
        switch (lastChar) {
            case '0':
            case '1':
            case '2':
                result.append(upceChars, 0, 2);
                result.append(lastChar);
                result.append("0000");
                result.append(upceChars, 2, 3);
                break;
            case '3':
                result.append(upceChars, 0, 3);
                result.append("00000");
                result.append(upceChars, 3, 2);
                break;
            case '4':
                result.append(upceChars, 0, 4);
                result.append("00000");
                result.append(upceChars[4]);
                break;
            default:
                result.append(upceChars, 0, 5);
                result.append("0000");
                result.append(lastChar);
                break;
        }
        // Only append check digit in conversion if supplied
        if (upce.length() >= 8) {
            result.append(upce.charAt(7));
        }
        return result.toString();
    }
}
