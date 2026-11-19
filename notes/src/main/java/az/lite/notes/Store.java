package az.lite.notes;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Qeydlər yalnız telefonda: files/notes.json. */
final class Store {

    static final int[] COLORS = {0xFF1F1F1F, 0xFF3A2E00, 0xFF0B3A1E, 0xFF0A2A4A, 0xFF3A1430, 0xFF4A1A12, 0xFF2E2E2E};

    static final class Item {
        String text = "";
        boolean done;
    }

    static final class Note {
        long id, updated;
        String title = "", body = "";
        int color;
        boolean pinned, checklist;
        final List<Item> items = new ArrayList<>();

        String preview() {
            if (!checklist) return body;
            StringBuilder b = new StringBuilder();
            for (Item i : items) b.append(i.done ? "☑ " : "☐ ").append(i.text).append('\n');
            return b.toString().trim();
        }

        boolean isEmpty() {
            if (!title.trim().isEmpty()) return false;
            if (!checklist) return body.trim().isEmpty();
            for (Item i : items) if (!i.text.trim().isEmpty()) return false;
            return true;
        }

        String asText() {
            String p = preview();
            return title.isEmpty() ? p : title + "\n\n" + p;
        }
    }

    private static Store instance;

    static synchronized Store get(Context c) {
        if (instance == null) instance = new Store(c.getApplicationContext());
        return instance;
    }

    final List<Note> notes = new ArrayList<>();
    private final File file;

    private Store(Context c) {
        file = new File(c.getFilesDir(), "notes.json");
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                byte[] b = new byte[(int) file.length()];
                int n = 0, r;
                while (n < b.length && (r = in.read(b, n, b.length - n)) > 0) n += r;
                fromJson(new String(b, 0, n, StandardCharsets.UTF_8));
            } catch (Exception ignored) { }
        }
    }

    boolean fromJson(String s) {
        try {
            JSONArray a = new JSONObject(s).getJSONArray("notes");
            List<Note> list = new ArrayList<>();
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                Note n = new Note();
                n.id = o.getLong("id");
                n.updated = o.optLong("u");
                n.title = o.optString("t");
                n.body = o.optString("b");
                n.color = o.optInt("c");
                n.pinned = o.optBoolean("p");
                n.checklist = o.optBoolean("l");
                JSONArray it = o.optJSONArray("i");
                if (it != null) for (int j = 0; j < it.length(); j++) {
                    JSONObject x = it.getJSONObject(j);
                    Item item = new Item();
                    item.text = x.optString("t");
                    item.done = x.optBoolean("d");
                    n.items.add(item);
                }
                list.add(n);
            }
            notes.clear();
            notes.addAll(list);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    String toJson() {
        JSONObject root = new JSONObject();
        try {
            JSONArray a = new JSONArray();
            for (Note n : notes) {
                JSONObject o = new JSONObject();
                o.put("id", n.id);
                o.put("u", n.updated);
                o.put("t", n.title);
                o.put("b", n.body);
                o.put("c", n.color);
                o.put("p", n.pinned);
                o.put("l", n.checklist);
                JSONArray it = new JSONArray();
                for (Item i : n.items) {
                    JSONObject x = new JSONObject();
                    x.put("t", i.text);
                    x.put("d", i.done);
                    it.put(x);
                }
                o.put("i", it);
                a.put(o);
            }
            root.put("notes", a);
        } catch (Exception ignored) { }
        return root.toString();
    }

    void save() {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(toJson().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) { }
    }

    Note byId(long id) {
        for (Note n : notes) if (n.id == id) return n;
        return null;
    }

    List<Note> sorted(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        List<Note> out = new ArrayList<>();
        for (Note n : notes) {
            if (q.isEmpty() || n.title.toLowerCase().contains(q) || n.preview().toLowerCase().contains(q)) out.add(n);
        }
        Collections.sort(out, (a, b) -> a.pinned != b.pinned ? (a.pinned ? -1 : 1) : Long.compare(b.updated, a.updated));
        return out;
    }
}
