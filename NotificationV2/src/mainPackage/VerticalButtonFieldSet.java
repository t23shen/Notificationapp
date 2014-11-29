package mainPackage;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.decor.Border;

/**
 * VerticalButtonFieldSet aligns a group of buttons vertically
 * so that they have equal widths
 * 
 * This Manager does not respect Horizontal style bits on the child nodes
 * since they're all fully justified
 * 
 * If USE_ALL_WIDTH is passed into this manager then the children 
 * will also use all available width
 */
public class VerticalButtonFieldSet extends Manager 
{
    
    public VerticalButtonFieldSet( )
    {
        this( Field.FIELD_HCENTER );
    } 
    
    public VerticalButtonFieldSet( long style ) 
    {
        super( style );
    }
    
    protected void sublayout( int width, int height ) 
    {
        int maxWidth   = 0;
        int numChildren = this.getFieldCount();
        
        if( isStyle( USE_ALL_WIDTH ) ) {
            // use all the width
            maxWidth = width;
        
        } else {
                    
            for( int i = 0; i < numChildren; i++ ) {
                Field currentField = getField( i );
                int currentPreferredWidth = currentField.getPreferredWidth() + FieldDimensionUtilities.getBorderWidth( currentField );
                maxWidth  = Math.max( maxWidth, currentPreferredWidth );
            }
        }
        
        int prevTopMargin = 0;
        int usedHeight = 0;
        int x;
        for( int i = 0; i < numChildren; i++ ) {
            
            Field currentField = getField( i );
            int currentPreferredWidth = currentField.getPreferredWidth() + FieldDimensionUtilities.getBorderWidth( currentField );
            if( currentPreferredWidth < maxWidth ) {
                int newPadding = ( maxWidth - currentPreferredWidth ) / 2; 
                currentField.setPadding( currentField.getPaddingTop(), newPadding, currentField.getPaddingBottom(), newPadding );
            }
            layoutChild( currentField, maxWidth, height );
            
            usedHeight += Math.max( prevTopMargin, currentField.getMarginBottom() );
            x = ( maxWidth - currentField.getWidth() ) / 2;
            setPositionChild( currentField, x, usedHeight );
            usedHeight += currentField.getHeight();
            prevTopMargin = currentField.getMarginBottom();
        }
        setExtent( maxWidth, usedHeight );
    }
    
    protected boolean navigationMovement(int dx, int dy, int status, int time) 
    {
        int focusIndex = getFieldWithFocusIndex();                   
        if ( dx < 0 && focusIndex == 0 ) {
            // we cannot go left
            return true;
        }
        if( dx > 0 && focusIndex == getFieldCount()-1 ) {
            // we cannot go right
            return true;
        }
        return super.navigationMovement( dx, dy, status, time );
    }    
}
//#preprocess

class HorizontalButtonFieldSet extends Manager 
{
    public static final int AXIS_SEQUENTIAL = 0;
    public static final int AXIS_VERTICAL   = 1 << 1;
    
    public HorizontalButtonFieldSet()
    {
        this( 0 );
    } 
    
    public HorizontalButtonFieldSet( long style ) 
    {
        super( style );
    }
    
