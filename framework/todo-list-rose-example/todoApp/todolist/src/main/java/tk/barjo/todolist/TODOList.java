package tk.barjo.todolist;

import java.util.List;


/**
 * TODOList service.
 * Allow you to add, remove, get and list <code>TODONote</code>.
 */
public interface TODOList {

    void addNote(TODONote note);

    boolean rmNote(String id);

    TODONote getNote(String id);

    List<TODONote> getAllNotes();
}
