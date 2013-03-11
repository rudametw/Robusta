package tk.barjo.todolist.impl;

import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.ow2.chameleon.rose.api.Machine;
import tk.barjo.todolist.TODOList;
import tk.barjo.todolist.TODONote;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

/**
 * User: barjo
 * Date: 05/02/13
 * Time: 14:17
 */
@Component
@Instantiate
@Provides(specifications = tk.barjo.todolist.impl.RESTodo.class)
@Path("/todo")
public class RESTodo {

    private Machine machine;

    @Requires
    private TODOList todoList;

    private final BundleContext context;

    public RESTodo(BundleContext context) {
        this.context = context;
    }

    @Validate
    private void start() throws InvalidSyntaxException {
        machine = Machine.MachineBuilder.machine(context, "TodoList_rose").create();
        machine.exporter("RoSe_exporter.jersey").create();
        machine.out("(objectClass="+RESTodo.class.getName()+")").protocol(Collections.singletonList("jax-rs")).add();
        machine.start();
    }

    @Invalidate
    private void stop() {
        machine.stop();
    }


    @POST
    public Response putTodo(@QueryParam("id")String id, @QueryParam("content")String content){
        try{
            TODONote note = new TODONote(id,content);
            todoList.addNote(note);

            return Response.ok().build();
        }catch (Exception e){
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delTodo(@PathParam("id")String id){
        try{
            todoList.rmNote(id);
            return Response.ok().build();
        }catch (Exception e){
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTodo(@PathParam("id")String id){
        try{
            TODONote note = todoList.getNote(id);
            if (note==null)
                return Response.status(404).build();
            else
                return Response.ok(note.toJson()).build();
        }catch (Exception e){
            return Response.status(400).entity(e.getMessage()).build();
        }
    }



    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTodo() {
        StringBuilder builder = new StringBuilder();


        List<TODONote> notes = todoList.getAllNotes();

        builder.append("[");
        for (TODONote note : notes){
            builder.append(note.toJson());
            builder.append(",");
        }
        builder.append("]");

        return Response.ok(builder.toString()).build();
    }

}