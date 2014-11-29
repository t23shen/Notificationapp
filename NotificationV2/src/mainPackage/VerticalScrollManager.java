package mainPackage;

import net.rim.device.api.ui.container.*;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.ScrollChangeListener;
/**
 * VerticalScrollManager - a VerticalFieldManager with a nicer-looking scrollbar 
 * and optional width and height limits
 */
public class VerticalScrollManager extends VerticalFieldManager implements ScrollChangeListener {
    /**
     * Some constants governing the exact look - colors and size - of the scrollbar.
     * Our scrollbar is a light grey bar on the right of the manager's visible area
     * with a darker grey scroll slider.
     */
    private static final int SCROLLBAR_COLOR = 0xCCCCCC;
    private static final int SLIDER_COLOR = 0x66666666;
    private static final int TOP_SHADE_COLOR = 0xDDDDDD;
    private static final int BOTTOM_SHADE_COLOR = 0x333333;
    private static final int SCROLLBAR_WIDTH = 8;
    private static final int SCROLLBAR_RIGHT_MARGIN = 0;
    private static final int SCROLLBAR_LEFT_MARGIN = 0;

    // The eventual height of the slider in pixels
    private int sliderHeight;
    // The eventual horizontal slider position - in this Manager's coordinates
    private int sliderXPosition;
    // Height and width limits - useful for creating Managers which should not take
    // the whole provided area 
    private int maxVisibleHeight;
    private int maxVisibleWidth;
    // Actual height and width - set in sublayout() below
    private int visibleHeight;
    private int visibleWidth;
    // Total (a.k.a "virtual") height of the Manager
    private int totalHeight;
    // Do we need to display a scrollbar?
    private boolean isScrolling;

    // Constructors - please observe the use of width and height limits
    // Also - you do want to use VERTICAL_SCROLL but definitely not the
    // default "scrollbar", thus we made that a default
    public VerticalScrollManager() {
        this(VERTICAL_SCROLL | NO_VERTICAL_SCROLLBAR, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public VerticalScrollManager(int w, int h) {
        this(VERTICAL_SCROLL | NO_VERTICAL_SCROLLBAR, w, h);
    }

    public VerticalScrollManager(long style) {
        this(style, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public VerticalScrollManager(long style, int w, int h) {
        super(style);
        maxVisibleHeight = h;
        maxVisibleWidth = w;
        setScrollListener(this);
    }

    // This is how we report our desided height and width to the parent manager
    public int getPreferredHeight() {
        return visibleHeight;
    }
    
    public int getPreferredWidth() {
        return visibleWidth;
    }

    // This is called by the framework just before displaying this Manager.  At this point we are
    // given the biggest rectangle within the parent Manager that our Manager is allowed to occupy
    // This is the natural place to make all necessary calculations
    protected void sublayout(int w, int h) {
    	// Initial value - no scrollbar unless VERTICAL_SCROLL is requested
        isScrolling = ((getStyle() & VERTICAL_SCROLL) == VERTICAL_SCROLL);
        // How much room (horizontally) do we need for the scrollbar
        int scrollbarWidth = isScrolling ? SCROLLBAR_WIDTH + SCROLLBAR_LEFT_MARGIN + SCROLLBAR_RIGHT_MARGIN : 0;
        // Further limit the given dimensions with the requested size
        visibleHeight = Math.min(h, maxVisibleHeight);
        visibleWidth = Math.min(w, maxVisibleWidth);
        // Before asking the parent class to layout, reserve the necessary room for the scrollbar
        int myWidth = visibleWidth - scrollbarWidth;
        super.sublayout(myWidth, visibleHeight);
        // After the VerticalFieldManager lays out its fields, let's ask it for dimensions and
        // adjust width back to include the scrollbar
        visibleHeight = getHeight();
        totalHeight = getVirtualHeight();
        visibleWidth = getWidth() + scrollbarWidth;
        // Report our proper dimensions to the parent Manager
        setExtent(visibleWidth, visibleHeight);
        // This is necessary for the overall BlackBerry framework to know how far we can scroll
        // Especially important for touch-screen devices
        setVirtualExtent(visibleWidth, totalHeight);
        // Now, let's double check whether any scrollbar is needed
        // If the visible area is tall enough, let's not bother
        isScrolling = (visibleHeight < totalHeight);

        // Finally, determine how big is the slider and where to start painting it horizontally
        if (isScrolling) {
        	sliderHeight = visibleHeight * visibleHeight / totalHeight;
        	sliderHeight = Math.max(sliderHeight, 1);    // show at least one pixel!
        	// Please observe that we reserved the width based on both left and right margin,
        	// but are going to paint based on right margin only - that's how we create
        	// that left margin
        	sliderXPosition = visibleWidth - SCROLLBAR_WIDTH - SCROLLBAR_RIGHT_MARGIN;
        }
    }

    // This is called each time our Manager needs repainting (invalidate(), scrolling, etc.) 
    protected void paint(Graphics g) {
    	// First, paint the fields "normally"
        super.paint(g);

        // Now, add the scrollbar if necessary
        if (isScrolling) {
        	// Determine how far have we scrolled
            int scrollPosition = getVerticalScroll();
            // The slider vertical position on the screen is proportional to the scroll position.
            // Please observe that we add the scroll position to the calculated result since
            // everything on the screen starts there.  All x and y coordinates for this Graphics
            // object are within the Manager's FULL (virtual) rectangle.
            int sliderYPosition = scrollPosition * visibleHeight / totalHeight + scrollPosition;
            // draw the scrollbar
            g.setColor(SCROLLBAR_COLOR);
            // Again, scrollbar starts at scroll position (top of the displayed part) and
            // is visibleHeight high
            g.fillRect(sliderXPosition, scrollPosition, SCROLLBAR_WIDTH, visibleHeight);
            // draw the slider
            g.setColor(SLIDER_COLOR);
            g.fillRect(sliderXPosition, sliderYPosition, SCROLLBAR_WIDTH, sliderHeight);
            // draw the shading - make it "3-D"
            g.setColor(TOP_SHADE_COLOR);
            if (sliderHeight > 2) {
            	g.drawLine(sliderXPosition, sliderYPosition, sliderXPosition + SCROLLBAR_WIDTH - 1, sliderYPosition);
            }
            g.drawLine(sliderXPosition, sliderYPosition, sliderXPosition, sliderYPosition + sliderHeight - 1);
            
            g.setColor(BOTTOM_SHADE_COLOR);
            if (sliderHeight > 2) {
            	g.drawLine(sliderXPosition, sliderYPosition + sliderHeight - 1, sliderXPosition + SCROLLBAR_WIDTH - 1, sliderYPosition + sliderHeight - 1);
            }
            g.drawLine(sliderXPosition + SCROLLBAR_WIDTH - 1, sliderYPosition, sliderXPosition + SCROLLBAR_WIDTH - 1, sliderYPosition + sliderHeight - 1);
        }
    }

    public void scrollChanged(Manager mgr, int newX, int newY) {
	if (mgr == this) {
            invalidate(newX + sliderXPosition, newY, SCROLLBAR_WIDTH + SCROLLBAR_RIGHT_MARGIN, getVisibleHeight());
	}
    }
}
