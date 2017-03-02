package com.glide_app.glide;

import android.content.Context;
import android.util.DisplayMetrics;

import java.util.Random;

public class Coordinates {

    // Identifiers:
    //   1 - Horizontal or vertical line (xy = 1, 3, 5, or 7 then horizontal, xy = 2, 4, or 6 then vertical)
    //       Horizontal - {id, startX, endX, yPos, xy}
    //       Vertical - {id, startY, endY, xPos, xy}
    //   2 - Tap
    //       {id, x, y}
    //   3 - Slant line
    //       {id, startX, startY, endX, endY}
    
    private Context context;
    private int level;
    private int allDisconnected;
    private int lineTapProbability;

    // How many lines are connected together, used to calculate gradient in DrawElements
    public int connectedLines = 1;

    public Coordinates(Context c, int l) {
        context = c
        // Currently set to 1, but value is passed from another class, which ranges between 0 and 2
        level = l;

        // Number of lines to be drawn
        int quantity = quantity();
        Random r = new Random();

        // These probabilities are only set once per game
        // Chance for all lines to be disconnected
        allDisconnected = r.nextInt(100);
        // Chance to have either line or tap generated next
        lineTapProbability = r.nextInt(100);

        //drawTimeCalc(quantity);
        all();
    }

    // Determines the number of lines to be drawn
    public int quantity() {
        Random r = new Random();
        int probability = r.nextInt(100);

        // Determines the number of lines/taps to be generated
        if (level == 0) {
            // 10% chance to generate 1 line/tap
            // 30% chance to generate 2 lines/taps
            // 60% chance to generate 3 lines/taps
            if (probability <= 9) {
                return 1;
            } else if (probability <= 39) {
                return 2;
            } else if (probability <= 99) {
                return 3;
            }
        }
        // Equal chance to generate 3, 4, or 5 lines/taps (33% chance)
        else if (level == 1) {
            if (probability <= 33) {
                return 3;
            } else if (probability <= 66) {
                return 4;
            } else if (probability <= 99) {
                return 5;
            }
        }
        // Equal chance to generate 5, 6, or 7 lines/taps (33% chance)
        else if (level == 2) {
            if (probability <= 33) {
                return 5;
            } else if (probability <= 66) {
                return 6;
            } else if (probability <= 99) {
                return 7;
            }
        }

        return 0;
    }

