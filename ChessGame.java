import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;

public class ChessGame extends JFrame {

    private final JButton[][] board = new JButton[8][8];
    private String[][] pieces = new String[8][8];

    private final Deque<GameState> history = new ArrayDeque<>();

    private boolean whiteTurn = true;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private final JTextArea movesArea = new JTextArea();

    // Castling state
    private boolean wKingMoved = false;
    private boolean bKingMoved = false;

    private boolean wRookLeftMoved = false;
    private boolean wRookRightMoved = false;

    private boolean bRookLeftMoved = false;
    private boolean bRookRightMoved = false;

    // En passant target square
    private int[] enPassantTarget = null;

    private int moveNumber = 1;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public ChessGame() {

        setTitle("Java Chess Game");

        setSize(950, 700);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        initializeBoard();

        // Chess board
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));

        for (int r = 0; r < 8; r++) {

            for (int c = 0; c < 8; c++) {

                JButton button = new JButton();

                button.setFont(
                        new Font(
                                "Segoe UI Symbol",
                                Font.PLAIN,
                                32
                        )
                );

                button.setFocusPainted(false);

                board[r][c] = button;

                int row = r;
                int col = c;

                button.addActionListener(
                        e -> handleClick(row, col)
                );

                boardPanel.add(button);
            }
        }

        // Right side
        JPanel side = new JPanel(
                new BorderLayout()
        );

        side.setPreferredSize(
                new Dimension(230, 0)
        );

        movesArea.setEditable(false);

        movesArea.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        14
                )
        );

        side.add(
                new JScrollPane(movesArea),
                BorderLayout.CENTER
        );

        // Buttons
        JPanel buttons = new JPanel(
                new GridLayout(2, 1, 5, 5)
        );

        JButton undo = new JButton("Undo");

        undo.addActionListener(
                e -> undoMove()
        );

        JButton draw = new JButton("Offer Draw");

        draw.addActionListener(
                e -> JOptionPane.showMessageDialog(
                        this,
                        "Draw agreed!"
                )
        );

        buttons.add(undo);
        buttons.add(draw);

        side.add(
                buttons,
                BorderLayout.SOUTH
        );

        add(
                boardPanel,
                BorderLayout.CENTER
        );

        add(
                side,
                BorderLayout.EAST
        );

        refreshBoard();

        setLocationRelativeTo(null);
    }

    // ============================================================
    // INITIALIZE BOARD
    // ============================================================

    private void initializeBoard() {

        String[] back = {
                "R", "N", "B", "Q",
                "K", "B", "N", "R"
        };

        for (int c = 0; c < 8; c++) {

            // Black
            pieces[0][c] = "b" + back[c];
            pieces[1][c] = "bP";

            // White
            pieces[6][c] = "wP";
            pieces[7][c] = "w" + back[c];
        }
    }

    // ============================================================
    // HANDLE CLICK
    // ============================================================

    private void handleClick(int r, int c) {

        // Select a piece
        if (selectedRow == -1) {

            if (pieces[r][c] != null &&
                    isCurrentPlayerPiece(pieces[r][c])) {

                selectedRow = r;
                selectedCol = c;

                highlightLegalMoves(
                        r,
                        c
                );
            }

            return;
        }

        // Click same square again
        if (r == selectedRow &&
                c == selectedCol) {

            resetSelection();

            return;
        }

        // Valid move
        if (isLegalMove(
                selectedRow,
                selectedCol,
                r,
                c
        )) {

            saveState();

            String movingPiece =
                    pieces[selectedRow][selectedCol];

            String capturedPiece =
                    pieces[r][c];

            String from =
                    getSquareName(
                            selectedRow,
                            selectedCol
                    );

            String to =
                    getSquareName(
                            r,
                            c
                    );

            makeMove(
                    selectedRow,
                    selectedCol,
                    r,
                    c
            );

            whiteTurn = !whiteTurn;

            String moveText;

            if (whiteTurn) {

                moveText =
                        moveNumber +
                        ". ... ";

            } else {

                moveText =
                        moveNumber +
                        ". ";
            }

            moveText +=
                    from +
                    "-" +
                    to;

            if (capturedPiece != null) {

                moveText += " x";
            }

            if (movingPiece.charAt(1) == 'K' &&
                    Math.abs(c - selectedCol) == 2) {

                if (c == 6) {

                    moveText += " O-O";

                } else {

                    moveText += " O-O-O";
                }
            }

            movesArea.append(
                    moveText +
                    "\n"
            );

            if (!whiteTurn) {

                moveNumber++;
            }

            resetSelection();

            refreshBoard();

            checkGameStatus();

        } else {

            // Select another own piece
            if (pieces[r][c] != null &&
                    isCurrentPlayerPiece(pieces[r][c])) {

                selectedRow = r;
                selectedCol = c;

                highlightLegalMoves(
                        r,
                        c
                );

            } else {

                resetSelection();
            }
        }
    }

    // ============================================================
    // MAKE MOVE
    // ============================================================

    private void makeMove(
            int sr,
            int sc,
            int dr,
            int dc) {

        String piece =
                pieces[sr][sc];

        // En passant capture
        if (piece.charAt(1) == 'P' &&
                enPassantTarget != null &&
                dr == enPassantTarget[0] &&
                dc == enPassantTarget[1] &&
                pieces[dr][dc] == null) {

            pieces[sr][dc] = null;
        }

        // Castling
        if (piece.charAt(1) == 'K' &&
                Math.abs(dc - sc) == 2) {

            // King side
            if (dc == 6) {

                pieces[sr][5] =
                        pieces[sr][7];

                pieces[sr][7] = null;

            }

            // Queen side
            else {

                pieces[sr][3] =
                        pieces[sr][0];

                pieces[sr][0] = null;
            }
        }

        // King movement
        if (piece.equals("wK")) {

            wKingMoved = true;
        }

        if (piece.equals("bK")) {

            bKingMoved = true;
        }

        // Rook movement
        if (piece.equals("wR") &&
                sr == 7 &&
                sc == 0) {

            wRookLeftMoved = true;
        }

        if (piece.equals("wR") &&
                sr == 7 &&
                sc == 7) {

            wRookRightMoved = true;
        }

        if (piece.equals("bR") &&
                sr == 0 &&
                sc == 0) {

            bRookLeftMoved = true;
        }

        if (piece.equals("bR") &&
                sr == 0 &&
                sc == 7) {

            bRookRightMoved = true;
        }

        // Rook captured on original square
        if (dr == 7 &&
                dc == 0 &&
                "wR".equals(pieces[dr][dc])) {

            wRookLeftMoved = true;
        }

        if (dr == 7 &&
                dc == 7 &&
                "wR".equals(pieces[dr][dc])) {

            wRookRightMoved = true;
        }

        if (dr == 0 &&
                dc == 0 &&
                "bR".equals(pieces[dr][dc])) {

            bRookLeftMoved = true;
        }

        if (dr == 0 &&
                dc == 7 &&
                "bR".equals(pieces[dr][dc])) {

            bRookRightMoved = true;
        }

        // Reset en passant
        enPassantTarget = null;

        // Set en passant target
        if (piece.charAt(1) == 'P' &&
                Math.abs(dr - sr) == 2) {

            enPassantTarget =
                    new int[]{
                            (sr + dr) / 2,
                            sc
                    };
        }

        // Pawn promotion
        if (piece.equals("wP") &&
                dr == 0) {

            piece =
                    choosePromotion("White");
        }

        if (piece.equals("bP") &&
                dr == 7) {

            piece =
                    choosePromotion("Black");
        }

        pieces[dr][dc] = piece;

        pieces[sr][sc] = null;
    }

    // ============================================================
    // PAWN PROMOTION
    // ============================================================

    private String choosePromotion(
            String color) {

        String[] choices = {
                "Queen",
                "Rook",
                "Bishop",
                "Knight"
        };

        String choice =
                (String) JOptionPane.showInputDialog(
                        this,
                        color +
                                " pawn promotion:",
                        "Pawn Promotion",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        choices,
                        choices[0]
                );

        if (choice == null) {

            choice = "Queen";
        }

        String prefix =
                color.equals("White")
                        ? "w"
                        : "b";

        switch (choice) {

            case "Rook":
                return prefix + "R";

            case "Bishop":
                return prefix + "B";

            case "Knight":
                return prefix + "N";

            default:
                return prefix + "Q";
        }
    }

    // ============================================================
    // LEGAL MOVE
    // ============================================================

    private boolean isLegalMove(
            int sr,
            int sc,
            int dr,
            int dc) {

        if (!insideBoard(sr, sc) ||
                !insideBoard(dr, dc)) {

            return false;
        }

        if (!validMove(
                sr,
                sc,
                dr,
                dc
        )) {

            return false;
        }

        String[][] backup =
                copyBoard();

        boolean oldWK =
                wKingMoved;

        boolean oldBK =
                bKingMoved;

        boolean oldWRL =
                wRookLeftMoved;

        boolean oldWRR =
                wRookRightMoved;

        boolean oldBRL =
                bRookLeftMoved;

        boolean oldBRR =
                bRookRightMoved;

        int[] oldEP =
                copyTarget(
                        enPassantTarget
                );

        String moving =
                pieces[sr][sc];

        boolean movingWhite =
                moving.charAt(0) == 'w';

        makeMoveForTest(
                sr,
                sc,
                dr,
                dc
        );

        boolean safe =
                !isInCheck(movingWhite);

        pieces = backup;

        wKingMoved = oldWK;
        bKingMoved = oldBK;

        wRookLeftMoved = oldWRL;
        wRookRightMoved = oldWRR;

        bRookLeftMoved = oldBRL;
        bRookRightMoved = oldBRR;

        enPassantTarget = oldEP;

        return safe;
    }

    // ============================================================
    // TEST MOVE
    // ============================================================

    private void makeMoveForTest(
            int sr,
            int sc,
            int dr,
            int dc) {

        String piece =
                pieces[sr][sc];

        // En passant
        if (piece.charAt(1) == 'P' &&
                enPassantTarget != null &&
                dr == enPassantTarget[0] &&
                dc == enPassantTarget[1] &&
                pieces[dr][dc] == null) {

            pieces[sr][dc] = null;
        }

        // Castling
        if (piece.charAt(1) == 'K' &&
                Math.abs(dc - sc) == 2) {

            if (dc == 6) {

                pieces[sr][5] =
                        pieces[sr][7];

                pieces[sr][7] = null;

            } else {

                pieces[sr][3] =
                        pieces[sr][0];

                pieces[sr][0] = null;
            }
        }

        pieces[dr][dc] =
                piece;

        pieces[sr][sc] =
                null;
    }

    // ============================================================
    // VALID BASIC MOVE
    // ============================================================

    private boolean validMove(
            int sr,
            int sc,
            int dr,
            int dc) {

        if (sr == dr &&
                sc == dc) {

            return false;
        }

        String piece =
                pieces[sr][sc];

        if (piece == null) {

            return false;
        }

        String target =
                pieces[dr][dc];

        // Cannot capture own piece
        if (target != null &&
                target.charAt(0) ==
                        piece.charAt(0)) {

            return false;
        }

        int rowDiff =
                dr - sr;

        int colDiff =
                dc - sc;

        char type =
                piece.charAt(1);

        boolean white =
                piece.charAt(0) == 'w';

        // ---------------- PAWN ----------------

        if (type == 'P') {

            int direction =
                    white ? -1 : 1;

            int startRow =
                    white ? 6 : 1;

            // One step
            if (dc == sc &&
                    dr == sr + direction &&
                    target == null) {

                return true;
            }

            // Two steps
            if (dc == sc &&
                    sr == startRow &&
                    dr == sr + 2 * direction &&
                    target == null &&
                    pieces[
                            sr + direction
                            ][sc] == null) {

                return true;
            }

            // Capture
            if (Math.abs(colDiff) == 1 &&
                    dr == sr + direction &&
                    target != null) {

                return true;
            }

            // En passant
            return Math.abs(colDiff) == 1 &&
                    dr == sr + direction &&
                    target == null &&
                    enPassantTarget != null &&
                    dr == enPassantTarget[0] &&
                    dc == enPassantTarget[1];
        }

        // ---------------- KNIGHT ----------------

        if (type == 'N') {

            return
                    (Math.abs(rowDiff) == 2 &&
                            Math.abs(colDiff) == 1)
                            ||
                    (Math.abs(rowDiff) == 1 &&
                            Math.abs(colDiff) == 2);
        }

        // ---------------- BISHOP ----------------

        if (type == 'B') {

            return
                    Math.abs(rowDiff) ==
                            Math.abs(colDiff)
                            &&
                    pathClear(
                            sr,
                            sc,
                            dr,
                            dc
                    );
        }

        // ---------------- ROOK ----------------

        if (type == 'R') {

            return
                    (sr == dr ||
                            sc == dc)
                            &&
                    pathClear(
                            sr,
                            sc,
                            dr,
                            dc
                    );
        }

        // ---------------- QUEEN ----------------

        if (type == 'Q') {

            boolean straight =
                    sr == dr ||
                            sc == dc;

            boolean diagonal =
                    Math.abs(rowDiff) ==
                            Math.abs(colDiff);

            return
                    (straight || diagonal)
                            &&
                    pathClear(
                            sr,
                            sc,
                            dr,
                            dc
                    );
        }

        // ---------------- KING ----------------

        if (type == 'K') {

            // Normal move
            if (Math.abs(rowDiff) <= 1 &&
                    Math.abs(colDiff) <= 1) {

                return true;
            }

            // Castling
            return
                    rowDiff == 0 &&
                    Math.abs(colDiff) == 2 &&
                    canCastle(
                            sr,
                            sc,
                            dc,
                            white
                    );
        }

        return false;
    }

    // ============================================================
    // CASTLING
    // ============================================================

    private boolean canCastle(
            int row,
            int col,
            int destinationCol,
            boolean white) {

        if (white && row != 7 ||
                !white && row != 0 ||
                col != 4) {

            return false;
        }

        // King cannot castle while in check
        if (isInCheck(white)) {

            return false;
        }

        // King side
        if (destinationCol == 6) {

            if (white) {

                if (wKingMoved ||
                        wRookRightMoved) {

                    return false;
                }

            } else {

                if (bKingMoved ||
                        bRookRightMoved) {

                    return false;
                }
            }

            if (!(white ? "wR" : "bR")
                    .equals(pieces[row][7])) {

                return false;
            }

            if (pieces[row][5] != null ||
                    pieces[row][6] != null) {

                return false;
            }

            return
                    !squareUnderAttack(
                            row,
                            5,
                            !white
                    )
                    &&
                    !squareUnderAttack(
                            row,
                            6,
                            !white
                    );
        }

        // Queen side
        if (destinationCol == 2) {

            if (white) {

                if (wKingMoved ||
                        wRookLeftMoved) {

                    return false;
                }

            } else {

                if (bKingMoved ||
                        bRookLeftMoved) {

                    return false;
                }
            }

            if (!(white ? "wR" : "bR")
                    .equals(pieces[row][0])) {

                return false;
            }

            if (pieces[row][1] != null ||
                    pieces[row][2] != null ||
                    pieces[row][3] != null) {

                return false;
            }

            return
                    !squareUnderAttack(
                            row,
                            3,
                            !white
                    )
                    &&
                    !squareUnderAttack(
                            row,
                            2,
                            !white
                    );
        }

        return false;
    }

    // ============================================================
    // PATH CLEAR
    // ============================================================

    private boolean pathClear(
            int sr,
            int sc,
            int dr,
            int dc) {

        int rowStep =
                Integer.compare(
                        dr,
                        sr
                );

        int colStep =
                Integer.compare(
                        dc,
                        sc
                );

        int r =
                sr + rowStep;

        int c =
                sc + colStep;

        while (r != dr ||
                c != dc) {

            if (pieces[r][c] != null) {

                return false;
            }

            r += rowStep;
            c += colStep;
        }

        return true;
    }

    // ============================================================
    // CHECK
    // ============================================================

    private boolean isInCheck(
            boolean white) {

        int[] king =
                findKing(white);

        if (king == null) {

            return true;
        }

        return squareUnderAttack(
                king[0],
                king[1],
                !white
        );
    }

    // ============================================================
    // FIND KING
    // ============================================================

    private int[] findKing(
            boolean white) {

        String king =
                white ? "wK" : "bK";

        for (int r = 0; r < 8; r++) {

            for (int c = 0; c < 8; c++) {

                if (king.equals(
                        pieces[r][c])) {

                    return new int[]{
                            r,
                            c
                    };
                }
            }
        }

        return null;
    }

    // ============================================================
    // SQUARE UNDER ATTACK
    // ============================================================

    private boolean squareUnderAttack(
            int row,
            int col,
            boolean byWhite) {

        for (int r = 0; r < 8; r++) {

            for (int c = 0; c < 8; c++) {

                String piece =
                        pieces[r][c];

                if (piece == null) {
                    continue;
                }

                boolean pieceWhite =
                        piece.charAt(0) == 'w';

                if (pieceWhite != byWhite) {
                    continue;
                }

                char type =
                        piece.charAt(1);

                int rowDiff =
                        row - r;

                int colDiff =
                        col - c;

                // Pawn
                if (type == 'P') {

                    int direction =
                            byWhite ? -1 : 1;

                    if (rowDiff == direction &&
                            Math.abs(colDiff) == 1) {

                        return true;
                    }
                }

                // Knight
                else if (type == 'N') {

                    if (
                            (Math.abs(rowDiff) == 2 &&
                                    Math.abs(colDiff) == 1)
                                    ||
                            (Math.abs(rowDiff) == 1 &&
                                    Math.abs(colDiff) == 2)
                    ) {

                        return true;
                    }
                }

                // Bishop / Queen
                else if (type == 'B' ||
                        type == 'Q') {

                    if (Math.abs(rowDiff) ==
                            Math.abs(colDiff) &&
                            pathClear(
                                    r,
                                    c,
                                    row,
                                    col
                            )) {

                        return true;
                    }
                }

                // Rook / Queen
                if (type == 'R' ||
                        type == 'Q') {

                    if ((rowDiff == 0 ||
                            colDiff == 0) &&
                            pathClear(
                                    r,
                                    c,
                                    row,
                                    col
                            )) {

                        return true;
                    }
                }

                // King
                if (type == 'K') {

                    if (Math.abs(rowDiff) <= 1 &&
                            Math.abs(colDiff) <= 1) {

                        return true;
                    }
                }
            }
        }

        return false;
    }

    // ============================================================
    // CHECK / CHECKMATE / STALEMATE
    // ============================================================

    private void checkGameStatus() {

        boolean currentPlayer =
                whiteTurn;

        boolean hasMove =
                hasAnyLegalMove(
                        currentPlayer
                );

        if (!hasMove) {

            if (isInCheck(
                    currentPlayer
            )) {

                JOptionPane.showMessageDialog(
                        this,
                        (currentPlayer
                                ? "White"
                                : "Black")
                                +
                                " is CHECKMATED!"
                );

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "STALEMATE! The game is a draw."
                );
            }

        } else if (isInCheck(
                currentPlayer
        )) {

            JOptionPane.showMessageDialog(
                    this,
                    (currentPlayer
                            ? "White"
                            : "Black")
                            +
                            " is in CHECK!"
            );
        }
    }

    private boolean hasAnyLegalMove(
            boolean white) {

        for (int r = 0; r < 8; r++) {

            for (int c = 0; c < 8; c++) {

                if (pieces[r][c] != null &&
                        (pieces[r][c].charAt(0) == 'w')
                                == white) {

                    for (int dr = 0; dr < 8; dr++) {

                        for (int dc = 0; dc < 8; dc++) {

                            if (isLegalMove(
                                    r,
                                    c,
                                    dr,
                                    dc
                            )) {

                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    // ============================================================
    // SAVE STATE
    // ============================================================

    private void saveState() {

        history.push(
                new GameState(this)
        );
    }

    // ============================================================
    // UNDO
    // ============================================================

    private void undoMove() {

        if (history.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "No moves to undo."
            );

            return;
        }

        GameState previous =
                history.pop();

        restoreState(previous);

        resetSelection();

        refreshBoard();

        movesArea.setText(
                "Previous move undone.\n"
        );
    }

    // ============================================================
    // HIGHLIGHT LEGAL MOVES
    // ============================================================

    private void highlightLegalMoves(
            int row,
            int col) {

        resetBoardColors();

        board[row][col]
                .setBackground(
                        Color.YELLOW
                );

        for (int r = 0; r < 8; r++) {

            for (int c = 0; c < 8; c++) {

                if (isLegalMove(
                        row,
                        col,
                        r,
                        c
                )) {

                    if (pieces[r][c] == null) {

                        board[r][c]
                                .setBackground(
                                        new Color(
                                                144,
                                                238,
                                                144
                                        )
                                );

                    } else {

                        board[r][c]
                                .setBackground(
                                        new Color(
                                                255,
                                                150,
                                                150
                                        )
                                );
                    }
                }
            }
        }
    }

    // ============================================================
    // RESET COLORS
    // ============================================================

    private void resetBoardColors() {

        for (int r = 0; r < 8; r++) {

            for (int c = 0; c < 8; c++) {

                if ((r + c) % 2 == 0) {

                    board[r][c]
                            .setBackground(
                                    Color.WHITE
                            );

                } else {

                    board[r][c]
                            .setBackground(
                                    Color.GRAY
                            );
                }
            }
        }
    }

    // ============================================================
    // RESET SELECTION
    // ============================================================

    private void resetSelection() {

        selectedRow = -1;

        selectedCol = -1;

        resetBoardColors();
    }

    // ============================================================
    // REFRESH BOARD
    // ============================================================

    private void refreshBoard() {

        for (int r = 0; r < 8; r++) {

            for (int c = 0; c < 8; c++) {

                if (pieces[r][c] == null) {

                    board[r][c]
                            .setText("");

                } else {

                    board[r][c]
                            .setText(
                                    getUnicodePiece(
                                            pieces[r][c]
                                    )
                            );
                }
            }
        }

        resetBoardColors();
    }

    // ============================================================
    // CHESS SYMBOLS
    // ============================================================

    private String getUnicodePiece(
            String piece) {

        switch (piece) {

            case "wK":
                return "♔";

            case "wQ":
                return "♕";

            case "wR":
                return "♖";

            case "wB":
                return "♗";

            case "wN":
                return "♘";

            case "wP":
                return "♙";

            case "bK":
                return "♚";

            case "bQ":
                return "♛";

            case "bR":
                return "♜";

            case "bB":
                return "♝";

            case "bN":
                return "♞";

            case "bP":
                return "♟";

            default:
                return "";
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private boolean isCurrentPlayerPiece(
            String piece) {

        return
                (piece.charAt(0) == 'w')
                        == whiteTurn;
    }

    private boolean insideBoard(
            int row,
            int col) {

        return row >= 0 &&
                row < 8 &&
                col >= 0 &&
                col < 8;
    }

    private String getSquareName(
            int row,
            int col) {

        char file =
                (char) ('a' + col);

        int rank =
                8 - row;

        return "" +
                file +
                rank;
    }

    private String[][] copyBoard() {

        return copyBoard(pieces);
    }

    private String[][] copyBoard(
            String[][] source) {

        String[][] copy =
                new String[8][8];

        for (int r = 0; r < 8; r++) {

            System.arraycopy(
                    source[r],
                    0,
                    copy[r],
                    0,
                    8
            );
        }

        return copy;
    }

    private int[] copyTarget(
            int[] target) {

        if (target == null) {

            return null;
        }

        return new int[]{
                target[0],
                target[1]
        };
    }

    // ============================================================
    // GAME STATE
    // ============================================================

    private static class GameState {

        String[][] pieces;

        boolean whiteTurn;

        boolean wKingMoved;
        boolean bKingMoved;

        boolean wRookLeftMoved;
        boolean wRookRightMoved;

        boolean bRookLeftMoved;
        boolean bRookRightMoved;

        int[] enPassantTarget;

        int moveNumber;

        GameState(
                ChessGame game) {

            pieces =
                    game.copyBoard();

            whiteTurn =
                    game.whiteTurn;

            wKingMoved =
                    game.wKingMoved;

            bKingMoved =
                    game.bKingMoved;

            wRookLeftMoved =
                    game.wRookLeftMoved;

            wRookRightMoved =
                    game.wRookRightMoved;

            bRookLeftMoved =
                    game.bRookLeftMoved;

            bRookRightMoved =
                    game.bRookRightMoved;

            enPassantTarget =
                    game.copyTarget(
                            game.enPassantTarget
                    );

            moveNumber =
                    game.moveNumber;
        }
    }

    // ============================================================
    // RESTORE GAME STATE
    // ============================================================

    private void restoreState(
            GameState state) {

        pieces =
                copyBoard(
                        state.pieces
                );

        whiteTurn =
                state.whiteTurn;

        wKingMoved =
                state.wKingMoved;

        bKingMoved =
                state.bKingMoved;

        wRookLeftMoved =
                state.wRookLeftMoved;

        wRookRightMoved =
                state.wRookRightMoved;

        bRookLeftMoved =
                state.bRookLeftMoved;

        bRookRightMoved =
                state.bRookRightMoved;

        enPassantTarget =
                copyTarget(
                        state.enPassantTarget
                );

        moveNumber =
                state.moveNumber;
    }

    // ============================================================
    // MAIN METHOD
    // ============================================================

    public static void main(
            String[] args) {

        SwingUtilities.invokeLater(
                () -> {

                    ChessGame game =
                            new ChessGame();

                    game.setVisible(true);
                }
        );
    }
}