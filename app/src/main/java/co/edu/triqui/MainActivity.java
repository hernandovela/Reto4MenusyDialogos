package co.edu.triqui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

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
    private TextView status;
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
        root.setBackgroundColor(Color.rgb(24,24,24));
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        setContentView(root);
        TextView title = label("AndroidTicTacToe", 14, true);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(8), 0, 0, 0);
        title.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xffaaaaaa, 0xff555555}));
        title.setOnClickListener(v -> showAbout());
        title.setContentDescription(getString(R.string.about));
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(32)));
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(6), dp(6), dp(6), 0);
        scroll.addView(content);
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
                        int size = Math.min(MeasureSpec.getSize(widthSpec), dp(160));
                        super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY));
                    }
                };
                cell.setTextSize(54); cell.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
                cell.setPadding(0, 0, 0, 0); cell.setMinWidth(0); cell.setMinimumWidth(0);
                cell.setOnClickListener(v -> play(position));
                LinearLayout.LayoutParams tile = new LinearLayout.LayoutParams(0, -2, 1);
                tile.setMargins(dp(4), dp(4), dp(4), dp(4));
                line.addView(cell, tile); cells[position] = cell;
            }
        }
        status = label("", 16, false);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        status.setPadding(dp(8), dp(20), dp(8), dp(20));
        content.addView(status, new LinearLayout.LayoutParams(-1, 0, 1));
        // Recreate the three-item menu from the tutorial, using its XML definitions.
        PopupMenu menu = new PopupMenu(this, title);
        getMenuInflater().inflate(R.menu.options_menu, menu.getMenu());
        LinearLayout bottom = new LinearLayout(this);
        root.addView(bottom, new LinearLayout.LayoutParams(-1, -2));
        for (int id : new int[]{R.id.new_game, R.id.ai_difficulty, R.id.quit}) {
            MenuItem item = menu.getMenu().findItem(id);
            Button action = new Button(this);
            action.setText(item.getTitle()); action.setAllCaps(false); action.setTextSize(12);
            action.setTextColor(Color.BLACK); action.setPadding(dp(2), dp(6), dp(2), dp(6));
            android.graphics.drawable.Drawable icon = item.getIcon();
            if (icon != null) icon.setBounds(0, 0, dp(28), dp(28));
            action.setCompoundDrawables(null, icon, null, null);
            action.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{0xffffffff, 0xffb7b7b7}));
            action.setOnClickListener(v -> onOptionsItemSelected(item));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(68), 1);
            params.setMargins(dp(1), dp(1), dp(1), 0);
            bottom.addView(action, params);
        }
        render();
    }

    private TextView label(String text, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text); view.setTextSize(size); view.setGravity(Gravity.CENTER);
        view.setTextColor(Color.LTGRAY);
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
            cells[i].setTextColor(mark == 'X' ? Color.rgb(0,220,0) : Color.RED);
            boolean highlight = false;
            for (int index : winning) if (index == i) highlight = true;
            GradientDrawable background = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    mark == ' ' ? new int[]{0xffffffff, 0xffeeeeee} : new int[]{0xffc7c7c7, 0xff999999});
            background.setCornerRadius(dp(2)); background.setStroke(dp(2), highlight ? 0xffeeeeee : 0xff777777);
            cells[i].setBackground(background);
            cells[i].setContentDescription(getString(R.string.cell, i / 3 + 1, i % 3 + 1,
                    mark == ' ' ? getString(R.string.empty) : String.valueOf(mark)));
        }
        status.setText(result == 1 ? R.string.draw : result == 2 ? R.string.win : result == 3 ? R.string.lose : computerTurn ? R.string.thinking : R.string.your_turn);

    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu); return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.new_game) startNewGame();
        else if (id == R.id.ai_difficulty) showDifficulty();
        else if (id == R.id.quit) showQuit();
        else if (id == R.id.about) showAbout();
        else return super.onOptionsItemSelected(item);
        return true;
    }
    private void showAbout() {
        dismissDialog();
        dialog = new AlertDialog.Builder(this).setView(R.layout.about_dialog)
                .setPositiveButton(R.string.ok, null).show();
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