    // Generates all line and tap coordinates
    public int[][] all() {
        Random r = new Random();
        int quantity = quantity();
        int[][] gameElements = new int[quantity][];

        // If probability is greater or equal to 60, meaning there is a 40% chance
        if (lineTapProbability >= 60) {
            // Draw all taps, 20% chance
            if (lineTapProbability > 80) {
                for (int i = 0; i < quantity; i++) {
                    gameElements[i] = generateTap();
                }
            }
            // Draw all lines, 20% chance
            else {
                // If connectedLines is true, then connect the current line with the previous line, if there is a previous line
                // Else if allDisconnected is less than 20, then all lines are disconnected (20% chance for this)
                // If allDisconnected is greater than or equal to 80, then all lines are connected (20% chance)
                for (int i = 0; i < quantity; i++) {
                    if (allDisconnected >= 80) {
                        if (i > 0 && gameElements[i - 1][0] == 1) {
                            int start = gameElements[i - 1][3];
                            int pos = gameElements[i - 1][2];
                            // If xy = 1, 3, 5, or 7 then horizontal line, if xy = 2, 4, 6, or 8 then vertical line
                            int xy = gameElements[i - 1][4] + 1;

                            connectedLines++;

                            if (gameElements[i - 1][4] == 1 || gameElements[i - 1][4] == 3 || gameElements[i - 1][4] == 5 || gameElements[i - 1][4] == 7) {
                                int end = generateY(start)[1];
                                gameElements[i] = new int[]{1, start, end, pos, xy};
                            } else {
                                int end = generateX(start)[1];
                                gameElements[i] = new int[]{1, start, end, pos, xy};
                            }
                        } else {
                            gameElements[i] = generateLine();
                        }
                    } else {
                        if (i > 0 && connectedLines() && allDisconnected >= 20) {
                            int start = gameElements[i - 1][3];
                            int pos = gameElements[i - 1][2];
                            // If xy = 1, 3, 5, or 7 then horizontal line, if xy = 2, 4, 6, or 8 then vertical line
                            int xy = gameElements[i - 1][4] + 1;

                            connectedLines++;

                            if (gameElements[i - 1][4] == 1 || gameElements[i - 1][4] == 3 || gameElements[i - 1][4] == 5 || gameElements[i - 1][4] == 7) {
                                int end = generateY(start)[1];
                                gameElements[i] = new int[]{1, start, end, pos, xy};
                            } else {
                                int end = generateX(start)[1];
                                gameElements[i] = new int[]{1, start, end, pos, xy};
                            }
                        } else {
                            gameElements[i] = generateLine();
                        }
                    }
                }
            }
            // Draw all slants, 13% chance
            else {
                for (int i = 0; i < quantity; i++) {
                    // If connectedLines is true, then connect the current line with the previous line, if there is a previous line
                    // Else if allDisconnected is less than 20, then all lines are disconnected (20% chance for this)
                    if (i > 0 && connectedLines() && allDisconnected >= 20) {
                        int startX = gameElements[i - 1][3];
                        int startY = gameElements[i - 1][4];
                        int endX = generateX()[1];
                        int endY = generateY()[1];

                        if (gameElements[i - 1][3] == 1) {
                            gameElements[i] = new int[]{3, startX, startY, endX, endY};
                        } else {
                            gameElements[i] = new int[]{3, startX, startY, endX, endY};
                        }
                    } else {
                    gameElements[i] = generateSlant();
                    }
                }
            }
        }
        // If probability is less than 60 (60% chance)
        else {
            for (int i = 0; i < quantity; i++) {
                // Determines the nextElement to be drawn
                // 1 - Horizontal or Vertical line
                // 2 - Tap
                // 3 - Slant line
                int nextElement = r.nextInt(3) + 1;

                // Generates a line for the next element
                if (nextElement == 1) {
                    // If connectedLines is true, then connect the current line with the previous line, if there is a previous line
                    // Else if allDisconnected is less than 20, then all lines are disconnected (20% chance for this)
                    // If allDisconnected is greater than or equal to 80, then all lines are connected (20% chance)
                    if (allDisconnected >= 80) {
                        if (i > 0 && gameElements[i - 1][0] == 1) {
                            int start = gameElements[i - 1][3];
                            int pos = gameElements[i - 1][2];
                            // If xy = 1, 3, 5, or 7 then horizontal line, if xy = 2, 4, 6, or 8 then vertical line
                            int xy = gameElements[i - 1][4] + 1;

                            connectedLines++;

                            if (gameElements[i - 1][4] == 1 || gameElements[i - 1][4] == 3 || gameElements[i - 1][4] == 5 || gameElements[i - 1][4] == 7) {
                                int end = generateY(start)[1];
                                gameElements[i] = new int[]{1, start, end, pos, xy};
                            } else {
                                int end = generateX(start)[1];
                                gameElements[i] = new int[]{1, start, end, pos, xy};
                            }
                        } else {
                            gameElements[i] = generateLine();
                        }
                    } else {
                        if (i > 0 && gameElements[i - 1][0] == 1 && connectedLines() && allDisconnected >= 20) {
                            int start = gameElements[i - 1][3];
                            int pos = gameElements[i - 1][2];
                            // If xy = 1, 3, 5, or 7 then horizontal line, if xy = 2, 4, 6, or 8 then vertical line
                            int xy = gameElements[i - 1][4] + 1;

                            connectedLines++;

                            if (gameElements[i - 1][4] == 1 || gameElements[i - 1][4] == 3 || gameElements[i - 1][4] == 5 || gameElements[i - 1][4] == 7) {
                                int end = generateY(start)[1];
                                gameElements[i] = new int[]{1, start, end, pos, xy};
                            } else {
                                int end = generateX(start)[1];
                                gameElements[i] = new int[]{1, start, end, pos, xy};
                            }
                        } else {
                            gameElements[i] = generateLine();
                        }
                    }
                }

                // Generates a tap for the next element
                else if (nextElement == 2) {
                    gameElements[i] = generateTap();
                }
                // Generates a slant for the next element
                else if (nextElement == 3) {
                    // If connectedLines is true, then connect the current line with the previous line, if there is a previous line
                    // Else if allDisconnected is less than 20, then all lines are disconnected (20% chance for this)
                    if (i > 0 && gameElements[i - 1][0] != 2 && connectedLines() && allDisconnected >= 20) {
                        int startX = gameElements[i - 1][3];
                        int startY = gameElements[i - 1][4];
                        int endX = generateX()[1];
                        int endY = generateY()[1];

                        if (gameElements[i - 1][3] == 1) {
                            gameElements[i] = new int[]{3, startX, startY, endX, endY};
                        } else {
                            gameElements[i] = new int[]{3, startX, startY, endX, endY};
                        }
                    } else {
                    gameElements[i] = generateSlant();
                    }
                }
            }
        }

        return gameElements;
    }

