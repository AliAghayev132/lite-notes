package az.lite.notes;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import az.lite.core.L;
import az.lite.core.LiteActivity;
import az.lite.core.Sheet;
import az.lite.core.Ui;

/** Qeyd redaktoru: adi mətn və ya yoxlama siyahısı. Avtomatik saxlanılır. */
public class EditActivity extends LiteActivity {

    private Store store;
    private Store.Note note;
    private boolean isNew;
    private EditText title, text;
    private LinearLayout itemsBox, content;
    private final List<EditText> itemEdits = new ArrayList<>();
    private final List<TextView> itemChecks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        L.init(this);
        super.onCreate(b);
        store = Store.get(this);
        long id = getIntent().getLongExtra("id", 0);
        note = id == 0 ? null : store.byId(id);
        if (note == null) {
            note = new Store.Note();
            note.id = System.currentTimeMillis();
            note.checklist = getIntent().getBooleanExtra("list", false);
            if (note.checklist) note.items.add(new Store.Item());
            isNew = true;
        }
        setTitleText(note.checklist ? L.t("Siyahı") : L.t("Qeyd"));
        action(note.pinned ? "★" : "☆", v -> {
            note.pinned = !note.pinned;
            ((TextView) v).setText(note.pinned ? "★" : "☆");
            toast(note.pinned ? L.t("Bərkidildi") : L.t("Bərkitmə götürüldü"));
        });
        action(L.t("Rəng"), v -> pickColor());
        action("⋯", v -> menu());

        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(8), dp(16), dp(24));
        sv.addView(content);
        body.addView(sv);

        title = plain(L.t("Başlıq"), 22, true);
        title.setText(note.title);
        content.addView(title);

        if (note.checklist) {
            itemsBox = new LinearLayout(this);
            itemsBox.setOrientation(LinearLayout.VERTICAL);
            content.addView(itemsBox);
            for (Store.Item it : note.items) addItemRow(it, false);
            TextView add = new TextView(this);
            add.setText(L.t("+ Bənd əlavə et"));
            add.setTextColor(Ui.ACCENT);
            add.setTypeface(Ui.BOLD);
            add.setPadding(0, dp(14), 0, dp(14));
            add.setOnClickListener(v -> addItemRow(new Store.Item(), true));
            content.addView(add);
        } else {
            text = plain(L.t("Qeydini yaz…"), 16, false);
            text.setSingleLine(false);
            text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            text.setGravity(Gravity.TOP);
            text.setMinLines(12);
            text.setText(note.body);
            content.addView(text, new LinearLayout.LayoutParams(-1, -2));
            if (isNew) text.requestFocus();
        }
        paint();
    }

    private EditText plain(String hint, int sp, boolean bold) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextColor(Ui.TEXT);
        e.setHintTextColor(Ui.TEXT2);
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        if (bold) e.setTypeface(Ui.BOLD);
        e.setBackground(null);
        e.setPadding(0, dp(8), 0, dp(8));
        e.setLineSpacing(dp(2), 1f);
        return e;
    }

    private void addItemRow(final Store.Item it, boolean focus) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        final TextView check = new TextView(this);
        check.setTextSize(22);
        check.setGravity(Gravity.CENTER);
        check.setTag(it.done);
        final EditText e = plain(L.t("Bənd"), 16, false);
        e.setText(it.text);
        e.setSingleLine(true);
        e.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        e.setOnEditorActionListener((v, id, ev) -> {
            if (id == EditorInfo.IME_ACTION_NEXT || (ev != null && ev.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                addItemRow(new Store.Item(), true);
                return true;
            }
            return false;
        });
        Runnable paintCheck = () -> {
            boolean d = (Boolean) check.getTag();
            check.setText(d ? "☑" : "☐");
            check.setTextColor(d ? Ui.ACCENT : Ui.TEXT2);
            e.setTextColor(d ? Ui.TEXT2 : Ui.TEXT);
            e.setPaintFlags(d ? e.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                    : e.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        };
        paintCheck.run();
        check.setOnClickListener(v -> { check.setTag(!(Boolean) check.getTag()); paintCheck.run(); });
        TextView del = new TextView(this);
        del.setText("✕");
        del.setTextColor(Ui.TEXT2);
        del.setGravity(Gravity.CENTER);
        del.setOnClickListener(v -> {
            itemsBox.removeView(row);
            itemEdits.remove(e);
            itemChecks.remove(check);
        });
        row.addView(check, new LinearLayout.LayoutParams(dp(40), dp(44)));
        row.addView(e, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(del, new LinearLayout.LayoutParams(dp(40), dp(44)));
        itemsBox.addView(row);
        itemEdits.add(e);
        itemChecks.add(check);
        if (focus) e.requestFocus();
    }

    private void paint() {
        int c = note.color == 0 ? BG : note.color;
        root.setBackgroundColor(c);
        getWindow().setStatusBarColor(c);
        getWindow().setNavigationBarColor(c);
    }

    private void pickColor() {
        String[] names = {L.t("Standart"), L.t("Sarı"), L.t("Yaşıl"), L.t("Mavi"), L.t("Bənövşəyi"), L.t("Qırmızı"), L.t("Boz")};
        Sheet.menu(root, L.t("Rəng"), names, i -> {
            note.color = i == 0 ? 0 : Store.COLORS[i];
            paint();
        });
    }

    private void menu() {
        Sheet.menu(root, L.t("Qeyd"), new String[]{L.t("Paylaş"), L.t("Kopyala"), L.t("!Sil")}, i -> {
            collect();
            if (i == 0) {
                startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, note.asText()), L.t("Paylaş")));
            } else if (i == 1) {
                android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (cm != null) cm.setPrimaryClip(android.content.ClipData.newPlainText("qeyd", note.asText()));
                toast(L.t("Kopyalandı"));
            } else {
                Sheet.confirm(root, L.t("Silinsin?"), L.t("Bu qeyd birdəfəlik silinəcək."), L.t("Sil"), () -> {
                    store.notes.remove(note);
                    store.save();
                    note = null;
                    finish();
                });
            }
        });
    }

    private void collect() {
        if (note == null) return;
        note.title = title.getText().toString();
        if (note.checklist) {
            note.items.clear();
            for (int i = 0; i < itemEdits.size(); i++) {
                Store.Item it = new Store.Item();
                it.text = itemEdits.get(i).getText().toString();
                it.done = (Boolean) itemChecks.get(i).getTag();
                if (!it.text.trim().isEmpty()) note.items.add(it);
            }
        } else note.body = text.getText().toString();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (note == null) return;
        String before = store.byId(note.id) == null ? null : store.toJson();
        collect();
        if (note.isEmpty()) {
            store.notes.remove(note);
        } else {
            if (store.byId(note.id) == null) store.notes.add(note);
            String after = store.toJson();
            if (!after.equals(before)) note.updated = System.currentTimeMillis();
        }
        store.save();
        NoteWidget.updateAll(this);
    }
}
