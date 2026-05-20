package org.example.controller;

@FunctionalInterface
interface SelectedBlockOperation {
    void execute(int startRow, int startCol, int endRow, int endCol);
}
