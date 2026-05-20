package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class SearchPattern {

    private Byte[] pattern;
    private boolean isMasked;

    public SearchPattern(String input) throws IllegalArgumentException {
        parsePattern(input);
    }

    private void parsePattern(String input) {
        String[] parts = input.trim().split("\\s+");
        List<Byte> patternList = new ArrayList<>();

        for (String part : parts) {
            if (part.equals("??") || part.equals("?")) {
                patternList.add(null);
                isMasked = true;
            } else {
                try {
                    byte b = (byte) Integer.parseInt(part, 16);
                    patternList.add(b);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Неверный hex-байт: " + part);
                }
            }
        }

        if (patternList.isEmpty()) {
            throw new IllegalArgumentException("Пустой шаблон поиска");
        }

        pattern = patternList.toArray(new Byte[0]);
    }

    public int length() {
        return pattern.length;
    }

    public Byte getByteAt(int index) {
        return pattern[index];
    }

    public boolean isMasked() {
        return isMasked;
    }

    public boolean isExact() {
        return !isMasked;
    }
}