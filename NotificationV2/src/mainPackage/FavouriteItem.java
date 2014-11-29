package mainPackage;

import java.util.Vector;

import net.rim.device.api.system.PersistentContent;
import net.rim.device.api.util.Persistable;

/**
 * This class is use for store data persistantly then
 * displays for savedInformationScreen
 */
public class FavouriteItem implements Persistable 
{
	// Declaring IDs for each field
	static final int PROGRAM_NAME = 0;
	static final int SPONSOR = 1;
	static final int TYPE = 2;
	static final int RANGE = 3;        
	static final int DEADLINE = 4;
	static final int DESCRIPTIONS = 5;
	static final int NOTES = 6;
    private static final int NUM_FIELDS = 7;
	private Vector _data;

	public FavouriteItem()
	{
		// Initialize a new empty vector
		_data = new Vector(NUM_FIELDS);
		for(int i = 0; i<_data.capacity();++i)
		{
			_data.addElement(new String(""));
		}
	}
	
	// Retrive data from accroding id(Fields)
    String getField(int id)
    {        
        Object encoding = _data.elementAt(id);
        
        // Acquiring a reference to a ticket guarantees access to encrypted data
        // even if the device locks during the decoding operation.
        Object ticket  = PersistentContent.getTicket();
        
        if(ticket != null)
        {
            return PersistentContent.decodeString(encoding);
        }
        else
        {
            return null;
        }    
    }
    
    // Set string value for given id(Field) 
    void setField(int id, String value)
    {
        Object encoding = PersistentContent.encode(value);
        _data.setElementAt(encoding, id);
    }  
    
    void reEncode()
    {
    	
        // Acquiring a reference to a ticket guarantees access to encrypted data
        // even if the device locks during the re-encoding operation.
        Object ticket  = PersistentContent.getTicket();
        
        if(ticket != null)
        {
            for (int i = 0; i < NUM_FIELDS; ++i)
            {
                Object encoding = _data.elementAt(i);
                if(!PersistentContent.checkEncoding(encoding))
                {
                    encoding = PersistentContent.reEncode(encoding);
                    _data.setElementAt(encoding, i);
                }
            }
        }
    }
}
