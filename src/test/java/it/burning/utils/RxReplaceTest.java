package it.burning.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RxReplaceTest {
    // Define replacement function
    final RxReplace tripler = new RxReplace("(\\d{1,2})") {
        public String replacement() {
            return String.valueOf(Integer.parseInt(group(0)) * 3);
        }
    };

    @Test
    void rewrite() {
        assertEquals("3 6 9 12", tripler.replace("1 2 3 4"), "All entries in the string have been tripled by their integer value");
    }
}