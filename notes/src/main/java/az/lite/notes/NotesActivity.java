package az.lite.notes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import az.lite.core.Glyph;
import az.lite.core.L;
import az.lite.core.LiteActivity;
import az.lite.core.Rows;
import az.lite.core.Sheet;
import az.lite.core.Ui;

public class NotesActivity extends LiteActivity {

    private static final int REQ_MD = 1, REQ_BACKUP = 2, REQ_RESTORE = 3;
    private Store store;
    private Rows<Store.Note> adapter;
    private EditText search;
    private TextView empty;

    @Override
    protected void onCreate(Bundle b) {
        L.init(this);
        super.onCreate(b);
        store = Store.get(this);
        setTitleText("Lite Notes");
        action("⋯", v -> menu());

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        search = Ui.input(this, L.t("Qeydlərdə axtar…"));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, -2);
        slp.setMargins(dp(12), dp(4), dp(12), dp(8));
        col.addView(search, slp);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable e) { refresh(); }
        });

        FrameLayout box = new FrameLayout(this);
        ListView list = Rows.list(this);
        adapter = new Rows<>((r, n, p) -> {
            String t = n.title.isEmpty() ? firstLine(n.preview()) : n.title;
            r.icon(new Glyph(n.checklist ? "☑" : (n.pinned ? "★" : "≡"), n.color == 0 ? Ui.SURFACE3 : n.color, Ui.ACCENT));
            r.title.setText(t.isEmpty() ? L.t("Boş qeyd") : t);
            String body = n.preview().replace('\n', ' ');
            r.sub.setText(body);
            r.right.setText(DateUtils.getRelativeTimeSpanString(n.updated, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS));
        });
        list.setAdapter(adapter);
        list.setOnItemClickListener((p, v, pos, id) -> open(adapter.getItem(pos).id, false));
        list.setOnItemLongClickListener((p, v, pos, id) -> {
            final Store.Note n = adapter.getItem(pos);
            Sheet.menu(root, n.title.isEmpty() ? L.t("Qeyd") : n.title, new String[]{n.pinned ? L.t("Bərkitməni götür") : L.t("Bərkit"),
                    L.t("Paylaş"), L.t("!Sil")}, i -> {
                if (i == 0) { n.pinned = !n.pinned; store.save(); refresh(); }
                else if (i == 1) share(n);
                else Sheet.confirm(root, L.t("Silinsin?"), L.t("Bu qeyd birdəfəlik silinəcək."), L.t("Sil"), () -> {
                        store.notes.remove(n);
                        store.save();
                        refresh();
                    });
            });
            return true;
        });
        box.addView(list);
        empty = new TextView(this);
        empty.setText(L.t("Qeyd yoxdur.\nAşağıdan yeni qeyd və ya siyahı yarat."));
        empty.setTextColor(Ui.TEXT2);
        empty.setGravity(Gravity.CENTER);
        box.addView(empty, new FrameLayout.LayoutParams(-1, -1));
        col.addView(box, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout bar = new LinearLayout(this);
        bar.setPadding(dp(12), dp(8), dp(12), dp(12));
        TextView list2 = Ui.button(this, L.t("+ Siyahı"), false);
        list2.setOnClickListener(v -> open(0, true));
        TextView note = Ui.button(this, L.t("+ Qeyd"), true);
        note.setOnClickListener(v -> open(0, false));
        bar.addView(list2, new LinearLayout.LayoutParams(0, dp(52), 1));
        bar.addView(Ui.spacer(this, 8, 1));
        bar.addView(note, new LinearLayout.LayoutParams(0, dp(52), 1));
        col.addView(bar);
        body.addView(col);

        handleShare(getIntent());
    }

    @Override
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        handleShare(i);
    }

    /** Başqa tətbiqdən paylaşılan mətn → yeni qeyd. */
    private void handleShare(Intent i) {
        if (i == null || !Intent.ACTION_SEND.equals(i.getAction())) return;
        String t = i.getStringExtra(Intent.EXTRA_TEXT);
        if (t == null) return;
        Store.Note n = new Store.Note();
        n.id = System.currentTimeMillis();
        n.updated = n.id;
        String subj = i.getStringExtra(Intent.EXTRA_SUBJECT);
        n.title = subj == null ? "" : subj;
        n.body = t;
        store.notes.add(n);
        store.save();
        setIntent(new Intent());
        open(n.id, false);
    }

    static String firstLine(String s) {
        int i = s.indexOf('\n');
        return (i < 0 ? s : s.substring(0, i)).trim();
    }

    private void open(long id, boolean checklist) {
        startActivity(new Intent(this, EditActivity.class).putExtra("id", id).putExtra("list", checklist));
    }

    private void share(Store.Note n) {
        Intent s = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, n.asText());
        startActivity(Intent.createChooser(s, L.t("Paylaş")));
    }

    private void refresh() {
        adapter.set(store.sorted(search.getText().toString()));
        empty.setVisibility(adapter.getCount() == 0 ? View.VISIBLE : View.GONE);
        NoteWidget.updateAll(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void menu() {
        Sheet.menu(root, "Lite Notes", new String[]{L.t("Hamısını .md faylı kimi ixrac et"), L.t("Ehtiyat nüsxə saxla"),
                L.t("Ehtiyat nüsxədən bərpa et"), L.t("Dil") + " / Language"}, i -> {
            if (i == 3) {
                String[] names = new String[L.CODES.length];
                for (int k = 0; k < names.length; k++) {
                    names[k] = (L.CODES[k].equals(L.lang()) ? "●  " : "") + L.NAMES[k];
                }
                Sheet.menu(root, L.t("Dil") + " / Language", names, k -> {
                    if (L.CODES[k].equals(L.lang())) return;
                    L.set(this, L.CODES[k]);
                    recreate();
                });
                return;
            }
            if (i == 0) create("qeydler.md", "text/markdown", REQ_MD);
            else if (i == 1) create("lite-notes-backup.json", "application/json", REQ_BACKUP);
            else startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("*/*"), REQ_RESTORE);
        });
    }

    private void create(String name, String mime, int req) {
        startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                .setType(mime).putExtra(Intent.EXTRA_TITLE, name), req);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent d) {
        super.onActivityResult(req, res, d);
        if (res != RESULT_OK || d == null || d.getData() == null) return;
        Uri u = d.getData();
        try {
            if (req == REQ_MD) {
                StringBuilder b = new StringBuilder();
                for (Store.Note n : store.sorted("")) {
                    b.append("# ").append(n.title.isEmpty() ? firstLine(n.preview()) : n.title).append("\n\n");
                    if (n.checklist) for (Store.Item it : n.items) b.append(it.done ? "- [x] " : "- [ ] ").append(it.text).append('\n');
                    else b.append(n.body).append('\n');
                    b.append("\n---\n\n");
                }
                write(u, b.toString());
                toast(L.t("İxrac olundu"));
            } else if (req == REQ_BACKUP) {
                write(u, store.toJson());
                toast(L.t("Ehtiyat nüsxə saxlanıldı"));
            } else if (req == REQ_RESTORE) {
                String json;
                try (InputStream in = getContentResolver().openInputStream(u)) {
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int n;
                    while (in != null && (n = in.read(buf)) > 0) bo.write(buf, 0, n);
                    json = new String(bo.toByteArray(), StandardCharsets.UTF_8);
                }
                final String j = json;
                Sheet.confirm(root, L.t("Bərpa edilsin?"), L.t("İndiki qeydlər ehtiyat nüsxə ilə əvəz olunacaq."), L.t("Bərpa et"), () -> {
                    if (store.fromJson(j)) { store.save(); refresh(); toast(L.t("Bərpa olundu")); }
                    else toast(L.t("Fayl uyğun deyil"));
                });
            }
        } catch (Exception e) {
            toast(L.t("Alınmadı"));
        }
    }

    private void write(Uri u, String s) throws Exception {
        try (OutputStream o = getContentResolver().openOutputStream(u, "wt")) {
            if (o == null) throw new IllegalStateException();
            o.write(s.getBytes(StandardCharsets.UTF_8));
        }
    }
}
