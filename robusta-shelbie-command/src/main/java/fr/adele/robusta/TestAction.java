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
@Command(name="test",
         scope="robusta",
         description="Used for testing purposes")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class TestAction implements Action {

    /**
     * An option is a named command parameter that should be valued (except if the type is boolean).
     * Example usage: hello --lang fr
     */
    @Option(name = "-l",
            aliases = {"--lang", "--language"},
            required = false,
            description = "Language to return the salutation")
    private String lang = "en";

    /**
     * Arguments are un-named values.
     */
    @Argument(multiValued = true,
              description = "The name of one or more person(s).")
    private List<String> who;

    public Object execute(CommandSession session) throws Exception {

        // Select the output language
        if ("en".equals(lang)) {

            // Directly print the message using System.out or System.err
            System.out.println("Hello " + who);
        } else if ("fr".equals(lang)) {

            // Really easy, isn't it ?
            System.out.println("Bonjour " + who);
        } else {
            throw new Exception("Unknown language");
        }
        return null;

    }
}
