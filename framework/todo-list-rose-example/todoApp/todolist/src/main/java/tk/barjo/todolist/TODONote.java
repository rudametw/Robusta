package tk.barjo.todolist;

/**
 *  Simple tk.barjo.todolist.TODONote note.
 *
 * User: barjo
 * Date: 1/30/13
 * Time: 2:28 PM
 */
public class TODONote {
    private final String id;
    private final String content;

    public TODONote(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public final String getName(){
        return id;
    }

    public final String getContent(){
        return content;
    }

    public final String toJson(){
        return "{ 'id' : '"+id+"', 'content' : '"+content+"' }";
    }
}
