package mainPackage;

import java.util.Vector;
import org.json.me.*;
import net.rim.device.api.command.Command;
import net.rim.device.api.command.CommandHandler;
import net.rim.device.api.command.ReadOnlyCommandMetadata;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.image.Image;
import net.rim.device.api.ui.image.ImageFactory;
import net.rim.device.api.ui.toolbar.ToolbarButtonField;
import net.rim.device.api.ui.toolbar.ToolbarManager;
import net.rim.device.api.util.StringProvider;

/**
 * This is a CustomButtonField class, its super class is Field
 * This custom field makes the favorite icon clickable and
 * displays at the right position of the screen
 */
class CustomButtonField extends Field 
{
	private Bitmap favorite = Bitmap.getBitmapResource("violet_afavorites48.png");
	
	public CustomButtonField()
	{
		super(Field.FIELD_RIGHT);
		
	}
	protected boolean navigationClick(int status, int time) {
        fieldChangeNotify(1);
        return true;
    }
    protected void fieldChangeNotify(int context){
        try {
            this.getChangeListener().fieldChanged(this, context);
        } catch (Exception exception) {
        }
    }
	public int getPreferredHeight()
	{
		return favorite.getHeight();
		
	}
	
	public int getPreferedWidth()
	{
		return favorite.getWidth();
	}
	// These two method makes the button change its appearance when the user clicks it
/*	protected void onFocus(int direction){
	        button = on;
	        invalidate();
	}

	protected void onUnfocus(){
	        button = off;
	        invalidate();
	}*/
	protected void layout(int width, int height) 
	{
		this.setExtent(this.getPreferedWidth(),this.getPreferredHeight());
	}
	protected void paint(Graphics graphics) 
	{
		graphics.drawBitmap(0, 0, getWidth(), getHeight(), favorite, 0, 0);
	}
	public boolean isFocusable()
	{
		return true;
	}
}

/**
 * This is basically a helper function class that has two methods
 * This class is use to optimize codes for methods that is commonly used in other classes
 */
class ToolMethods
{
	// getBannar method which displays the bannar on top of the screen
	// with customized fontstyle, fontsize and fontfamily
	// Returns a LabelField
	static LabelField getBannar()
	{
		FontFamily fontObject = null;
		try {
			fontObject = FontFamily.forName("Arial");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Font fontStyle = fontObject.getFont(Font.BOLD, 18);	
		LabelField bannar = new LabelField("University of Waterloo - Research",LabelField.FIELD_HCENTER);
		bannar.setFont(fontStyle);
		
		return bannar;
	}
	
	// getToolbar method displays the toolbar on botton of the screen
	// with four different buttons that listens for click events
	// Four buttons in the toolbar leads to four different screens
	static ToolbarManager getToolbar()
	{
		
		ToolbarManager toolbar = new ToolbarManager();
		Image home = getImage("violet_home48.png");
		Image find = getImage("violet_folder48.png");
		Image search = getImage("violet_search48.png");
		Image favorite = getImage("violet_favorites48.png");
		
		ToolbarButtonField _home = new ToolbarButtonField(home,new StringProvider("Home"));
		ToolbarButtonField _find = new ToolbarButtonField(find,new StringProvider("Find"));
		ToolbarButtonField _search = new ToolbarButtonField(search,new StringProvider("Search"));
		ToolbarButtonField _favorite = new ToolbarButtonField(favorite,new StringProvider("Favorite"));

		_home.setCommand(new Command(new CommandHandler()
		{
			public void execute(ReadOnlyCommandMetadata metadata, Object context)
			{
				UiApplication.getUiApplication().pushScreen(new HomeScreen());
			}
		}
		));
		_find.setCommand(new Command(new CommandHandler()
		{
			public void execute(ReadOnlyCommandMetadata metadata, Object context)
			{
				UiApplication.getUiApplication().pushScreen(new FindScreen());
			}
		}
		));
		_search.setCommand(new Command(new CommandHandler()
		{
			public void execute(ReadOnlyCommandMetadata metadata, Object context)
			{
				UiApplication.getUiApplication().pushScreen(new SearchScreen());
			}
		}
		));
		_favorite.setCommand(new Command(new CommandHandler()
		{
			public void execute(ReadOnlyCommandMetadata metadata, Object context)
			{
				UiApplication.getUiApplication().pushScreen(new FavoriteScreen());
			}
		}
		));
		
		toolbar.add(_home);
		toolbar.add(_find);
		toolbar.add(_search);
		toolbar.add(_favorite);
		
		return toolbar;
	}
	private static Bitmap getBitmap(String name)
	{
		return Bitmap.getBitmapResource(name);
	}
	
	private static Image getImage(String name)
	{
		return ImageFactory.createImage(getBitmap(name));
	}
}
/** 
 * This is a Program object class that use to display on InformationScreen
 */
class Program
{
	private String _programName;
	private String _sponsor;
	private String _type;
	private String _range;
	private String _deadline;
	private String _description;
	
