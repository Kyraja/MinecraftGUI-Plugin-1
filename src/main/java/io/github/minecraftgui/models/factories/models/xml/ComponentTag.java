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

import io.github.minecraftgui.models.components.Component;
import io.github.minecraftgui.models.components.UserGui;
import io.github.minecraftgui.models.factories.GuiFactory;
import io.github.minecraftgui.models.factories.models.css.CssRule;
import io.github.minecraftgui.models.factories.models.xml.functions.Function;
import io.github.minecraftgui.models.factories.models.xml.functions.HideChildren;
import io.github.minecraftgui.models.factories.models.xml.functions.HideComponent;
import io.github.minecraftgui.models.factories.models.xml.functions.ShowChildren;
import io.github.minecraftgui.models.factories.models.xml.functions.ShowComponent;
import io.github.minecraftgui.models.forms.Form;
import io.github.minecraftgui.models.forms.Valuable;
import io.github.minecraftgui.models.listeners.OnBlurListener;
import io.github.minecraftgui.models.listeners.OnClickListener;
import io.github.minecraftgui.models.listeners.OnDoubleClickListener;
import io.github.minecraftgui.models.listeners.OnFocusListener;
import io.github.minecraftgui.models.listeners.OnMouseEnterListener;
import io.github.minecraftgui.models.listeners.OnMouseLeaveListener;
import io.github.minecraftgui.models.shapes.EllipseColor;
import io.github.minecraftgui.models.shapes.PolygonColor;
import io.github.minecraftgui.models.shapes.RectangleColor;
import io.github.minecraftgui.models.shapes.RectangleImage;
import io.github.minecraftgui.models.shapes.Shape;
import io.github.minecraftgui.views.PluginInterface;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Samuel on 2016-01-12.
 */
public abstract class ComponentTag extends Tag {

    private static final Pattern FUNCTION = Pattern.compile( "\\w+\\((.+(, .+)*)*\\)" );
    private static final Object EVENTS[][] = {
            { "onBlur", OnBlurListener.class },
            { "onClick", OnClickListener.class },
            { "onDoubleClick", OnDoubleClickListener.class },
            { "onFocus", OnFocusListener.class },
            { "onMouseEnter", OnMouseEnterListener.class },
            { "onMouseLeave", OnMouseLeaveListener.class }
    };

    protected abstract Component createComponent( PluginInterface service, UserGui userGui );

    protected final String id;
    protected final ArrayList<String> classes;
    protected Class<? extends Shape> shape;
    protected final ArrayList<CssRule> rules;
    protected final String form;
    private final String name;
    private final String action;
    private final HashMap<Class, ArrayList<Function>> functions;

    public ComponentTag( Element element, GuiFactory.GuiModel model ) {
        super( element, model );
        id = element.getAttribute( "id" );
        form = element.getAttribute( "form" );
        name = element.getAttribute( "name" );
        action = element.getAttribute( "action" );
        shape = getShapeByName( element.getAttribute( "shape" ) );
        this.classes = generateClasses( element );
        this.rules = new ArrayList<>();
        this.functions = new HashMap<>();
        initFunctions( element );
    }

    public ArrayList<CssRule> getRules() {
        return rules;
    }

    public void addCssRule( CssRule rule ) {
        if ( !rules.contains( rule ) ) {
            rules.add( rule );
        }
    }

    public String getId() {
        return id;
    }

    public ArrayList<String> getClasses() {
        return classes;
    }

    protected void setAttributes( PluginInterface plugin, UserGui userGui, Component component ) {
        for ( CssRule cssRule : rules ) {
            cssRule.applyRulesOnComponent( component );
        }

        if ( !form.equals( "" ) ) {
            Form form = userGui.getForm( this.form );

            if ( !name.equals( "" ) && component instanceof Valuable ) {
                form.addValuable( name, (Valuable) component );
            } else if ( action.equalsIgnoreCase( "submit" ) ) {
                form.setButton( component );
            }
        }

        setFunctions( userGui, component );
    }

    protected void initAfterChildrenCreated( PluginInterface service, UserGui userGui, Component component ) {
    }

