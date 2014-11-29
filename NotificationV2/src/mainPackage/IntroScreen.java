package mainPackage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connection;
import javax.microedition.io.InputConnection;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.container.*;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;
import net.rim.device.api.ui.decor.*;    
import net.rim.device.api.ui.extension.component.PictureScrollField;
import net.rim.device.api.ui.extension.component.PictureScrollField.*;
import net.rim.device.api.util.Arrays;

/**
 * IntroScreen class is subclass of UiApplication and used ListFieldCallback methods
 * IntroScreen class includes HttpConnection and Persistantstore functions
 * IntroScreen class enters the main event thread and pushes the main screen TopScreen
*/
public class IntroScreen extends UiApplication implements ListFieldCallback
{
	// Declaring variables for PersistantStore
    private Vector _favouriteItem;
    private PersistentObject _store;    
    private FavoriteScreen _screen;
    
    static final long PERSISTENT_STORE_DEMO_ID = 0x220d57d6848faeffL;
    static final long PERSISTENT_STORE_DEMO_CONTROLLED_ID = 0xbf768b0f3ae726daL; 
    
    // Declaring variables for HTTPConnection
	final String URL = "http://www.engineering.uwaterloo.ca/research/notification/JSONparseWebservice.php";
	ConnectionFactory factory = new ConnectionFactory();
	int [] preferredTransportTypes  = {TransportInfo.TRANSPORT_TCP_WIFI,
									  TransportInfo.TRANSPORT_WAP2,
									  TransportInfo.TRANSPORT_BIS_B,
									  TransportInfo.TRANSPORT_MDS,
									  TransportInfo.TRANSPORT_WAP};
    
    
	static void main(String[] args){
		if ( args != null && args.length > 0 && args[0].equals("startup") )
        {
            PersistentObject store = PersistentStore.getPersistentObject(PERSISTENT_STORE_DEMO_ID);                       
             
            // Synchronize on the PersistentObject so that no other object can
            // acquire the lock before we finish our commit operation.     
            synchronized(store)
            {         
            	System.out.println("This is in the sychronized store method and the if statement");
                // If the PersistentObject is empty, initialize it
                if(store.getContents() == null)
                {
                	System.out.println("Creating new persistance store");
                    store.setContents(new Vector(40)); // Initialize a vector with size of 40
                    PersistentObject.commit(store);
                }  
                PersistentContent.addListener(new PersistentStoreListener());

            }
        }
            // Launch GUI version of the application.
        	IntroScreen firstScreen = new IntroScreen();
         
            // Make the currently running thread the application's event
            // dispatch thread and begin processing events.
        	firstScreen.enterEventDispatcher();
  
	}

	public IntroScreen(){
		
		// Acquire HttpConnection and Retrive Json Data from PHP scripts
		HttpConnection();
		
        // Persist an object protected by a code signing key     
        PersistentObject controlledStore = PersistentStore.getPersistentObject(PERSISTENT_STORE_DEMO_CONTROLLED_ID); 
        synchronized(controlledStore)
        {            
            CodeSigningKey codeSigningKey = CodeSigningKey.get(CodeModuleManager.getModuleHandle("NotificationV2"), "ACME");
            controlledStore.setContents(new ControlledAccess(new Vector(), codeSigningKey));                        
            PersistentObject.commit(controlledStore);                        
        }
/*        
		final UiEngineInstance engine = Ui.getUiEngineInstance();
		TransitionContext transition = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
		transition.setIntAttribute(TransitionContext.ATTR_DURATION, 500);
		transition.setIntAttribute(TransitionContext.ATTR_DIRECTION,TransitionContext.DIRECTION_RIGHT);
		transition.setIntAttribute(TransitionContext.ATTR_STYLE,TransitionContext.STYLE_PUSH);
		engine.setTransition(findScreen, homeScreen, UiEngineInstance.TRIGGER_POP, transition);

		transition.setIntAttribute(TransitionContext.ATTR_DURATION, 500);
		transition.setIntAttribute(TransitionContext.ATTR_DIRECTION,TransitionContext.DIRECTION_LEFT);
		transition.setIntAttribute(TransitionContext.ATTR_STYLE,TransitionContext.STYLE_PUSH);
		engine.setTransition(homeScreen, findScreen, UiEngineInstance.TRIGGER_POP, transition);*/
		
        // Retrieve the persistent object for this application                
        _store = PersistentStore.getPersistentObject(PERSISTENT_STORE_DEMO_ID); 
        
        // Retrieve the saved FavouriteItem objects from the persistent store
        _favouriteItem = (Vector)_store.getContents(); 
        
        // Create a FavoriteScreen to be use for updatelist later on
        _screen = new FavoriteScreen();
        
        // Create the main screen for the application and push it onto the UI
        // stack for rendering     
        pushScreen(new TopScreen());
        
	}
	
