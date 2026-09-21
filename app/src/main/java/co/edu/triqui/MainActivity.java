package co.edu.triqui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

/** Native Android UI, with the game rules kept independently testable. */
public class MainActivity extends Activity {
    private final TicTacToeGame game = new TicTacToeGame();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Button[] cells = new Button[9];
    private TextView status, score;
    private Button difficulty;
    private boolean computerTurn;
    private int wins, draws, losses;
    private AlertDialog dialog;
    private final Runnable computerMove = () -> {
        if (!computerTurn || game.checkForWinner() != 0) return;
        int move = game.getComputerMove();
        if (move >= 0) game.setMove(TicTacToeGame.COMPUTER_PLAYER, move);
        computerTurn = false;
        finishTurn();
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        int level = getPreferences(MODE_PRIVATE).getInt("difficulty", 2);
        game.setDifficultyLevel(TicTacToeGame.DifficultyLevel.values()[Math.max(0, Math.min(2, level))]);
        if (state != null) {
            game.restore(state.getString("board"));
            computerTurn = state.getBoolean("computerTurn");
            wins = state.getInt("wins"); draws = state.getInt("draws"); losses = state.getInt("losses");
        }
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245,243,238));
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        setContentView(root);
        Toolbar toolbar = new Toolbar(this);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(Color.rgb(24,59,53));
        root.addView(toolbar, new LinearLayout.LayoutParams(-1, dp(56)));
        setActionBar(toolbar);
        ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(24), dp(20), dp(24), dp(24));
        scroll.addView(content);
        content.addView(label(getString(R.string.eyebrow), 11, false));
        TextView title = label(getString(R.string.title), 36, true);
        title.setPadding(0, dp(12), 0, dp(6)); content.addView(title);
        content.addView(label(getString(R.string.subtitle), 14, false));
        difficulty = new Button(this);
        difficulty.setAllCaps(false);
        difficulty.setOnClickListener(v -> showDifficulty());
        LinearLayout.LayoutParams chip = new LinearLayout.LayoutParams(-2, dp(52));
        chip.topMargin = dp(16); content.addView(difficulty, chip);
        TextView players = label(getString(R.string.players), 13, true);
        players.setPadding(0, dp(20), 0, dp(16)); content.addView(players);
        LinearLayout board = new LinearLayout(this);
        board.setOrientation(LinearLayout.VERTICAL);
        content.addView(board, new LinearLayout.LayoutParams(-1, -2));
        for (int row = 0; row < 3; row++) {
            LinearLayout line = new LinearLayout(this);
            board.addView(line, new LinearLayout.LayoutParams(-1, -2));
            for (int col = 0; col < 3; col++) {
                final int position = row * 3 + col;
                Button cell = new Button(this) {
                    @Override protected void onMeasure(int widthSpec, int heightSpec) {
                        int size = Math.min(MeasureSpec.getSize(widthSpec), dp(128));
                        super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY));
                    }
                };
                cell.setTextSize(36); cell.setTypeface(null, Typeface.BOLD);
                cell.setPadding(0, 0, 0, 0); cell.setMinWidth(0); cell.setMinimumWidth(0);
                cell.setOnClickListener(v -> play(position));
                LinearLayout.LayoutParams tile = new LinearLayout.LayoutParams(0, -2, 1);
                tile.setMargins(dp(4), dp(4), dp(4), dp(4));
                line.addView(cell, tile); cells[position] = cell;
            }
        }
        status = label("", 19, true);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        status.setPadding(0, dp(22), 0, dp(10)); content.addView(status);
        score = label("", 14, false); content.addView(score);
        Button restart = new Button(this);
        restart.setText(R.string.new_game); restart.setAllCaps(false);
        restart.setTextColor(Color.WHITE);
        restart.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(23,107,89)));
        restart.setOnClickListener(v -> startNewGame());
        LinearLayout.LayoutParams restartParams = new LinearLayout.LayoutParams(-1, dp(56));
        restartParams.topMargin = dp(22); content.addView(restart, restartParams);
        TextView hint = label(getString(R.string.hint), 12, false);
        hint.setPadding(0, dp(14), 0, 0); content.addView(hint);
        render();
    }

    private TextView label(String text, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text); view.setTextSize(size); view.setGravity(Gravity.CENTER);
        view.setTextColor(Color.rgb(24,59,53));
        if (bold) view.setTypeface(null, Typeface.BOLD);
        return view;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private void play(int position) {
        if (computerTurn || !game.setMove(TicTacToeGame.HUMAN_PLAYER, position)) return;
        if (game.checkForWinner() == 0) {
            computerTurn = true;
            handler.postDelayed(computerMove, 450);
        }
        finishTurn();
    }
    private void finishTurn() {
        switch (game.checkForWinner()) {
            case 1: draws++; break;
            case 2: wins++; break;
            case 3: losses++; break;
        }
        render();
    }
    private void startNewGame() {
        handler.removeCallbacks(computerMove);
        computerTurn = false; game.clearBoard(); render();
    }
    private void render() {
        int result = game.checkForWinner();
        int[] winning = game.winningLine();
        for (int i = 0; i < 9; i++) {
            char mark = game.at(i);
            cells[i].setText(mark == ' ' ? "" : String.valueOf(mark));
            cells[i].setEnabled(result == 0 && !computerTurn && mark == ' ');
            cells[i].setTextColor(mark == 'X' ? Color.rgb(23,107,89) : Color.rgb(191,90,55));
            boolean highlight = false;
            for (int index : winning) if (index == i) highlight = true;
            GradientDrawable background = new GradientDrawable();
            background.setColor(highlight ? Color.rgb(211,235,217) : Color.WHITE);
            background.setCornerRadius(dp(16)); background.setStroke(dp(1), Color.rgb(222,226,219));
            cells[i].setBackground(background);
            cells[i].setContentDescription(getString(R.string.cell, i / 3 + 1, i % 3 + 1,
                    mark == ' ' ? getString(R.string.empty) : String.valueOf(mark)));
        }
        status.setText(result == 1 ? R.string.draw : result == 2 ? R.string.win : result == 3 ? R.string.lose : computerTurn ? R.string.thinking : R.string.your_turn);
        score.setText(getString(R.string.score, wins, draws, losses));
        difficulty.setText(getString(R.string.difficulty_changed, getResources().getStringArray(R.array.difficulty_levels)[game.getDifficultyLevel().ordinal()]));
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu); return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.new_game) startNewGame();
        else if (id == R.id.ai_difficulty) showDifficulty();
        else if (id == R.id.quit) showQuit();
        else if (id == R.id.about) {
            dismissDialog();
            dialog = new AlertDialog.Builder(this).setView(R.layout.about_dialog)
                    .setPositiveButton(R.string.ok, null).show();
        } else return super.onOptionsItemSelected(item);
        return true;
    }
    private void dismissDialog() { if (dialog != null) dialog.dismiss(); }
    private void showDifficulty() {
        dismissDialog();
        String[] levels = getResources().getStringArray(R.array.difficulty_levels);
        dialog = new AlertDialog.Builder(this).setTitle(R.string.difficulty_choose)
                .setSingleChoiceItems(levels, game.getDifficultyLevel().ordinal(), (d, choice) -> {
                    game.setDifficultyLevel(TicTacToeGame.DifficultyLevel.values()[choice]);
                    getPreferences(MODE_PRIVATE).edit().putInt("difficulty", choice).apply();
                    d.dismiss(); render();
                    Toast.makeText(this, getString(R.string.difficulty_changed, levels[choice]), Toast.LENGTH_SHORT).show();
                }).show();
    }
    private void showQuit() {
        dismissDialog();
        dialog = new AlertDialog.Builder(this).setTitle(R.string.quit_question).setMessage(R.string.quit_detail)
                .setCancelable(false).setPositiveButton(R.string.yes, (d, which) -> finish())
                .setNegativeButton(R.string.no, null).show();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("board", game.snapshot()); state.putBoolean("computerTurn", computerTurn);
        state.putInt("wins", wins); state.putInt("draws", draws); state.putInt("losses", losses);
    }
    @Override protected void onPause() { super.onPause(); handler.removeCallbacks(computerMove); }
    @Override protected void onResume() {
        super.onResume();
        if (computerTurn) handler.postDelayed(computerMove, 450);
    }
    @Override protected void onDestroy() { dismissDialog(); handler.removeCallbacks(computerMove); super.onDestroy(); }
}
