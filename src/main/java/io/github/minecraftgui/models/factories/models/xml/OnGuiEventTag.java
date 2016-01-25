/*
 *
 *  *     Minecraft GUI Server
 *  *     Copyright (C) 2015  Samuel Marchildon-Lavoie
 *  *
 *  *     This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License, or
 *  *     (at your option) any later version.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU General Public License
 *  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package io.github.minecraftgui.models.factories.models.xml;

import io.github.minecraftgui.models.components.UserGui;
import io.github.minecraftgui.models.factories.GuiFactory;
import io.github.minecraftgui.models.factories.models.xml.functions.Function;
import io.github.minecraftgui.models.factories.models.xml.functions.HideComponent;
import io.github.minecraftgui.models.factories.models.xml.functions.ShowComponent;
import io.github.minecraftgui.models.listeners.OnGuiListener;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Samuel on 2016-01-17.
 */
public class OnGuiEventTag extends Tag {

    private static final Pattern FUNCTION = Pattern.compile("\\w+\\((.+(, .+)*)*\\)");

    private final HashMap<Class, Function> events;

    public OnGuiEventTag(Element element, GuiFactory.GuiModel model) {
        super(element, model);
        this.events = new HashMap<>();
        initEvents(element);
    }

    public void setEvents(OnGuiListener plugin, UserGui userGui){
        for(Map.Entry pairs : events.entrySet()){
            Class listener = (Class) pairs.getKey();
            Function event = (Function) pairs.getValue();

            if(listener == OnGuiListener.OnGuiOpen.class) {
                userGui.addOnGuiOpenListener(plugin, new OnGuiListener.OnGuiOpen() {
                    @Override
                    public void onGuiOpen(UserGui userGui) {
                        event.event(userGui, null);
                    }
                });
            }
            else if(listener == OnGuiListener.OnGuiClose.class) {
                userGui.addOnGuiCloseListener(plugin, new OnGuiListener.OnGuiClose() {
                    @Override
                    public void onGuiClose(UserGui userGui) {
                        event.event(userGui, null);
                    }
                });
            }
        }
    }

    private void initEvents(Element element){
        if(element.hasAttribute("onOpen"))
            events.put(OnGuiListener.OnGuiOpen.class, createEvent(element.getAttribute("onOpen")));
        if(element.hasAttribute("onClose"))
            events.put(OnGuiListener.OnGuiClose.class, createEvent(element.getAttribute("onClose")));
    }

    private Function createEvent(String value){
        Function event = null;
        Matcher matcher = FUNCTION.matcher(value);

        if(matcher.find()){
            String values[] = value.trim().split("\\(");
            String args[] = values[1].substring(0, values[1].indexOf(")")).split(",");
            String function = values[0].toLowerCase().trim();

            for(int i = 0; i < args.length; i++)
                args[i] = args[i].trim();

            switch (function){
                case "showcomponent": event = new ShowComponent(args); break;
                case "hidecomponent": event = new HideComponent(args); break;
            }
        }

        return event;
    }

}