	// This method connects to a preferred connection and then retrive the JSON data
	private void HttpConnection()
	{
		//Remove any transports that are not available
		for(int i = 0; preferredTransportTypes.length<i;i++)
		{
			int transport = preferredTransportTypes[i];
			if(!TransportInfo.isTransportTypeAvailable(transport)||!TransportInfo.hasSufficientCoverage(transport))
			{
				Arrays.removeAt(preferredTransportTypes, i);
			}
		}
		
		//Set Connection Options
		if(preferredTransportTypes.length > 0)
		{
			factory.setPreferredTransportTypes(preferredTransportTypes);
		}
		else
		{
			Dialog.alert("Please connection to the internet");
		}
		factory.setAttemptsLimit(10);
		
		//Open a connection on a new thread
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				ConnectionDescriptor cd = factory.getConnection(URL);
				
				//if the connection was successful, test to show the content
				if(cd != null)
				{
					Connection c = cd.getConnection();
					InputStream is = null;
					String jsonString = "";
					try
					{
						// Get the inputConnection and read the server's response
						// Save the JSON data into jsonString
						InputConnection inputConn = (InputConnection) c;
						is = inputConn.openInputStream();
						byte[] data = IOUtilities.streamToBytes(is);
						jsonString = new String(data);
						
					}
					catch(Exception e)
					{
						jsonString = "ERROR";
					}
					finally
					{
						//Close OutputStream
						if(is!= null)
						{
							try
							{
								is.close();
							}
							catch(IOException e){}
						}
						try
						{
							c.close();
						}
						catch(IOException ioe){}
					}
					
					// Send the JSON data into the parsing class JsonParsing
					JsonParsing.setJsonData(jsonString);
				}
				
			}
		});
		t.start();
	}
	
	// Called by FavoriteScreen constructor to save the FavouriteItem object
    void saveToFavourite(FavouriteItem favouriteitem, int index)
    {  
        if(index >= 0)
        {
        	_favouriteItem.setElementAt(favouriteitem, index);
        }
        else
        {
        	_favouriteItem.addElement(favouriteitem);
        }                
       _screen.updateList();        
    }
    
    
    
    // Returns collection of FavouriteItem objects
    // Return A vector of FavouriteItem objects 
    Vector getPrograms()
    {
//		System.out.println("The Vector size is :  " + _favouriteItem.size());
        return _favouriteItem;
    }
    
    
    
    //  Commits the updated vector of FavouriteItem objects to the
    //  persistent store.  
    void persist()
    {
        // Synchronize on the PersistentObject so that no other object can
        // acquire the lock before we finish the commit operation.
        synchronized( _store )
        { 
            _store.setContents(_favouriteItem);
            PersistentObject.commit(_store);
        }
    } 
    
    // ListFieldCallback methods - Update the list whenever a new FavouriteItem object is added
    public void drawListRow(ListField list, Graphics graphics, int index, int y, int w)
    {
    	FavouriteItem favorite = (FavouriteItem)_favouriteItem.elementAt(index);
        String text = favorite.getField(FavouriteItem.PROGRAM_NAME);
        graphics.drawText(text, 0, y, 0, w);
    }
    
    public Object get(ListField list, int index)
    {
        return null; // Not implemented
    }
  
    public int indexOfList(ListField list, String p, int s)
    {
        return 0; // Not implemented
    }
    
    public int getPreferredWidth(ListField list)
    {
        return Display.getWidth();
    }

}

