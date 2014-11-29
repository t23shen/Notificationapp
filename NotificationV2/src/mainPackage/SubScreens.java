package mainPackage;

import java.util.Vector;
import org.json.me.JSONException;
import net.rim.device.api.collection.util.BasicFilteredList;
import net.rim.device.api.collection.util.BasicFilteredListResult;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.AutoCompleteField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.Arrays;

/**
 * HomeScreen class is use to display the current deadlines for the funding opportunities
 * Deadlines are sorted by the most recent ones to most late ones
 * Deadlines can only exceed one month in advance
*/
class HomeScreen extends MainScreen
{
	// Decaling fields to be used later on
	private LabelField title;
	private VerticalScrollManager listField;
	private ObjectListField infoList;
	private Vector programObject;
	private String [] programList;
	
	public HomeScreen()
	{	
		// Invoke the MainScreens constructor
		// no vertical scroll and no vertical scrollbar
		// is used since VerticalScrollManager adds the
		// customized scrollbar
		super(NO_VERTICAL_SCROLL|NO_VERTICAL_SCROLLBAR);
		
		// Set Banner, Title and Toolbar
		title = new LabelField("Current Deadlines");
		setBanner(ToolMethods.getBannar());
		setTitle(title);
		setToolbar(ToolMethods.getToolbar());
		
		// Initialize the field and adds to the main screen
		listField = new VerticalScrollManager();
		add(listField);
		
		// Use try/catch to catch null pointer to JSON data
		// programObject is a vector that stores the parsed JSON data
		// The parsed JSON data is in the type of Program object
		try {
			programObject = JsonParsing.getProgramObjectList(false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Stores all the Program object name in to programList
		// for display purpose
		programList = new String[programObject.size()];
		infoList = new ObjectListField(Field.USE_ALL_WIDTH);
		for(int i = 0; i< programObject.size();i++){
			programList[i] = programObject.elementAt(i).toString();
		}
		
		// Initialize a new ObjectListField and adds all program names
		// into the field
		// Then adds the field to main screen
		infoList = new ObjectListField(Field.USE_ALL_WIDTH);
		infoList.set(programList);
		listField.add(infoList);
		
	}
	
	// Retrive users selected index
	// Invoke InformationScreen to displays the detail program information 
    private void displayInfo()
    {
    	// Checks if the list is empty or not
        if( !infoList.isEmpty() )
        {
        	int index = infoList.getSelectedIndex();
			UiApplication.getUiApplication().pushScreen(new InformationScreen((Program)programObject.elementAt(index)));
        }        
    } 
    
    // Overrides the Field class method keyChar to keep track of key ENTER events
    protected boolean keyChar(char key, int status, int time)
    {
        // Intercept the ENTER key 
    	// Displays the InformationScreen
       if (key == Characters.ENTER)
       {
    	   displayInfo();  
           return true;     // If we've consumed the event   
       }
       return super.keyChar(key, status, time);
   }
   
    // Overrides the Field class method invokeAction to detect trackball click
   protected boolean invokeAction(int action)
   {        
       switch(action)
       {
       	   // Trackball click
           case ACTION_INVOKE: 
               displayInfo();                
               return true; // If we've consumed the event
       }    
       return  super.invokeAction(action);
   } 
   
   // Overrides the main screen onClose method
   // Provides a dialog to let users choose if exit or not
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
 * FindScreen class is to find programs according to sponsors, types and categories
 * Users can select from three of the dropdown menu to filiter a list of program that they want
 * The programs will be filitered in a list after users click Find button
*/
class FindScreen extends MainScreen
{
	// Decaling fields to be used later on
	private LabelField title;
	private LabelField filter;
	private VerticalScrollManager objectField;
	private String [] spon = null;
	public FindScreen()
	{
		// Invoke the MainScreens constructor
		// no vertical scroll and no vertical scrollbar
		// is used since VerticalScrollManager adds the
		// customized scrollbar
		super(NO_VERTICAL_SCROLL|NO_VERTICAL_SCROLLBAR);
		// Set Banner, Title and Toolbar
		title = new LabelField("Find Fundings");
		setBanner(ToolMethods.getBannar());
		setTitle(title);
		setToolbar(ToolMethods.getToolbar());
		
		// Initialize layout fields and creating UI
		objectField = new VerticalScrollManager();
		add(objectField);
		filter = new LabelField("Filter By: ");
		
		// Use try/catch to catch null pointer to JSON data
		// spon stores all the sponsors that parsed from JSON data
		try {
			spon = JsonParsing.getSponsor();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// Types and categories are static
		// Adds all choices into accroding ObjectChoiceField
		final String [] ty = {"ALL TYPE", "INFRASTRUCTURE","OPERATING", "SCHOLARSHIP","TRAVEL/EXCHANGE"};
		final String [] cate = {"ALL CATEGORY","FEDERAL","PROVINCIAL","INTERNATIONAL"};
		final ObjectChoiceField sponsor = new ObjectChoiceField("Sponsors: ",spon);		
		final ObjectChoiceField type = new ObjectChoiceField("Types: ",ty);
		final ObjectChoiceField category = new ObjectChoiceField("Category: ",cate);
		
		// Creating a custom button field for Find Button- VerticalButtonFieldSet
		VerticalButtonFieldSet filteredProgram = new VerticalButtonFieldSet(Field.FIELD_HCENTER);
	    ButtonField findButton = new ButtonField("Find",ButtonField.CONSUME_CLICK);
	    filteredProgram.setMargin(10, 0, 0,0);
	    
	    // FieldChangeListener used for detect click event on the Find Button
	    // The selected index of the three drop down menu is passed into FilteredProgramScreen
	    // to display a filtered list
	    FieldChangeListener listenerFind = new FieldChangeListener(){
	    	public void fieldChanged(Field field, int context){
	    		int sponsorIndex =  sponsor.getSelectedIndex();
	    		int typeIndex = type.getSelectedIndex();
	    		int categoryIndex = category.getSelectedIndex();
	    		UiApplication.getUiApplication().pushScreen(new FilteredProgramScreen(spon[sponsorIndex],ty[typeIndex],cate[categoryIndex]));
	    	}
	    };
	    findButton.setChangeListener(listenerFind);
	    
	    // Displays all fields
	    filteredProgram.add(findButton);
	    objectField.add(filter);
	    objectField.add(sponsor);
	    objectField.add(type);
	    objectField.add(category);
	    objectField.add(filteredProgram);
	}
	
	// Overrides the main screen onClose method
	// Provides a dialog to let users choose if exit or not
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
 * SearchScreen class is use to search the program by enter the program name or part of the program name
 * Users can select from the result of the search
 * Click on one of the result of search will lead users to the InformationScreen
*/
class SearchScreen extends MainScreen
{
	// Declaring fields to be used later on
	private LabelField title;
	private Vector programObject;
	private String[] allPrograms;
	private BasicFilteredList filterList;
	
	public SearchScreen()
	{
		// Invoke the MainScreens constructor
		// no vertical scroll and no vertical scrollbar is specified
		super(NO_VERTICAL_SCROLL|NO_VERTICAL_SCROLLBAR);
		
		// Set Banner, Title and Toolbar
		title = new LabelField("Search");
		setBanner(ToolMethods.getBannar());
		setTitle(title);
		setToolbar(ToolMethods.getToolbar());
		
		// Use try/catch to catch null pointer to JSON data
		// programObject stores all the Programs object in a vector that parsed from JSON data
		try {
			programObject = JsonParsing.getProgramObjectList(true);
		} catch (JSONException e) {
			e.printStackTrace();
		}	
		
		// Stores all the Program object name in allPrograms array
		allPrograms = new String[programObject.size()];
		for(int i = 0; i< programObject.size();i++){
			allPrograms[i] = programObject.elementAt(i).toString();
		}
		
		// Use of filterList and AutoCompleteField to filter the keywords and display the filter result
		filterList = new BasicFilteredList();
		filterList.addDataSet(1, allPrograms, "Search", BasicFilteredList.COMPARISON_IGNORE_CASE);
		AutoCompleteField autoCompleteField = new AutoCompleteField(filterList){
			// Overrides onSelect method for AutoCompleteField class
			protected void onSelect(Object selection, int type)
			{
				// BasicFilteredListResult gets the selected result and call displayInfo to
				// invoke InformationScreen class
				BasicFilteredListResult result = (BasicFilteredListResult)selection;
				String r = (String)result._object;
				displayInfo(Arrays.getIndex(allPrograms, r));
			}
		};
		
		autoCompleteField.setHintText("Enter part of the program name");
		add(autoCompleteField);
	}
	
	// displayInfo invokes InformationScreen class and pass in a Program object to
	// display detail program information
	private void displayInfo(int index)
	{
		if(filterList.size() != 0 )
	    {
	       UiApplication.getUiApplication().pushScreen(new InformationScreen((Program)programObject.elementAt(index)));
	    }        
	 } 
	
	// Overrides the main screen onClose method
	// Provides a dialog to let users choose if exit or not
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
 * FavoriteScreen class is to display a list of saved FavouriteItem object
 * Users can select a program that they saved from the list 
 * This will invoke savedInformationScreen class to displays the detail
 * program information that they saved before
*/
class FavoriteScreen extends MainScreen
{
	// Declaring fields to be used later on
	private LabelField title;
    private ListField _programList;       
    private IntroScreen _introApp; 
    
	public FavoriteScreen()
	{
		super(NO_VERTICAL_SCROLL|NO_VERTICAL_SCROLLBAR);
		
        _introApp = (IntroScreen)UiApplication.getUiApplication();
		// Set Banner, Title and Toolbar
		title = new LabelField("Favorite");
		setBanner(ToolMethods.getBannar());
		setTitle(title);
		setToolbar(ToolMethods.getToolbar());
		
		// Create a new ListField
        _programList = new ListField();        
        add(_programList); 
        
        // Set list field callback and update the FavouriteItem object list
        _programList.setCallback(_introApp);
        updateList();   
	
	}
	
	// Update the size of the list whenever one item is added
	public void updateList() {
		System.out.println("The Vector size is :  " + _introApp.getPrograms().size());
		 _programList.setSize(_introApp.getPrograms().size()); 
	}
	
	// Retrive users selected index
	// Invoke savedInformationScreen to displays the detail program information 
	private void displayProgram()
    {
        if( !_programList.isEmpty() )
        {
            int index = _programList.getSelectedIndex();   
            Vector programs = _introApp.getPrograms();        
            _introApp.pushScreen( new savedInformationScreen((FavouriteItem)programs.elementAt(index)) ); 
        }        
    }
   
	// Add viewItem, deleteItem and closeitem menus
    protected void makeMenu(Menu menu, int instance)
    {
        if(_programList.getSize() > 0)
        {	
        	
            menu.add(viewItem);
            menu.add(deleteItem);
            menu.add(closeItem);
        }  
        
        super.makeMenu(menu, instance);      
    }
	
    // Overrides the Field class method keyChar to keep track of key ENTER events
    protected boolean keyChar(char key, int status, int time)
    {
        // Intercept the ENTER key 
    	// Displays the savedInformationScreen
       if (key == Characters.ENTER)
       {
           displayProgram();  
           return true;       //if we've consumed the event   
       }
       
       // Intercept the ESC key - exit the app on its receipt.
       if(key == Characters.ESCAPE)
       {
           _introApp.persist();
           close();
           return true;    
       }
       return super.keyChar(key, status, time);
   }
   
   // Overrides the Field class method invokeAction to detect trackball click
   protected boolean invokeAction(int action)
   {        
       switch(action)
       {
       	   // Trackball click
           case ACTION_INVOKE:
               displayProgram();                
               return true; // if we've consumed the event
       }    
       return  super.invokeAction(action);
   }  
	
    // Create menu item View
    private MenuItem viewItem = new MenuItem("View", 65636, 1)
    {
       
        // Displays the selected FavouriteItem for viewing
        public void run()
        {            
            displayProgram();
        }
    };
	
    // Create menu item Delete
    private MenuItem deleteItem = new MenuItem("Delete", 65636, 3)
    {
        
        // Retrieves the highlighted FavouriteItem object and removes it from the
        // vector, then updates the list field to reflect the change.
        public void run()
        {            
            int i = _programList.getSelectedIndex();                
            String meetingName = ((FavouriteItem)_introApp.getPrograms().elementAt(i)).getField(FavouriteItem.PROGRAM_NAME);
            int result = Dialog.ask(Dialog.DELETE, "Delete " + meetingName + "?");                
            if(result == Dialog.YES)
            {
                _introApp.getPrograms().removeElementAt(i);
                updateList();            
            }            
        }
    }; 
    
 // Create menu item Close
    private MenuItem closeItem = new MenuItem("Close", 65636, 1)
    {
       
        // close the screen
        public void run()
        {            
            close();
        }
    };
   
	// Overrides the main screen onClose method
	// Provides a dialog to let users choose if exit or not
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
 * FilteredProgramScreen class is to display a filtered list from the FindScreen class
 * This class receives three parameters which are the sponsor, type and category
 * Users can select a program from the list to invoke InformationScreen class
 * Then displays the detail program information
*/
class FilteredProgramScreen extends MainScreen
{
	private LabelField title;
	private VerticalScrollManager listField;
	private ObjectListField infoList;
	private Vector programObject;
	private String [] programList;
	public FilteredProgramScreen(String sponsor, String type, String category)
	{
		
		// Invoke the MainScreens constructor
		// no vertical scroll and no vertical scrollbar
		// is used since VerticalScrollManager adds the
		// customized scrollbar
		super(NO_VERTICAL_SCROLL|NO_VERTICAL_SCROLLBAR);
		
		// Set Banner, Title and Toolbar
		title = new LabelField("Filtered Programs");
		setBanner(ToolMethods.getBannar());
		setTitle(title);
		setToolbar(ToolMethods.getToolbar());
		
		// Creating UI
		listField = new VerticalScrollManager();
		add(listField);
		
		// Displays all program if this case
		if(sponsor == "ALL SPONSOR" && type == "ALL TYPE" && category == "ALL CATEGORY")
		{
			
			// Use try/catch to catch a exception null pointer to JSON data
			// programObject is a vector that stores the parsed JSON data
			// The parsed JSON data is in the type of Program object
			try {
				programObject = JsonParsing.getProgramObjectList(true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else
		{
			try {
				programObject = JsonParsing.getFilteredProgramObjectList(sponsor,type,category);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		// Stores all the Program object name in to programList
		// for display purpose
		programList = new String[programObject.size()];
		infoList = new ObjectListField(Field.USE_ALL_WIDTH);
		for(int i = 0; i< programObject.size();i++){
			programList[i] = programObject.elementAt(i).toString();
		}
		
		infoList = new ObjectListField(Field.USE_ALL_WIDTH);
		infoList.set(programList);
		listField.add(infoList);
		
	}
	
	// Retrive users selected index
	// Invoke InformationScreen to displays the detail program information 
    private void displayInfo()
    {
        if( !infoList.isEmpty() )
        {
        	int index = infoList.getSelectedIndex();
			UiApplication.getUiApplication().pushScreen(new InformationScreen((Program)programObject.elementAt(index)));
        }        
    } 
    
    // Overrides the Field class method keyChar to keep track of key ENTER events
    protected boolean keyChar(char key, int status, int time)
    {
        // Intercept the ENTER key 
    	// Displays the InformationScreen
       if (key == Characters.ENTER)
       {
    	   displayInfo();  
           return true;  // if we've consumed the event        
       }
       return super.keyChar(key, status, time);
   }
   
   // Overrides the Field class method invokeAction to detect trackball click
   protected boolean invokeAction(int action)
   {        
       switch(action)
       {
       	   // Trackball click
           case ACTION_INVOKE: 
               displayInfo();                
               return true; // if we've consumed the event
       }    
       return  super.invokeAction(action);
   } 
}
