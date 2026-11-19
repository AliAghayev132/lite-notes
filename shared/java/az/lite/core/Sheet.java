package az.lite.core;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Öz alt panelimiz (native AlertDialog əvəzinə). Activity-nin kök FrameLayout-una əlavə olunur,
 * ayrıca pəncərə yaratmır — yüngül və tam bizim dizaynımızdadır.
 */
public final class Sheet {

    public interface Pick {
        void on(int index);
    }

    private static final List<Sheet> STACK = new ArrayList<>();

    private final FrameLayout root;
    private final FrameLayout overlay;
    public final LinearLayout panel;
    private final LinearLayout content;
    private final LinearLayout buttons;
    private Runnable onDismiss;
    private boolean shown;

    public Sheet(FrameLayout root, String title) {
        this.root = root;
        Context c = root.getContext();

        overlay = new FrameLayout(c);
        overlay.setBackgroundColor(Ui.SCRIM);
        overlay.setClickable(true);
        overlay.setOnClickListener(v -> dismiss());

        panel = new LinearLayout(c);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(Ui.SURFACE);
        panel.setClickable(true); // panelə toxunuş arxaya keçməsin
        panel.setPadding(Ui.dp(c, 20), Ui.dp(c, 16), Ui.dp(c, 20), Ui.dp(c, 18));

        View bar = new View(c);
        bar.setBackgroundColor(Ui.ACCENT);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(Ui.dp(c, 36), Ui.dp(c, 4));
        blp.bottomMargin = Ui.dp(c, 14);
        panel.addView(bar, blp);

        if (title != null) panel.addView(Ui.title(c, title));

        content = new LinearLayout(c);
        content.setOrientation(LinearLayout.VERTICAL);
        panel.addView(content, new LinearLayout.LayoutParams(-1, -2));

        buttons = new LinearLayout(c);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.END);
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(-1, -2);
        bl.topMargin = Ui.dp(c, 16);
        panel.addView(buttons, bl);
        buttons.setVisibility(View.GONE);

        overlay.addView(panel, new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM));
    }

    public Context context() { return root.getContext(); }

    public Sheet add(View v) {
        content.addView(v, new LinearLayout.LayoutParams(-1, -2));
        return this;
    }

    public Sheet add(View v, ViewGroup.LayoutParams lp) {
        content.addView(v, lp);
        return this;
    }

    /** Düymə: basanda əvvəl panel bağlanır, sonra action işləyir. keepOpen=true olsa bağlanmır. */
    public Sheet button(String text, boolean primary, boolean keepOpen, Runnable action) {
        Context c = context();
        TextView b = Ui.button(c, text, primary);
        b.setOnClickListener(v -> {
            if (!keepOpen) dismiss();
            if (action != null) action.run();
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        if (buttons.getChildCount() > 0) lp.leftMargin = Ui.dp(c, 8);
        buttons.addView(b, lp);
        buttons.setVisibility(View.VISIBLE);
        return this;
    }

    public Sheet button(String text, boolean primary, Runnable action) {
        return button(text, primary, false, action);
    }

    public Sheet onDismiss(Runnable r) {
        onDismiss = r;
        return this;
    }

    public Sheet show() {
        if (shown) return this;
        shown = true;
        root.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        STACK.add(this);
        overlay.setAlpha(0f);
        overlay.animate().alpha(1f).setDuration(110).start();
        panel.setTranslationY(Ui.dp(context(), 40));
        panel.animate().translationY(0).setDuration(140).start();
        return this;
    }

    public void dismiss() {
        if (!shown) return;
        shown = false;
        STACK.remove(this);
        InputMethodManager imm = (InputMethodManager) context().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(overlay.getWindowToken(), 0);
        root.removeView(overlay);
        if (onDismiss != null) onDismiss.run();
    }

    public boolean isShown() { return shown; }

    /** Sadə menyu: başlıq + tam enli sətirlər. */
    public static Sheet menu(FrameLayout root, String title, String[] items, Pick pick) {
        final Sheet s = new Sheet(root, title);
        Context c = root.getContext();
        for (int i = 0; i < items.length; i++) {
            final int idx = i;
            String label = L.t(items[i]);
            TextView r = Ui.row(c, label);
            if (label.startsWith("!")) {        // "!" ilə başlayan = təhlükəli əməliyyat (qırmızı)
                r.setText(label.substring(1));
                r.setTextColor(Ui.DANGER);
            }
            r.setOnClickListener(v -> {
                s.dismiss();
                pick.on(idx);
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = Ui.dp(c, 2);
            s.add(r, lp);
        }
        return s.show();
    }

    public static Sheet confirm(FrameLayout root, String title, String message, String yes, Runnable onYes) {
        Context c = root.getContext();
        TextView m = new TextView(c);
        m.setText(message);
        m.setTextColor(Ui.TEXT2);
        m.setTextSize(15);
        return new Sheet(root, title).add(m)
                .button("Ləğv et", false, null)
                .button(yes, true, onYes)
                .show();
    }

    public interface Text { void on(String value); }

    /** Bir sahəli giriş paneli. inputType 0 → adi mətn. */
    public static Sheet ask(FrameLayout root, String title, String hint, String initial, int inputType, final Text cb) {
        Context c = root.getContext();
        final android.widget.EditText in = Ui.input(c, hint);
        if (inputType != 0) in.setInputType(inputType);
        if (initial != null) { in.setText(initial); in.setSelection(in.length()); }
        Sheet s = new Sheet(root, title).add(in)
                .button("Ləğv et", false, null)
                .button("Saxla", true, () -> cb.on(in.getText().toString().trim()))
                .show();
        in.requestFocus();
        in.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(in, 0);
        }, 150);
        return s;
    }

    public static boolean closeTop() {
        if (STACK.isEmpty()) return false;
        STACK.get(STACK.size() - 1).dismiss();
        return true;
    }

    public static void closeAll() {
        while (!STACK.isEmpty()) STACK.get(STACK.size() - 1).dismiss();
    }

    public static boolean anyOpen() { return !STACK.isEmpty(); }
}