	// Constructs a Program
	public Program(String _programName,String _sponsor, String _type, String _range,String _deadline,String _description )
	{
		this._programName = _programName;
		this._sponsor = _sponsor;
		this._type = _type;
		this._range = _range;
		this._deadline = _deadline;
		this._description = _description;
	}
	
	//Acessor method
	String getSponsor()
	{
		return _sponsor;
	}
	
	String getType()
	{
		return _type;
	}
	
	String getRange()
	{
		return _range;
	}
	
	String getDeadline()
	{
		return _deadline;
	}
	
	String getDescription()
	{
		return _description;
	}
	
	public String toString()
	{
		return _programName;
	}
	
}

/** 
 * This class is use for parsing JSON data into String array and Vectors
 * then called by other mainscreen subclass to display detail informations
 */
class JsonParsing
{
	// Declaring key string for parsing JSON DATA
	private static final String 
	CATEGORY = "category",
	ALLINFO = "AllInfo",
	ALLPROGRAMS = "AllPrograms",
	CURRINFO = "CurrInfo",
	DAY = "day",
	MONTH = "month",
	YEAR = "year",
	PROGRAM ="program",
	DESCRIPTION = "desc",
	SPONSOR = "sponsor",
	TYPE = "type",
	RANGE = "range",
	COMMA = ",",
	SPACE = " ",
	ALL_SPONSORS = "S";
	
	// Declaring arrays to store information later on
	private static String []
	program = null,
	desc = null,
	parsedDeadline = null,
	parsedDescription = null,
	day = null,
	month = null,
	year = null,
	sponsor = null,
	type = null,
	range = null;
	
	// Declaring variables to be used later on
	private static JSONObject wholeObject;
	private static JSONArray allMainObject;
	private static JSONObject mainObject;
	private static JSONArray allSubObject;
	private static JSONObject subObject;
	private static String jsonData;
	
	// No parameter needs to passed in
	public JsonParsing(){}
	
	// Initialize arrays with certain size that determined in methods
	public static void delcareArrays (int length)
	{
		program = new String[length];
		desc = new String[length];
		day = new String[length];
		month = new String[length];
		year = new String[length];
		type = new String[length];
		sponsor = new String[length];
		range = new String[length];
		parsedDeadline = new String[length];
		parsedDescription = new String[length];

	}
	
	// getSponsor method parse part of the JSON data that has
	// all the sponsor name, then return a list of sponsor in
	// a String array
	public static String [] getSponsor()throws JSONException
	{	
		int index = 1;
		
		// wholeObject access the outer brace
		// allMainObject access the inner brace
		// mainObject access mainObject array
		wholeObject = new JSONObject(jsonData);
		allMainObject = wholeObject.getJSONArray(ALLPROGRAMS);
		mainObject = allMainObject.getJSONObject(2);
		if(mainObject != null){
			
			// allSubObject access the array inside
			allSubObject = mainObject.getJSONArray(ALL_SPONSORS);
			if(allSubObject != null){
				
				// Adds the string "ALL SPONSOR" in the beginning of the list
				sponsor = new String[allSubObject.length()+1];
				sponsor[0] = "ALL SPONSOR";
				for(int i = 0;i < allSubObject.length();i++)
				{
					subObject = allSubObject.getJSONObject(i); 
					sponsor[index] = subObject.getString(SPONSOR);
					index++;
				}
			}
		}	
		return sponsor;		
	}