    protected void sublayout( int width, int height )
    {
//      int availableWidth = width;
        int numFields = getFieldCount();
//      int maxPreferredWidth = 0;
        int maxHeight = 0;
        

        // There may be a few remaining pixels after dividing up the space
        // we must split up the space between the first and last buttons
        int fieldWidth = width / numFields;
        int firstFieldExtra = 0;
        int lastFieldExtra = 0;
        
        int unUsedWidth = width - fieldWidth * numFields;
        if( unUsedWidth > 0 ) {
            firstFieldExtra = unUsedWidth / 2;
            lastFieldExtra = unUsedWidth - firstFieldExtra;
        }
        
        int prevRightMargin = 0;
        
        // Layout the child fields, and calculate the max height
        for( int i = 0; i < numFields; i++ ) {
            
            int nextLeftMargin = 0;
            if( i < numFields - 1 ) {
                Field nextField = getField( i );
                nextLeftMargin = nextField.getMarginLeft();
            }
            
            Field currentField = getField( i );
            
            int widthForButton = fieldWidth;
            
            int leftMargin  = Math.max( prevRightMargin, currentField.getMarginLeft() ) / 2;
            int rightMargin = Math.max( nextLeftMargin, currentField.getMarginRight() ) / 2;
            if( i == 0 ) {
                widthForButton = fieldWidth + firstFieldExtra;
                leftMargin = currentField.getMarginLeft();
            } else if( i == numFields -1 ) {
                widthForButton = fieldWidth + lastFieldExtra;
                rightMargin = currentField.getMarginRight();
            }
            
            int currentVerticalMargins = currentField.getMarginTop() + currentField.getMarginBottom();
            int currentHorizontalMargins = leftMargin + rightMargin;
            
            widthForButton -= currentHorizontalMargins;
            
            int currentPreferredWidth = currentField.getPreferredWidth() + FieldDimensionUtilities.getBorderWidth( currentField );
            if( currentPreferredWidth < widthForButton ) {
                int newPadding = ( widthForButton - currentPreferredWidth ) / 2; 
                currentField.setPadding( currentField.getPaddingTop(), newPadding, currentField.getPaddingBottom(), newPadding );
            }
            layoutChild( currentField, widthForButton, height );
            maxHeight = Math.max( maxHeight, currentField.getHeight() + currentVerticalMargins );
   
            prevRightMargin = rightMargin;
            nextLeftMargin = 0;
        }

        // Now position the fields, respecting the Vertical style bits
        int usedWidth = 0;
        int y;
        prevRightMargin = 0;
        for( int i = 0; i < numFields; i++ ) {
            
            Field currentField = getField( i );
            int marginTop = currentField.getMarginTop();
            int marginBottom = currentField.getMarginBottom();
            int marginLeft = Math.max( currentField.getMarginLeft(), prevRightMargin );
            int marginRight = currentField.getMarginRight();
            
            if( currentField.isStyle( FIELD_BOTTOM ) ) {
                    y = maxHeight - currentField.getHeight() - currentField.getMarginBottom();
            } else if( currentField.isStyle( FIELD_VCENTER ) ) {
                    y = marginTop + ( maxHeight - marginTop - currentField.getHeight() - marginBottom ) >> 1;
            } else {
            	y = marginTop;
            }
            setPositionChild( currentField, usedWidth + marginLeft, y );
            usedWidth += currentField.getWidth() + marginLeft;
            prevRightMargin = marginRight;
        }
        setExtent( width, maxHeight );
    }  
}

class FieldDimensionUtilities
{
    private FieldDimensionUtilities() { }
    
    public static int getBorderWidth( Field field )
    {
        int width = 0;

      //#ifdef VER_4.1.0 | VER_4.2.0 | VER_4.2.1 | VER_4.3.0 | VER_4.5.0
        width = field.getWidth() - field.getContentWidth() - field.getPaddingLeft() - field.getPaddingRight();
      //#else
        Border border = field.getBorder();
        if( border != null ) {
            width = border.getLeft() + border.getRight();
        }
      //#endif
        return width;
    }
    
    public static int getBorderHeight( Field field )
    {
        int height = 0;
        
      //#ifdef VER_4.1.0 | VER_4.2.0 | VER_4.2.1 | VER_4.3.0 | VER_4.5.0
        height = field.getWidth() - field.getContentHeight() - field.getPaddingTop() - field.getPaddingBottom();
      //#else
        Border border = field.getBorder();
        if( border != null ) {
            height = border.getTop() + border.getBottom();
        }
      //#endif
        return height;
    }

    public static int getBorderAndPaddingWidth( Field field )
    {
        int width = 0;
      //#ifdef VER_4.1.0 | VER_4.2.0 | VER_4.2.1 | VER_4.3.0 | VER_4.5.0
        width = field.getWidth() - field.getContentWidth();
      //#else
        width = field.getPaddingLeft() + field.getPaddingRight();
        Border border = field.getBorder();
        if( border != null ) {
            width += border.getLeft() + border.getRight();
        }
      //#endif
        return width;
    }

    public static int getBorderAndPaddingHeight( Field field )
    {
        int height = 0;
      //#ifdef VER_4.1.0 | VER_4.2.0 | VER_4.2.1 | VER_4.3.0 | VER_4.5.0
        height = field.getHeight() - field.getContentHeight();
      //#else
        height = field.getPaddingTop() + field.getPaddingBottom();
        Border border = field.getBorder();
        if( border != null ) {
            height += border.getTop() + border.getBottom();
        }
      //#endif
        return height;
    }

}
