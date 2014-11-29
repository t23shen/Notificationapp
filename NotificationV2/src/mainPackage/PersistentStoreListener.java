/*
 * PersistentStoreListener.java
 *
 * Copyright � 1998-2010 Research In Motion Ltd.
 * 
 * Note: For the sake of simplicity, this sample application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 */

package mainPackage;

import java.util.Vector;
import net.rim.device.api.system.*;

/**
 * Persistent content listener for the PersistentStoreDemo app.  Listens for
 * changes to the device's Content Protection/Compression security settings and
 * re-encodes data accordingly. Changes to the device's state are ignored.
 */
public final class PersistentStoreListener implements PersistentContentListener
{
    /**
     * Called when the state of the device changes
     * (unlocked/locking/locked insecure/locked secure). This app doesn't care
     * about these state changes because data is always encoded inside the
     * Meeting objects; thus, there is no need to encode or decode them during
     * locking and unlocking. 
     * @param state The device's new state.
     */
    public void persistentContentStateChanged(int state)
    {
        // Ignored
    }    
    
    
    /**
     * Called when the device's Content Protection/Compression security settings
     * are changed.  Re-encodes the data accordingly. 
     * @param generation Used to determine if the user has changed the content protection settings since the listener was notified.
     */
    public void persistentContentModeChanged(int generation)
    {        
        PersistentObject persist = PersistentStore.getPersistentObject(IntroScreen.PERSISTENT_STORE_DEMO_ID);            
            
        if (persist != null)
        {  
            synchronized(persist.getContents())
            {
                Vector meetings = (Vector) persist.getContents();
                if (meetings == null)
                {
                    // Contents empty; nothing to re-encode.
                    return;
                }           
                for (int i = 0; i < meetings.size(); ++i)
                {
                    FavouriteItem meeting = (FavouriteItem)meetings.elementAt( i );
                    meeting.reEncode();
                    if ( generation != PersistentContent.getModeGeneration() )
                    {
                        // Device's Content Protection/Compression security
                        // settings have changed again since the listener was
                        // last notified.  Abort this re-encoding because it
                        // will have to be done again anyway according to the
                        // new Content Protection/Compression security settings.
                        break;
                    }
                }
                // Commit the updated data to the persistent store.           
                persist.commit();  
             }           
        }
    }        
}
