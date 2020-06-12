package com.burning.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RxReplaceTest {
    // Define replacement function
    final RxReplace tripler = new RxReplace("(\\d{1,2})") {
        public String replacement() {
            int intValue = Integer.parseInt(group(1));

            return String.valueOf(intValue * 3);
        }
    };

    @Test
    void rewrite() {
        assertEquals("3 6 9 12", tripler.rewrite("1 2 3 4"), "All entries in the string have been tripled by their integer value");
    }
}