	// This method parse part of the JSON data
	// Return list of all Program object in a vector if the parameter is 'true'
	// Return list of current deadlines Program object in a vector if the parameter is 'false'
	public static Vector getProgramObjectList(boolean isAllProgram)throws JSONException
	{
		// Create Vector object
		Vector list = new Vector();
		
		// wholeObject access the outer brace
		// allMainObject access the inner brace
		// mainObject access mainObject array	
		wholeObject = new JSONObject(jsonData);
		allMainObject = wholeObject.getJSONArray(ALLPROGRAMS);
		
		// Check if is all programs or not
		if(isAllProgram)
		{
			mainObject = allMainObject.getJSONObject(0);
		}
		else 
		{
			mainObject = allMainObject.getJSONObject(1);
		}
		
		if(mainObject != null){	
			if(isAllProgram)
			{
				// allSubObject access the array inside
				allSubObject = mainObject.getJSONArray(ALLINFO);
			}
			else
			{
				allSubObject = mainObject.getJSONArray(CURRINFO);
			}
			if(allSubObject != null){
				
				// give all the arrays fixed length to be use it later
				delcareArrays(allSubObject.length());	
				
				for(int i = 0;i < allSubObject.length();i++)
				{
					subObject = allSubObject.getJSONObject(i);
					
					program[i] = subObject.getString(PROGRAM);
					day[i] = subObject.getString(DAY);
					month[i] = subObject.getString(MONTH);
					year[i] = subObject.getString(YEAR);
					sponsor[i] = subObject.getString(SPONSOR);
					type[i] = subObject.getString(TYPE);
					range[i] = subObject.getString(RANGE);
					desc[i] = subObject.getString(DESCRIPTION);
					parsedDeadline[i] = month[i] + SPACE + day[i] + COMMA + SPACE + year[i];
					parsedDescription[i] = desc[i];
					
					list.addElement(new Program(program[i],sponsor[i],type[i],range[i],parsedDeadline[i],parsedDescription[i]));
				}
			}
		}
	return list;
	}
	
