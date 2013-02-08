/**
 * Copyright 2010 OW2 Shelbie
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.adele.robusta;

import java.util.List;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.service.command.CommandSession;

@Component
@Command(name="classycle",
         scope="robusta",
         description="Used for testing purposes")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class Classycle implements Action {

    /**
     * An option is a named command parameter that should be valued (except if the type is boolean).
     * Example usage: hello --lang fr
     */
    @Option(name = "-l",
            aliases = {"--lang", "--language"},
            required = false,
            description = "Language to return the salutation")
    private String lang = "en";

     public Object execute(CommandSession session) throws Exception {


    	 return null;

    }
}