/**
 * TopScreen is a introduction screen to the users
 * Users can choose the screen by touch gesture or trackballs
 * Users can open the screen by clicking on the icons
*/
class TopScreen extends MainScreen
{
	private VerticalFieldManager manager;
	private Background bg;
	public TopScreen()
	{		
		super(NO_VERTICAL_SCROLL|NO_VERTICAL_SCROLLBAR);
		// Setting up the layout and toolbar for the main screen
		manager = new VerticalFieldManager(VerticalFieldManager.USE_ALL_HEIGHT|VerticalFieldManager.NO_VERTICAL_SCROLL|VerticalFieldManager.NO_HORIZONTAL_SCROLLBAR);
		bg = BackgroundFactory.createSolidTransparentBackground(Color.BLACK, 250);
		manager.setBackground(bg);
		add(manager);
		setBanner(ToolMethods.getBannar());

		// Setting up the icons, labels and tooltips for PictureScrollField
		Bitmap[] images = new Bitmap[4];
        String[] labels = new String[4];
        String[] tooltips = new String[4];

        images[0] = Bitmap.getBitmapResource("violet_favorites.png");
        labels[0] = "Favorite";
        tooltips[0] = "Your stored favorites";

        images[1] = Bitmap.getBitmapResource("violet_folder.png");
        labels[1] = "Find Fundings";
        tooltips[1] = "Fundings";

        images[2] = Bitmap.getBitmapResource("violet_home.png");
        labels[2] = "Current Deadlines";
        tooltips[2] = "Deadlines";

        images[3] = Bitmap.getBitmapResource("violet_search.png");
        labels[3] = "Search";
        tooltips[3] = "Search Programs";
        
        // Put in all the icons, labels and tooltips into entries
        ScrollEntry[] entries = new ScrollEntry[4];
        for (int i = 0; i < entries.length; i++) 
        { 
             entries[i] = new ScrollEntry(images[i], labels[i],tooltips[i]);
        }
        
        // Create PictureScrollField with 84 x 84 pixel according to the icon size
        // Create touchEvent for pictureScrollField for different click situation
        final PictureScrollField pictureScrollField = new PictureScrollField(84, 84){
        	protected boolean touchEvent(TouchEvent message)
        	{
            	if(message.getEvent()==TouchEvent.CLICK||message.getEvent() == TouchEvent.UNCLICK)
            	{
            		return false;
            	}
            	else if(message.getEvent() == TouchGesture.CLICK_REPEAT){
            		return true;
            	}
            	return super.touchEvent(message);
        	}
   
        };
        
        // Setup the pictureScrollField for different properties of appearance
        pictureScrollField.setData(entries, 2);
        pictureScrollField.setHighlightStyle(HighlightStyle.ILLUMINATE_WITH_SHRINK_LENS);
        pictureScrollField.setHighlightBorderColor(Color.BLUE);
        pictureScrollField.setBackground(BackgroundFactory.createSolidTransparentBackground(Color.BLACK,230 ));
        pictureScrollField.setTextColor(Color.WHITE);
        pictureScrollField.setLabelsVisible(true); 
        pictureScrollField.setMargin(100,0, 0, 0);
        pictureScrollField.setImageDistance(100);

        // Create FieldChangeListsener for every icon in the pictureScrollField 
		FieldChangeListener Picturelistener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context){
				if(pictureScrollField.getCurrentImageIndex() == 0)
				{
					UiApplication.getUiApplication().pushScreen(new FavoriteScreen());
				}
				else if(pictureScrollField.getCurrentImageIndex() == 1)
				{
					UiApplication.getUiApplication().pushScreen(new FindScreen());
				}
				else if(pictureScrollField.getCurrentImageIndex() == 2)
				{
					UiApplication.getUiApplication().pushScreen(new HomeScreen());
				}
				else
				{
					UiApplication.getUiApplication().pushScreen(new SearchScreen());
				}
		    }
		 };	 
		 
		pictureScrollField.setChangeListener(Picturelistener);
        manager.add(pictureScrollField);  	
	}
	
	// Override the main screen method onClose()
	public boolean onClose(){
		Bitmap questionMark = Bitmap.getBitmapResource("questionmark.png");
		Dialog closeDialog = new Dialog(Dialog.D_OK_CANCEL,"Do you wish to exit?",Dialog.OK,questionMark, 0);
		closeDialog.doModal();
		if(closeDialog.getSelectedValue() == Dialog.OK){	
			System.exit(0);
			return true;
		}
		else{
			return false;
		}
	}
}

/**
 * InformationScreen is used to display detail information about programs
 * Users can type their personal notes into one of the fields called Notes
 * Users can click the favorite button to add this information into their favorite screens
*/
class InformationScreen extends MainScreen
{
	// Declare all the fields
	final private VerticalFieldManager favorite;
	private CustomButtonField addFavoriteButton;
	private VerticalFieldManager wholeScreen;
	private TextField _name;
	private TextField _sponsor;
	private TextField _type;
	private TextField _range;
	private TextField _deadline;
	private TextField _description;
	private TextField _notes;
	private FavouriteItem _favor;
	private IntroScreen _introApp;
	
