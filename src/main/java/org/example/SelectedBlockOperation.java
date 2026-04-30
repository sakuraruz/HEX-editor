package org.example;

@FunctionalInterface
interface SelectedBlockOperation {
    void execute(int startRow, int startCol, int endRow, int endCol);
}