	// This method returns a list of Program object in a vector
	// according to the three parameters
	public static Vector getFilteredProgramObjectList(String _sponsor, String _type, String _category)throws JSONException
	{
		// Create Vector object
		// Initialize variables to be used later
		Vector list = new Vector();
		final String ALLSPONSOR = "ALL SPONSOR";
		final String ALLTYPE = "ALL TYPE";
		final String ALLCATEGORY = "ALL CATEGORY";
		
		// Initialize boolean equations to be used in later cases
		final boolean CASE1 = _sponsor != ALLSPONSOR && _type == ALLTYPE && _category == ALLCATEGORY,
					  CASE2 = _sponsor == ALLSPONSOR && _type != ALLTYPE && _category == ALLCATEGORY,
					  CASE3 = _sponsor == ALLSPONSOR && _type == ALLTYPE && _category != ALLCATEGORY,
					  CASE4 = _sponsor != ALLSPONSOR && _type != ALLTYPE && _category == ALLCATEGORY,
					  CASE5 = _sponsor == ALLSPONSOR && _type != ALLTYPE && _category != ALLCATEGORY,
					  CASE6 = _sponsor != ALLSPONSOR && _type == ALLTYPE && _category != ALLCATEGORY;
		
		// Initialize strings and string arrays to be used later on 
		String compareObject = "",
		 	   compareObject2 = "",
		 	   selectedObject = "",
		 	   selectedObject2 = "",
			   compareObject3 = "",
			   selectedObject3 = "";
		String [] defaultCategory = null,
				  defaultSponsor = null,
				  defaultType = null,
				  compareArray = null,
				  compareArray2 = null,
				  compareArray3 = null;
		int index = 0,
			size = 0;
		
		// wholeObject access the outer brace
		// allMainObject access the inner brace
		// mainObject access mainObject array	
		wholeObject = new JSONObject(jsonData);
		allMainObject = wholeObject.getJSONArray(ALLPROGRAMS);	
		mainObject = allMainObject.getJSONObject(0);
	
		if(mainObject != null){
			// allSubObject access the array inside
			allSubObject = mainObject.getJSONArray(ALLINFO);

			if(allSubObject != null){
				
				if(CASE1)
				{
					selectedObject = SPONSOR;
					compareObject = _sponsor;
					defaultSponsor = new String[allSubObject.length()];
					for(int j = 0;j < allSubObject.length();j++){
						subObject = allSubObject.getJSONObject(j);
						defaultSponsor[j] = subObject.getString(SPONSOR);
						if (defaultSponsor[j].intern() == _sponsor.intern()){
							size++; 
						}
					}
				}
				else if(CASE2)
				{
					selectedObject = TYPE;
					compareObject = _type;
					defaultType = new String[allSubObject.length()];
					for(int j = 0;j < allSubObject.length();j++){
						subObject = allSubObject.getJSONObject(j);
						defaultType[j] = subObject.getString(TYPE);
						if (defaultType[j].intern() == _type.intern()){
							size++; 
						}
					}
				}
				else if(CASE3)
				{
					selectedObject = CATEGORY;
					compareObject = _category;
					defaultCategory= new String[allSubObject.length()];
					for(int j = 0;j < allSubObject.length();j++){
						subObject = allSubObject.getJSONObject(j);
						defaultCategory[j] = subObject.getString(CATEGORY);
						if (defaultCategory[j].intern() == _category.intern()){
							size++; 
						}
					}
				}
				else if(CASE4)
				{
					selectedObject = SPONSOR;
					compareObject = _sponsor;
					selectedObject2 = TYPE;
					compareObject2 = _type;
					defaultCategory= new String[allSubObject.length()];
					compareArray2 = new String[allSubObject.length()];
					
					for(int j = 0;j < allSubObject.length();j++){
						subObject = allSubObject.getJSONObject(j);
						defaultCategory[j] = subObject.getString(SPONSOR);
						compareArray2[j] = subObject.getString(TYPE);
						if (defaultCategory[j].intern() == _sponsor.intern()&& compareArray2[j].intern() == _type.intern()){
							size++; 
						}
					}
					
				}
				else if(CASE5)
				{
					selectedObject = CATEGORY;
					compareObject = _category;
					selectedObject2 = TYPE;
					compareObject2 = _type;
					compareArray2 = new String[allSubObject.length()];
					defaultCategory= new String[allSubObject.length()];		
					for(int j = 0;j < allSubObject.length();j++){
						subObject = allSubObject.getJSONObject(j);
						defaultCategory[j] = subObject.getString(CATEGORY);
						compareArray2[j] = subObject.getString(TYPE);
						if (defaultCategory[j].intern() == _category.intern() && compareArray2[j].intern() == _type.intern()){
							size++; 
						}
					}
				}
				else if(CASE6)
				{
					selectedObject = CATEGORY;
					compareObject = _category;
					selectedObject2 = SPONSOR;
					compareObject2 = _sponsor;
					compareArray2 = new String[allSubObject.length()];
					defaultCategory= new String[allSubObject.length()];
					for(int j = 0;j < allSubObject.length();j++){
						subObject = allSubObject.getJSONObject(j);
						defaultCategory[j] = subObject.getString(CATEGORY);
						compareArray2[j] = subObject.getString(SPONSOR);
						if (defaultCategory[j].intern() == _category.intern()&& compareArray2[j].intern() == _sponsor.intern() ){
							size++; 
						}
					}
				}
				else
				{
					selectedObject = CATEGORY;
					compareObject = _category;
					selectedObject2 = SPONSOR;
					compareObject2 = _sponsor;
					selectedObject3 = TYPE;
					compareObject3 = _type;
					compareArray2 = new String[allSubObject.length()];
					defaultCategory= new String[allSubObject.length()];
					compareArray3 = new String[allSubObject.length()];
					for(int j = 0;j < allSubObject.length();j++){
						subObject = allSubObject.getJSONObject(j);
						defaultCategory[j] = subObject.getString(CATEGORY);
						compareArray2[j] = subObject.getString(SPONSOR);
						compareArray3[j] = subObject.getString(TYPE);
						if (defaultCategory[j].intern() == _category.intern()&& compareArray2[j].intern() == _sponsor.intern()&& compareArray3[j].intern() == _type.intern()){
							size++; 
						}
					}
				}
//				System.out.println("SIZE = " + size);

				// give all the arrays fixed length to be use it later
				delcareArrays(size);
				
				if(CASE1 || CASE2 || CASE3)
				{
					compareArray = new String[allSubObject.length()];
					for(int i = 0;i < allSubObject.length();i++)
					{
						subObject = allSubObject.getJSONObject(i);
						compareArray[i] = subObject.getString(selectedObject);
			
						if (compareArray[i].intern() == compareObject.intern())
						{	
							
							// Parse and store all the informations into string arrays
							// then wrap as a Program object and append to a vector Object
							program[index] = subObject.getString(PROGRAM);
							day[index] = subObject.getString(DAY);
							month[index] = subObject.getString(MONTH);
							year[index] = subObject.getString(YEAR);
							sponsor[index] = subObject.getString(SPONSOR);
							type[index] = subObject.getString(TYPE);
							range[index] = subObject.getString(RANGE);
							desc[index] = subObject.getString(DESCRIPTION);
							parsedDeadline[index] = month[index] + SPACE + day[index] + COMMA + SPACE + year[index];
							parsedDescription[index] = desc[index];
							
							list.addElement(new Program(program[index],sponsor[index],type[index],range[index],parsedDeadline[index],parsedDescription[index]));
							index++;
						}
					}
				}
				else if(CASE4||CASE5||CASE6)
				{
					compareArray = new String[allSubObject.length()];
					compareArray2 = new String[allSubObject.length()];
					
					for(int i = 0;i < allSubObject.length();i++)
					{
						subObject = allSubObject.getJSONObject(i);
						compareArray[i] = subObject.getString(selectedObject);
						compareArray2[i] = subObject.getString(selectedObject2);
						
						if (compareArray[i].intern() == compareObject.intern() && compareArray2[i].intern() == compareObject2.intern() )
						{	
							
							// Parse and store all the informations into string arrays
							// then wrap as a Program object and append to a vector Object
							program[index] = subObject.getString(PROGRAM);
							day[index] = subObject.getString(DAY);
							month[index] = subObject.getString(MONTH);
							year[index] = subObject.getString(YEAR);
							sponsor[index] = subObject.getString(SPONSOR);
							type[index] = subObject.getString(TYPE);
							range[index] = subObject.getString(RANGE);
							desc[index] = subObject.getString(DESCRIPTION);
							parsedDeadline[index] = month[index] + SPACE + day[index] + COMMA + SPACE + year[index];
							parsedDescription[index] = desc[index];
							
							list.addElement(new Program(program[index],sponsor[index],type[index],range[index],parsedDeadline[index],parsedDescription[index]));
							index++;
						}
					}
				}
				else
				{
					compareArray = new String[allSubObject.length()];
					compareArray2 = new String[allSubObject.length()];
					compareArray3 = new String[allSubObject.length()];
					for(int i = 0;i < allSubObject.length();i++)
					{
						subObject = allSubObject.getJSONObject(i);
						compareArray[i] = subObject.getString(selectedObject);
						compareArray2[i] = subObject.getString(selectedObject2);
						compareArray3[i] = subObject.getString(selectedObject3);
						if (compareArray[i].intern() == compareObject.intern() && compareArray2[i].intern() == compareObject2.intern() && compareArray3[i].intern() == compareObject3.intern() )
						{	
							
							// Parse and store all the informations into string arrays
							// then wrap as a Program object and append to a vector Object
							program[index] = subObject.getString(PROGRAM);
							day[index] = subObject.getString(DAY);
							month[index] = subObject.getString(MONTH);
							year[index] = subObject.getString(YEAR);
							sponsor[index] = subObject.getString(SPONSOR);
							type[index] = subObject.getString(TYPE);
							range[index] = subObject.getString(RANGE);
							desc[index] = subObject.getString(DESCRIPTION);
							parsedDeadline[index] = month[index] + SPACE + day[index] + COMMA + SPACE + year[index];
							parsedDescription[index] = desc[index];
							
							list.addElement(new Program(program[index],sponsor[index],type[index],range[index],parsedDeadline[index],parsedDescription[index]));
							index++;
						}
					}
				}
			}
		}
		return list;
}
	
	// A SETTER method to set JSON data
	public static void setJsonData(String jsonString) {
		jsonData = jsonString;
	}
	
	// A GETTER method to get JSON data
	public static String getJsonData() {
		return jsonData;
	}

}
