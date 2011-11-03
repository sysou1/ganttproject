/* LICENSE: GPL2
Copyright (C) 2004-2011 Dmitry Barashev

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package net.sourceforge.ganttproject.util;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

/**
 * Contains methods to calculate the text length
 *
 * @author bard
 */
public class TextLengthCalculatorImpl implements TextLengthCalculator {
    private Graphics2D myGraphics;

    private State myState;

    public TextLengthCalculatorImpl(Graphics2D g) {
        setGraphics(g);
    }

    public void setGraphics(Graphics2D g) {
        myGraphics = g;
        myState = null;
    }

    /** @return the length of text for the given g */
    public static int getTextLength(Graphics2D g, String text) {
        if(text.length() == 0) {
            return 0;
        }
        FontRenderContext frc = g.getFontRenderContext();
        Font font = g.getFont();
        TextLayout layout = new TextLayout(text, font, frc);
        Rectangle2D bounds = layout.getBounds();
        return (int) bounds.getWidth() + 1;
    }

    public int getTextHeight(String text) {
        return (int) myGraphics.getFontMetrics().getLineMetrics(text, myGraphics).getAscent();
    }

    public int getTextLength(String text) {
        return getTextLength(myGraphics, text);
    }

    public Object getState() {
        if (myState == null) {
            myState = new State(myGraphics.getFontRenderContext(), myGraphics
                    .getFont());
        }
        return myState;
    }

    /** Internally used class containing unique variable for the current state */
    private static class State {
        // Internal values determining the uniqueness of the state
        FontRenderContext context;
        Font font;

        State(FontRenderContext context, Font font) {
            this.context = context;
            this.font = font;
            assert context != null;
            assert font != null;
        }

        @Override
        public boolean equals(Object o) {
            State rvalue = (State) o;
            if (rvalue == null) {
                return false;
            }
            return rvalue.context.equals(this.context)
                    && rvalue.font.equals(this.font);
        }

        @Override
        public int hashCode() {
            return font.hashCode();
        }
    }
}