    // Generates the coordinates of a single line
    private int[] generateLine() {
        Random r = new Random();

        int[] genX = generateX(-1);

        int[] genY = generateY(-1);

        int startX = genX[0];
        int endX = genX[1];
        int startY = genY[0];
        int endY = genY[1];

        // Generates a number between 1 and 2, if 1 then line is horizontal, if 2 then line is vertical
        int xy = r.nextInt(3 - 1) + 1;

        // Line array contains id (1 for lines), starting x, ending x, y position, and vertical or horizontal
        if (xy == 1) {
            return new int[]{1, startX, endX, startY, 1};
        } else if (xy == 2) {
            return new int[]{1, startY, endY, startX, 2};
        }

        return null;
    }

    // Generates the coordinates of a slant line
    private int[] generateSlant() {
        Random r = new Random();

        int[] genX = generateX(-1);

        int[] genY = generateY(-1);

        int startX = genX[0];
        int endX = genX[1];
        int startY = genY[0];
        int endY = genY[1];

        return new int[]{3, startX, startY, endX, endY};
    }

    // Generates the coordinates of a single tap
    private int[] generateTap() {
        Random r = new Random();

        int x = generateX(-1)[0];
        int y = generateY(-1)[0];

        // Tap array contains id (2 for taps), and x and y coordinates
        return new int[]{2, x, y};
    }

    // Generates an int array with starting x coordinate and ending x coordinate
    private int[] generateX(int start) {
        // Gets the left and right edge of the game area
        DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
        int xLeft = (int) (15 * displaymetrics.density);
        int xRight = (int) (displaymetrics.widthPixels - (15 * displaymetrics.density));
        int endX;

        Random r = new Random();
        int startX = r.nextInt(xRight - xLeft) + xLeft;

        if (start == -1) {
            endX = randomExcluding(r, startX, xLeft, xRight);
        } else {
            endX = randomExcluding(r, start, xLeft, xRight);
        }

        return new int[]{startX, endX};
    }

    // Generates an int array with starting y coordinate and ending y coordinate
    private int[] generateY(int start) {
        // Gets the top and bottom edge of the game area
        DisplayMetrics displaymetrics = context.getResources().getDisplayMetrics();
        int yTop = (int) (60 * displaymetrics.density);
        int yBottom = (int) (displaymetrics.heightPixels - (52 * displaymetrics.density));
        int endY;

        Random r = new Random();
        int startY = r.nextInt(yBottom - yTop) + yTop;

        if (start == -1) {
            endY = randomExcluding(r, startY, yTop, yBottom);
        } else {
            endY = randomExcluding(r, start, yTop, yBottom);
        }

        return new int[]{startY, endY};
    }

    // Produces true if line is connected and false otherwise
    private boolean connectedLines() {
        Random r = new Random();
        int i = r.nextInt(2);

        return i == 0;
    }

    // Generates a random number between the ranges of start and end, excluding numbers +/- 150 pixels of start
    private int randomExcluding(Random r, int start, int startBound, int endBound) {
        int random;
        random = r.nextInt((endBound + 1) - startBound) + startBound;

        if ((random - start >= 150) || (random - start <= -150)) {
            return random;
        } else if ((random - start < 150) && (random - start > 0)) {
            if ((start + 150) > endBound) {
                return start - 150;
            } else {
                return start + 150;
            }
        } else if ((random - start > -150) && (random - start < 0)) {
            if ((start - 150) < startBound){
                return start + 150;
            } else {
                return start - 150;
            }
        } else if ((random - start) == 0) {
            if (random + 150 > endBound) {
                return random - 150;
            } else {
                return random + 150;
            }
        }

        return random;
    }
}
