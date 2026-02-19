package org.example;

import java.io.IOException;

@FunctionalInterface
interface SelectedBlockOperation {
    void execute(int startRow, int startCol, int endRow, int endCol);
}
