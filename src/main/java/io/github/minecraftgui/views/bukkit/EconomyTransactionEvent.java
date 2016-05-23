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

package io.github.minecraftgui.views.bukkit;

import io.github.minecraftgui.models.components.Paragraph;

import java.util.UUID;

/**
 * Created by Martin on 23.05.2016.
 */
public class EconomyTransactionEvent {

    private final Paragraph paragraph;
    private final UUID player;
    private final String currency;

    public EconomyTransactionEvent( Paragraph paragraph, UUID player, String currency ) {
        this.paragraph = paragraph;
        this.player = player;
        this.currency = currency;
    }

    //TODO implement updating of the player's balance
    public void changed( UUID id, double newValue ) {
        if ( id.equals( player ) ) {
            paragraph.setText( newValue + "" );
        }
    }
}