	public InformationScreen(Program program)
	{
		super(NO_VERTICAL_SCROLL|NO_VERTICAL_SCROLLBAR);
		
		// Set Banner, Title and Toolbar
		setBanner(ToolMethods.getBannar());
		setTitle(program.toString());
		setToolbar(ToolMethods.getToolbar());
		
		// Layout of the Screen
		favorite = new VerticalFieldManager(Field.USE_ALL_WIDTH);
		wholeScreen = new VerticalFieldManager();
		add(favorite);
		add(wholeScreen);
		
		// Create a new FavouriteItem object to be saved later on
		// Retrive the current Uiapplication
		_favor = new FavouriteItem();		
		_introApp = (IntroScreen)UiApplication.getUiApplication();
	
		addFavoriteButton = new CustomButtonField();
		
		// initialize all the fields
		_name = new TextField(Field.READONLY);
		_sponsor = new TextField(Field.READONLY);
		_type = new TextField(Field.READONLY);
		_range = new TextField(Field.READONLY);
		_deadline = new TextField(Field.READONLY);
		_description = new TextField(Field.READONLY);
		_notes = new TextField(Field.EDITABLE);
		
		// Set labels
		_name.setLabel("Name: ");
		_sponsor.setLabel("Sponsor: ");
		_type.setLabel("Type: ");
		_range.setLabel("Funding Range: ");
		_deadline.setLabel("Deadline: ");
		_description.setLabel("Description: ");
		_notes.setLabel("Notes: ");
		
		// Retrive information from Program object
		_name.setText(program.toString());
		_sponsor.setText(program.getSponsor());
		_type.setText(program.getType());
		_range.setText(program.getRange());
		_deadline.setText(program.getDeadline());
		_description.setText(program.getDescription());
		_notes.setText("");
		
		// FieldChangeListener for the favorite button
		FieldChangeListener favoriteListener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context){	
				if(onSave())
				{
					close();
				}
		    }
		 };
		
		// Add the FieldChangeListener to the button
		// Adds all field to the mainscreen
		addFavoriteButton.setChangeListener(favoriteListener);
		 
		favorite.add(addFavoriteButton);
		wholeScreen.add(_name);
		wholeScreen.add(_sponsor);
		wholeScreen.add(_type);
		wholeScreen.add(_range);
		wholeScreen.add(_deadline);
		wholeScreen.add(_description);
		wholeScreen.add(_notes);
	}
	
	// Saves all the informations into persistant stores
    protected boolean onSave()
    {
		_favor.setField(FavouriteItem.PROGRAM_NAME,_name.getText());
		_favor.setField(FavouriteItem.SPONSOR,_sponsor.getText());            
		_favor.setField(FavouriteItem.TYPE,_type.getText());
        _favor.setField(FavouriteItem.RANGE,_range.getText());
        _favor.setField(FavouriteItem.DEADLINE,_deadline.getText()); 
        _favor.setField(FavouriteItem.DESCRIPTIONS,_description.getText()); 
        _favor.setField(FavouriteItem.NOTES, _notes.getText());
        _introApp.saveToFavourite(_favor,-1);              
        Dialog.inform("Added");
        return super.onSave();
    }
}

/**
 * savedInformationScreen is used to display saved detail information about programs
 * Users can access his screen by going into the FavoriteScreen, then click on the
 * list of programs that they saved
*/
class savedInformationScreen extends MainScreen
{
	// Declare all the fields
	private VerticalFieldManager wholeScreen;
	private TextField _name;
	private TextField _sponsor;
	private TextField _type;
	private TextField _range;
	private TextField _deadline;
	private TextField _description;
	private TextField _notes;

	public savedInformationScreen(FavouriteItem favor)
	{
		super(NO_VERTICAL_SCROLL|NO_VERTICAL_SCROLLBAR);
		//Set Banner, Title and Toolbar
		setBanner(ToolMethods.getBannar());
		setTitle(favor.getField(FavouriteItem.PROGRAM_NAME));
		setToolbar(ToolMethods.getToolbar());
		
		// Layout of the Screen
		wholeScreen = new VerticalFieldManager();
		add(wholeScreen);	
		
		// Initialize all the fields
		_name = new TextField(Field.READONLY);
		_sponsor = new TextField(Field.READONLY);
		_type = new TextField(Field.READONLY);
		_range = new TextField(Field.READONLY);
		_deadline = new TextField(Field.READONLY);
		_description = new TextField(Field.READONLY);
		_notes = new TextField(Field.READONLY);
		
		// Set labels
		_name.setLabel("Name: ");
		_sponsor.setLabel("Sponsor: ");
		_type.setLabel("Type: ");
		_range.setLabel("Funding Range: ");
		_deadline.setLabel("Deadline: ");
		_description.setLabel("Description: ");
		_notes.setLabel("Notes: ");
		
		// Display information from the FavouriteItem object
		_name.setText(favor.getField(FavouriteItem.PROGRAM_NAME));
		_sponsor.setText(favor.getField(FavouriteItem.SPONSOR));
		_type.setText(favor.getField(FavouriteItem.TYPE));
		_range.setText(favor.getField(FavouriteItem.RANGE));
		_deadline.setText(favor.getField(FavouriteItem.DEADLINE));
		_description.setText(favor.getField(FavouriteItem.DESCRIPTIONS));
		_notes.setText(favor.getField(FavouriteItem.NOTES));
				
		// Adds all field to the mainscreen
		wholeScreen.add(_name);
		wholeScreen.add(_sponsor);
		wholeScreen.add(_type);
		wholeScreen.add(_range);
		wholeScreen.add(_deadline);
		wholeScreen.add(_description);
		wholeScreen.add(_notes);
	}
}