    public Component getComponent( PluginInterface service, UserGui userGui ) {
        Component component = createComponent( service, userGui );
        setAttributes( service, userGui, component );

        for ( Tag tag : getChildren() ) {
            if ( tag instanceof ComponentTag ) {
                ( (ComponentTag) tag ).getComponent( service, userGui, component );
            }
        }

        return component;
    }

    private void getComponent( PluginInterface service, UserGui userGui, Component parent ) {
        Component component = createComponent( service, userGui );
        parent.add( component, userGui );

        setAttributes( service, userGui, component );

        for ( Tag tag : getChildren() ) {
            if ( tag instanceof ComponentTag ) {
                ComponentTag componentTag = ( (ComponentTag) tag );

                componentTag.getComponent( service, userGui, component );
            }
        }

        initAfterChildrenCreated( service, userGui, component );
    }

    private ArrayList<String> generateClasses( Element element ) {
        if ( !element.getAttribute( "class" ).equals( "" ) ) {
            return new ArrayList<>( Arrays.asList( element.getAttribute( "class" ).split( " " ) ) );
        } else {
            return new ArrayList<>();
        }
    }

    protected static Class<? extends Shape> getShapeByName( String name ) {
        name = name.toLowerCase();

        if ( name.equals( "ellipse-color" ) ) return EllipseColor.class;
        if ( name.equals( "polygon-color" ) ) return PolygonColor.class;
        if ( name.equals( "rectangle-color" ) ) return RectangleColor.class;
        if ( name.equals( "rectangle-image" ) ) return RectangleImage.class;

        return RectangleColor.class;
    }

    private void setFunctions( UserGui userGui, Component component ) {
        for ( Map.Entry pairs : functions.entrySet() ) {
            Class listener = (Class) pairs.getKey();
            ArrayList<Function> functions = (ArrayList) pairs.getValue();

            for ( Function function : functions ) {
                if ( listener == OnBlurListener.class ) {
                    component.addOnBlurListener( component1 -> function.execute( userGui, component1 ) );
                } else if ( listener == OnClickListener.class ) {
                    component.addOnClickListener( component1 -> function.execute( userGui, component1 ) );
                } else if ( listener == OnDoubleClickListener.class ) {
                    component.addOnDoubleClickListener( component1 -> function.execute( userGui, component1 ) );
                } else if ( listener == OnFocusListener.class ) {
                    component.addOnFocusListener( component1 -> function.execute( userGui, component1 ) );
                } else if ( listener == OnMouseLeaveListener.class ) {
                    component.addOnMouseLeaveListener( component1 -> function.execute( userGui, component1 ) );
                } else if ( listener == OnMouseEnterListener.class ) {
                    component.addOnMouseEnterListener( component1 -> function.execute( userGui, component1 ) );
                }
            }
        }
    }

    private void initFunctions( Element element ) {
        for ( Object obj[] : EVENTS ) {
            if ( element.hasAttribute( (String) obj[0] ) ) {
                String value = element.getAttribute( (String) obj[0] );
                String values[];

                if ( value.contains( ";" ) ) {
                    values = value.split( ";" );
                } else {
                    values = new String[]{ value };
                }

                for ( String val : values ) {
                    ArrayList<Function> functions = this.functions.get( obj[1] );

                    if ( functions == null ) {
                        functions = new ArrayList<>();
                        this.functions.put( (Class) obj[1], functions );
                    }

                    functions.add( createFunction( val ) );
                }
            }
        }
    }

    private Function createFunction( String value ) {
        Function fct = null;
        Matcher matcher = FUNCTION.matcher( value.trim() );

        if ( matcher.find() ) {
            String values[] = value.trim().split( "\\(" );
            String args[] = values[1].substring( 0, values[1].indexOf( ")" ) ).split( "," );
            String function = values[0].toLowerCase().trim();

            for ( int i = 0; i < args.length; i++ ) {
                args[i] = args[i].trim();
            }

            switch ( function ) {
                case "hidechildren":
                    fct = new HideChildren( args );
                    break;
                case "showchildren":
                    fct = new ShowChildren( args );
                    break;
                case "showcomponent":
                    fct = new ShowComponent( args );
                    break;
                case "hidecomponent":
                    fct = new HideComponent( args );
                    break;
            }
        }

        return fct;
    }

}
